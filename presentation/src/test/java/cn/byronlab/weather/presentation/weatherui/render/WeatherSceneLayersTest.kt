package cn.byronlab.weather.presentation.weatherui.render

import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
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

    @Test
    fun `fog uses irregular banks with a continuous opacity falloff`() {
        assertEquals(5, fogBankSpecs.size)
        assertTrue(fogBankSpecs.map { it.widthFraction }.distinct().size >= 4)
        assertTrue(fogBankSpecs.map { it.heightFraction }.distinct().size >= 4)
        assertTrue(fogBankSpecs.map { it.centerXFraction }.distinct().size >= 4)
        assertTrue(fogBankSpecs.any { it.widthFraction < 1f })
        assertTrue(fogBankSpecs.any { it.widthFraction > 1f })
        assertTrue(fogBankSpecs.any { it.direction < 0f })
        assertTrue(fogBankSpecs.any { it.direction > 0f })
        assertTrue(fogBankSpecs.all { it.alpha in 0.07f..0.14f })
        assertTrue(fogBankSpecs.all { it.centerXFraction in 0f..1f })
        assertTrue(fogBankSpecs.all { it.centerYFraction in 0f..1f })
        assertEquals(0f, fogOpacityProfile.first().first)
        assertEquals(1f, fogOpacityProfile.first().second)
        assertEquals(1f, fogOpacityProfile.last().first)
        assertEquals(0f, fogOpacityProfile.last().second)
        assertTrue(fogOpacityProfile.zipWithNext().all { (current, next) ->
            current.first < next.first && current.second > next.second
        })
    }

    @Test
    fun `rain uses increasing density with three distinct depth layers`() {
        val drizzle = rainVisualStyle(WeatherIntensity.Light, drizzle = true)
        val light = rainVisualStyle(WeatherIntensity.Light, drizzle = false)
        val moderate = rainVisualStyle(WeatherIntensity.Moderate, drizzle = false)
        val heavy = rainVisualStyle(WeatherIntensity.Heavy, drizzle = false)

        assertTrue(drizzle.lengthDp < light.lengthDp)
        assertTrue(light.dropCount < moderate.dropCount)
        assertTrue(moderate.dropCount < heavy.dropCount)
        assertTrue(light.lengthDp < moderate.lengthDp)
        assertTrue(moderate.lengthDp < heavy.lengthDp)
        assertTrue(light.alpha < moderate.alpha)
        assertTrue(moderate.alpha < heavy.alpha)
        assertTrue(drizzle.veilAlpha < light.veilAlpha)
        assertTrue(light.veilAlpha < moderate.veilAlpha)
        assertTrue(moderate.veilAlpha < heavy.veilAlpha)
        assertTrue(heavy.dropCount >= moderate.dropCount * 1.3f)

        assertEquals(3, rainLayerSpecs.size)
        assertEquals(1f, rainLayerSpecs.sumOf { it.countFraction.toDouble() }.toFloat(), 0.001f)
        assertTrue(rainLayerSpecs.zipWithNext().all { (farther, nearer) ->
            farther.lengthScale < nearer.lengthScale &&
                farther.strokeWidthDp < nearer.strokeWidthDp &&
                farther.alphaScale < nearer.alphaScale &&
                farther.fallCycles < nearer.fallCycles
        })
    }

    @Test
    fun `rain distribution noise is stable varied and normalized`() {
        val firstPass = List(32) { rainNoise(it, salt = 17) }
        val secondPass = List(32) { rainNoise(it, salt = 17) }

        assertEquals(firstPass, secondPass)
        assertTrue(firstPass.all { it in 0f..1f })
        assertTrue(firstPass.distinct().size >= 30)
        assertTrue(firstPass.any { it < 0.15f })
        assertTrue(firstPass.any { it > 0.85f })
    }

    @Test
    fun `strong and violent showers progressively intensify rain presentation`() {
        val regular = rainPatternStyle(
            intensity = WeatherIntensity.Moderate,
            pattern = WeatherPrecipitationPattern.Showers,
            windLevel = WeatherWindLevel.Breezy,
        )
        val strong = rainPatternStyle(
            intensity = WeatherIntensity.Heavy,
            pattern = WeatherPrecipitationPattern.Showers,
            windLevel = WeatherWindLevel.Breezy,
        )
        val violent = rainPatternStyle(
            intensity = WeatherIntensity.Heavy,
            pattern = WeatherPrecipitationPattern.Showers,
            windLevel = WeatherWindLevel.Windy,
        )

        assertTrue(regular.countScale < strong.countScale)
        assertTrue(strong.countScale < violent.countScale)
        assertTrue(regular.lengthScale < strong.lengthScale)
        assertTrue(strong.lengthScale < violent.lengthScale)
        assertTrue(regular.alphaScale < strong.alphaScale)
        assertTrue(strong.alphaScale < violent.alphaScale)
        assertTrue(regular.veilScale < strong.veilScale)
        assertTrue(strong.veilScale < violent.veilScale)
        assertTrue(regular.pulseMax < strong.pulseMax)
        assertTrue(strong.pulseMax < violent.pulseMax)
    }

    @Test
    fun `strong and violent showers fall faster than steady heavy rain`() {
        val steady = WeatherSceneSpec(
            precipitation = WeatherPrecipitation.Rain,
            precipitationIntensity = WeatherIntensity.Heavy,
        )
        val strong = steady.copy(
            precipitationPattern = WeatherPrecipitationPattern.Showers,
            windLevel = WeatherWindLevel.Breezy,
        )
        val violent = strong.copy(windLevel = WeatherWindLevel.Windy)

        assertTrue(strong.precipitationMotionDurationMillis() < steady.precipitationMotionDurationMillis())
        assertTrue(violent.precipitationMotionDurationMillis() < strong.precipitationMotionDurationMillis())
    }

    @Test
    fun `lightning remains hidden between brief double pulse events`() {
        val occasional = lightningFlashProfile(WeatherLightningIntensity.Occasional)
        val frequent = lightningFlashProfile(WeatherLightningIntensity.Frequent)

        listOf(occasional, frequent).forEach { profile ->
            assertEquals(0, profile.frames.first().timeMillis)
            assertEquals(0f, profile.frames.first().intensity, 0.001f)
            assertEquals(profile.durationMillis, profile.frames.last().timeMillis)
            assertEquals(0f, profile.frames.last().intensity, 0.001f)
            assertTrue(profile.frames.all { it.intensity in 0f..1f })
            assertTrue(profile.frames.zipWithNext().all { (current, next) ->
                current.timeMillis < next.timeMillis
            })
            assertTrue(profile.frames.count { it.intensity >= 0.5f } >= 2)
            assertTrue(profile.frames.last { it.intensity > 0f }.timeMillis -
                profile.frames.first { it.intensity > 0f }.timeMillis < 700)
        }

        assertTrue(occasional.durationMillis > frequent.durationMillis)
        assertTrue(occasional.durationMillis <= 5_800)
        assertTrue(frequent.durationMillis <= 3_600)
        assertTrue(occasional.frames.first { it.intensity > 0f }.timeMillis > occasional.durationMillis / 2)
        assertTrue(frequent.frames.first { it.intensity > 0f }.timeMillis > frequent.durationMillis / 2)
        assertTrue(frequent.frames.count { it.intensity > 0f } > occasional.frames.count { it.intensity > 0f })
    }

    @Test
    fun `lightning visibility has no static baseline and rises smoothly`() {
        assertEquals(0f, lightningVisibility(0f), 0.001f)
        assertEquals(0f, lightningVisibility(0.02f), 0.001f)
        assertTrue(lightningVisibility(0.10f) > 0f)
        assertTrue(lightningVisibility(0.60f) > lightningVisibility(0.10f))
        assertEquals(1f, lightningVisibility(1f), 0.001f)
    }

    @Test
    fun `lightning rotates through varied origins lengths branches and directions`() {
        assertTrue(lightningStrikeSpecs.size >= 6)
        assertTrue(lightningStrikeSpecs.map { it.originXFraction }.distinct().size >= 6)
        assertTrue(lightningStrikeSpecs.map { it.mainPath.last().yFraction }.distinct().size >= 5)
        assertTrue(lightningStrikeSpecs.map { it.branchPaths.size }.distinct().size >= 2)
        assertTrue(lightningStrikeSpecs.any { it.mainPath.last().xFraction < 0f })
        assertTrue(lightningStrikeSpecs.any { it.mainPath.last().xFraction > 0f })
        assertTrue(lightningStrikeSpecs.any { strike ->
            strike.branchPaths.any { branch -> branch.first().yFraction == 0f }
        })

        lightningStrikeSpecs.forEach { strike ->
            assertTrue(strike.originXFraction in 0.2f..0.9f)
            assertTrue(strike.originYFraction in 0.08f..0.17f)
            assertTrue(strike.mainPath.size >= 7)
            assertEquals(LightningPathPoint(0f, 0f), strike.mainPath.first())
            assertTrue(strike.branchPaths.all { it.size >= 3 })
            assertTrue(strike.brightnessScale in 0.8f..1.1f)
            assertTrue(strike.glowScale in 0.8f..1.1f)
        }

        assertEquals(0, lightningStrikeIndex(0f))
        assertEquals(0, lightningStrikeIndex(0.99f))
        assertEquals(1, lightningStrikeIndex(1f))
        assertEquals(lightningStrikeSpecs.lastIndex, lightningStrikeIndex(Float.MAX_VALUE))
    }

    @Test
    fun `snow uses increasing density with distinct depth layers`() {
        val light = snowVisualStyle(WeatherIntensity.Light)
        val moderate = snowVisualStyle(WeatherIntensity.Moderate)
        val heavy = snowVisualStyle(WeatherIntensity.Heavy)

        assertTrue(light.flakeCount < moderate.flakeCount)
        assertTrue(moderate.flakeCount < heavy.flakeCount)
        assertTrue(light.maxRadiusDp < moderate.maxRadiusDp)
        assertTrue(moderate.maxRadiusDp < heavy.maxRadiusDp)
        assertTrue(light.alpha < moderate.alpha)
        assertTrue(moderate.alpha < heavy.alpha)
        assertTrue(light.veilAlpha < moderate.veilAlpha)
        assertTrue(moderate.veilAlpha < heavy.veilAlpha)

        assertEquals(3, snowLayerSpecs.size)
        assertEquals(1f, snowLayerSpecs.sumOf { it.countFraction.toDouble() }.toFloat(), 0.001f)
        assertTrue(snowLayerSpecs.zipWithNext().all { (farther, nearer) ->
            farther.radiusScale < nearer.radiusScale &&
                farther.alphaScale < nearer.alphaScale &&
                farther.fallScale < nearer.fallScale &&
                farther.driftScale < nearer.driftScale
        })
    }

    @Test
    fun `heavy snow showers amplify steady snow without fading out`() {
        val steady = snowPatternStyle(
            intensity = WeatherIntensity.Heavy,
            pattern = WeatherPrecipitationPattern.Steady,
            windLevel = WeatherWindLevel.Calm,
        )
        val flurries = snowPatternStyle(
            intensity = WeatherIntensity.Moderate,
            pattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Breezy,
        )
        val heavyFlurries = snowPatternStyle(
            intensity = WeatherIntensity.Heavy,
            pattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Windy,
        )

        assertTrue(steady.countScale < flurries.countScale)
        assertTrue(flurries.countScale < heavyFlurries.countScale)
        assertTrue(flurries.radiusScale < heavyFlurries.radiusScale)
        assertTrue(flurries.alphaScale < heavyFlurries.alphaScale)
        assertTrue(flurries.veilScale < heavyFlurries.veilScale)
        assertTrue(flurries.driftScale < heavyFlurries.driftScale)
        assertTrue(flurries.pulseMin > 0.8f)
        assertTrue(heavyFlurries.pulseMin > 0.9f)
    }

    @Test
    fun `snow showers snow grains sleet and hail have distinct motion and density`() {
        val lightSnow = WeatherSceneSpec(
            precipitation = WeatherPrecipitation.Snow,
            precipitationIntensity = WeatherIntensity.Light,
        )
        val moderateSnow = lightSnow.copy(precipitationIntensity = WeatherIntensity.Moderate)
        val heavySnow = lightSnow.copy(precipitationIntensity = WeatherIntensity.Heavy)
        val snowShower = moderateSnow.copy(
            precipitationPattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Breezy,
        )
        val heavySnowShower = heavySnow.copy(
            precipitationPattern = WeatherPrecipitationPattern.Flurries,
            windLevel = WeatherWindLevel.Windy,
        )

        assertTrue(moderateSnow.precipitationMotionDurationMillis() < lightSnow.precipitationMotionDurationMillis())
        assertTrue(heavySnow.precipitationMotionDurationMillis() < moderateSnow.precipitationMotionDurationMillis())
        assertTrue(snowShower.precipitationMotionDurationMillis() < moderateSnow.precipitationMotionDurationMillis())
        assertTrue(heavySnowShower.precipitationMotionDurationMillis() < heavySnow.precipitationMotionDurationMillis())
        assertTrue(snowGrainCount(WeatherIntensity.Light) > snowVisualStyle(WeatherIntensity.Light).flakeCount)
        assertTrue(iceParticleCount(WeatherIntensity.Heavy, isHail = true) >
            iceParticleCount(WeatherIntensity.Heavy, isHail = false))
    }
}
