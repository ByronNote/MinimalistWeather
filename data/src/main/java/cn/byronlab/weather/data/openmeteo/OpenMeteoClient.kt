package cn.byronlab.weather.data.openmeteo

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class OpenMeteoClient(
    private val okHttpClient: OkHttpClient,
) {

    fun searchCities(query: String): String {
        return requestPayload(
            urlBuilder(GEOCODING_BASE_URL)
                .addQueryParameter("name", query)
                .addQueryParameter("count", "10")
                .addQueryParameter("language", "zh")
                .addQueryParameter("format", "json")
                .build(),
        )
    }

    internal fun getForecast(city: OpenMeteoCity): String {
        return requestPayload(
            urlBuilder(FORECAST_BASE_URL)
                .addQueryParameter("latitude", city.latitude.toString())
                .addQueryParameter("longitude", city.longitude.toString())
                .addQueryParameter(
                    "current",
                    "temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
                        "precipitation,rain,weather_code,surface_pressure,wind_speed_10m," +
                        "wind_direction_10m",
                )
                .addQueryParameter(
                    "daily",
                    "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum," +
                        "precipitation_probability_max,uv_index_max,sunrise,sunset",
                )
                .addQueryParameter(
                    "hourly",
                    "temperature_2m,weather_code,is_day",
                )
                .addQueryParameter("timezone", "auto")
                .addQueryParameter("forecast_days", "7")
                .addQueryParameter("wind_speed_unit", "ms")
                .build(),
        )
    }

    internal fun getAirQuality(city: OpenMeteoCity): String {
        return requestPayload(
            urlBuilder(AIR_QUALITY_BASE_URL)
                .addQueryParameter("latitude", city.latitude.toString())
                .addQueryParameter("longitude", city.longitude.toString())
                .addQueryParameter(
                    "current",
                    "us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone",
                )
                .addQueryParameter("timezone", "auto")
                .build(),
        )
    }

    private fun requestPayload(url: HttpUrl): String {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Open-Meteo request failed with HTTP ${response.code}.")
            }
            return response.body.string()
        }
    }

    private fun urlBuilder(baseUrl: String): HttpUrl.Builder {
        return baseUrl.toHttpUrl().newBuilder()
    }

    private companion object {
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        const val FORECAST_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/v1/air-quality"
    }
}
