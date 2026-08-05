package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenMeteoWeatherCodeMapperTest {

    @Test
    fun `maps every supported WMO weather code to a domain condition`() {
        val expectedConditions = mapOf(
            0 to WeatherCondition.Clear,
            1 to WeatherCondition.MainlyClear,
            2 to WeatherCondition.PartlyCloudy,
            3 to WeatherCondition.Overcast,
            45 to WeatherCondition.Fog,
            48 to WeatherCondition.Fog,
            51 to WeatherCondition.Drizzle,
            53 to WeatherCondition.Drizzle,
            55 to WeatherCondition.Drizzle,
            56 to WeatherCondition.FreezingDrizzle,
            57 to WeatherCondition.FreezingDrizzle,
            61 to WeatherCondition.LightRain,
            63 to WeatherCondition.ModerateRain,
            65 to WeatherCondition.HeavyRain,
            66 to WeatherCondition.FreezingRain,
            67 to WeatherCondition.FreezingRain,
            71 to WeatherCondition.LightSnow,
            73 to WeatherCondition.ModerateSnow,
            75 to WeatherCondition.HeavySnow,
            77 to WeatherCondition.SnowGrains,
            80 to WeatherCondition.RainShowers,
            81 to WeatherCondition.HeavyRainShowers,
            82 to WeatherCondition.ViolentRainShowers,
            85 to WeatherCondition.SnowShowers,
            86 to WeatherCondition.HeavySnowShowers,
            95 to WeatherCondition.Thunderstorm,
            96 to WeatherCondition.ThunderstormWithHail,
            99 to WeatherCondition.ThunderstormWithHail,
        )

        expectedConditions.forEach { (code, expectedCondition) ->
            assertEquals(code.toString(), expectedCondition, OpenMeteoWeatherCodeMapper.toDomainCondition(code))
        }
    }

    @Test
    fun `maps missing and unsupported codes to unknown`() {
        assertEquals(WeatherCondition.Unknown, OpenMeteoWeatherCodeMapper.toDomainCondition(null))
        assertEquals(WeatherCondition.Unknown, OpenMeteoWeatherCodeMapper.toDomainCondition(-1))
        assertEquals(WeatherCondition.Unknown, OpenMeteoWeatherCodeMapper.toDomainCondition(1000))
    }
}
