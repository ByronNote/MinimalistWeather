package cn.byronlab.weather.presentation.weatherui.tokens

import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
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

    @Test
    fun `strong and violent rain showers use progressively darker palettes`() {
        val strong = WeatherSceneSpec(
            cloudCover = WeatherCloudCover.Overcast,
            precipitation = WeatherPrecipitation.Rain,
            precipitationIntensity = WeatherIntensity.Heavy,
            precipitationPattern = WeatherPrecipitationPattern.Showers,
            windLevel = WeatherWindLevel.Breezy,
        )
        val violent = strong.copy(windLevel = WeatherWindLevel.Windy)

        assertNotEquals(
            weatherVisualStyle(strong).backgroundColors,
            weatherVisualStyle(violent).backgroundColors,
        )
        assertNotEquals(
            weatherVisualStyle(strong.copy(skyPhase = WeatherSkyPhase.Night)).backgroundColors,
            weatherVisualStyle(violent.copy(skyPhase = WeatherSkyPhase.Night)).backgroundColors,
        )
    }

    @Test
    fun `steady snow snow grains and snow showers use distinct palettes`() {
        val steady = WeatherSceneSpec(
            cloudCover = WeatherCloudCover.Overcast,
            precipitation = WeatherPrecipitation.Snow,
            precipitationIntensity = WeatherIntensity.Moderate,
        )
        val grains = steady.copy(
            precipitation = WeatherPrecipitation.SnowGrains,
            precipitationIntensity = WeatherIntensity.Light,
            precipitationPattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Breezy,
        )
        val flurries = steady.copy(
            precipitationPattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Breezy,
        )
        val heavyFlurries = flurries.copy(
            precipitationIntensity = WeatherIntensity.Heavy,
            windLevel = WeatherWindLevel.Windy,
        )

        assertNotEquals(weatherVisualStyle(steady).backgroundColors, weatherVisualStyle(grains).backgroundColors)
        assertNotEquals(weatherVisualStyle(steady).backgroundColors, weatherVisualStyle(flurries).backgroundColors)
        assertNotEquals(weatherVisualStyle(flurries).backgroundColors, weatherVisualStyle(heavyFlurries).backgroundColors)
    }
}
