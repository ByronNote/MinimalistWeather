package cn.byronlab.weather.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.repository.CurrentLocationRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidCurrentLocationRepository @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : CurrentLocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): DomainResult<DeviceLocation> = withContext(ioDispatcher) {
        if (!context.hasLocationPermission()) {
            return@withContext DomainResult.failure(
                DomainError(
                    DomainError.Type.LOCATION_PERMISSION_REQUIRED,
                    "Location permission is required.",
                ),
            )
        }

        val locationManager = context.getSystemService(LocationManager::class.java)
            ?: return@withContext locationUnavailable("Location service is unavailable.")
        val enabledProvider = preferredProviders.firstOrNull { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        } ?: return@withContext locationUnavailable("Location service is disabled.")

        val lastKnownLocation = preferredProviders
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)
        val recentLastKnownLocation = lastKnownLocation?.takeIf { location ->
            System.currentTimeMillis() - location.time <= LAST_KNOWN_LOCATION_MAX_AGE_MILLIS
        }
        val currentLocation = recentLastKnownLocation ?: try {
            withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                locationManager.awaitCurrentLocation(enabledProvider)
            }
        } catch (_: SecurityException) {
            return@withContext locationPermissionRequired("Location permission was revoked.")
        } ?: lastKnownLocation
            ?: return@withContext locationUnavailable("Current location could not be determined.")

        val address = try {
            withTimeoutOrNull(GEOCODER_TIMEOUT_MILLIS) {
                reverseGeocode(currentLocation)
            }
        } catch (exception: IOException) {
            return@withContext DomainResult.failure(
                DomainError(DomainError.Type.NETWORK, "Location lookup failed.", exception),
            )
        } ?: return@withContext locationUnavailable("The city for this location could not be determined.")

        val cityName = listOf(address.locality, address.subAdminArea, address.adminArea)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
        if (cityName.isBlank()) {
            return@withContext locationUnavailable("The city for this location could not be determined.")
        }

        DomainResult.success(
            DeviceLocation(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                cityName = cityName,
                country = address.countryName.orEmpty(),
                adminArea = address.adminArea.orEmpty().takeUnless { it == cityName }.orEmpty(),
            ),
        )
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private suspend fun LocationManager.awaitCurrentLocation(provider: String): Location? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
            }
        }

        return suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }

                override fun onProviderEnabled(provider: String) = Unit

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }
            requestSingleUpdate(provider, listener, Looper.getMainLooper())
            continuation.invokeOnCancellation { removeUpdates(listener) }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(location: Location): Address? {
        if (!Geocoder.isPresent()) {
            return null
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses.firstOrNull())
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    },
                )
            }
        }
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
    }

    private fun locationUnavailable(message: String): DomainResult<DeviceLocation> {
        return DomainResult.failure(DomainError(DomainError.Type.LOCATION_UNAVAILABLE, message))
    }

    private fun locationPermissionRequired(message: String): DomainResult<DeviceLocation> {
        return DomainResult.failure(DomainError(DomainError.Type.LOCATION_PERMISSION_REQUIRED, message))
    }

    private fun Context.hasLocationPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
        const val GEOCODER_TIMEOUT_MILLIS = 8_000L
        const val LAST_KNOWN_LOCATION_MAX_AGE_MILLIS = 5 * 60_000L
        val preferredProviders = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
    }
}
