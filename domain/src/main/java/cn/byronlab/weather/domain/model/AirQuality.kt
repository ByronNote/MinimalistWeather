package cn.byronlab.weather.domain.model

data class AirQuality(
    val cityId: String,
    val aqi: Int,
    val pm25: Int,
    val pm10: Int,
    val publishTime: String,
    val advice: String,
    val cityRank: String,
    val quality: String,
    val co: String,
    val so2: String,
    val no2: String,
    val o3: String,
    val primaryPollutant: String,
)
