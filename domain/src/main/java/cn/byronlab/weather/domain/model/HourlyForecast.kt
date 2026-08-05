package cn.byronlab.weather.domain.model

data class HourlyForecast(
    val cityId: String,
    val dateTime: String,
    val condition: WeatherCondition,
    val temperature: Int,
    val isDay: Boolean = true,
)
