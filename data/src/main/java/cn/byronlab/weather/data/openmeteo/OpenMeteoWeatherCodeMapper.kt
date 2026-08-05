package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.WeatherCondition

internal object OpenMeteoWeatherCodeMapper {

    fun toDomainCondition(code: Int?): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.Clear
            1 -> WeatherCondition.MainlyClear
            2 -> WeatherCondition.PartlyCloudy
            3 -> WeatherCondition.Overcast
            45, 48 -> WeatherCondition.Fog
            51, 53, 55 -> WeatherCondition.Drizzle
            56, 57 -> WeatherCondition.FreezingDrizzle
            61 -> WeatherCondition.LightRain
            63 -> WeatherCondition.ModerateRain
            65 -> WeatherCondition.HeavyRain
            66, 67 -> WeatherCondition.FreezingRain
            71 -> WeatherCondition.LightSnow
            73 -> WeatherCondition.ModerateSnow
            75 -> WeatherCondition.HeavySnow
            77 -> WeatherCondition.SnowGrains
            80 -> WeatherCondition.RainShowers
            81 -> WeatherCondition.HeavyRainShowers
            82 -> WeatherCondition.ViolentRainShowers
            85 -> WeatherCondition.SnowShowers
            86 -> WeatherCondition.HeavySnowShowers
            95 -> WeatherCondition.Thunderstorm
            96, 99 -> WeatherCondition.ThunderstormWithHail
            else -> WeatherCondition.Unknown
        }
    }
}
