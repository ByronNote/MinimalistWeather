package cn.byronlab.weather.data.openmeteo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenMeteoGeocodingResponse(
    val results: List<OpenMeteoGeocodingCityResponse> = emptyList(),
)

@Serializable
internal data class OpenMeteoGeocodingCityResponse(
    val id: Long? = null,
    val name: String? = null,
    val country: String? = null,
    val admin1: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val population: Int? = null,
)

@Serializable
internal data class OpenMeteoForecastResponse(
    val current: OpenMeteoCurrentForecastResponse? = null,
    val daily: OpenMeteoDailyForecastResponse? = null,
    val hourly: OpenMeteoHourlyForecastResponse? = null,
)

@Serializable
internal data class OpenMeteoCurrentForecastResponse(
    val time: String? = null,
    @SerialName("temperature_2m")
    val temperature2m: Double? = null,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: Double? = null,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double? = null,
    @SerialName("is_day")
    val isDay: Int? = null,
    val precipitation: Double? = null,
    val rain: Double? = null,
    @SerialName("weather_code")
    val weatherCode: Int? = null,
    @SerialName("surface_pressure")
    val surfacePressure: Double? = null,
    @SerialName("wind_speed_10m")
    val windSpeed10m: Double? = null,
    @SerialName("wind_direction_10m")
    val windDirection10m: Double? = null,
)

@Serializable
internal data class OpenMeteoDailyForecastResponse(
    val time: List<String> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double?> = emptyList(),
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Double?> = emptyList(),
    @SerialName("uv_index_max")
    val uvIndexMax: List<Double?> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
)

@Serializable
internal data class OpenMeteoHourlyForecastResponse(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m")
    val temperature2m: List<Double?> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
    @SerialName("is_day")
    val isDay: List<Int?> = emptyList(),
)

@Serializable
internal data class OpenMeteoAirQualityResponse(
    val current: OpenMeteoCurrentAirQualityResponse? = null,
)

@Serializable
internal data class OpenMeteoCurrentAirQualityResponse(
    val time: String? = null,
    @SerialName("us_aqi")
    val usAqi: Double? = null,
    val pm10: Double? = null,
    @SerialName("pm2_5")
    val pm25: Double? = null,
    @SerialName("carbon_monoxide")
    val carbonMonoxide: Double? = null,
    @SerialName("nitrogen_dioxide")
    val nitrogenDioxide: Double? = null,
    @SerialName("sulphur_dioxide")
    val sulphurDioxide: Double? = null,
    val ozone: Double? = null,
)
