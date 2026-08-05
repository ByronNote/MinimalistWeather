package cn.byronlab.weather.data.repository

import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCatalog
import cn.byronlab.weather.data.openmeteo.OpenMeteoClient
import cn.byronlab.weather.data.openmeteo.OpenMeteoJsonParser
import cn.byronlab.weather.data.openmeteo.OpenMeteoWeatherMapper
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class OpenMeteoWeatherRepository @Inject constructor(
    private val client: OpenMeteoClient,
    private val cache: DataStoreWeatherCache,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    private val parser = OpenMeteoJsonParser()
    private val mapper = OpenMeteoWeatherMapper()

    override suspend fun getWeather(cityId: String, refreshNow: Boolean): DomainResult<Weather> {
        if (refreshNow) {
            return loadRemoteWeather(cityId)
        }
        val city = OpenMeteoCityCatalog.resolve(cityId)
            ?: return DomainResult.failure(DomainError.invalidInput("Unsupported Open-Meteo city id."))
        val cachedPayload = cache.read(cityId)
        if (cachedPayload != null) {
            try {
                return DomainResult.success(
                    mapper.map(
                        city = city,
                        forecast = parser.parseForecast(cachedPayload.forecastPayload),
                        airQuality = parser.parseAirQuality(cachedPayload.airQualityPayload),
                    ),
                )
            } catch (_: SerializationException) {
                cache.clear(cityId)
            }
        }
        return loadRemoteWeather(cityId)
    }

    override suspend fun refreshWeather(cityId: String): DomainResult<Weather> = loadRemoteWeather(cityId)

    private suspend fun loadRemoteWeather(cityId: String): DomainResult<Weather> = withContext(ioDispatcher) {
        val city = OpenMeteoCityCatalog.resolve(cityId)
            ?: return@withContext DomainResult.failure(DomainError.invalidInput("Unsupported Open-Meteo city id."))

        try {
            val forecastPayload = client.getForecast(city)
            val airQualityPayload = client.getAirQuality(city)
            val weather = mapper.map(
                city = city,
                forecast = parser.parseForecast(forecastPayload),
                airQuality = parser.parseAirQuality(airQualityPayload),
            )
            cache.write(
                cityId = cityId,
                forecastPayload = forecastPayload,
                airQualityPayload = airQualityPayload,
            )
            DomainResult.success(weather)
        } catch (exception: IOException) {
            DomainResult.failure(DomainError(DomainError.Type.NETWORK, "Open-Meteo weather request failed.", exception))
        } catch (exception: SerializationException) {
            DomainResult.failure(DomainError(DomainError.Type.UNKNOWN, "Open-Meteo weather response could not be parsed.", exception))
        }
    }
}
