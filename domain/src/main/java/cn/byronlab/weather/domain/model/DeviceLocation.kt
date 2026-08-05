package cn.byronlab.weather.domain.model

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String,
    val adminArea: String,
)
