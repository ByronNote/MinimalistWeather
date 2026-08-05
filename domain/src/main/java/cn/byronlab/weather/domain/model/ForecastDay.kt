package cn.byronlab.weather.domain.model

data class ForecastDay(
    val cityId: String,
    val condition: WeatherCondition,
    val dayCondition: WeatherCondition,
    val nightCondition: WeatherCondition,
    val tempMax: Int,
    val tempMin: Int,
    val wind: String,
    val date: String,
    val week: String,
    val precipitationProbability: String,
    val uvLevel: String,
    val visibility: String,
    val humidity: String,
    val pressure: String,
    val precipitation: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
)
