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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherSceneLayersTest {

    @Test
    fun `star field is reserved for clear nights`() {
        assertTrue(
            WeatherSceneSpec(skyPhase = WeatherSkyPhase.Night).showsClearNightStars(),
        )
        assertFalse(WeatherSceneSpec().showsClearNightStars())
        assertFalse(
            WeatherSceneSpec(
                skyPhase = WeatherSkyPhase.Night,
                cloudCover = WeatherCloudCover.MostlyClear,
            ).showsClearNightStars(),
        )
        assertFalse(
            WeatherSceneSpec(
                skyPhase = WeatherSkyPhase.Night,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Light,
            ).showsClearNightStars(),
        )
        assertFalse(
            WeatherSceneSpec(
                skyPhase = WeatherSkyPhase.Night,
                atmosphere = WeatherAtmosphere.Fog,
            ).showsClearNightStars(),
        )
    }

    @Test
    fun `clear night stars use irregular size brightness color and placement`() {
        assertEquals(72, clearNightStarSpecs.size)
        assertTrue(clearNightStarSpecs.all { it.xFraction in 0.025f..0.975f })
        assertTrue(clearNightStarSpecs.all { it.yFraction in 0.025f..0.635f })
        assertTrue(clearNightStarSpecs.map { it.xFraction }.distinct().size > 65)
        assertTrue(clearNightStarSpecs.map { it.yFraction }.distinct().size > 65)
        assertTrue(clearNightStarSpecs.minOf { it.radiusDp } < 0.4f)
        assertTrue(clearNightStarSpecs.maxOf { it.radiusDp } > 1.1f)
        assertTrue(clearNightStarSpecs.minOf { it.alpha } < 0.4f)
        assertTrue(clearNightStarSpecs.maxOf { it.alpha } > 0.65f)
        assertEquals(NightStarTone.entries.toSet(), clearNightStarSpecs.map { it.tone }.toSet())
        val sparklingStars = clearNightStarSpecs.filter { it.sparkleStrength > 0f }
        assertTrue(sparklingStars.size in 6..9)
        assertTrue(sparklingStars.all { it.sparkleStrength in 0.62f..0.78f })
        assertEquals(sparklingStars.size, sparklingStars.map { it.sparklePhase }.distinct().size)
    }

    @Test
    fun `only selected bright stars sparkle in short independent windows`() {
        val sparklingStar = clearNightStarSpecs.first { it.sparkleStrength > 0f }
        val stableStar = clearNightStarSpecs.first { it.sparkleStrength == 0f }
        val peakProgress = (1f - sparklingStar.sparklePhase) % 1f

        assertEquals(
            sparklingStar.sparkleStrength,
            nightStarSparklePulse(sparklingStar, peakProgress),
            0.0001f,
        )
        assertEquals(0f, nightStarSparklePulse(sparklingStar, peakProgress - 0.04f), 0f)
        assertTrue(nightStarSparklePulse(sparklingStar, peakProgress + 0.04f) > 0f)
        assertEquals(0f, nightStarSparklePulse(sparklingStar, peakProgress + 0.10f), 0f)
        assertEquals(0f, nightStarSparklePulse(stableStar, peakProgress), 0f)
        assertTrue(
            kotlin.math.abs(nightStarChromaticShift(sparklingStar, peakProgress)) <= 0.12f,
        )
        assertEquals(0f, nightStarChromaticShift(stableStar, peakProgress), 0f)
    }

    @Test
    fun `weather scenes use the expected generated image layers`() {
        assertEquals(
            emptyList<WeatherSceneAsset>(),
            weatherSceneLayers(
                WeatherSceneSpec(
                    cloudCover = WeatherCloudCover.Overcast,
                    atmosphere = WeatherAtmosphere.Neutral,
                ),
            ).map { it.asset },
        )
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
        assertTrue(drizzle.dropCount >= 330)
        assertTrue(light.dropCount >= 500)
        assertTrue(moderate.dropCount >= 850)
        assertTrue(heavy.dropCount >= 1_500)

        assertEquals(3, rainLayerSpecs.size)
        assertEquals(1f, rainLayerSpecs.sumOf { it.countFraction.toDouble() }.toFloat(), 0.001f)
        assertTrue(rainLayerSpecs[1].countFraction >= 0.36f)
        assertTrue(rainLayerSpecs[2].countFraction >= 0.14f)
        assertTrue(rainLayerSpecs.zipWithNext().all { (farther, nearer) ->
            farther.lengthScale < nearer.lengthScale &&
                farther.strokeWidthDp < nearer.strokeWidthDp &&
                farther.alphaScale < nearer.alphaScale &&
                farther.fallCycles < nearer.fallCycles
        })
    }

    @Test
    fun `foreground rain stays sparse while depth and visibility follow intensity`() {
        val drizzle = foregroundRainVisualStyle(WeatherIntensity.Light, drizzle = true)
        val light = foregroundRainVisualStyle(WeatherIntensity.Light, drizzle = false)
        val moderate = foregroundRainVisualStyle(WeatherIntensity.Moderate, drizzle = false)
        val heavy = foregroundRainVisualStyle(WeatherIntensity.Heavy, drizzle = false)

        assertTrue(drizzle.dropCount < light.dropCount)
        assertTrue(light.dropCount < moderate.dropCount)
        assertTrue(moderate.dropCount < heavy.dropCount)
        assertTrue(drizzle.maxLengthDp < light.maxLengthDp)
        assertTrue(light.maxLengthDp < moderate.maxLengthDp)
        assertTrue(moderate.maxLengthDp < heavy.maxLengthDp)
        assertTrue(drizzle.alpha < light.alpha)
        assertTrue(light.alpha < moderate.alpha)
        assertTrue(moderate.alpha < heavy.alpha)
        assertTrue(heavy.dropCount < rainVisualStyle(WeatherIntensity.Heavy, drizzle = false).dropCount / 6)
    }

    @Test
    fun `rain card impacts increase without becoming a continuous edge`() {
        val drizzle = rainCardImpactStyle(WeatherIntensity.Light, drizzle = true)
        val light = rainCardImpactStyle(WeatherIntensity.Light, drizzle = false)
        val moderate = rainCardImpactStyle(WeatherIntensity.Moderate, drizzle = false)
        val heavy = rainCardImpactStyle(WeatherIntensity.Heavy, drizzle = false)

        assertTrue(drizzle.impactCount < light.impactCount)
        assertTrue(light.impactCount < moderate.impactCount)
        assertTrue(moderate.impactCount < heavy.impactCount)
        assertTrue(drizzle.beadCount < light.beadCount)
        assertTrue(light.beadCount < moderate.beadCount)
        assertTrue(moderate.beadCount < heavy.beadCount)
        assertTrue(drizzle.alpha < light.alpha)
        assertTrue(light.alpha < moderate.alpha)
        assertTrue(moderate.alpha < heavy.alpha)
        assertTrue(heavy.impactCount < rainVisualStyle(
            WeatherIntensity.Heavy,
            drizzle = false,
        ).dropCount / 5)
    }

    @Test
    fun `glass impacts become clearly stronger in heavy rain`() {
        val drizzle = rainGlassVisualStyle(WeatherIntensity.Light, drizzle = true)
        val light = rainGlassVisualStyle(WeatherIntensity.Light, drizzle = false)
        val moderate = rainGlassVisualStyle(WeatherIntensity.Moderate, drizzle = false)
        val heavy = rainGlassVisualStyle(WeatherIntensity.Heavy, drizzle = false)

        assertTrue(drizzle.impactCount < light.impactCount)
        assertTrue(light.impactCount < moderate.impactCount)
        assertTrue(moderate.impactCount < heavy.impactCount)
        assertTrue(drizzle.beadCount < light.beadCount)
        assertTrue(light.beadCount < moderate.beadCount)
        assertTrue(moderate.beadCount < heavy.beadCount)
        assertTrue(drizzle.alpha < light.alpha)
        assertTrue(light.alpha < moderate.alpha)
        assertTrue(moderate.alpha < heavy.alpha)
        assertTrue(heavy.beadCount > heavy.impactCount)
    }

    @Test
    fun `glass drops impact cling accelerate and fade in distinct phases`() {
        val impact = glassDropMotion(0.05f)
        val cling = glassDropMotion(0.20f)
        val earlySlide = glassDropMotion(0.38f)
        val mergeStart = glassDropMotion(0.44f)
        val mergeEnd = glassDropMotion(0.58f)
        val lateSlide = glassDropMotion(0.76f)
        val nearBottom = glassDropMotion(0.95f)
        val atBottom = glassDropMotion(0.965f)
        val fade = glassDropMotion(0.985f)

        assertTrue(impact.impactVisibility > 0.8f)
        assertEquals(0f, cling.impactVisibility, 0.001f)
        assertEquals(0f, cling.travelProgress, 0.001f)
        assertTrue(cling.dropVisibility > impact.dropVisibility)
        assertTrue(earlySlide.slideProgress > 0f)
        assertTrue(earlySlide.trailVisibility > 0f)
        assertTrue(mergeEnd.mergeProgress > mergeStart.mergeProgress)
        assertTrue(mergeEnd.mergeScale > mergeStart.mergeScale)
        assertTrue(mergeEnd.travelProgress > mergeStart.travelProgress)
        assertTrue(lateSlide.travelProgress > earlySlide.travelProgress)
        assertTrue(nearBottom.travelProgress > lateSlide.travelProgress)
        assertTrue(nearBottom.travelProgress > 0.9f)
        assertEquals(1f, atBottom.travelProgress, 0.001f)
        assertEquals(1f, atBottom.dropVisibility, 0.001f)
        assertTrue(fade.dropVisibility < lateSlide.dropVisibility)
        assertTrue(fade.trailVisibility < lateSlide.trailVisibility)
        assertEquals(
            glassDropMotion(0.05f),
            glassDropMotion(1.05f),
        )
    }

    @Test
    fun `rain rhythm stays subtle and depth layers remain out of phase`() {
        val samples = List(24) { index ->
            rainRhythmPulse(
                progress = index / 24f,
                pulseMin = 0.92f,
                pulseMax = 1.08f,
            )
        }
        val shiftedSamples = List(24) { index ->
            rainRhythmPulse(
                progress = index / 24f,
                pulseMin = 0.92f,
                pulseMax = 1.08f,
                phaseOffset = 0.31f,
            )
        }

        assertTrue(samples.all { it in 0.92f..1.08f })
        assertTrue(samples.max() - samples.min() > 0.10f)
        assertTrue(samples != shiftedSamples)
        assertTrue(kotlin.math.abs(rainGustSkew(0.25f)) < 0.04f)
    }

    @Test
    fun `card edge wetness forms separated impact clusters`() {
        val strengths = List(13) { cell ->
            rainEdgeClusterStrength((cell + 0.5f) / 13f)
        }

        assertTrue(strengths.count { it == 0f } in 3..6)
        assertTrue(strengths.count { it > 0.7f } >= 4)
        assertTrue(strengths.first() > 0f)
        assertTrue(strengths.last() > 0f)
    }

    @Test
    fun `card rain follows the rounded top edge into both sides`() {
        val width = 100f
        val edgeY = 12f
        val radius = 18f

        assertEquals(edgeY + radius, roundedCardTopEdgeY(0f, width, edgeY, radius), 0.001f)
        assertEquals(edgeY, roundedCardTopEdgeY(radius, width, edgeY, radius), 0.001f)
        assertEquals(edgeY, roundedCardTopEdgeY(width / 2f, width, edgeY, radius), 0.001f)
        assertEquals(
            roundedCardTopEdgeY(5f, width, edgeY, radius),
            roundedCardTopEdgeY(width - 5f, width, edgeY, radius),
            0.001f,
        )

        val leftTop = roundedCardCornerPoint(width, edgeY, radius, left = true, progress = 0f)
        val leftSide = roundedCardCornerPoint(width, edgeY, radius, left = true, progress = 1f)
        val rightTop = roundedCardCornerPoint(width, edgeY, radius, left = false, progress = 0f)
        val rightSide = roundedCardCornerPoint(width, edgeY, radius, left = false, progress = 1f)

        assertEquals(radius, leftTop.x, 0.001f)
        assertEquals(edgeY, leftTop.y, 0.001f)
        assertEquals(0f, leftSide.x, 0.001f)
        assertEquals(edgeY + radius, leftSide.y, 0.001f)
        assertEquals(width - radius, rightTop.x, 0.001f)
        assertEquals(edgeY, rightTop.y, 0.001f)
        assertEquals(width, rightSide.x, 0.001f)
        assertEquals(edgeY + radius, rightSide.y, 0.001f)
    }

    @Test
    fun `most glass drops stay pinned while only a few slide far`() {
        assertEquals(GlassDropBehavior.Pinned, glassDropBehavior(0.10f))
        assertEquals(GlassDropBehavior.Pinned, glassDropBehavior(0.57f))
        assertEquals(GlassDropBehavior.Creeping, glassDropBehavior(0.58f))
        assertEquals(GlassDropBehavior.Creeping, glassDropBehavior(0.85f))
        assertEquals(GlassDropBehavior.Sliding, glassDropBehavior(0.86f))
        assertEquals(GlassDropBehavior.Sliding, glassDropBehavior(1f))
        assertEquals(0, heroGlassDropCount(WeatherIntensity.Light))
        assertEquals(2, heroGlassDropCount(WeatherIntensity.Moderate))
        assertEquals(4, heroGlassDropCount(WeatherIntensity.Heavy))
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
        assertTrue(regular.countScale > 1f)
        assertTrue(regular.alphaScale > 1f)
        assertTrue(regular.pulseMin < 1f)
        assertTrue(regular.pulseMax > 1f)
    }

    @Test
    fun `freezing rain increases rain lines impacts and glass accumulation`() {
        val regularRain = rainVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
        )
        val freezingRain = rainVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
            freezing = true,
        )
        val regularForeground = foregroundRainVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
        )
        val freezingForeground = foregroundRainVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
            freezing = true,
        )
        val regularCard = rainCardImpactStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
        )
        val freezingCard = rainCardImpactStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
            freezing = true,
        )
        val regularGlass = rainGlassVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
        )
        val freezingGlass = rainGlassVisualStyle(
            intensity = WeatherIntensity.Moderate,
            drizzle = false,
            freezing = true,
        )

        assertTrue(freezingRain.dropCount > regularRain.dropCount)
        assertTrue(freezingRain.lengthDp > regularRain.lengthDp)
        assertTrue(freezingRain.alpha > regularRain.alpha)
        assertTrue(freezingRain.veilAlpha > regularRain.veilAlpha)
        assertTrue(freezingForeground.dropCount > regularForeground.dropCount)
        assertTrue(freezingForeground.alpha > regularForeground.alpha)
        assertTrue(freezingCard.impactCount > regularCard.impactCount)
        assertTrue(freezingCard.beadCount > regularCard.beadCount)
        assertTrue(freezingGlass.impactCount > regularGlass.impactCount)
        assertTrue(freezingGlass.beadCount > regularGlass.beadCount)
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
        assertTrue(light.flakeCount >= 295)
        assertTrue(moderate.flakeCount >= 580)
        assertTrue(heavy.flakeCount >= 945)

        assertEquals(3, snowLayerSpecs.size)
        assertEquals(1f, snowLayerSpecs.sumOf { it.countFraction.toDouble() }.toFloat(), 0.001f)
        assertTrue(snowLayerSpecs[1].countFraction >= 0.39f)
        assertTrue(snowLayerSpecs[2].countFraction >= 0.25f)
        assertTrue(snowLayerSpecs.zipWithNext().all { (farther, nearer) ->
            farther.radiusScale < nearer.radiusScale &&
                farther.alphaScale < nearer.alphaScale &&
                farther.fallScale < nearer.fallScale &&
                farther.driftScale < nearer.driftScale
        })
    }

    @Test
    fun `foreground snow and card accumulation scale with snowfall intensity`() {
        val lightForeground = foregroundSnowVisualStyle(WeatherIntensity.Light)
        val moderateForeground = foregroundSnowVisualStyle(WeatherIntensity.Moderate)
        val heavyForeground = foregroundSnowVisualStyle(WeatherIntensity.Heavy)
        val lightCard = snowCardImpactStyle(WeatherIntensity.Light)
        val moderateCard = snowCardImpactStyle(WeatherIntensity.Moderate)
        val heavyCard = snowCardImpactStyle(WeatherIntensity.Heavy)

        assertTrue(lightForeground.flakeCount < moderateForeground.flakeCount)
        assertTrue(moderateForeground.flakeCount < heavyForeground.flakeCount)
        assertTrue(lightForeground.maxRadiusDp < moderateForeground.maxRadiusDp)
        assertTrue(moderateForeground.maxRadiusDp < heavyForeground.maxRadiusDp)
        assertTrue(lightForeground.alpha < moderateForeground.alpha)
        assertTrue(moderateForeground.alpha < heavyForeground.alpha)
        assertTrue(lightForeground.flakeCount >= 34)
        assertTrue(moderateForeground.flakeCount >= 64)
        assertTrue(heavyForeground.flakeCount >= 98)
        assertTrue(lightCard.impactCount < moderateCard.impactCount)
        assertTrue(moderateCard.impactCount < heavyCard.impactCount)
        assertTrue(lightCard.settledFlakeCount < moderateCard.settledFlakeCount)
        assertTrue(moderateCard.settledFlakeCount < heavyCard.settledFlakeCount)
        assertTrue(lightCard.snowCapAlpha < moderateCard.snowCapAlpha)
        assertTrue(moderateCard.snowCapAlpha < heavyCard.snowCapAlpha)
        assertTrue(lightCard.capWidthScale < moderateCard.capWidthScale)
        assertTrue(moderateCard.capWidthScale < heavyCard.capWidthScale)
        assertTrue(lightCard.impactEnergy < moderateCard.impactEnergy)
        assertTrue(moderateCard.impactEnergy < heavyCard.impactEnergy)
        assertTrue(heavyCard.settledFlakeCount >= 119)
    }

    @Test
    fun `snow grains stay smaller while increasing foreground impacts`() {
        val snowForeground = foregroundSnowVisualStyle(WeatherIntensity.Moderate)
        val grainForeground = foregroundSnowVisualStyle(
            intensity = WeatherIntensity.Moderate,
            grains = true,
        )
        val snowCard = snowCardImpactStyle(WeatherIntensity.Moderate)
        val grainCard = snowCardImpactStyle(
            intensity = WeatherIntensity.Moderate,
            grains = true,
        )

        assertTrue(grainForeground.flakeCount > snowForeground.flakeCount)
        assertTrue(grainForeground.maxRadiusDp < snowForeground.maxRadiusDp)
        assertTrue(grainCard.impactCount > snowCard.impactCount)
        assertTrue(grainCard.snowCapAlpha < snowCard.snowCapAlpha)
        assertTrue(grainCard.capWidthScale < snowCard.capWidthScale)
        assertTrue(grainCard.impactEnergy > snowCard.impactEnergy)
    }

    @Test
    fun `snow accumulation forms interrupted clusters across the rounded card edge`() {
        val strengths = List(13) { cell ->
            snowEdgeClusterStrength((cell + 0.5f) / 13f)
        }

        assertTrue(strengths.count { it == 0f } in 2..5)
        assertTrue(strengths.count { it > 0.7f } >= 5)
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
        assertTrue(flurries.pulseMin >= 0.8f)
        assertTrue(heavyFlurries.pulseMin >= 0.9f)
        assertTrue(heavyFlurries.countScale >= 1.97f)
        assertTrue(heavyFlurries.pulseMax >= 1.72f)
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
        assertTrue(snowGrainCount(WeatherIntensity.Heavy) >= 960)
        assertTrue(iceParticleCount(WeatherIntensity.Heavy, isHail = true) >
            iceParticleCount(WeatherIntensity.Heavy, isHail = false))
    }
}
