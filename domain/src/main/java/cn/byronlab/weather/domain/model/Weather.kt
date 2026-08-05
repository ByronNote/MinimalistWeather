package cn.byronlab.weather.domain.model

data class Weather(
    val city: City,
    val currentWeather: CurrentWeather?,
    val forecastDays: List<ForecastDay> = emptyList(),
    val hourlyForecasts: List<HourlyForecast> = emptyList(),
    val airQuality: AirQuality?,
    val lifeIndexes: List<LifeIndex> = emptyList(),
) {
    val cityId: String
        get() = city.cityId

    val cityName: String
        get() = city.name

    val cityNameEn: String
        get() = city.nameEn
}
