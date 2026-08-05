package cn.byronlab.weather.data.repository

import cn.byronlab.weather.data.openmeteo.OpenMeteoCity
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCatalog
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCodec
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityMapper
import cn.byronlab.weather.data.openmeteo.OpenMeteoClient
import cn.byronlab.weather.data.openmeteo.OpenMeteoJsonParser
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class OpenMeteoCityRepository @Inject constructor(
    private val client: OpenMeteoClient,
    private val cityStore: DataStoreCityStore,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : CityRepository {

    private val parser = OpenMeteoJsonParser()
    private val mapper = OpenMeteoCityMapper()

    override suspend fun searchCities(keyword: String): DomainResult<List<City>> = withContext(ioDispatcher) {
        val query = keyword.trim()
        if (query.isBlank()) {
            return@withContext DomainResult.success(emptyList())
        }

        val localCities = OpenMeteoCityCatalog.searchLocal(query)
        val remoteResult = try {
            Result.success(mapper.map(parser.parseGeocoding(client.searchCities(query))))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (exception: SerializationException) {
            Result.failure(exception)
        }

        if (remoteResult.isFailure && localCities.isEmpty()) {
            return@withContext DomainResult.failure(remoteResult.exceptionOrNull().toDomainError())
        }

        val cities = (localCities + remoteResult.getOrDefault(emptyList()))
            .distinctBy { "${it.geonameId}:${it.latitude}:${it.longitude}" }
            .sortedWith(compareByDescending<OpenMeteoCity> { it.population }.thenBy { it.name })
            .take(20)
            .map(OpenMeteoCityCodec::toDomainCity)
        DomainResult.success(cities)
    }

    override suspend fun getPopularCities(): DomainResult<List<City>> = withContext(ioDispatcher) {
        DomainResult.success(OpenMeteoCityCatalog.popularCities.map(OpenMeteoCityCodec::toDomainCity))
    }

    override fun resolveLocation(location: DeviceLocation): DomainResult<City> {
        if (
            !location.latitude.isFinite() ||
            !location.longitude.isFinite() ||
            location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0 ||
            location.cityName.isBlank()
        ) {
            return DomainResult.failure(DomainError.invalidInput("Device location is invalid."))
        }
        val city = OpenMeteoCity(
            geonameId = "device-location",
            name = location.cityName,
            nameEn = location.cityName,
            country = location.country,
            admin1 = location.adminArea,
            latitude = location.latitude,
            longitude = location.longitude,
            timezone = "auto",
        )
        return DomainResult.success(OpenMeteoCityCodec.toDomainCity(city))
    }

    override fun observeAddedCities(): Flow<DomainResult<List<City>>> {
        return cityStore.observeAddedCityIds().map { result ->
            when (result) {
                is DomainResult.Success -> DomainResult.success(citiesFor(result.data))
                is DomainResult.Failure -> result
            }
        }
    }

    override suspend fun getAddedCities(): DomainResult<List<City>> {
        return when (val result = cityStore.getAddedCityIds()) {
            is DomainResult.Success -> DomainResult.success(citiesFor(result.data))
            is DomainResult.Failure -> return result
        }
    }

    override suspend fun addCity(cityId: String): DomainResult<Unit> {
        if (OpenMeteoCityCatalog.resolve(cityId) == null) {
            return DomainResult.failure(DomainError(DomainError.Type.NOT_FOUND, "City could not be resolved."))
        }
        return cityStore.addCity(cityId)
    }

    override suspend fun removeCity(cityId: String): DomainResult<Unit> {
        return cityStore.removeCity(cityId)
    }

    override suspend fun getRecentCities(): DomainResult<List<City>> {
        return when (val result = cityStore.getRecentCityIds()) {
            is DomainResult.Success -> DomainResult.success(citiesFor(result.data))
            is DomainResult.Failure -> result
        }
    }

    override suspend fun recordRecentCity(cityId: String): DomainResult<Unit> {
        if (OpenMeteoCityCatalog.resolve(cityId) == null) {
            return DomainResult.failure(DomainError(DomainError.Type.NOT_FOUND, "City could not be resolved."))
        }
        return cityStore.recordRecentCity(cityId)
    }

    private fun citiesFor(cityIds: List<String>): List<City> {
        return cityIds.mapNotNull { cityId ->
            OpenMeteoCityCatalog.resolve(cityId)?.let(OpenMeteoCityCodec::toDomainCity)
        }
    }

    private fun Throwable?.toDomainError(): DomainError {
        return when (this) {
            is IOException -> DomainError(DomainError.Type.NETWORK, "Open-Meteo city search failed.", this)
            is SerializationException -> DomainError(
                DomainError.Type.UNKNOWN,
                "Open-Meteo city response could not be parsed.",
                this,
            )
            else -> DomainError.unknown(this)
        }
    }
}
