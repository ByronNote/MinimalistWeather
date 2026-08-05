package cn.byronlab.weather.presentation.weatherui.render

import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherSceneLayersTest {

    @Test
    fun `weather scenes use the expected generated image layers`() {
        assertEquals(
            listOf(WeatherSceneAsset.Sun, WeatherSceneAsset.ThinCloud),
            weatherSceneLayers(WeatherSceneSpec()).map { it.asset },
        )
        assertEquals(
            listOf(WeatherSceneAsset.Moon, WeatherSceneAsset.ThinCloud),
            weatherSceneLayers(WeatherSceneSpec(skyPhase = WeatherSkyPhase.Night)).map { it.asset },
        )
        assertEquals(
            listOf(WeatherSceneAsset.Sun, WeatherSceneAsset.ThinCloud),
            weatherSceneLayers(
                WeatherSceneSpec(cloudCover = WeatherCloudCover.MostlyClear),
            ).map { it.asset },
        )
        assertEquals(
            listOf(WeatherSceneAsset.Sun, WeatherSceneAsset.ThinCloud, WeatherSceneAsset.BrightCloud),
            weatherSceneLayers(
                WeatherSceneSpec(cloudCover = WeatherCloudCover.PartlyCloudy),
            ).map { it.asset },
        )
        assertEquals(
            listOf(
                WeatherSceneAsset.BrightCloud,
                WeatherSceneAsset.ThinCloud,
                WeatherSceneAsset.BrightCloud,
            ),
            weatherSceneLayers(
                WeatherSceneSpec(cloudCover = WeatherCloudCover.Cloudy),
            ).map { it.asset },
        )
        assertEquals(
            listOf(
                WeatherSceneAsset.OvercastCloud,
                WeatherSceneAsset.OvercastCloud,
                WeatherSceneAsset.ThinCloud,
            ),
            weatherSceneLayers(
                WeatherSceneSpec(cloudCover = WeatherCloudCover.Overcast),
            ).map { it.asset },
        )
        assertEquals(
            listOf(WeatherSceneAsset.RainCloud, WeatherSceneAsset.RainCloud),
            weatherSceneLayers(
                WeatherSceneSpec(
                    precipitation = WeatherPrecipitation.Rain,
                    precipitationIntensity = WeatherIntensity.Moderate,
                ),
            ).map { it.asset },
        )
        assertEquals(
            listOf(WeatherSceneAsset.SnowCloud, WeatherSceneAsset.SnowCloud),
            weatherSceneLayers(
                WeatherSceneSpec(
                    precipitation = WeatherPrecipitation.Snow,
                    precipitationIntensity = WeatherIntensity.Moderate,
                ),
            ).map { it.asset },
        )
        assertEquals(
            listOf(
                WeatherSceneAsset.StormCloud,
                WeatherSceneAsset.OvercastCloud,
                WeatherSceneAsset.StormCloud,
            ),
            weatherSceneLayers(
                WeatherSceneSpec(
                    cloudCover = WeatherCloudCover.Overcast,
                    precipitation = WeatherPrecipitation.Hail,
                    precipitationIntensity = WeatherIntensity.Heavy,
                    precipitationPattern = WeatherPrecipitationPattern.Showers,
                    lightningIntensity = WeatherLightningIntensity.Frequent,
                ),
            ).map { it.asset },
        )
    }

    @Test
    fun `all image layers have valid presentation values`() {
        val scenes = listOf(
            WeatherSceneSpec(),
            WeatherSceneSpec(skyPhase = WeatherSkyPhase.Night),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.MostlyClear),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.PartlyCloudy),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.Cloudy),
            WeatherSceneSpec(cloudCover = WeatherCloudCover.Overcast),
            WeatherSceneSpec(
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
            ),
            WeatherSceneSpec(
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Moderate,
            ),
        )

        scenes.flatMap(::weatherSceneLayers).forEach { layer ->
            assertTrue(layer.size > 0)
            assertTrue(layer.alpha in 0f..1f)
        }
    }

    @Test
    fun `fog removes cloud cutouts and heavy precipitation increases cloud density`() {
        val fogLayers = weatherSceneLayers(
            WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                atmosphere = WeatherAtmosphere.Fog,
            ),
        )
        val lightRainLayers = weatherSceneLayers(
            WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Light,
            ),
        )
        val heavyRainLayers = weatherSceneLayers(
            WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
            ),
        )

        assertTrue(fogLayers.isEmpty())
        assertEquals(2, lightRainLayers.size)
        assertEquals(3, heavyRainLayers.size)
        assertTrue(heavyRainLayers.first().alpha > lightRainLayers.first().alpha)
    }
}
