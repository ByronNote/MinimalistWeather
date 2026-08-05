package cn.byronlab.weather.domain.model

data class CurrentWeather(
    val cityId: String,
    val condition: WeatherCondition,
    val temperature: String,
    val humidity: String,
    val windDirection: String,
    val windSpeed: String,
    val observedAtMillis: Long,
    val windPower: String,
    val rain: String,
    val feelsTemperature: String,
    val airPressure: String,
    val isDay: Boolean = true,
)
