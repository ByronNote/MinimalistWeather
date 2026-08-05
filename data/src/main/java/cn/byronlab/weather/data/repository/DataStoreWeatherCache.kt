package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.data.di.WeatherCacheDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

internal data class CachedWeatherPayload(
    val cityId: String,
    val forecastPayload: String,
    val airQualityPayload: String,
    val cachedAtMillis: Long,
)

class DataStoreWeatherCache @Inject constructor(
    @param:WeatherCacheDataStore
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    internal suspend fun read(cityId: String): CachedWeatherPayload? = withContext(ioDispatcher) {
        try {
            val preferences = dataStore.data.first()
            val keys = keysFor(cityId)
            val cachedCityId = preferences[keys.cityId]
                ?: preferences[LEGACY_CITY_ID].takeIf { it == cityId }
                ?: return@withContext null
            val useLegacyPayload = preferences[keys.cityId] == null
            val forecastPayload = preferences[
                if (useLegacyPayload) LEGACY_FORECAST_PAYLOAD else keys.forecastPayload
            ].orEmpty()
            val airQualityPayload = preferences[
                if (useLegacyPayload) LEGACY_AIR_QUALITY_PAYLOAD else keys.airQualityPayload
            ].orEmpty()
            if (cachedCityId != cityId || forecastPayload.isBlank() || airQualityPayload.isBlank()) {
                null
            } else {
                CachedWeatherPayload(
                    cityId = cachedCityId,
                    forecastPayload = forecastPayload,
                    airQualityPayload = airQualityPayload,
                    cachedAtMillis = preferences[
                        if (useLegacyPayload) LEGACY_CACHED_AT_MILLIS else keys.cachedAtMillis
                    ] ?: 0L,
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }
    }

    internal suspend fun write(
        cityId: String,
        forecastPayload: String,
        airQualityPayload: String,
        cachedAtMillis: Long = System.currentTimeMillis(),
    ) = withContext(ioDispatcher) {
        try {
            val keys = keysFor(cityId)
            dataStore.edit { preferences ->
                preferences[keys.cityId] = cityId
                preferences[keys.forecastPayload] = forecastPayload
                preferences[keys.airQualityPayload] = airQualityPayload
                preferences[keys.cachedAtMillis] = cachedAtMillis
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // A fresh network result remains usable even if the best-effort cache write fails.
        }
    }

    internal suspend fun clear(cityId: String? = null) = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                if (cityId == null) {
                    preferences.clear()
                } else {
                    val keys = keysFor(cityId)
                    preferences.remove(keys.cityId)
                    preferences.remove(keys.forecastPayload)
                    preferences.remove(keys.airQualityPayload)
                    preferences.remove(keys.cachedAtMillis)
                    if (preferences[LEGACY_CITY_ID] == cityId) {
                        preferences.remove(LEGACY_CITY_ID)
                        preferences.remove(LEGACY_FORECAST_PAYLOAD)
                        preferences.remove(LEGACY_AIR_QUALITY_PAYLOAD)
                        preferences.remove(LEGACY_CACHED_AT_MILLIS)
                    }
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            Unit
        }
    }

    companion object {
        const val CACHE_DATASTORE_NAME = "weather_response_cache"

        private val LEGACY_CITY_ID = stringPreferencesKey("city_id")
        private val LEGACY_FORECAST_PAYLOAD = stringPreferencesKey("forecast_payload")
        private val LEGACY_AIR_QUALITY_PAYLOAD = stringPreferencesKey("air_quality_payload")
        private val LEGACY_CACHED_AT_MILLIS = longPreferencesKey("cached_at_millis")

        private fun keysFor(cityId: String): CityCacheKeys {
            val suffix = MessageDigest.getInstance("SHA-256")
                .digest(cityId.toByteArray(Charsets.UTF_8))
                .take(12)
                .joinToString(separator = "") { byte ->
                    val value = byte.toInt() and 0xFF
                    "${HEX_DIGITS[value ushr 4]}${HEX_DIGITS[value and 0x0F]}"
                }
            return CityCacheKeys(
                cityId = stringPreferencesKey("city_id_$suffix"),
                forecastPayload = stringPreferencesKey("forecast_payload_$suffix"),
                airQualityPayload = stringPreferencesKey("air_quality_payload_$suffix"),
                cachedAtMillis = longPreferencesKey("cached_at_millis_$suffix"),
            )
        }

        private const val HEX_DIGITS = "0123456789abcdef"
    }
}

private data class CityCacheKeys(
    val cityId: Preferences.Key<String>,
    val forecastPayload: Preferences.Key<String>,
    val airQualityPayload: Preferences.Key<String>,
    val cachedAtMillis: Preferences.Key<Long>,
)
