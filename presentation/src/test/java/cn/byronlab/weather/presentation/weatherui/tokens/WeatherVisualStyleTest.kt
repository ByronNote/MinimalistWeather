package cn.byronlab.weather.presentation.weatherui.tokens

import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WeatherVisualStyleTest {

    @Test
    fun `every major weather family has a distinct night palette`() {
        val daytimeScenes = listOf(
            WeatherSceneSpec(),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.MostlyClear),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.Cloudy),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.Overcast),
            WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
            ),
            WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Moderate,
            ),
        )

        daytimeScenes.forEach { daytimeScene ->
            val nightScene = daytimeScene.copy(skyPhase = WeatherSkyPhase.Night)
            assertNotEquals(
                daytimeScene.toString(),
                weatherVisualStyle(daytimeScene).backgroundColors,
                weatherVisualStyle(nightScene).backgroundColors,
            )
        }
    }

    @Test
    fun `showers freezing rain and thunderstorms use distinct palettes`() {
        val steadyRain = WeatherSceneSpec(
            cloudCover = WeatherCloudCover.Overcast,
            precipitation = WeatherPrecipitation.Rain,
            precipitationIntensity = WeatherIntensity.Moderate,
        )
        val shower = steadyRain.copy(
            cloudCover = WeatherCloudCover.Cloudy,
            precipitationPattern = WeatherPrecipitationPattern.Showers,
        )
        val freezingRain = steadyRain.copy(freezing = true)
        val thunderstorm = steadyRain.copy(
            precipitationIntensity = WeatherIntensity.Heavy,
            precipitationPattern = WeatherPrecipitationPattern.Showers,
            lightningIntensity = WeatherLightningIntensity.Occasional,
        )

        assertNotEquals(
            weatherVisualStyle(steadyRain).backgroundColors,
            weatherVisualStyle(shower).backgroundColors,
        )
        assertNotEquals(
            weatherVisualStyle(steadyRain).backgroundColors,
            weatherVisualStyle(freezingRain).backgroundColors,
        )
        assertNotEquals(
            weatherVisualStyle(shower).backgroundColors,
            weatherVisualStyle(thunderstorm).backgroundColors,
        )
    }
}
