package cn.byronlab.weather.data.openmeteo

internal data class OpenMeteoCity(
    val geonameId: String,
    val name: String,
    val nameEn: String,
    val country: String,
    val admin1: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val population: Int = 0,
)
