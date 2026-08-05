package cn.byronlab.weather.data.openmeteo

import kotlinx.serialization.json.Json

internal class OpenMeteoJsonParser(
    private val json: Json = defaultJson,
) {

    fun parseGeocoding(payload: String): OpenMeteoGeocodingResponse =
        json.decodeFromString(payload)

    fun parseForecast(payload: String): OpenMeteoForecastResponse =
        json.decodeFromString(payload)

    fun parseAirQuality(payload: String): OpenMeteoAirQualityResponse =
        json.decodeFromString(payload)

    private companion object {
        val defaultJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
