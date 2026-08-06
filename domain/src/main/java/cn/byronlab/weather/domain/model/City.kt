package cn.byronlab.weather.domain.model

data class City(
    val cityId: String,
    val name: String,
    val nameEn: String,
    val root: String,
    val parent: String,
    val longitude: String,
    val latitude: String,
    val uniqueId: String = cityId,
)
