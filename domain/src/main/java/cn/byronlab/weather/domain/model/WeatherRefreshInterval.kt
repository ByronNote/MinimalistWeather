package cn.byronlab.weather.domain.model

enum class WeatherRefreshInterval(
    val minutes: Long,
) {
    FifteenMinutes(15),
    ThirtyMinutes(30),
    OneHour(60),
    ThreeHours(180),
}
