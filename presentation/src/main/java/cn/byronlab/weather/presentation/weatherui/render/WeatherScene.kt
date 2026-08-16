package cn.byronlab.weather.presentation.weatherui.render

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Paint as AndroidPaint
import android.graphics.Shader

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.byronlab.weather.presentation.R
import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.WeatherVisualType
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
import cn.byronlab.weather.presentation.weatherui.model.visualType
import cn.byronlab.weather.presentation.weatherui.tokens.WeatherVisualStyle
import cn.byronlab.weather.presentation.weatherui.tokens.weatherVisualStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal enum class NightStarTone {
    Neutral,
    Cool,
    Warm,
}

internal data class NightStarSpec(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val alpha: Float,
    val tone: NightStarTone,
    val sparklePhase: Float,
    val sparkleStrength: Float,
)

internal val clearNightStarSpecs: List<NightStarSpec> = List(72) { index ->
    val prominent = index % 17 == 4 || index % 23 == 9
    val sizeNoise = rainNoise(index, 701)
    val brightnessNoise = rainNoise(index, 719)
    val toneNoise = rainNoise(index, 743)
    NightStarSpec(
        xFraction = 0.025f + rainNoise(index, 683) * 0.95f,
        yFraction = 0.025f + rainNoise(index, 691).pow(1.08f) * 0.61f,
        radiusDp = if (prominent) {
            1.02f + sizeNoise * 0.48f
        } else {
            0.28f + sizeNoise.pow(2.2f) * 0.58f
        },
        alpha = if (prominent) {
            0.52f + brightnessNoise * 0.14f
        } else {
            0.22f + brightnessNoise.pow(1.55f) * 0.46f
        },
        tone = when {
            toneNoise < 0.16f -> NightStarTone.Warm
            toneNoise > 0.70f -> NightStarTone.Cool
            else -> NightStarTone.Neutral
        },
        sparklePhase = rainNoise(index, 757),
        sparkleStrength = if (prominent) 0.62f + rainNoise(index, 769) * 0.16f else 0f,
    )
}

@Composable
internal fun WeatherScene(
    scene: WeatherSceneSpec,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = scene,
        modifier = modifier,
        animationSpec = tween(durationMillis = 900),
        label = "weather-scene-transition",
    ) { targetScene ->
        WeatherSceneContent(scene = targetScene)
    }
}

/**
 * A restrained near-camera rain pass. The main scene keeps most drops behind the dashboard while
 * this layer lets a few soft, fast streaks cross translucent panels like real rain on a lens.
 */
@Composable
internal fun WeatherRainForeground(
    scene: WeatherSceneSpec,
    modifier: Modifier = Modifier,
) {
    if (
        scene.precipitation != WeatherPrecipitation.Rain &&
        scene.precipitation != WeatherPrecipitation.Drizzle
    ) {
        return
    }

    val motion = rememberInfiniteTransition(label = "weather-foreground-rain-motion")
    val motionProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.precipitationMotionDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-foreground-rain-fall",
    )
    val glassProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7_200,
                easing = LinearEasing,
            ),
        ),
        label = "weather-glass-rain-cling",
    )
    val rhythmProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4_200,
                easing = LinearEasing,
            ),
        ),
        label = "weather-foreground-rain-rhythm",
    )
    val rainColor = weatherRainColor(scene)
    val rainSprite = remember(rainColor) { createRainStreakSprite(rainColor) }
    val glassMistTexture = remember(rainColor) { createRainGlassMistTexture(rainColor) }

    Canvas(modifier = modifier) {
        drawForegroundRain(
            scene = scene,
            motionProgress = motionProgress,
            rhythmProgress = rhythmProgress,
            rainSprite = rainSprite,
        )
        drawRainGlassImpacts(
            scene = scene,
            impactProgress = motionProgress,
            clingProgress = glassProgress,
            rhythmProgress = rhythmProgress,
            mistTexture = glassMistTexture,
        )
    }
}

@Composable
internal fun WeatherSnowForeground(
    scene: WeatherSceneSpec,
    modifier: Modifier = Modifier,
) {
    if (
        scene.precipitation != WeatherPrecipitation.Snow &&
        scene.precipitation != WeatherPrecipitation.SnowGrains
    ) {
        return
    }

    val motion = rememberInfiniteTransition(label = "weather-foreground-snow-motion")
    val motionProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.precipitationMotionDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-foreground-snow-fall",
    )
    val rhythmProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5_600,
                easing = LinearEasing,
            ),
        ),
        label = "weather-foreground-snow-rhythm",
    )

    Canvas(modifier = modifier) {
        drawForegroundSnow(
            scene = scene,
            motionProgress = motionProgress,
            rhythmProgress = rhythmProgress,
        )
    }
}

@Composable
internal fun WeatherCardRainImpact(
    scene: WeatherSceneSpec,
    edgeInset: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    if (
        scene.precipitation != WeatherPrecipitation.Rain &&
        scene.precipitation != WeatherPrecipitation.Drizzle
    ) {
        return
    }

    val motion = rememberInfiniteTransition(label = "weather-card-rain-impact-motion")
    val impactProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.precipitationMotionDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-card-rain-impact",
    )
    val rhythmProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4_200,
                easing = LinearEasing,
            ),
        ),
        label = "weather-card-rain-rhythm",
    )

    Canvas(modifier = modifier) {
        drawRainCardImpacts(
            scene = scene,
            motionProgress = impactProgress,
            rhythmProgress = rhythmProgress,
            edgeY = edgeInset.toPx(),
            cornerRadius = cornerRadius.toPx(),
        )
    }
}

@Composable
internal fun WeatherCardSnowImpact(
    scene: WeatherSceneSpec,
    edgeInset: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    if (
        scene.precipitation != WeatherPrecipitation.Snow &&
        scene.precipitation != WeatherPrecipitation.SnowGrains
    ) {
        return
    }

    val motion = rememberInfiniteTransition(label = "weather-card-snow-impact-motion")
    val impactProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.precipitationMotionDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-card-snow-impact",
    )
    val rhythmProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5_600,
                easing = LinearEasing,
            ),
        ),
        label = "weather-card-snow-rhythm",
    )

    Canvas(modifier = modifier) {
        drawSnowCardImpacts(
            scene = scene,
            motionProgress = impactProgress,
            rhythmProgress = rhythmProgress,
            edgeY = edgeInset.toPx(),
            cornerRadius = cornerRadius.toPx(),
        )
    }
}

@Composable
private fun WeatherSceneContent(scene: WeatherSceneSpec) {
    val style = weatherVisualStyle(scene)
    val rainColor = weatherRainColor(scene)
    val rainSprite = remember(rainColor) { createRainStreakSprite(rainColor) }
    val motion = rememberInfiniteTransition(label = "weather-scene-motion")
    val cloudMotionProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.cloudMotionDurationMillis(),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "weather-cloud-motion",
    )
    val precipitationMotionProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.precipitationMotionDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-precipitation-motion",
    )
    val rainRhythmProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4_200,
                easing = LinearEasing,
            ),
        ),
        label = "weather-rain-rhythm",
    )
    val starSparkleProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 15_700,
                easing = LinearEasing,
            ),
        ),
        label = "weather-star-sparkle",
    )
    val lightningFlash by motion.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = scene.lightningAnimation(),
        ),
        label = "weather-lightning-flash",
    )
    val lightningStrikePhase by motion.animateFloat(
        initialValue = 0f,
        targetValue = lightningStrikeSpecs.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scene.lightningStrikeCycleDurationMillis(),
                easing = LinearEasing,
            ),
        ),
        label = "weather-lightning-strike-variant",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(style.backgroundColors)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawWeatherBackdropTint(scene, lightningFlash)
            if (scene.showsClearNightStars()) {
                drawClearNightStarField(sparkleProgress = starSparkleProgress)
            }
        }
        WeatherImageLayers(
            scene = scene,
            celestialOnly = false,
            motionProgress = cloudMotionProgress,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPrecipitation(
                scene = scene,
                width = size.width,
                height = size.height,
                motionProgress = precipitationMotionProgress,
                rainRhythmProgress = rainRhythmProgress,
                rainSprite = rainSprite,
            )
            drawWeatherPhotoOverlays(style)
            if (scene.atmosphere == WeatherAtmosphere.Fog) {
                drawFog(
                    width = size.width,
                    height = size.height,
                    motionProgress = cloudMotionProgress,
                    isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
                )
            }
            if (scene.lightningIntensity != WeatherLightningIntensity.None) {
                drawThunderstorm(
                    width = size.width,
                    height = size.height,
                    flash = lightningFlash,
                    strikeIndex = lightningStrikeIndex(lightningStrikePhase),
                )
            }
        }
        WeatherImageLayers(
            scene = scene,
            celestialOnly = true,
            motionProgress = cloudMotionProgress,
        )
    }
}

internal fun WeatherSceneSpec.showsClearNightStars(): Boolean {
    return skyPhase == WeatherSkyPhase.Night &&
        cloudCover == WeatherCloudCover.Clear &&
        precipitation == WeatherPrecipitation.None &&
        atmosphere == WeatherAtmosphere.Clear &&
        lightningIntensity == WeatherLightningIntensity.None
}

internal fun nightStarSparklePulse(
    star: NightStarSpec,
    progress: Float,
): Float {
    if (star.sparkleStrength <= 0f) {
        return 0f
    }
    val phase = ((progress + star.sparklePhase) % 1f + 1f) % 1f
    val signedPhase = if (phase > 0.5f) phase - 1f else phase
    val normalized = if (signedPhase < 0f) {
        1f + signedPhase / 0.028f
    } else {
        1f - signedPhase / 0.072f
    }
    if (normalized <= 0f) {
        return 0f
    }
    val envelope = sin(normalized * PI.toFloat() / 2f).pow(1.7f)
    val atmosphericRipple = 1f -
        0.10f * sin(normalized * PI.toFloat() * 2f).pow(2)
    return envelope * atmosphericRipple * star.sparkleStrength
}

internal fun nightStarChromaticShift(
    star: NightStarSpec,
    progress: Float,
    sparkle: Float = nightStarSparklePulse(star, progress),
): Float {
    if (sparkle <= 0f || star.sparkleStrength <= 0f) {
        return 0f
    }
    val intensity = (sparkle / star.sparkleStrength).coerceIn(0f, 1f)
    val colorPhase = (progress * 7f + star.sparklePhase * 11f) * PI.toFloat() * 2f
    return sin(colorPhase) * intensity.pow(0.8f) * 0.12f
}

private fun DrawScope.drawClearNightStarField(sparkleProgress: Float) {
    clearNightStarSpecs.forEach { star ->
        val center = Offset(
            x = size.width * star.xFraction,
            y = size.height * star.yFraction,
        )
        val bottomFade = if (star.yFraction <= 0.50f) {
            1f
        } else {
            (1f - (star.yFraction - 0.50f) / 0.32f).coerceIn(0.58f, 1f)
        }
        val sparkle = nightStarSparklePulse(star, sparkleProgress)
        val alpha = (star.alpha * bottomFade + sparkle * 0.34f).coerceIn(0f, 1f)
        val radius = star.radiusDp.dp.toPx()
        val baseColor = when (star.tone) {
            NightStarTone.Neutral -> Color(0xFFF7FAFF)
            NightStarTone.Cool -> Color(0xFFD7E7FF)
            NightStarTone.Warm -> Color(0xFFFFE6C5)
        }
        val chromaticShift = nightStarChromaticShift(star, sparkleProgress, sparkle)
        val atmosphericColor = if (chromaticShift >= 0f) {
            Color(0xFFC8DEFF)
        } else {
            Color(0xFFFFDDB7)
        }
        val color = lerp(baseColor, atmosphericColor, kotlin.math.abs(chromaticShift))

        if (star.sparkleStrength > 0f) {
            val glowAlpha = ((0.05f + sparkle * 0.18f) * bottomFade).coerceIn(0f, 0.20f)
            val glowRadius = radius * (3.2f + sparkle * 1.8f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha),
                        color.copy(alpha = glowAlpha * 0.28f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = glowRadius,
                ),
                center = center,
                radius = glowRadius,
            )
        }
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius * (1f + sparkle * 0.15f),
            center = center,
        )
        if (star.sparkleStrength > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = (alpha * 0.72f + sparkle * 0.10f).coerceAtMost(1f)),
                radius = radius * (0.32f + sparkle * 0.04f),
                center = center.copy(x = center.x - radius * 0.12f, y = center.y - radius * 0.12f),
            )
        }
    }
}

private fun WeatherSceneSpec.cloudMotionDurationMillis(): Int {
    return when (windLevel) {
        WeatherWindLevel.Calm -> 28_000
        WeatherWindLevel.Breezy -> 21_000
        WeatherWindLevel.Windy -> 14_000
    }
}

internal fun WeatherSceneSpec.precipitationMotionDurationMillis(): Int {
    return when (precipitation) {
        WeatherPrecipitation.None -> 12_000
        WeatherPrecipitation.Drizzle -> 3_000
        WeatherPrecipitation.Rain -> when {
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Showers &&
                windLevel == WeatherWindLevel.Windy -> 1_000
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Showers -> 1_180
            precipitationIntensity == WeatherIntensity.None -> 12_000
            precipitationIntensity == WeatherIntensity.Light -> 2_500
            precipitationIntensity == WeatherIntensity.Moderate -> 1_900
            else -> 1_350
        }
        WeatherPrecipitation.Snow -> when {
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Flurries -> 1_850
            precipitationIntensity == WeatherIntensity.Heavy -> 2_800
            precipitationIntensity == WeatherIntensity.Moderate &&
                precipitationPattern == WeatherPrecipitationPattern.Flurries -> 2_500
            precipitationIntensity == WeatherIntensity.Moderate -> 3_400
            else -> 4_600
        }
        WeatherPrecipitation.SnowGrains -> 2_100
        WeatherPrecipitation.Sleet -> 2_150
        WeatherPrecipitation.Hail -> 1_550
    }
}

internal data class LightningFlashFrame(
    val timeMillis: Int,
    val intensity: Float,
)

internal data class LightningFlashProfile(
    val durationMillis: Int,
    val frames: List<LightningFlashFrame>,
)

internal fun lightningFlashProfile(intensity: WeatherLightningIntensity): LightningFlashProfile {
    return when (intensity) {
        WeatherLightningIntensity.None -> LightningFlashProfile(
            durationMillis = 8_000,
            frames = listOf(
                LightningFlashFrame(0, 0f),
                LightningFlashFrame(8_000, 0f),
            ),
        )
        WeatherLightningIntensity.Occasional -> LightningFlashProfile(
            durationMillis = 5_800,
            frames = listOf(
                LightningFlashFrame(0, 0f),
                LightningFlashFrame(3_350, 0f),
                LightningFlashFrame(3_410, 0.10f),
                LightningFlashFrame(3_460, 0.90f),
                LightningFlashFrame(3_525, 0.15f),
                LightningFlashFrame(3_585, 0f),
                LightningFlashFrame(3_680, 0.62f),
                LightningFlashFrame(3_760, 0.10f),
                LightningFlashFrame(3_830, 0f),
                LightningFlashFrame(5_800, 0f),
            ),
        )
        WeatherLightningIntensity.Frequent -> LightningFlashProfile(
            durationMillis = 3_600,
            frames = listOf(
                LightningFlashFrame(0, 0f),
                LightningFlashFrame(1_900, 0f),
                LightningFlashFrame(1_960, 0.18f),
                LightningFlashFrame(2_010, 1f),
                LightningFlashFrame(2_070, 0.12f),
                LightningFlashFrame(2_130, 0f),
                LightningFlashFrame(2_220, 0.76f),
                LightningFlashFrame(2_295, 0.08f),
                LightningFlashFrame(2_360, 0f),
                LightningFlashFrame(2_460, 0.36f),
                LightningFlashFrame(2_520, 0.05f),
                LightningFlashFrame(2_580, 0f),
                LightningFlashFrame(3_600, 0f),
            ),
        )
    }
}

private fun WeatherSceneSpec.lightningStrikeCycleDurationMillis(): Int {
    return lightningFlashProfile(lightningIntensity).durationMillis * lightningStrikeSpecs.size
}

internal fun lightningVisibility(flash: Float): Float {
    return ((flash - 0.025f) / 0.975f).coerceIn(0f, 1f)
}

internal data class LightningPathPoint(
    val xFraction: Float,
    val yFraction: Float,
)

internal data class LightningStrikeSpec(
    val originXFraction: Float,
    val originYFraction: Float,
    val mainPath: List<LightningPathPoint>,
    val branchPaths: List<List<LightningPathPoint>>,
    val brightnessScale: Float,
    val glowScale: Float,
)

internal val lightningStrikeSpecs = listOf(
    LightningStrikeSpec(
        originXFraction = 0.76f,
        originYFraction = 0.105f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(-0.016f, 0.035f),
            LightningPathPoint(0.008f, 0.065f),
            LightningPathPoint(-0.028f, 0.102f),
            LightningPathPoint(-0.008f, 0.135f),
            LightningPathPoint(-0.055f, 0.180f),
            LightningPathPoint(-0.036f, 0.222f),
            LightningPathPoint(-0.082f, 0.275f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(-0.028f, 0.102f),
                LightningPathPoint(-0.075f, 0.126f),
                LightningPathPoint(-0.100f, 0.160f),
            ),
            listOf(
                LightningPathPoint(-0.055f, 0.180f),
                LightningPathPoint(-0.015f, 0.206f),
                LightningPathPoint(-0.002f, 0.243f),
            ),
        ),
        brightnessScale = 1f,
        glowScale = 1f,
    ),
    LightningStrikeSpec(
        originXFraction = 0.30f,
        originYFraction = 0.130f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(0.018f, 0.030f),
            LightningPathPoint(-0.004f, 0.055f),
            LightningPathPoint(0.034f, 0.090f),
            LightningPathPoint(0.015f, 0.125f),
            LightningPathPoint(0.062f, 0.160f),
            LightningPathPoint(0.044f, 0.195f),
            LightningPathPoint(0.090f, 0.245f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(0.034f, 0.090f),
                LightningPathPoint(0.082f, 0.105f),
                LightningPathPoint(0.105f, 0.140f),
            ),
            listOf(
                LightningPathPoint(0.062f, 0.160f),
                LightningPathPoint(0.020f, 0.178f),
                LightningPathPoint(-0.002f, 0.210f),
            ),
        ),
        brightnessScale = 0.90f,
        glowScale = 0.92f,
    ),
    LightningStrikeSpec(
        originXFraction = 0.61f,
        originYFraction = 0.090f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(-0.028f, 0.030f),
            LightningPathPoint(-0.010f, 0.060f),
            LightningPathPoint(-0.048f, 0.095f),
            LightningPathPoint(-0.025f, 0.132f),
            LightningPathPoint(-0.073f, 0.172f),
            LightningPathPoint(-0.055f, 0.215f),
            LightningPathPoint(-0.110f, 0.310f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(-0.010f, 0.060f),
                LightningPathPoint(0.038f, 0.085f),
                LightningPathPoint(0.060f, 0.120f),
            ),
            listOf(
                LightningPathPoint(-0.073f, 0.172f),
                LightningPathPoint(-0.126f, 0.194f),
                LightningPathPoint(-0.151f, 0.238f),
            ),
            listOf(
                LightningPathPoint(-0.055f, 0.215f),
                LightningPathPoint(-0.012f, 0.244f),
                LightningPathPoint(0.006f, 0.280f),
            ),
        ),
        brightnessScale = 1.06f,
        glowScale = 1.08f,
    ),
    LightningStrikeSpec(
        originXFraction = 0.86f,
        originYFraction = 0.155f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(-0.035f, 0.026f),
            LightningPathPoint(-0.020f, 0.054f),
            LightningPathPoint(-0.072f, 0.080f),
            LightningPathPoint(-0.047f, 0.112f),
            LightningPathPoint(-0.100f, 0.150f),
            LightningPathPoint(-0.078f, 0.190f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(-0.020f, 0.054f),
                LightningPathPoint(0.020f, 0.077f),
                LightningPathPoint(0.034f, 0.108f),
            ),
            listOf(
                LightningPathPoint(-0.072f, 0.080f),
                LightningPathPoint(-0.116f, 0.094f),
                LightningPathPoint(-0.140f, 0.128f),
            ),
        ),
        brightnessScale = 0.82f,
        glowScale = 0.84f,
    ),
    LightningStrikeSpec(
        originXFraction = 0.49f,
        originYFraction = 0.110f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(0.025f, 0.032f),
            LightningPathPoint(0.006f, 0.066f),
            LightningPathPoint(0.052f, 0.100f),
            LightningPathPoint(0.032f, 0.142f),
            LightningPathPoint(0.081f, 0.181f),
            LightningPathPoint(0.058f, 0.223f),
            LightningPathPoint(0.105f, 0.282f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(0.006f, 0.066f),
                LightningPathPoint(-0.040f, 0.088f),
                LightningPathPoint(-0.061f, 0.124f),
            ),
            listOf(
                LightningPathPoint(0.052f, 0.100f),
                LightningPathPoint(0.095f, 0.123f),
                LightningPathPoint(0.115f, 0.160f),
            ),
            listOf(
                LightningPathPoint(0.081f, 0.181f),
                LightningPathPoint(0.038f, 0.204f),
                LightningPathPoint(0.018f, 0.242f),
            ),
        ),
        brightnessScale = 0.96f,
        glowScale = 1.02f,
    ),
    LightningStrikeSpec(
        originXFraction = 0.70f,
        originYFraction = 0.135f,
        mainPath = listOf(
            LightningPathPoint(0f, 0f),
            LightningPathPoint(0.012f, 0.028f),
            LightningPathPoint(-0.020f, 0.058f),
            LightningPathPoint(0.006f, 0.091f),
            LightningPathPoint(-0.036f, 0.124f),
            LightningPathPoint(-0.018f, 0.165f),
            LightningPathPoint(-0.062f, 0.218f),
        ),
        branchPaths = listOf(
            listOf(
                LightningPathPoint(0f, 0f),
                LightningPathPoint(0.050f, 0.042f),
                LightningPathPoint(0.077f, 0.095f),
                LightningPathPoint(0.056f, 0.154f),
                LightningPathPoint(0.093f, 0.208f),
            ),
            listOf(
                LightningPathPoint(0.006f, 0.091f),
                LightningPathPoint(-0.082f, 0.112f),
                LightningPathPoint(-0.111f, 0.154f),
            ),
        ),
        brightnessScale = 0.92f,
        glowScale = 0.96f,
    ),
)

internal fun lightningStrikeIndex(phase: Float): Int {
    return floor(phase).toInt().coerceIn(0, lightningStrikeSpecs.lastIndex)
}

private fun WeatherSceneSpec.lightningAnimation() = keyframes {
    val profile = lightningFlashProfile(lightningIntensity)
    durationMillis = profile.durationMillis
    profile.frames.forEach { frame ->
        frame.intensity at frame.timeMillis
    }
}

internal enum class WeatherSceneAsset {
    Sun,
    Moon,
    ThinCloud,
    BrightCloud,
    OvercastCloud,
    RainCloud,
    SnowCloud,
    StormCloud,
}

internal enum class WeatherSceneAlignment {
    TopStart,
    TopCenter,
    TopEnd,
}

internal data class WeatherSceneLayer(
    val asset: WeatherSceneAsset,
    val alignment: WeatherSceneAlignment,
    val offsetX: Int,
    val offsetY: Int,
    val size: Int,
    val alpha: Float,
    val mirrored: Boolean = false,
)

internal fun weatherSceneLayers(scene: WeatherSceneSpec): List<WeatherSceneLayer> {
    if (scene.atmosphere == WeatherAtmosphere.Fog) {
        return emptyList()
    }
    if (scene.atmosphere == WeatherAtmosphere.Neutral && scene.precipitation == WeatherPrecipitation.None) {
        return emptyList()
    }
    if (scene.precipitation != WeatherPrecipitation.None) {
        return precipitationSceneLayers(scene)
    }
    val isDaytime = scene.skyPhase == WeatherSkyPhase.Day
    return when (scene.cloudCover) {
        WeatherCloudCover.Clear -> listOf(
            WeatherSceneLayer(
                asset = if (isDaytime) WeatherSceneAsset.Sun else WeatherSceneAsset.Moon,
                alignment = WeatherSceneAlignment.TopEnd,
                offsetX = 12,
                offsetY = 68,
                size = 188,
                alpha = if (isDaytime) 0.90f else 0.86f,
            ),
            WeatherSceneLayer(WeatherSceneAsset.ThinCloud, WeatherSceneAlignment.TopStart, -160, 135, 390, 0.12f),
        )
        WeatherCloudCover.MostlyClear -> listOf(
            WeatherSceneLayer(
                asset = if (isDaytime) WeatherSceneAsset.Sun else WeatherSceneAsset.Moon,
                alignment = WeatherSceneAlignment.TopEnd,
                offsetX = 12,
                offsetY = 68,
                size = 184,
                alpha = if (isDaytime) 0.87f else 0.84f,
            ),
            WeatherSceneLayer(WeatherSceneAsset.ThinCloud, WeatherSceneAlignment.TopStart, -145, 140, 410, 0.24f),
        )
        WeatherCloudCover.PartlyCloudy -> listOf(
            WeatherSceneLayer(
                asset = if (isDaytime) WeatherSceneAsset.Sun else WeatherSceneAsset.Moon,
                alignment = WeatherSceneAlignment.TopEnd,
                offsetX = 12,
                offsetY = 68,
                size = 180,
                alpha = if (isDaytime) 0.78f else 0.78f,
            ),
            WeatherSceneLayer(WeatherSceneAsset.ThinCloud, WeatherSceneAlignment.TopStart, -150, 145, 350, 0.20f),
            WeatherSceneLayer(WeatherSceneAsset.BrightCloud, WeatherSceneAlignment.TopEnd, 92, 112, 315, 0.34f, mirrored = true),
        )
        WeatherCloudCover.Cloudy -> listOf(
            WeatherSceneLayer(WeatherSceneAsset.BrightCloud, WeatherSceneAlignment.TopStart, -125, 25, 430, 0.56f),
            WeatherSceneLayer(WeatherSceneAsset.ThinCloud, WeatherSceneAlignment.TopEnd, 115, 135, 430, 0.38f, mirrored = true),
            WeatherSceneLayer(WeatherSceneAsset.BrightCloud, WeatherSceneAlignment.TopCenter, 0, -25, 320, 0.22f),
        )
        WeatherCloudCover.Overcast -> listOf(
            WeatherSceneLayer(WeatherSceneAsset.OvercastCloud, WeatherSceneAlignment.TopStart, -105, 8, 510, 0.70f),
            WeatherSceneLayer(WeatherSceneAsset.OvercastCloud, WeatherSceneAlignment.TopEnd, 125, 105, 470, 0.52f, mirrored = true),
            WeatherSceneLayer(WeatherSceneAsset.ThinCloud, WeatherSceneAlignment.TopCenter, 0, -70, 460, 0.18f),
        )
    }
}

private fun precipitationSceneLayers(scene: WeatherSceneSpec): List<WeatherSceneLayer> {
    val cloudAsset = when {
        scene.lightningIntensity != WeatherLightningIntensity.None -> WeatherSceneAsset.StormCloud
        scene.precipitation == WeatherPrecipitation.Snow ||
            scene.precipitation == WeatherPrecipitation.SnowGrains -> WeatherSceneAsset.SnowCloud
        scene.precipitation == WeatherPrecipitation.Drizzle -> WeatherSceneAsset.OvercastCloud
        else -> WeatherSceneAsset.RainCloud
    }
    val secondaryAsset = when {
        scene.lightningIntensity != WeatherLightningIntensity.None -> WeatherSceneAsset.OvercastCloud
        scene.precipitationPattern == WeatherPrecipitationPattern.Showers -> WeatherSceneAsset.BrightCloud
        else -> cloudAsset
    }
    val (basePrimaryAlpha, baseSecondaryAlpha) = when (scene.precipitationIntensity) {
        WeatherIntensity.None -> 0.42f to 0.24f
        WeatherIntensity.Light -> 0.52f to 0.28f
        WeatherIntensity.Moderate -> 0.72f to 0.46f
        WeatherIntensity.Heavy -> 0.82f to 0.58f
    }
    val rainCloudSoftening = when {
        scene.visualType == WeatherVisualType.Rain &&
            scene.precipitationIntensity == WeatherIntensity.Heavy -> 0.44f
        scene.visualType == WeatherVisualType.Rain -> 0.52f
        else -> 1f
    }
    val primaryAlpha = basePrimaryAlpha * rainCloudSoftening
    val secondaryAlpha = baseSecondaryAlpha * rainCloudSoftening
    val primarySize = when {
        scene.lightningIntensity != WeatherLightningIntensity.None -> 540
        scene.precipitationIntensity == WeatherIntensity.Light -> 440
        else -> 490
    }
    val secondarySize = if (scene.precipitationIntensity == WeatherIntensity.Light) 410 else 500
    return buildList {
        add(
            WeatherSceneLayer(
                asset = cloudAsset,
                alignment = WeatherSceneAlignment.TopStart,
                offsetX = -95,
                offsetY = if (scene.lightningIntensity != WeatherLightningIntensity.None) -38 else 20,
                size = primarySize,
                alpha = primaryAlpha,
            ),
        )
        add(
            WeatherSceneLayer(
                asset = secondaryAsset,
                alignment = WeatherSceneAlignment.TopEnd,
                offsetX = 140,
                offsetY = 120,
                size = secondarySize,
                alpha = secondaryAlpha,
                mirrored = true,
            ),
        )
        if (
            scene.precipitationIntensity == WeatherIntensity.Heavy ||
            scene.lightningIntensity != WeatherLightningIntensity.None
        ) {
            add(
                WeatherSceneLayer(
                    asset = if (scene.lightningIntensity != WeatherLightningIntensity.None) {
                        WeatherSceneAsset.StormCloud
                    } else {
                        WeatherSceneAsset.OvercastCloud
                    },
                    alignment = WeatherSceneAlignment.TopCenter,
                    offsetX = 0,
                    offsetY = -55,
                    size = 390,
                    alpha = if (scene.visualType == WeatherVisualType.Rain) 0.16f else 0.34f,
                ),
            )
        }
    }
}

@Composable
private fun WeatherImageLayers(
    scene: WeatherSceneSpec,
    celestialOnly: Boolean,
    motionProgress: Float,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        weatherSceneLayers(scene)
            .filter { layer ->
                val isCelestial = layer.asset == WeatherSceneAsset.Sun ||
                    layer.asset == WeatherSceneAsset.Moon
                isCelestial == celestialOnly
            }
            .forEach { layer ->
                val isCelestial = layer.asset == WeatherSceneAsset.Sun ||
                    layer.asset == WeatherSceneAsset.Moon
                val layerModifier = Modifier
                    .align(layer.alignment.toComposeAlignment())
                    .offset(x = layer.offsetX.dp, y = layer.offsetY.dp)
                    .size(layer.size.dp)

                if (layer.asset == WeatherSceneAsset.Sun || layer.asset == WeatherSceneAsset.Moon) {
                    Canvas(modifier = layerModifier) {
                        val glowColor = if (layer.asset == WeatherSceneAsset.Sun) {
                            Color(0xFFFFE19A)
                        } else {
                            Color(0xFFDCEBFF)
                        }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = 0.18f),
                                    glowColor.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = size.minDimension * 0.36f,
                            ),
                            radius = size.minDimension * 0.36f,
                            center = center,
                        )
                    }
                }
                Image(
                    painter = painterResource(weatherSceneResource(layer.asset)),
                    contentDescription = null,
                    modifier = layerModifier
                        .then(
                            if (!isCelestial && scene.visualType == WeatherVisualType.Rain) {
                                Modifier.blur(
                                    if (scene.precipitationIntensity == WeatherIntensity.Heavy) {
                                        16.dp
                                    } else {
                                        12.dp
                                    },
                                )
                            } else {
                                Modifier
                            },
                        )
                        .alpha(layer.alpha)
                        .graphicsLayer {
                            scaleX = if (layer.mirrored) -1f else 1f
                            if (!isCelestial) {
                                val windTravel = when (scene.windLevel) {
                                    WeatherWindLevel.Calm -> 5f
                                    WeatherWindLevel.Breezy -> 10f
                                    WeatherWindLevel.Windy -> 18f
                                }
                                val direction = if (layer.mirrored) -1f else 1f
                                translationX = (windTravel * direction * (motionProgress * 2f - 1f)).dp.toPx()
                            }
                        },
                    contentScale = ContentScale.Fit,
                )
            }
    }
}

private fun weatherSceneResource(asset: WeatherSceneAsset): Int {
    return when (asset) {
        WeatherSceneAsset.Sun -> R.drawable.weather_sun_v4
        WeatherSceneAsset.Moon -> R.drawable.weather_moon
        WeatherSceneAsset.ThinCloud -> R.drawable.weather_cloud_thin
        WeatherSceneAsset.BrightCloud -> R.drawable.weather_cloud_bright
        WeatherSceneAsset.OvercastCloud -> R.drawable.weather_cloud_overcast
        WeatherSceneAsset.RainCloud -> R.drawable.weather_cloud_rain
        WeatherSceneAsset.SnowCloud -> R.drawable.weather_cloud_snow
        WeatherSceneAsset.StormCloud -> R.drawable.weather_cloud_storm
    }
}

private fun WeatherSceneAlignment.toComposeAlignment(): Alignment {
    return when (this) {
        WeatherSceneAlignment.TopStart -> Alignment.TopStart
        WeatherSceneAlignment.TopCenter -> Alignment.TopCenter
        WeatherSceneAlignment.TopEnd -> Alignment.TopEnd
    }
}

private fun DrawScope.drawWeatherBackdropTint(
    scene: WeatherSceneSpec,
    lightningFlash: Float,
) {
    if (scene.atmosphere == WeatherAtmosphere.Fog) {
        val isDaytime = scene.skyPhase == WeatherSkyPhase.Day
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDaytime) {
                    listOf(
                        Color(0xFF5F7484).copy(alpha = 0.08f),
                        Color(0xFFD5DDE0).copy(alpha = 0.17f),
                        Color(0xFFE7EBEC).copy(alpha = 0.22f),
                    )
                } else {
                    listOf(
                        Color(0xFF172734).copy(alpha = 0.12f),
                        Color(0xFF81929C).copy(alpha = 0.13f),
                        Color(0xFFB6C1C6).copy(alpha = 0.16f),
                    )
                },
            ),
            size = size,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDaytime) 0.09f else 0.035f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.68f, size.height * 0.20f),
                radius = size.width * 0.72f,
            ),
            radius = size.width * 0.72f,
            center = Offset(size.width * 0.68f, size.height * 0.20f),
        )
        return
    }
    if (scene.lightningIntensity != WeatherLightningIntensity.None) {
        drawRect(Color(0xFF071426).copy(alpha = 0.48f), size = size)
        val visibility = lightningVisibility(lightningFlash)
        if (visibility > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE7F1FF).copy(alpha = visibility * 0.065f),
                        Color(0xFFD5E8FF).copy(alpha = visibility * 0.035f),
                        Color.Transparent,
                    ),
                    endY = size.height * 0.58f,
                ),
                size = size,
            )
        }
        return
    }
    if (scene.freezing) {
        drawRect(Color(0xFFB7E1F7).copy(alpha = 0.12f), size = size)
    }
    when (scene.visualType) {
        WeatherVisualType.Clear -> Unit
        WeatherVisualType.PartlyCloudy -> drawRect(Color(0xFF5C7D9D).copy(alpha = 0.05f), size = size)
        WeatherVisualType.Cloudy -> drawRect(Color(0xFF4E6788).copy(alpha = 0.16f), size = size)
        WeatherVisualType.Overcast -> drawRect(Color(0xFF3F5268).copy(alpha = 0.28f), size = size)
        WeatherVisualType.Rain -> {
            val alpha = when (scene.precipitationIntensity) {
                WeatherIntensity.None -> 0.24f
                WeatherIntensity.Light -> 0.38f
                WeatherIntensity.Moderate -> 0.50f
                WeatherIntensity.Heavy -> 0.64f
            }
            val showerAdjustment = if (scene.precipitationPattern == WeatherPrecipitationPattern.Showers) {
                -0.04f
            } else {
                0f
            }
            drawRect(Color(0xFF081625).copy(alpha = (alpha + showerAdjustment).coerceAtLeast(0f)), size = size)
        }
        WeatherVisualType.Snow -> {
            val alpha = when (scene.precipitationIntensity) {
                WeatherIntensity.None -> 0.12f
                WeatherIntensity.Light -> 0.14f
                WeatherIntensity.Moderate -> 0.18f
                WeatherIntensity.Heavy -> 0.22f
            }
            drawRect(Color.White.copy(alpha = alpha), size = size)
        }
    }
}

internal fun DrawScope.drawWeatherThumbnailScene(
    scene: WeatherSceneSpec,
    style: WeatherVisualStyle,
) {
    val width = size.width
    val height = size.height

    if (scene.atmosphere == WeatherAtmosphere.Fog) {
        drawRect(Color(0xFFB4C0C9).copy(alpha = 0.34f), size = size)
        drawWeatherPhotoOverlays(style)
        drawFog(
            width = width,
            height = height,
            motionProgress = 0.35f,
            isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
        )
        return
    }

    when (scene.visualType) {
        WeatherVisualType.Clear -> {
            drawCelestial(scene, style, Offset(width * 0.72f, height * 0.22f), width * 0.12f)
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = width * 0.42f,
                center = Offset(width * 0.74f, height * 0.24f),
            )
        }
        WeatherVisualType.PartlyCloudy -> {
            drawCelestial(scene, style, Offset(width * 0.76f, height * 0.20f), width * 0.10f)
            drawRect(Color(0xFF5C7D9D).copy(alpha = 0.05f), size = size)
            drawCloud(Offset(width * 0.18f, height * 0.27f), 0.88f, Color.White.copy(alpha = 0.26f))
            if (scene.cloudCover == WeatherCloudCover.PartlyCloudy) {
                drawCloud(Offset(width * 0.78f, height * 0.32f), 0.82f, Color.White.copy(alpha = 0.24f))
            }
        }
        WeatherVisualType.Cloudy -> {
            drawRect(Color(0xFF4E6788).copy(alpha = 0.16f), size = size)
            drawCloud(Offset(width * 0.16f, height * 0.25f), 0.92f, Color.White.copy(alpha = 0.32f))
            drawCloud(Offset(width * 0.72f, height * 0.32f), 1.18f, Color.White.copy(alpha = 0.26f))
            drawCloud(Offset(width * 0.45f, height * 0.17f), 0.72f, Color.White.copy(alpha = 0.20f))
        }
        WeatherVisualType.Overcast -> {
            drawRect(Color(0xFF3F5268).copy(alpha = 0.28f), size = size)
            if (scene.atmosphere != WeatherAtmosphere.Neutral) {
                drawCloud(
                    Offset(width * 0.16f, height * 0.22f),
                    1.24f,
                    Color.White.copy(alpha = 0.46f),
                )
                drawCloud(
                    Offset(width * 0.74f, height * 0.29f),
                    1.42f,
                    Color.White.copy(alpha = 0.38f),
                )
                drawCloud(
                    Offset(width * 0.45f, height * 0.15f),
                    1.02f,
                    Color.White.copy(alpha = 0.30f),
                )
            }
        }
        WeatherVisualType.Rain -> {
            val isThunderstorm = scene.lightningIntensity != WeatherLightningIntensity.None
            drawRect(
                Color(0xFF132C47).copy(alpha = if (isThunderstorm) 0.52f else 0.36f),
                size = size,
            )
            drawCloud(Offset(width * 0.18f, height * 0.24f), 1.18f, Color.White.copy(alpha = 0.32f))
            drawCloud(Offset(width * 0.70f, height * 0.28f), 1.5f, Color.White.copy(alpha = 0.24f))
            drawPrecipitation(scene, width, height, motionProgress = 0.38f)
            if (isThunderstorm) {
                drawThunderstorm(width, height, flash = 0.62f)
            }
        }
        WeatherVisualType.Snow -> {
            drawRect(Color.White.copy(alpha = 0.16f), size = size)
            drawCloud(Offset(width * 0.22f, height * 0.25f), 1.05f, Color.White.copy(alpha = 0.48f))
            drawCloud(Offset(width * 0.70f, height * 0.32f), 1.36f, Color.White.copy(alpha = 0.36f))
            drawPrecipitation(scene, width, height, motionProgress = 0.42f)
        }
    }

    drawWeatherPhotoOverlays(style)
}

private fun DrawScope.drawCelestial(
    scene: WeatherSceneSpec,
    style: WeatherVisualStyle,
    center: Offset,
    radius: Float,
) {
    if (scene.skyPhase == WeatherSkyPhase.Day) {
        drawSun(style, center, radius)
    } else {
        drawCircle(style.sunGlow.copy(alpha = 0.16f), radius = radius * 2.8f, center = center)
        drawCircle(Color(0xFFE8F2FF), radius = radius, center = center)
        drawCircle(
            color = style.backgroundColors.first().copy(alpha = 0.78f),
            radius = radius * 0.82f,
            center = center.copy(x = center.x + radius * 0.46f, y = center.y - radius * 0.18f),
        )
    }
}

private fun DrawScope.drawWeatherPhotoOverlays(style: WeatherVisualStyle) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(style.photoOverlayTop, Color.Transparent, style.photoOverlayBottom),
        ),
        size = size,
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(style.photoOverlaySide, Color.Transparent),
        ),
        size = size,
    )
}

private fun DrawScope.drawSun(
    style: WeatherVisualStyle,
    center: Offset,
    radius: Float,
) {
    drawCircle(style.sunGlow.copy(alpha = 0.18f), radius = radius * 3.8f, center = center)
    drawCircle(style.sunGlow.copy(alpha = 0.30f), radius = radius * 2.1f, center = center)
    drawCircle(style.sunCore, radius = radius, center = center)
}

private fun DrawScope.drawCloud(
    center: Offset,
    scale: Float,
    color: Color,
) {
    val width = 150.dp.toPx() * scale
    val height = 58.dp.toPx() * scale
    drawOval(
        color = color,
        topLeft = Offset(center.x - width / 2f, center.y - height * 0.14f),
        size = Size(width, height),
    )
    drawCircle(color = color, radius = 32.dp.toPx() * scale, center = center.copy(x = center.x - 42.dp.toPx() * scale))
    drawCircle(color = color, radius = 44.dp.toPx() * scale, center = center.copy(y = center.y - 18.dp.toPx() * scale))
    drawCircle(color = color, radius = 28.dp.toPx() * scale, center = center.copy(x = center.x + 48.dp.toPx() * scale))
}

internal data class RainVisualStyle(
    val dropCount: Int,
    val lengthDp: Float,
    val alpha: Float,
    val veilAlpha: Float,
)

internal data class RainLayerSpec(
    val countFraction: Float,
    val lengthScale: Float,
    val strokeWidthDp: Float,
    val alphaScale: Float,
    val fallCycles: Int,
)

internal data class RainPatternStyle(
    val countScale: Float,
    val lengthScale: Float,
    val alphaScale: Float,
    val veilScale: Float,
    val pulseMin: Float,
    val pulseMax: Float,
)

internal data class ForegroundRainVisualStyle(
    val dropCount: Int,
    val minLengthDp: Float,
    val maxLengthDp: Float,
    val alpha: Float,
)

internal data class RainCardImpactStyle(
    val impactCount: Int,
    val beadCount: Int,
    val alpha: Float,
)

internal data class RainGlassVisualStyle(
    val impactCount: Int,
    val beadCount: Int,
    val alpha: Float,
)

internal data class GlassDropMotion(
    val impactVisibility: Float,
    val dropVisibility: Float,
    val slideProgress: Float,
    val travelProgress: Float,
    val trailVisibility: Float,
    val mergeProgress: Float,
    val mergeScale: Float,
)

internal enum class GlassDropBehavior {
    Pinned,
    Creeping,
    Sliding,
}

internal fun glassDropBehavior(noise: Float): GlassDropBehavior {
    return when {
        noise < 0.58f -> GlassDropBehavior.Pinned
        noise < 0.86f -> GlassDropBehavior.Creeping
        else -> GlassDropBehavior.Sliding
    }
}

internal fun glassDropMotion(progress: Float): GlassDropMotion {
    val phase = ((progress % 1f) + 1f) % 1f
    val impactEnd = 0.10f
    val slideStart = 0.28f
    val mergeStart = 0.43f
    val mergeEnd = 0.58f
    val releaseStart = 0.54f
    val releaseEnd = 0.965f
    val fadeStart = releaseEnd
    val impactProgress = (phase / impactEnd).coerceIn(0f, 1f)
    val slideProgress = ((phase - slideStart) / (releaseEnd - slideStart)).coerceIn(0f, 1f)
    val rawMergeProgress = ((phase - mergeStart) / (mergeEnd - mergeStart)).coerceIn(0f, 1f)
    val mergeProgress = rawMergeProgress * rawMergeProgress * (3f - 2f * rawMergeProgress)
    val travelProgress = when {
        phase < slideStart -> 0f
        phase < mergeStart -> {
            val creepProgress = (phase - slideStart) / (mergeStart - slideStart)
            0.12f * creepProgress.pow(0.76f)
        }
        phase < releaseStart -> 0.12f
        phase < releaseEnd -> {
            val releaseProgress = (phase - releaseStart) / (releaseEnd - releaseStart)
            0.12f + 0.88f * releaseProgress.pow(2.35f)
        }
        else -> 1f
    }
    val fade = if (phase < fadeStart) {
        1f
    } else {
        (1f - (phase - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
    }
    val appearance = (phase / impactEnd).coerceIn(0f, 1f)
    val impactVisibility = if (phase < impactEnd) {
        sin(impactProgress * PI.toFloat()).coerceAtLeast(0f).pow(0.72f)
    } else {
        0f
    }
    val trailAppearance = sin(slideProgress * PI.toFloat() / 2f).coerceAtLeast(0f)
    return GlassDropMotion(
        impactVisibility = impactVisibility,
        dropVisibility = appearance * fade,
        slideProgress = slideProgress,
        travelProgress = travelProgress,
        trailVisibility = trailAppearance * fade * if (travelProgress > 0f) 1f else 0f,
        mergeProgress = mergeProgress,
        mergeScale = 1f + mergeProgress * 0.23f,
    )
}

internal val rainLayerSpecs = listOf(
    RainLayerSpec(0.50f, 0.32f, 0.24f, 0.48f, 1),
    RainLayerSpec(0.36f, 0.62f, 0.46f, 0.82f, 2),
    RainLayerSpec(0.14f, 1.00f, 0.82f, 1.00f, 3),
)

internal fun foregroundRainVisualStyle(
    intensity: WeatherIntensity,
    drizzle: Boolean,
    freezing: Boolean = false,
): ForegroundRainVisualStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> ForegroundRainVisualStyle(0, 0f, 0f, 0f)
        WeatherIntensity.Light -> if (drizzle) {
            ForegroundRainVisualStyle(22, 6f, 13f, 0.32f)
        } else {
            ForegroundRainVisualStyle(34, 10f, 21f, 0.42f)
        }
        WeatherIntensity.Moderate -> ForegroundRainVisualStyle(58, 15f, 34f, 0.54f)
        WeatherIntensity.Heavy -> ForegroundRainVisualStyle(96, 17f, 42f, 0.66f)
    }
    return if (freezing) {
        base.copy(
            dropCount = (base.dropCount * 1.18f).roundToInt(),
            alpha = (base.alpha * 1.08f).coerceAtMost(1f),
        )
    } else {
        base
    }
}

internal fun rainCardImpactStyle(
    intensity: WeatherIntensity,
    drizzle: Boolean,
    freezing: Boolean = false,
): RainCardImpactStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> RainCardImpactStyle(0, 0, 0f)
        WeatherIntensity.Light -> if (drizzle) {
            RainCardImpactStyle(18, 14, 0.42f)
        } else {
            RainCardImpactStyle(34, 22, 0.54f)
        }
        WeatherIntensity.Moderate -> RainCardImpactStyle(76, 30, 0.74f)
        WeatherIntensity.Heavy -> RainCardImpactStyle(128, 42, 0.88f)
    }
    return if (freezing) {
        base.copy(
            impactCount = (base.impactCount * 1.15f).roundToInt(),
            beadCount = (base.beadCount * 1.15f).roundToInt(),
            alpha = (base.alpha * 1.06f).coerceAtMost(1f),
        )
    } else {
        base
    }
}

internal fun rainGlassVisualStyle(
    intensity: WeatherIntensity,
    drizzle: Boolean,
    freezing: Boolean = false,
): RainGlassVisualStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> RainGlassVisualStyle(0, 0, 0f)
        WeatherIntensity.Light -> if (drizzle) {
            RainGlassVisualStyle(10, 14, 0.36f)
        } else {
            RainGlassVisualStyle(16, 22, 0.46f)
        }
        WeatherIntensity.Moderate -> RainGlassVisualStyle(26, 36, 0.62f)
        WeatherIntensity.Heavy -> RainGlassVisualStyle(44, 68, 0.78f)
    }
    return if (freezing) {
        base.copy(
            impactCount = (base.impactCount * 1.15f).roundToInt(),
            beadCount = (base.beadCount * 1.15f).roundToInt(),
            alpha = (base.alpha * 1.06f).coerceAtMost(1f),
        )
    } else {
        base
    }
}

internal fun heroGlassDropCount(intensity: WeatherIntensity): Int {
    return when (intensity) {
        WeatherIntensity.Heavy -> 4
        WeatherIntensity.Moderate -> 2
        else -> 0
    }
}

internal fun rainVisualStyle(
    intensity: WeatherIntensity,
    drizzle: Boolean,
    freezing: Boolean = false,
): RainVisualStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> RainVisualStyle(
            dropCount = 0,
            lengthDp = 0f,
            alpha = 0f,
            veilAlpha = 0f,
        )
        WeatherIntensity.Light -> if (drizzle) {
            RainVisualStyle(
                dropCount = 330,
                lengthDp = 8.5f,
                alpha = 0.46f,
                veilAlpha = 0.016f,
            )
        } else {
            RainVisualStyle(
                dropCount = 500,
                lengthDp = 13f,
                alpha = 0.56f,
                veilAlpha = 0.030f,
            )
        }
        WeatherIntensity.Moderate -> RainVisualStyle(
            dropCount = 850,
            lengthDp = 19f,
            alpha = 0.68f,
            veilAlpha = 0.060f,
        )
        WeatherIntensity.Heavy -> RainVisualStyle(
            dropCount = 1_500,
            lengthDp = 27f,
            alpha = 0.84f,
            veilAlpha = 0.18f,
        )
    }
    return if (freezing) {
        base.copy(
            dropCount = (base.dropCount * if (drizzle) 1.12f else 1.18f).roundToInt(),
            lengthDp = base.lengthDp * 1.06f,
            alpha = (base.alpha * 1.08f).coerceAtMost(1f),
            veilAlpha = (base.veilAlpha * 1.14f).coerceAtMost(1f),
        )
    } else {
        base
    }
}

private fun weatherRainColor(scene: WeatherSceneSpec): Color {
    return when {
        scene.freezing -> Color(0xFFEAF9FF)
        scene.skyPhase == WeatherSkyPhase.Day -> Color(0xFFDCEAF2)
        else -> Color(0xFFB8D1DF)
    }
}

/**
 * Builds a tiny software-rendered rain texture once, then the GPU can reuse it for every particle.
 * The soft halo, tapered tail and short bright core mimic the exposure blur visible in real rain;
 * scaling plain Canvas lines cannot produce the same optical falloff.
 */
private fun createRainStreakSprite(color: Color): ImageBitmap {
    val width = 40
    val height = 224
    val centerX = width / 2f
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    fun drawPass(
        strokeWidth: Float,
        blurRadius: Float,
        alphaScale: Float,
    ) {
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            this.strokeWidth = strokeWidth
            shader = AndroidLinearGradient(
                centerX,
                8f,
                centerX,
                height - 8f,
                intArrayOf(
                    Color.Transparent.toArgb(),
                    color.copy(alpha = 0.05f * alphaScale).toArgb(),
                    color.copy(alpha = 0.24f * alphaScale).toArgb(),
                    color.copy(alpha = 0.80f * alphaScale).toArgb(),
                    color.copy(alpha = 0.18f * alphaScale).toArgb(),
                    Color.Transparent.toArgb(),
                ),
                floatArrayOf(0f, 0.16f, 0.50f, 0.80f, 0.94f, 1f),
                Shader.TileMode.CLAMP,
            )
            if (blurRadius > 0f) {
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
        }
        canvas.drawLine(centerX, 12f, centerX, height - 12f, paint)
    }

    drawPass(strokeWidth = 9f, blurRadius = 6f, alphaScale = 0.52f)
    drawPass(strokeWidth = 3.2f, blurRadius = 1.5f, alphaScale = 0.82f)
    drawPass(strokeWidth = 1.05f, blurRadius = 0f, alphaScale = 0.92f)
    return bitmap.asImageBitmap()
}

/**
 * The reference contains hundreds of nearly stationary pinhead droplets. Baking those tiny marks
 * once keeps their irregular wet-glass texture while avoiding hundreds of Canvas calls per frame.
 */
private fun createRainGlassMistTexture(color: Color): ImageBitmap {
    val width = 480
    val height = 960
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val darkPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
        this.color = Color(0xFF06121D).toArgb()
    }
    val rainPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
        this.color = color.toArgb()
    }
    val highlightPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
        this.color = Color.White.toArgb()
    }
    val softPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
        this.color = color.toArgb()
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    // A handful of cached, low-frequency moisture patches soften the content underneath. Paired
    // light and dark offsets suggest refraction without paying for a real-time full-screen shader.
    repeat(5) { index ->
        val noiseIndex = 9_100 + index
        val centerX = width * (0.08f + rainNoise(noiseIndex, 407) * 0.84f)
        val centerY = height * (0.10f + rainNoise(noiseIndex, 409) * 0.80f)
        val radiusX = 22f + rainNoise(noiseIndex, 411) * 54f
        val radiusY = 48f + rainNoise(noiseIndex, 413) * 106f
        val direction = if (index % 2 == 0) -1f else 1f
        softPaint.color = Color(0xFF07131F).toArgb()
        softPaint.alpha = 7 + (rainNoise(noiseIndex, 415) * 4f).roundToInt()
        canvas.drawOval(
            centerX - radiusX + direction * 4f,
            centerY - radiusY,
            centerX + radiusX + direction * 4f,
            centerY + radiusY,
            softPaint,
        )
        softPaint.color = color.toArgb()
        softPaint.alpha = 9 + (rainNoise(noiseIndex, 417) * 5f).roundToInt()
        canvas.drawOval(
            centerX - radiusX - direction * 5f,
            centerY - radiusY * 0.96f,
            centerX + radiusX - direction * 5f,
            centerY + radiusY * 0.96f,
            softPaint,
        )
    }

    repeat(380) { index ->
        val noiseIndex = 9_500 + index
        val x = width * (0.012f + rainNoise(noiseIndex, 421) * 0.976f)
        val yNoise = rainNoise(noiseIndex, 431).pow(0.76f)
        val y = height * (0.014f + yNoise * 0.972f)
        val shapeVariant = index % 5
        val large = index % 19 == 5
        val sliding = index % 13 == 2
        val radiusNoise = rainNoise(noiseIndex, 419).pow(1.65f)
        val radius = when {
            large -> 2.0f + radiusNoise * 3.2f
            sliding -> 1.25f + radiusNoise * 2.1f
            else -> 0.42f + radiusNoise * 1.45f
        }
        val depthAlpha = 0.62f + yNoise * 0.38f
        val alphaNoise = (0.64f + rainNoise(noiseIndex, 439) * 0.36f) * depthAlpha

        if (sliding) {
            val trailLength = 5f + rainNoise(noiseIndex, 420) * 16f
            darkPaint.style = AndroidPaint.Style.STROKE
            darkPaint.strokeCap = AndroidPaint.Cap.ROUND
            darkPaint.strokeWidth = (radius * 0.58f).coerceAtLeast(0.8f)
            darkPaint.alpha = (11f * alphaNoise).roundToInt()
            canvas.drawLine(x, y - trailLength, x, y - radius * 0.38f, darkPaint)
            rainPaint.style = AndroidPaint.Style.STROKE
            rainPaint.strokeCap = AndroidPaint.Cap.ROUND
            rainPaint.strokeWidth = (radius * 0.16f).coerceAtLeast(0.45f)
            rainPaint.alpha = (34f * alphaNoise).roundToInt()
            canvas.drawLine(
                x - radius * 0.18f,
                y - trailLength,
                x - radius * 0.18f,
                y - radius * 0.38f,
                rainPaint,
            )
        }

        val widthScale = when (shapeVariant) {
            0 -> 0.74f
            1 -> 0.92f
            2 -> 0.66f
            3 -> 1.02f
            else -> 0.82f
        }
        val heightScale = when (shapeVariant) {
            0 -> 1.10f
            1 -> 0.82f
            2 -> 1.34f
            3 -> 0.96f
            else -> 1.18f
        }
        val direction = if (rainNoise(noiseIndex, 425) < 0.5f) -1f else 1f
        val centerOffsetX = direction * radius * (0.08f + rainNoise(noiseIndex, 427) * 0.13f)

        softPaint.color = color.toArgb()
        softPaint.alpha = (19f * alphaNoise).roundToInt()
        canvas.drawOval(
            x - radius * widthScale * 1.18f,
            y - radius * heightScale * 0.94f,
            x + radius * widthScale * 1.18f,
            y + radius * heightScale * 1.18f,
            softPaint,
        )

        darkPaint.style = AndroidPaint.Style.FILL
        darkPaint.alpha = (12f * alphaNoise).roundToInt()
        canvas.drawOval(
            x - radius * widthScale + centerOffsetX,
            y - radius * heightScale * 0.68f,
            x + radius * widthScale + centerOffsetX,
            y + radius * heightScale * (if (large) 1.34f else 0.98f),
            darkPaint,
        )
        rainPaint.style = AndroidPaint.Style.FILL
        rainPaint.alpha = (56f * alphaNoise).roundToInt()
        canvas.drawOval(
            x - radius * widthScale * 0.70f - centerOffsetX * 0.35f,
            y - radius * heightScale * 0.54f,
            x + radius * widthScale * 0.70f - centerOffsetX * 0.35f,
            y + radius * heightScale * 0.72f,
            rainPaint,
        )
        if (shapeVariant == 1 || shapeVariant == 3 || large) {
            rainPaint.alpha = (31f * alphaNoise).roundToInt()
            canvas.drawOval(
                x + direction * radius * 0.42f - radius * 0.38f,
                y + radius * 0.10f - radius * 0.30f,
                x + direction * radius * 0.42f + radius * 0.38f,
                y + radius * 0.10f + radius * 0.30f,
                rainPaint,
            )
        }
        if (large || index % 3 == 0) {
            highlightPaint.style = AndroidPaint.Style.STROKE
            highlightPaint.strokeCap = AndroidPaint.Cap.ROUND
            highlightPaint.strokeWidth = (radius * 0.12f).coerceAtLeast(0.38f)
            highlightPaint.alpha = (118f * alphaNoise).roundToInt()
            canvas.drawArc(
                x - radius * widthScale * 0.72f,
                y - radius * heightScale * 0.62f,
                x + radius * widthScale * 0.72f,
                y + radius * heightScale * 0.70f,
                196f + shapeVariant * 7f,
                78f,
                false,
                highlightPaint,
            )
        }
        if (large) {
            repeat(2) { satelliteIndex ->
                val direction = if (satelliteIndex == 0) -1f else 1f
                val satelliteNoise = rainNoise(noiseIndex + satelliteIndex, 443)
                rainPaint.alpha = (52f * alphaNoise).roundToInt()
                canvas.drawCircle(
                    x + direction * radius * (1.10f + satelliteNoise * 0.75f),
                    y - radius * (0.18f + satelliteNoise * 0.62f),
                    radius * (0.08f + satelliteNoise * 0.08f),
                    rainPaint,
                )
            }
        }
    }
    return bitmap.asImageBitmap()
}

internal fun rainPatternStyle(
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
): RainPatternStyle {
    if (pattern != WeatherPrecipitationPattern.Showers) {
        val (pulseMin, pulseMax) = when (intensity) {
            WeatherIntensity.None -> 1f to 1f
            WeatherIntensity.Light -> 0.98f to 1.02f
            WeatherIntensity.Moderate -> 0.96f to 1.04f
            WeatherIntensity.Heavy -> 0.92f to 1.08f
        }
        return RainPatternStyle(
            countScale = 1f,
            lengthScale = 1f,
            alphaScale = 1f,
            veilScale = 1f,
            pulseMin = pulseMin,
            pulseMax = pulseMax,
        )
    }
    return when {
        intensity == WeatherIntensity.Heavy && windLevel == WeatherWindLevel.Windy -> RainPatternStyle(
            countScale = 1.55f,
            lengthScale = 1.26f,
            alphaScale = 1.20f,
            veilScale = 2.00f,
            pulseMin = 1.00f,
            pulseMax = 1.38f,
        )
        intensity == WeatherIntensity.Heavy -> RainPatternStyle(
            countScale = 1.32f,
            lengthScale = 1.14f,
            alphaScale = 1.12f,
            veilScale = 1.55f,
            pulseMin = 0.90f,
            pulseMax = 1.30f,
        )
        intensity == WeatherIntensity.Moderate -> RainPatternStyle(
            countScale = 1.20f,
            lengthScale = 1.08f,
            alphaScale = 1.10f,
            veilScale = 1.30f,
            pulseMin = 0.86f,
            pulseMax = 1.26f,
        )
        else -> RainPatternStyle(
            countScale = 1.12f,
            lengthScale = 1.05f,
            alphaScale = 1.06f,
            veilScale = 1.18f,
            pulseMin = 0.84f,
            pulseMax = 1.22f,
        )
    }
}

internal fun rainRhythmPulse(
    progress: Float,
    pulseMin: Float,
    pulseMax: Float,
    phaseOffset: Float = 0f,
): Float {
    if (pulseMin == pulseMax) return pulseMin
    val phase = ((progress + phaseOffset) % 1f + 1f) % 1f
    val primary = (sin(phase * PI.toFloat() * 2f - PI.toFloat() / 2f) + 1f) / 2f
    val secondary = (sin(phase * PI.toFloat() * 4f + 1.35f) + 1f) / 2f
    val irregularPulse = (primary * 0.78f + secondary * 0.22f).coerceIn(0f, 1f)
    return pulseMin + (pulseMax - pulseMin) * irregularPulse
}

internal fun rainGustSkew(progress: Float, phaseOffset: Float = 0f): Float {
    val phase = ((progress + phaseOffset) % 1f + 1f) % 1f
    return sin(phase * PI.toFloat() * 2f) * 0.026f +
        sin(phase * PI.toFloat() * 4f + 0.9f) * 0.009f
}

internal fun rainEdgeClusterStrength(xFraction: Float): Float {
    val normalizedX = xFraction.coerceIn(0f, 0.9999f)
    val cell = (normalizedX * 13f).toInt()
    val clusterNoise = rainNoise(cell, 503)
    return when {
        clusterNoise < 0.34f -> 0f
        clusterNoise < 0.56f -> 0.42f
        else -> 0.72f + rainNoise(cell, 521) * 0.28f
    }
}

internal fun roundedCardTopEdgeY(
    x: Float,
    width: Float,
    edgeY: Float,
    cornerRadius: Float,
): Float {
    if (width <= 0f || cornerRadius <= 0f) return edgeY
    val radius = cornerRadius.coerceAtMost(width / 2f)
    val clampedX = x.coerceIn(0f, width)
    val distanceFromSide = minOf(clampedX, width - clampedX)
    if (distanceFromSide >= radius) return edgeY
    val horizontalDistance = radius - distanceFromSide
    return edgeY + radius - sqrt(
        (radius * radius - horizontalDistance * horizontalDistance).coerceAtLeast(0f),
    )
}

internal fun roundedCardCornerPoint(
    width: Float,
    edgeY: Float,
    cornerRadius: Float,
    left: Boolean,
    progress: Float,
): Offset {
    val radius = cornerRadius.coerceIn(0f, width.coerceAtLeast(0f) / 2f)
    val angle = progress.coerceIn(0f, 1f) * PI.toFloat() / 2f
    val horizontalOffset = sin(angle) * radius
    return Offset(
        x = if (left) radius - horizontalOffset else width - radius + horizontalOffset,
        y = edgeY + radius - cos(angle) * radius,
    )
}

internal fun rainNoise(index: Int, salt: Int): Float {
    var value = index * 0x45D9F3B + salt * 0x27D4EB2D
    value = (value xor (value ushr 16)) * 0x45D9F3B
    value = value xor (value ushr 16)
    return (value and Int.MAX_VALUE) / Int.MAX_VALUE.toFloat()
}

private fun DrawScope.drawRain(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
    rainRhythmProgress: Float = motionProgress,
    drizzle: Boolean = false,
    freezing: Boolean = false,
    isDaytime: Boolean,
    rainSprite: ImageBitmap? = null,
) {
    val style = rainVisualStyle(
        intensity = intensity,
        drizzle = drizzle,
        freezing = freezing,
    )
    if (style.dropCount == 0) return

    val patternStyle = rainPatternStyle(
        intensity = intensity,
        pattern = pattern,
        windLevel = windLevel,
    )
    val overallPulse = rainRhythmPulse(
        progress = rainRhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
    )
    val baseSkew = when (windLevel) {
        WeatherWindLevel.Calm -> 0.018f
        WeatherWindLevel.Breezy -> 0.065f
        WeatherWindLevel.Windy -> 0.18f
    }
    val rainColor = when {
        freezing -> Color(0xFFE7F7FF)
        isDaytime -> Color(0xFFD5E7F1)
        else -> Color(0xFFAAC8D9)
    }

    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.42f to rainColor.copy(
                    alpha = style.veilAlpha * patternStyle.veilScale * overallPulse * 0.48f,
                ),
                1f to rainColor.copy(
                    alpha = style.veilAlpha * patternStyle.veilScale * overallPulse,
                ),
            ),
            startY = 0f,
            endY = height,
        ),
        size = Size(width, height),
    )

    rainLayerSpecs.forEachIndexed { layerIndex, layer ->
        val layerPulse = rainRhythmPulse(
            progress = rainRhythmProgress,
            pulseMin = patternStyle.pulseMin,
            pulseMax = patternStyle.pulseMax,
            phaseOffset = layerIndex * 0.17f,
        )
        val count = (style.dropCount * patternStyle.countScale * layer.countFraction).roundToInt()
        val baseLength = style.lengthDp.dp.toPx() * patternStyle.lengthScale * layer.lengthScale *
            (1f + (layerPulse - 1f) * 0.18f)
        val maxLength = baseLength * 1.20f
        val travelHeight = height + maxLength * 2f
        val horizontalPadding = maxLength * 2f
        val travelWidth = width + horizontalPadding * 2f

        repeat(count) { dropIndex ->
            val noiseIndex = layerIndex * 1_000 + dropIndex
            val initialPhase = rainNoise(noiseIndex, 17)
            val fallProgress = (initialPhase + motionProgress * layer.fallCycles) % 1f
            val length = baseLength * (0.72f + rainNoise(noiseIndex, 31) * 0.48f)
            val skew = (baseSkew + rainGustSkew(rainRhythmProgress, layerIndex * 0.17f)) *
                (0.86f + rainNoise(noiseIndex, 47) * 0.28f)
            val baseX = rainNoise(noiseIndex, 71) * travelWidth - horizontalPadding
            val rawX = baseX - fallProgress * height * skew
            val x = ((rawX + horizontalPadding) % travelWidth + travelWidth) % travelWidth - horizontalPadding
            val y = fallProgress * travelHeight - maxLength
            val strokeWidth = layer.strokeWidthDp.dp.toPx() *
                (0.84f + rainNoise(noiseIndex, 89) * 0.30f)
            val dropAlpha = (style.alpha * patternStyle.alphaScale * layer.alphaScale * layerPulse *
                (0.72f + rainNoise(noiseIndex, 107) * 0.28f)).coerceIn(0f, 1f)
            if (rainSprite != null) {
                drawRainStreakSprite(
                    sprite = rainSprite,
                    x = x,
                    y = y,
                    length = length,
                    width = strokeWidth * 4.4f,
                    skew = skew,
                    alpha = dropAlpha * if (layerIndex == 0) 0.62f else 0.86f,
                )
            } else {
                val start = Offset(x, y)
                val end = Offset(x - length * skew, y + length)
                if (layerIndex > 0) {
                    drawLine(
                        color = rainColor.copy(alpha = dropAlpha * 0.075f),
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth * 2.7f,
                        cap = StrokeCap.Round,
                    )
                }
                drawLine(
                    color = rainColor.copy(
                        alpha = dropAlpha * if (layerIndex == 0) 0.48f else 0.60f,
                    ),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawRainStreakSprite(
    sprite: ImageBitmap,
    x: Float,
    y: Float,
    length: Float,
    width: Float,
    skew: Float,
    alpha: Float,
) {
    val spriteWidth = width.roundToInt().coerceAtLeast(2)
    val spriteHeight = length.roundToInt().coerceAtLeast(4)
    val topLeft = IntOffset(
        x = (x - spriteWidth / 2f).roundToInt(),
        y = y.roundToInt(),
    )
    val pivot = Offset(x, y + spriteHeight / 2f)
    val rotationDegrees = (atan(skew) * 180f / PI.toFloat())
    withTransform({ rotate(degrees = rotationDegrees, pivot = pivot) }) {
        drawImage(
            image = sprite,
            dstOffset = topLeft,
            dstSize = IntSize(spriteWidth, spriteHeight),
            alpha = alpha.coerceIn(0f, 1f),
        )
    }
}

private fun DrawScope.drawForegroundRain(
    scene: WeatherSceneSpec,
    motionProgress: Float,
    rhythmProgress: Float,
    rainSprite: ImageBitmap,
) {
    val drizzle = scene.precipitation == WeatherPrecipitation.Drizzle
    val style = foregroundRainVisualStyle(
        intensity = scene.precipitationIntensity,
        drizzle = drizzle,
        freezing = scene.freezing,
    )
    if (style.dropCount == 0) return

    val patternStyle = rainPatternStyle(
        intensity = scene.precipitationIntensity,
        pattern = scene.precipitationPattern,
        windLevel = scene.windLevel,
    )
    val foregroundPulse = rainRhythmPulse(
        progress = rhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
        phaseOffset = 0.31f,
    )
    val skew = when (scene.windLevel) {
        WeatherWindLevel.Calm -> 0.018f
        WeatherWindLevel.Breezy -> 0.065f
        WeatherWindLevel.Windy -> 0.18f
    } + rainGustSkew(rhythmProgress, phaseOffset = 0.31f)
    val count = (style.dropCount * patternStyle.countScale).roundToInt()
    val minLength = style.minLengthDp.dp.toPx() * patternStyle.lengthScale
    val maxLength = style.maxLengthDp.dp.toPx() * patternStyle.lengthScale
    val travelHeight = size.height + maxLength * 2f
    val horizontalPadding = maxLength
    val travelWidth = size.width + horizontalPadding * 2f

    repeat(count) { index ->
        val noiseIndex = 4_000 + index
        val fallCycles = if (rainNoise(noiseIndex, 149) < 0.68f) 1 else 2
        val fallProgress = (rainNoise(noiseIndex, 157) + motionProgress * fallCycles) % 1f
        val lengthNoise = rainNoise(noiseIndex, 163).pow(0.72f)
        val length = minLength + (maxLength - minLength) * lengthNoise
        val dropSkew = skew * (0.72f + rainNoise(noiseIndex, 179) * 0.52f)
        val baseX = rainNoise(noiseIndex, 181) * travelWidth - horizontalPadding
        val rawX = baseX - fallProgress * size.height * dropSkew
        val x = ((rawX + horizontalPadding) % travelWidth + travelWidth) % travelWidth - horizontalPadding
        val y = fallProgress * travelHeight - maxLength
        val strokeWidth = (0.72f + rainNoise(noiseIndex, 191) * 0.52f).dp.toPx()
        val alpha = (style.alpha * patternStyle.alphaScale * foregroundPulse *
            (0.60f + rainNoise(noiseIndex, 199) * 0.40f)).coerceIn(0f, 1f)

        drawRainStreakSprite(
            sprite = rainSprite,
            x = x,
            y = y,
            length = length,
            width = strokeWidth * 5.2f,
            skew = dropSkew,
            alpha = alpha,
        )
    }
}

private fun DrawScope.drawRainGlassImpacts(
    scene: WeatherSceneSpec,
    impactProgress: Float,
    clingProgress: Float,
    rhythmProgress: Float,
    mistTexture: ImageBitmap,
) {
    val drizzle = scene.precipitation == WeatherPrecipitation.Drizzle
    val style = rainGlassVisualStyle(
        intensity = scene.precipitationIntensity,
        drizzle = drizzle,
        freezing = scene.freezing,
    )
    if (style.impactCount == 0 || size.width <= 0f || size.height <= 0f) return

    val rainColor = when {
        scene.freezing -> Color(0xFFF0FBFF)
        scene.skyPhase == WeatherSkyPhase.Day -> Color(0xFFE5F1F7)
        else -> Color(0xFFC9DDE8)
    }
    val patternStyle = rainPatternStyle(
        intensity = scene.precipitationIntensity,
        pattern = scene.precipitationPattern,
        windLevel = scene.windLevel,
    )
    val impactPulse = rainRhythmPulse(
        progress = rhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
        phaseOffset = 0.46f,
    )

    // Keep the cached optical veil below the animated beads so the hero drops remain legible.
    drawImage(
        image = mistTexture,
        dstSize = IntSize(
            width = size.width.roundToInt().coerceAtLeast(1),
            height = size.height.roundToInt().coerceAtLeast(1),
        ),
        alpha = (style.alpha * 0.72f * impactPulse).coerceIn(0f, 1f),
    )

    // A glass hit is a brief, irregular burst. Keeping it filled and soft avoids the synthetic
    // bubble rings that a stroked circle creates.
    repeat(style.impactCount) { index ->
        val noiseIndex = 8_000 + index
        val center = Offset(
            x = size.width * (0.025f + rainNoise(noiseIndex, 311) * 0.95f),
            y = size.height * (0.035f + rainNoise(noiseIndex, 313) * 0.93f),
        )
        val age = (impactProgress + rainNoise(noiseIndex, 317)) % 1f

        if (age > 0.94f) {
            val approachProgress = (age - 0.94f) / 0.06f
            val approachLength = (7f + rainNoise(noiseIndex, 319) * 13f).dp.toPx()
            val endY = center.y - approachLength * (1f - approachProgress)
            val alpha = style.alpha * impactPulse *
                sin(approachProgress * PI.toFloat()).pow(0.6f)
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        rainColor.copy(alpha = alpha * 0.42f),
                        Color.Transparent,
                    ),
                    startY = endY - approachLength,
                    endY = endY,
                ),
                start = Offset(center.x, endY - approachLength),
                end = Offset(center.x, endY),
                strokeWidth = (0.52f + rainNoise(noiseIndex, 331) * 0.62f).dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val lifetime = 0.075f + rainNoise(noiseIndex, 337) * 0.055f
        if (age < lifetime) {
            val lifeProgress = age / lifetime
            val visibility = sin((1f - lifeProgress) * PI.toFloat() / 2f).pow(1.3f)
            val baseRadius = (1.8f + rainNoise(noiseIndex, 347) * 4.2f).dp.toPx()
            val radius = baseRadius * (0.42f + lifeProgress * 1.18f)
            val alpha = style.alpha * impactPulse * visibility *
                (0.70f + rainNoise(noiseIndex, 349) * 0.30f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * 0.16f),
                        rainColor.copy(alpha = alpha * 0.07f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 1.15f,
                ),
                radius = radius * 1.15f,
                center = center,
            )
            val satelliteCount = 3 + (rainNoise(noiseIndex, 353) * 3f).toInt()
            repeat(satelliteCount) { satelliteIndex ->
                val direction = if (satelliteIndex % 2 == 0) -1f else 1f
                val spreadNoise = rainNoise(noiseIndex + satelliteIndex, 355)
                val scatterX = direction * radius * (0.55f + spreadNoise * 1.25f)
                val scatterY = -radius * (0.20f + rainNoise(noiseIndex + satelliteIndex, 357) * 0.85f)
                val satelliteRadius = radius * (0.045f + spreadNoise * 0.07f)
                drawCircle(
                    color = rainColor.copy(alpha = alpha * (0.24f + spreadNoise * 0.20f)),
                    radius = satelliteRadius,
                    center = center + Offset(scatterX, scatterY),
                )
            }
        }
    }

    // Most hits stay as tiny, soft wet marks. A smaller set creeps down the glass and only a few
    // gather enough water to slide farther. This uneven mix matches the reference better than a
    // screen full of identical teardrops.
    val heroDropCount = heroGlassDropCount(scene.precipitationIntensity)
    repeat(style.beadCount) { index ->
        val noiseIndex = 9_000 + index
        val isHeroDrop = index < heroDropCount
        val phase = if (isHeroDrop) {
            (clingProgress + index.toFloat() / heroDropCount.coerceAtLeast(1)) % 1f
        } else {
            (clingProgress + rainNoise(noiseIndex, 359)) % 1f
        }
        val dropMotion = glassDropMotion(phase)
        val behavior = if (isHeroDrop) {
            GlassDropBehavior.Sliding
        } else {
            glassDropBehavior(rainNoise(noiseIndex, 363))
        }
        val radiusNoise = rainNoise(noiseIndex, 367).pow(1.35f)
        val baseRadius = when (behavior) {
            GlassDropBehavior.Pinned -> (0.72f + radiusNoise * 1.38f).dp.toPx()
            GlassDropBehavior.Creeping -> (1.14f + radiusNoise * 2.14f).dp.toPx()
            GlassDropBehavior.Sliding -> if (isHeroDrop) {
                (3.1f + radiusNoise * 4.1f).dp.toPx()
            } else {
                (2.18f + radiusNoise * 3.82f).dp.toPx()
            }
        }
        val radius = baseRadius * if (behavior == GlassDropBehavior.Pinned) {
            1f
        } else {
            dropMotion.mergeScale
        }
        val baseX = size.width * (0.035f + rainNoise(noiseIndex, 373) * 0.93f)
        val baseY = size.height * (0.045f + rainNoise(noiseIndex, 379) * 0.86f)
        val availableTravel = when (behavior) {
            GlassDropBehavior.Sliding -> (size.height - baseY + radius * 0.35f).coerceAtLeast(0f)
            else -> (size.height - baseY - 22.dp.toPx()).coerceAtLeast(0f)
        }
        val travelNoise = rainNoise(noiseIndex, 383)
        val intendedTravel = when (behavior) {
            GlassDropBehavior.Pinned -> (1.5f + travelNoise * 5.5f).dp.toPx()
            GlassDropBehavior.Creeping -> (12f + travelNoise * 30f).dp.toPx()
            GlassDropBehavior.Sliding -> availableTravel
        }
        val travelDistance = intendedTravel.coerceAtMost(availableTravel)
        val traveledDistance = travelDistance * dropMotion.travelProgress
        val wobblePhase = rainNoise(noiseIndex, 387) * PI.toFloat() * 2f
        val x = baseX + sin(dropMotion.slideProgress * PI.toFloat() * 1.35f + wobblePhase) *
            radius * 0.42f * dropMotion.slideProgress
        val y = baseY + traveledDistance
        val behaviorAlpha = when (behavior) {
            GlassDropBehavior.Pinned -> 0.70f
            GlassDropBehavior.Creeping -> 0.82f
            GlassDropBehavior.Sliding -> 0.96f
        }
        val alpha = (style.alpha * impactPulse * behaviorAlpha * dropMotion.dropVisibility *
            (0.82f + rainNoise(noiseIndex, 389) * 0.24f)).coerceIn(0f, 1f)

        if (dropMotion.impactVisibility > 0f) {
            val impactAlpha = style.alpha * impactPulse * dropMotion.impactVisibility
            val impactRadius = radius * (1.45f + dropMotion.impactVisibility * 1.15f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = impactAlpha * 0.20f),
                        rainColor.copy(alpha = impactAlpha * 0.10f),
                        Color.Transparent,
                    ),
                    center = Offset(baseX, baseY),
                    radius = impactRadius,
                ),
                radius = impactRadius,
                center = Offset(baseX, baseY),
            )
            val splashCount = 3 + (rainNoise(noiseIndex, 390) * 4f).toInt()
            repeat(splashCount) { splashIndex ->
                val direction = if (splashIndex % 2 == 0) -1f else 1f
                val spread = impactRadius *
                    (0.55f + rainNoise(noiseIndex + splashIndex, 391) * 0.75f)
                val lift = impactRadius *
                    (0.15f + rainNoise(noiseIndex + splashIndex, 393) * 0.62f)
                val splashCenter = Offset(baseX + direction * spread, baseY - lift)
                val splashRadius = radius *
                    (0.05f + rainNoise(noiseIndex + splashIndex, 395) * 0.09f)
                if (splashIndex % 3 == 0) {
                    drawLine(
                        color = rainColor.copy(alpha = impactAlpha * 0.34f),
                        start = splashCenter,
                        end = splashCenter + Offset(
                            direction * splashRadius * 1.8f,
                            -splashRadius * 2.6f,
                        ),
                        strokeWidth = splashRadius.coerceAtLeast(0.22.dp.toPx()),
                        cap = StrokeCap.Round,
                    )
                } else {
                    drawCircle(
                        color = rainColor.copy(alpha = impactAlpha * 0.42f),
                        radius = splashRadius,
                        center = splashCenter,
                    )
                }
            }
        }

        if (
            behavior != GlassDropBehavior.Pinned &&
            dropMotion.mergeProgress > 0f &&
            dropMotion.mergeProgress < 1f
        ) {
            val absorptionVisibility = sin(dropMotion.mergeProgress * PI.toFloat())
                .coerceAtLeast(0f)
            repeat(if (isHeroDrop) 2 else 1) { mergeIndex ->
                val direction = if (mergeIndex % 2 == 0) -1f else 1f
                val approachDistance = radius *
                    (0.55f + (1f - dropMotion.mergeProgress) * (1.8f + mergeIndex * 0.55f))
                drawSoftGlassBlotch(
                    center = Offset(
                        x = x + direction * approachDistance * (0.24f + mergeIndex * 0.10f),
                        y = y + approachDistance,
                    ),
                    radiusX = radius * (0.16f + mergeIndex * 0.04f),
                    radiusY = radius * (0.22f + mergeIndex * 0.05f),
                    rainColor = rainColor,
                    alpha = alpha * absorptionVisibility * 0.70f,
                    lobeNoise = rainNoise(noiseIndex + mergeIndex, 394),
                )
            }
        }

        if (
            behavior != GlassDropBehavior.Pinned &&
            dropMotion.trailVisibility > 0f &&
            traveledDistance > radius
        ) {
            val maxTrailLength = when (behavior) {
                GlassDropBehavior.Creeping -> (11f + rainNoise(noiseIndex, 397) * 20f).dp.toPx()
                GlassDropBehavior.Sliding -> (28f + rainNoise(noiseIndex, 397) * 34f).dp.toPx()
                GlassDropBehavior.Pinned -> 0f
            }
            val trailLength = traveledDistance.coerceAtMost(maxTrailLength)
            val trailStartY = (y - trailLength).coerceAtLeast(baseY)
            val trailAlpha = style.alpha * impactPulse * dropMotion.trailVisibility
            val segmentCount = if (behavior == GlassDropBehavior.Sliding) 4 else 2
            val usableLength = (trailLength - radius * 0.6f).coerceAtLeast(0f)
            repeat(segmentCount) { segmentIndex ->
                val segmentNoise = rainNoise(noiseIndex + segmentIndex, 398)
                val slotLength = usableLength / segmentCount
                val segmentStartY = trailStartY + slotLength * segmentIndex +
                    slotLength * (0.10f + segmentNoise * 0.17f)
                val segmentEndY = (
                    segmentStartY + slotLength * (0.35f + segmentNoise * 0.34f)
                    ).coerceAtMost(y - radius * 0.38f)
                if (segmentEndY > segmentStartY) {
                    val startFraction = ((segmentStartY - baseY) / travelDistance)
                        .coerceIn(0f, 1f)
                    val endFraction = ((segmentEndY - baseY) / travelDistance)
                        .coerceIn(0f, 1f)
                    val startX = baseX + (x - baseX) * startFraction
                    val endX = baseX + (x - baseX) * endFraction
                    drawLine(
                        color = Color(0xFF06121D).copy(alpha = trailAlpha * 0.13f),
                        start = Offset(startX, segmentStartY),
                        end = Offset(endX, segmentEndY),
                        strokeWidth = (radius * 0.72f).coerceAtLeast(0.48.dp.toPx()),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = rainColor.copy(alpha = trailAlpha * 0.18f),
                        start = Offset(startX - radius * 0.18f, segmentStartY),
                        end = Offset(endX - radius * 0.18f, segmentEndY),
                        strokeWidth = (radius * 0.18f).coerceAtLeast(0.24.dp.toPx()),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        drawSoftGlassBlotch(
            center = Offset(x, y),
            radiusX = radius * (0.82f + rainNoise(noiseIndex, 401) * 0.32f),
            radiusY = radius * (0.92f + rainNoise(noiseIndex, 403) * 0.38f +
                dropMotion.slideProgress * 0.24f),
            rainColor = rainColor,
            alpha = alpha,
            lobeNoise = rainNoise(noiseIndex, 405),
        )
    }

}

private fun DrawScope.drawSoftGlassBlotch(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    rainColor: Color,
    alpha: Float,
    lobeNoise: Float,
) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.23f),
                rainColor.copy(alpha = alpha * 0.11f),
                Color(0xFF07131F).copy(alpha = alpha * 0.075f),
                Color.Transparent,
            ),
            center = center.copy(
                x = center.x - radiusX * 0.24f,
                y = center.y - radiusY * 0.28f,
            ),
            radius = (radiusX.coerceAtLeast(radiusY) * 1.18f).coerceAtLeast(0.5f),
        ),
        topLeft = Offset(center.x - radiusX * 1.22f, center.y - radiusY * 1.18f),
        size = Size(radiusX * 2.44f, radiusY * 2.36f),
    )
    val lobeDirection = if (lobeNoise < 0.5f) -1f else 1f
    drawOval(
        color = rainColor.copy(alpha = alpha * 0.14f),
        topLeft = Offset(
            center.x + lobeDirection * radiusX * 0.46f - radiusX * 0.42f,
            center.y + radiusY * 0.16f - radiusY * 0.35f,
        ),
        size = Size(radiusX * 0.84f, radiusY * 0.70f),
    )
    drawOval(
        color = Color.White.copy(alpha = alpha * 0.38f),
        topLeft = Offset(
            center.x - radiusX * 0.48f,
            center.y - radiusY * 0.51f,
        ),
        size = Size(radiusX * 0.28f, radiusY * 0.23f),
    )
    val edgeWidth = (radiusX.coerceAtMost(radiusY) * 0.17f)
        .coerceAtLeast(0.24.dp.toPx())
    drawArc(
        color = Color(0xFF07131F).copy(alpha = alpha * 0.14f),
        startAngle = 8f + lobeNoise * 24f,
        sweepAngle = 116f,
        useCenter = false,
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f),
        style = Stroke(width = edgeWidth, cap = StrokeCap.Round),
    )
    drawArc(
        color = rainColor.copy(alpha = alpha * 0.46f),
        startAngle = 188f - lobeNoise * 18f,
        sweepAngle = 88f,
        useCenter = false,
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f),
        style = Stroke(width = edgeWidth * 0.72f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawLensDrop(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    rainColor: Color,
    alpha: Float,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radiusY)
        cubicTo(
            center.x + radiusX * 0.82f,
            center.y - radiusY * 0.62f,
            center.x + radiusX,
            center.y + radiusY * 0.18f,
            center.x,
            center.y + radiusY,
        )
        cubicTo(
            center.x - radiusX,
            center.y + radiusY * 0.18f,
            center.x - radiusX * 0.82f,
            center.y - radiusY * 0.62f,
            center.x,
            center.y - radiusY,
        )
        close()
    }
    withTransform({ translate(left = radiusX * 0.18f, top = radiusY * 0.16f) }) {
        drawPath(
            path = path,
            color = Color(0xFF06111C).copy(alpha = alpha * 0.09f),
        )
    }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.19f),
                rainColor.copy(alpha = alpha * 0.085f),
                Color(0xFF07131F).copy(alpha = alpha * 0.075f),
            ),
            start = Offset(center.x - radiusX, center.y - radiusY),
            end = Offset(center.x + radiusX, center.y + radiusY),
        ),
    )
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.44f),
                Color.White.copy(alpha = alpha * 0.10f),
                Color.Transparent,
            ),
            center = center.copy(
                x = center.x - radiusX * 0.30f,
                y = center.y - radiusY * 0.38f,
            ),
            radius = (radiusX * 0.46f).coerceAtLeast(0.5f),
        ),
        topLeft = Offset(
            center.x - radiusX * 0.62f,
            center.y - radiusY * 0.66f,
        ),
        size = Size(radiusX * 0.66f, radiusY * 0.56f),
    )
}

private fun DrawScope.roundedCardTopEdgePath(
    startX: Float,
    endX: Float,
    edgeY: Float,
    cornerRadius: Float,
): Path {
    val path = Path()
    val span = (endX - startX).coerceAtLeast(0f)
    val segmentCount = maxOf(2, (span / 2.dp.toPx()).roundToInt())
    repeat(segmentCount + 1) { index ->
        val fraction = index / segmentCount.toFloat()
        val x = startX + span * fraction
        val y = roundedCardTopEdgeY(x, size.width, edgeY, cornerRadius)
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    return path
}

private fun DrawScope.drawCardCornerRunoff(
    rhythmProgress: Float,
    edgeY: Float,
    cornerRadius: Float,
    rainColor: Color,
    alpha: Float,
) {
    if (cornerRadius <= 0f) return

    repeat(2) { sideIndex ->
        val phase = (rhythmProgress + 0.13f + sideIndex * 0.47f) % 1f
        val arrivalEnd = 0.78f
        val headProgress = (phase / arrivalEnd).coerceIn(0f, 1f)
        val tailProgress = (headProgress - 0.30f).coerceAtLeast(0f)
        val fade = if (phase <= arrivalEnd) {
            1f
        } else {
            (1f - (phase - arrivalEnd) / (1f - arrivalEnd)).coerceIn(0f, 1f)
        }
        if (fade <= 0f || headProgress <= 0f) return@repeat

        val path = Path()
        repeat(9) { sampleIndex ->
            val sampleProgress = tailProgress +
                (headProgress - tailProgress) * sampleIndex / 8f
            val point = roundedCardCornerPoint(
                width = size.width,
                edgeY = edgeY,
                cornerRadius = cornerRadius,
                left = sideIndex == 0,
                progress = sampleProgress,
            )
            if (sampleIndex == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        val flowAlpha = alpha * fade * (0.18f + 0.12f * sin(headProgress * PI.toFloat()))
        drawPath(
            path = path,
            color = rainColor.copy(alpha = flowAlpha),
            style = Stroke(
                width = (0.55f + sideIndex * 0.14f).dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        val head = roundedCardCornerPoint(
            width = size.width,
            edgeY = edgeY,
            cornerRadius = cornerRadius,
            left = sideIndex == 0,
            progress = headProgress,
        )
        val beadRadius = (0.52f + sideIndex * 0.12f).dp.toPx()
        drawLensDrop(
            center = head,
            radiusX = beadRadius,
            radiusY = beadRadius * 1.35f,
            rainColor = rainColor,
            alpha = alpha * fade * 0.42f,
        )
    }
}

private fun DrawScope.drawRainCardImpacts(
    scene: WeatherSceneSpec,
    motionProgress: Float,
    rhythmProgress: Float,
    edgeY: Float,
    cornerRadius: Float,
) {
    val drizzle = scene.precipitation == WeatherPrecipitation.Drizzle
    val style = rainCardImpactStyle(
        intensity = scene.precipitationIntensity,
        drizzle = drizzle,
        freezing = scene.freezing,
    )
    if (style.impactCount == 0 || size.width <= 0f || size.height <= 0f) return

    val rainColor = when {
        scene.freezing -> Color(0xFFF0FBFF)
        scene.skyPhase == WeatherSkyPhase.Day -> Color(0xFFE7F2F7)
        else -> Color(0xFFC7DBE7)
    }
    val patternStyle = rainPatternStyle(
        intensity = scene.precipitationIntensity,
        pattern = scene.precipitationPattern,
        windLevel = scene.windLevel,
    )
    val edgePulse = rainRhythmPulse(
        progress = rhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
        phaseOffset = 0.12f,
    )
    val resolvedCornerRadius = cornerRadius.coerceIn(0f, size.width / 2f)

    // Broken wet clusters leave real gaps along the edge instead of forming a synthetic rule.
    repeat(13) { index ->
        val cellCenterFraction = (index + 0.5f) / 13f
        val clusterStrength = rainEdgeClusterStrength(cellCenterFraction)
        if (clusterStrength <= 0f) return@repeat
        val segmentWidth = (8f + rainNoise(index, 223) * 24f).dp.toPx()
        val jitter = (rainNoise(index, 227) - 0.5f) * size.width / 18f
        val centerX = (cellCenterFraction * size.width + jitter).coerceIn(0f, size.width)
        val startX = (centerX - segmentWidth / 2f).coerceAtLeast(0f)
        val endX = (centerX + segmentWidth / 2f).coerceAtMost(size.width)
        if (endX > startX) {
            drawPath(
                path = roundedCardTopEdgePath(
                    startX = startX,
                    endX = endX,
                    edgeY = edgeY,
                    cornerRadius = resolvedCornerRadius,
                ),
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        rainColor.copy(alpha = style.alpha * edgePulse * clusterStrength * 0.20f),
                        Color.White.copy(alpha = style.alpha * edgePulse * clusterStrength * 0.08f),
                        Color.Transparent,
                    ),
                    startX = startX,
                    endX = endX,
                ),
                style = Stroke(
                    width = (0.38f + rainNoise(index, 233) * 0.52f).dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }

    drawCardCornerRunoff(
        rhythmProgress = rhythmProgress,
        edgeY = edgeY,
        cornerRadius = resolvedCornerRadius,
        rainColor = rainColor,
        alpha = style.alpha * edgePulse,
    )

    // Small beads share the same event phase and x position as their incoming streak, keeping the
    // collision haze spatially and temporally coupled to rainfall.
    repeat(style.impactCount * 3) { index ->
        val noiseIndex = 5_000 + index
        val x = size.width * (0.012f + rainNoise(noiseIndex, 211) * 0.976f)
        val impactY = roundedCardTopEdgeY(x, size.width, edgeY, resolvedCornerRadius)
        val clusterStrength = rainEdgeClusterStrength(x / size.width)
        if (clusterStrength <= 0f) return@repeat
        val radius = (0.10f + rainNoise(noiseIndex, 213).pow(1.55f) * 0.64f).dp.toPx()
        val eventAge = (motionProgress * 2f + rainNoise(noiseIndex, 217)) % 1f
        if (eventAge > 0.22f) return@repeat
        val shimmer = sin(eventAge / 0.22f * PI.toFloat()).coerceAtLeast(0f).pow(1.25f)
        val lift = rainNoise(noiseIndex, 219).pow(1.35f) * 3.0.dp.toPx()
        drawOval(
            color = rainColor.copy(
                alpha = style.alpha * edgePulse * clusterStrength * shimmer * 0.58f,
            ),
            topLeft = Offset(x - radius, impactY - lift - radius),
            size = Size(radius * 2f, radius * (1.2f + rainNoise(noiseIndex, 221) * 1.2f)),
        )
        if (index % 13 == 0) {
            drawCircle(
                color = Color.White.copy(
                    alpha = style.alpha * edgePulse * clusterStrength * shimmer * 0.24f,
                ),
                radius = radius * 0.28f,
                center = Offset(x - radius * 0.22f, impactY - lift - radius * 0.24f),
            )
        }
    }

    repeat(style.impactCount) { index ->
        val noiseIndex = 6_000 + index
        val age = (motionProgress + rainNoise(noiseIndex, 239)) % 1f
        val lifetime = 0.085f + rainNoise(noiseIndex, 241) * 0.060f
        val x = size.width * (0.025f + rainNoise(noiseIndex, 251) * 0.95f)
        val impactY = roundedCardTopEdgeY(x, size.width, edgeY, resolvedCornerRadius)
        val clusterStrength = rainEdgeClusterStrength(x / size.width)
        if (clusterStrength <= 0f) return@repeat
        val eventAlpha = edgePulse * clusterStrength

        if (age > 0.93f) {
            val approachProgress = (age - 0.93f) / 0.07f
            val approachLength = (5f + rainNoise(noiseIndex, 247) * 9f).dp.toPx()
            val dropEndY = impactY - approachLength * (1f - approachProgress)
            val approachAlpha = style.alpha * eventAlpha *
                sin(approachProgress * PI.toFloat()).pow(0.55f)
            val approachSkew = rainGustSkew(rhythmProgress, 0.12f) * approachLength
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        rainColor.copy(alpha = approachAlpha * 0.68f),
                        Color.Transparent,
                    ),
                    startY = dropEndY - approachLength,
                    endY = dropEndY,
                ),
                start = Offset(x + approachSkew, dropEndY - approachLength),
                end = Offset(x, dropEndY),
                strokeWidth = (0.48f + rainNoise(noiseIndex, 249) * 0.44f).dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        if (age < lifetime) {
            val lifeProgress = age / lifetime
            val visibility = (1f - lifeProgress).pow(1.35f)
            val crownRadius = (1.2f + rainNoise(noiseIndex, 257) * 2.6f).dp.toPx()
            val strokeWidth = (0.24f + rainNoise(noiseIndex, 263) * 0.30f).dp.toPx()
            val alpha = style.alpha * eventAlpha * visibility *
                (0.62f + rainNoise(noiseIndex, 269) * 0.38f)

            drawOval(
                color = rainColor.copy(alpha = alpha * 0.26f),
                topLeft = Offset(
                    x = x - crownRadius * (0.42f + lifeProgress * 0.85f),
                    y = impactY - strokeWidth * 0.40f,
                ),
                size = Size(
                    width = crownRadius * (0.84f + lifeProgress * 1.70f),
                    height = strokeWidth * 1.35f,
                ),
            )
            drawArc(
                color = rainColor.copy(alpha = alpha * 0.62f),
                startAngle = 202f,
                sweepAngle = 136f,
                useCenter = false,
                topLeft = Offset(x - crownRadius, impactY - crownRadius * 0.54f),
                size = Size(crownRadius * 2f, crownRadius * 0.96f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            val spread = crownRadius * (0.55f + lifeProgress * 0.85f)
            val lift = crownRadius * (0.32f + sin(lifeProgress * PI.toFloat()) * 1.30f)
            val satelliteRadius = strokeWidth * (0.48f + (1f - lifeProgress) * 0.22f)
            drawCircle(
                color = rainColor.copy(alpha = alpha * 0.66f),
                radius = satelliteRadius,
                center = Offset(x - spread, impactY - lift),
            )
            if (index % 2 == 0) {
                drawCircle(
                    color = rainColor.copy(alpha = alpha * 0.56f),
                    radius = satelliteRadius * 0.82f,
                    center = Offset(x + spread * 0.78f, impactY - lift * 0.72f),
                )
            }
            if (index % 3 == 0) {
                drawLine(
                    color = rainColor.copy(alpha = alpha * 0.34f),
                    start = Offset(x, impactY),
                    end = Offset(x + spread * 0.28f, impactY - lift * 1.12f),
                    strokeWidth = strokeWidth * 0.55f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }

    // A few tiny drops remain on the first card after impact.
    repeat(style.beadCount) { index ->
        val noiseIndex = 7_000 + index
        val slideProgress = (motionProgress * 0.42f + rainNoise(noiseIndex, 277)) % 1f
        val x = size.width * (0.035f + rainNoise(noiseIndex, 281) * 0.93f)
        val clusterStrength = rainEdgeClusterStrength(x / size.width)
        if (clusterStrength <= 0f) return@repeat
        val baseY = size.height * (0.08f + rainNoise(noiseIndex, 283) * 0.78f)
        val y = baseY + slideProgress * 5.dp.toPx()
        val radius = (0.20f + rainNoise(noiseIndex, 293).pow(1.35f) * 0.62f).dp.toPx()
        val visibility = sin(slideProgress * PI.toFloat()).pow(0.7f)
        if (index % 5 == 0) {
            val trailLength = (3f + rainNoise(noiseIndex, 307) * 8f).dp.toPx()
            drawLine(
                color = rainColor.copy(
                    alpha = style.alpha * edgePulse * clusterStrength * visibility * 0.060f,
                ),
                start = Offset(x, y - trailLength),
                end = Offset(x, y),
                strokeWidth = (radius * 0.72f).coerceAtLeast(0.28.dp.toPx()),
                cap = StrokeCap.Round,
            )
        }
        drawLensDrop(
            center = Offset(x, y),
            radiusX = radius,
            radiusY = radius * 1.65f,
            rainColor = rainColor,
            alpha = style.alpha * edgePulse * clusterStrength * visibility * 0.72f,
        )
    }
}

internal data class SnowVisualStyle(
    val flakeCount: Int,
    val minRadiusDp: Float,
    val maxRadiusDp: Float,
    val alpha: Float,
    val veilAlpha: Float,
)

internal data class SnowLayerSpec(
    val countFraction: Float,
    val radiusScale: Float,
    val alphaScale: Float,
    val fallScale: Float,
    val driftScale: Float,
)

internal data class SnowPatternStyle(
    val countScale: Float,
    val radiusScale: Float,
    val alphaScale: Float,
    val veilScale: Float,
    val driftScale: Float,
    val pulseMin: Float,
    val pulseMax: Float,
)

internal data class ForegroundSnowVisualStyle(
    val flakeCount: Int,
    val minRadiusDp: Float,
    val maxRadiusDp: Float,
    val alpha: Float,
)

internal data class SnowCardImpactStyle(
    val impactCount: Int,
    val settledFlakeCount: Int,
    val alpha: Float,
    val snowCapAlpha: Float,
    val capWidthScale: Float,
    val impactEnergy: Float,
)

internal val snowLayerSpecs = listOf(
    SnowLayerSpec(0.36f, 0.56f, 0.50f, 0.70f, 0.66f),
    SnowLayerSpec(0.39f, 0.86f, 0.80f, 0.98f, 0.96f),
    SnowLayerSpec(0.25f, 1.16f, 0.94f, 1.25f, 1.30f),
)

internal fun snowVisualStyle(intensity: WeatherIntensity): SnowVisualStyle {
    return when (intensity) {
        WeatherIntensity.None -> SnowVisualStyle(0, 0f, 0f, 0f, 0f)
        WeatherIntensity.Light -> SnowVisualStyle(295, 0.80f, 2.70f, 0.75f, 0.038f)
        WeatherIntensity.Moderate -> SnowVisualStyle(580, 0.90f, 3.50f, 0.87f, 0.078f)
        WeatherIntensity.Heavy -> SnowVisualStyle(945, 0.98f, 4.30f, 0.97f, 0.135f)
    }
}

internal fun foregroundSnowVisualStyle(
    intensity: WeatherIntensity,
    grains: Boolean = false,
): ForegroundSnowVisualStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> ForegroundSnowVisualStyle(0, 0f, 0f, 0f)
        WeatherIntensity.Light -> ForegroundSnowVisualStyle(34, 1.1f, 2.8f, 0.43f)
        WeatherIntensity.Moderate -> ForegroundSnowVisualStyle(64, 1.25f, 3.3f, 0.51f)
        WeatherIntensity.Heavy -> ForegroundSnowVisualStyle(98, 1.4f, 3.8f, 0.59f)
    }
    return if (grains) {
        base.copy(
            flakeCount = (base.flakeCount * 1.22f).roundToInt(),
            minRadiusDp = base.minRadiusDp * 0.48f,
            maxRadiusDp = base.maxRadiusDp * 0.52f,
            alpha = (base.alpha * 1.08f).coerceAtMost(1f),
        )
    } else {
        base
    }
}

internal fun snowCardImpactStyle(
    intensity: WeatherIntensity,
    grains: Boolean = false,
): SnowCardImpactStyle {
    val base = when (intensity) {
        WeatherIntensity.None -> SnowCardImpactStyle(0, 0, 0f, 0f, 0f, 0f)
        WeatherIntensity.Light -> SnowCardImpactStyle(26, 39, 0.51f, 0.22f, 1.08f, 0.98f)
        WeatherIntensity.Moderate -> SnowCardImpactStyle(54, 73, 0.69f, 0.325f, 1.36f, 1.21f)
        WeatherIntensity.Heavy -> SnowCardImpactStyle(96, 119, 0.85f, 0.46f, 1.68f, 1.49f)
    }
    return if (grains) {
        base.copy(
            impactCount = (base.impactCount * 1.18f).roundToInt(),
            settledFlakeCount = (base.settledFlakeCount * 0.82f).roundToInt(),
            snowCapAlpha = base.snowCapAlpha * 0.55f,
            capWidthScale = base.capWidthScale * 0.68f,
            impactEnergy = base.impactEnergy * 1.12f,
        )
    } else {
        base
    }
}

internal fun snowEdgeClusterStrength(xFraction: Float): Float {
    val normalizedX = xFraction.coerceIn(0f, 0.9999f)
    val cell = (normalizedX * 13f).toInt()
    val clusterNoise = rainNoise(cell, 811)
    return when {
        clusterNoise < 0.22f -> 0f
        clusterNoise < 0.48f -> 0.48f
        else -> 0.74f + rainNoise(cell, 823) * 0.26f
    }
}

internal fun snowPatternStyle(
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
): SnowPatternStyle {
    if (pattern != WeatherPrecipitationPattern.Flurries) {
        return SnowPatternStyle(1f, 1f, 1f, 1f, 1f, 1f, 1f)
    }
    return when {
        intensity == WeatherIntensity.Heavy && windLevel == WeatherWindLevel.Windy -> SnowPatternStyle(
            countScale = 1.97f,
            radiusScale = 1.16f,
            alphaScale = 1.20f,
            veilScale = 2.40f,
            driftScale = 1.84f,
            pulseMin = 0.90f,
            pulseMax = 1.72f,
        )
        intensity == WeatherIntensity.Heavy -> SnowPatternStyle(
            countScale = 1.72f,
            radiusScale = 1.12f,
            alphaScale = 1.15f,
            veilScale = 2.00f,
            driftScale = 1.53f,
            pulseMin = 0.84f,
            pulseMax = 1.53f,
        )
        intensity == WeatherIntensity.Moderate -> SnowPatternStyle(
            countScale = 1.51f,
            radiusScale = 1.08f,
            alphaScale = 1.10f,
            veilScale = 1.70f,
            driftScale = 1.40f,
            pulseMin = 0.80f,
            pulseMax = 1.45f,
        )
        else -> SnowPatternStyle(
            countScale = 1.35f,
            radiusScale = 1.05f,
            alphaScale = 1.08f,
            veilScale = 1.51f,
            driftScale = 1.30f,
            pulseMin = 0.76f,
            pulseMax = 1.34f,
        )
    }
}

private fun DrawScope.drawSnow(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
) {
    val style = snowVisualStyle(intensity)
    if (style.flakeCount == 0) return

    val patternStyle = snowPatternStyle(
        intensity = intensity,
        pattern = pattern,
        windLevel = windLevel,
    )
    val windAmplitude = when (windLevel) {
        WeatherWindLevel.Calm -> 10.dp.toPx()
        WeatherWindLevel.Breezy -> 28.dp.toPx()
        WeatherWindLevel.Windy -> 52.dp.toPx()
    }
    val directionalDrift = when (windLevel) {
        WeatherWindLevel.Calm -> width * 0.012f
        WeatherWindLevel.Breezy -> width * 0.070f
        WeatherWindLevel.Windy -> width * 0.155f
    }
    val flurryPulse = if (pattern == WeatherPrecipitationPattern.Flurries) {
        val pulseProgress = (sin(motionProgress * PI * 2.0).toFloat() + 1f) / 2f
        patternStyle.pulseMin + (patternStyle.pulseMax - patternStyle.pulseMin) * pulseProgress
    } else {
        1f
    }

    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.46f to Color.White.copy(
                    alpha = style.veilAlpha * patternStyle.veilScale * flurryPulse * 0.40f,
                ),
                1f to Color.White.copy(
                    alpha = style.veilAlpha * patternStyle.veilScale * flurryPulse,
                ),
            ),
            startY = 0f,
            endY = height,
        ),
        size = Size(width, height),
    )

    snowLayerSpecs.forEachIndexed { layerIndex, layer ->
        val count = (style.flakeCount * patternStyle.countScale * layer.countFraction).roundToInt()
        val maxRadius = style.maxRadiusDp.dp.toPx() * patternStyle.radiusScale * layer.radiusScale
        val travelHeight = height + maxRadius * 4f
        val horizontalPadding = maxRadius * 4f + windAmplitude
        val travelWidth = width + horizontalPadding * 2f

        repeat(count) { flakeIndex ->
            val noiseIndex = layerIndex * 1_000 + flakeIndex
            val initialPhase = rainNoise(noiseIndex, 211)
            val fallProgress = (initialPhase + motionProgress * layer.fallScale) % 1f
            val baseX = rainNoise(noiseIndex, 223) * travelWidth - horizontalPadding
            val swayFrequency = 0.62f + rainNoise(noiseIndex, 239) * 0.72f
            val swayPhase = rainNoise(noiseIndex, 251) * PI.toFloat() * 2f
            val sway = sin(
                motionProgress * PI.toFloat() * 2f * swayFrequency + swayPhase,
            ) * windAmplitude * patternStyle.driftScale * layer.driftScale
            val rawX = baseX + sway - fallProgress * directionalDrift * patternStyle.driftScale * layer.driftScale
            val x = ((rawX + horizontalPadding) % travelWidth + travelWidth) % travelWidth - horizontalPadding
            val y = fallProgress * travelHeight - maxRadius * 2f
            val radiusNoise = rainNoise(noiseIndex, 269)
            val radiusDp = style.minRadiusDp + (style.maxRadiusDp - style.minRadiusDp) * radiusNoise
            val radius = radiusDp.dp.toPx() * patternStyle.radiusScale * layer.radiusScale
            val alpha = (
                style.alpha * patternStyle.alphaScale * layer.alphaScale * flurryPulse *
                    (0.78f + rainNoise(noiseIndex, 281) * 0.22f)
                ).coerceIn(0f, 1f)

            if (layerIndex == snowLayerSpecs.lastIndex && flakeIndex % 3 == 0) {
                drawSnowFlakeSprite(
                    center = Offset(x, y),
                    radius = radius,
                    alpha = alpha,
                    softFocus = radius >= 1.75.dp.toPx(),
                    rotationFraction = rainNoise(noiseIndex, 293),
                    motionSkew = when (windLevel) {
                        WeatherWindLevel.Calm -> 0.04f
                        WeatherWindLevel.Breezy -> 0.18f
                        WeatherWindLevel.Windy -> 0.36f
                    },
                )
            } else {
                drawCircle(
                    color = Color.White.copy(
                        alpha = alpha * if (layerIndex == snowLayerSpecs.lastIndex) 0.70f else 0.78f,
                    ),
                    radius = radius * if (layerIndex == snowLayerSpecs.lastIndex) 0.64f else 0.72f,
                    center = Offset(x, y),
                )
            }
        }
    }
}

private fun DrawScope.drawSnowFlakeSprite(
    center: Offset,
    radius: Float,
    alpha: Float,
    softFocus: Boolean,
    rotationFraction: Float,
    motionSkew: Float,
) {
    if (radius <= 0f || alpha <= 0f) return

    // Real foreground snowfall is usually out of focus. Mix mostly soft asymmetric clumps with
    // a smaller number of motion-blurred flakes so the layer does not become a repeated symbol.
    val lobeAngle = rotationFraction * PI.toFloat() * 2f
    val lobeAxis = Offset(cos(lobeAngle), sin(lobeAngle))
    val fallAxisLength = sqrt(1f + motionSkew * motionSkew)
    val fallAxis = Offset(-motionSkew / fallAxisLength, 1f / fallAxisLength)
    val shapeVariant = rotationFraction

    if (!softFocus || shapeVariant < 0.58f) {
        fun drawSoftLobe(blobCenter: Offset, blobRadius: Float, blobAlpha: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = blobAlpha * 0.62f),
                        Color(0xFFE7F7FF).copy(alpha = blobAlpha * 0.24f),
                        Color.Transparent,
                    ),
                    center = blobCenter,
                    radius = blobRadius,
                ),
                radius = blobRadius,
                center = blobCenter,
            )
        }

        val focusAlpha = alpha * if (softFocus) 0.54f else 0.74f
        drawSoftLobe(center, radius * if (softFocus) 1.04f else 0.72f, focusAlpha)
        drawSoftLobe(
            blobCenter = center + lobeAxis * radius * 0.42f,
            blobRadius = radius * 0.62f,
            blobAlpha = focusAlpha * 0.72f,
        )
        drawSoftLobe(
            blobCenter = center - lobeAxis * radius * 0.34f + fallAxis * radius * 0.20f,
            blobRadius = radius * 0.48f,
            blobAlpha = focusAlpha * 0.56f,
        )
        return
    }

    val streakScale = if (shapeVariant < 0.86f) 0.68f else 1.16f
    val stretch = (0.92f + abs(motionSkew) * 1.20f) * streakScale
    val smearLength = radius * stretch
    val bodyWidth = radius * 0.70f

    drawLine(
        color = Color(0xFFDDF4FF).copy(alpha = alpha * 0.045f),
        start = center - fallAxis * smearLength * 0.78f,
        end = center + fallAxis * smearLength * 0.48f,
        strokeWidth = bodyWidth * 1.85f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = alpha * 0.32f),
        start = center - fallAxis * smearLength * 0.52f,
        end = center + fallAxis * smearLength * 0.30f,
        strokeWidth = bodyWidth,
        cap = StrokeCap.Round,
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.24f),
        radius = radius * 0.32f,
        center = center + lobeAxis * radius * 0.30f,
    )
    drawCircle(
        color = Color(0xFFF3FBFF).copy(alpha = alpha * 0.18f),
        radius = radius * 0.23f,
        center = center - lobeAxis * radius * 0.38f + fallAxis * radius * 0.10f,
    )
}

private fun DrawScope.drawForegroundSnow(
    scene: WeatherSceneSpec,
    motionProgress: Float,
    rhythmProgress: Float,
) {
    val grains = scene.precipitation == WeatherPrecipitation.SnowGrains
    val style = foregroundSnowVisualStyle(
        intensity = scene.precipitationIntensity,
        grains = grains,
    )
    if (style.flakeCount == 0 || size.width <= 0f || size.height <= 0f) return

    val patternStyle = snowPatternStyle(
        intensity = scene.precipitationIntensity,
        pattern = scene.precipitationPattern,
        windLevel = scene.windLevel,
    )
    val pulse = rainRhythmPulse(
        progress = rhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
        phaseOffset = 0.29f,
    )
    val count = (style.flakeCount * patternStyle.countScale).roundToInt()
    val windAmplitude = when (scene.windLevel) {
        WeatherWindLevel.Calm -> 16.dp.toPx()
        WeatherWindLevel.Breezy -> 38.dp.toPx()
        WeatherWindLevel.Windy -> 70.dp.toPx()
    }
    val directionalDrift = when (scene.windLevel) {
        WeatherWindLevel.Calm -> size.width * 0.018f
        WeatherWindLevel.Breezy -> size.width * 0.085f
        WeatherWindLevel.Windy -> size.width * 0.18f
    }
    val maxRadius = style.maxRadiusDp.dp.toPx() * patternStyle.radiusScale
    val travelHeight = size.height + maxRadius * 4f
    val horizontalPadding = maxRadius * 4f + windAmplitude
    val travelWidth = size.width + horizontalPadding * 2f

    repeat(count) { index ->
        val noiseIndex = 12_000 + index
        val fallScale = if (grains) {
            1.35f + rainNoise(noiseIndex, 827) * 0.75f
        } else {
            0.76f + rainNoise(noiseIndex, 827) * 0.62f
        }
        val fallProgress = (rainNoise(noiseIndex, 829) + motionProgress * fallScale) % 1f
        val swayFrequency = 0.70f + rainNoise(noiseIndex, 839) * 0.85f
        val swayPhase = rainNoise(noiseIndex, 853) * PI.toFloat() * 2f
        val sway = sin(
            motionProgress * PI.toFloat() * 2f * swayFrequency + swayPhase,
        ) * windAmplitude * patternStyle.driftScale
        val baseX = rainNoise(noiseIndex, 857) * travelWidth - horizontalPadding
        val rawX = baseX + sway - fallProgress * directionalDrift * patternStyle.driftScale
        val x = ((rawX + horizontalPadding) % travelWidth + travelWidth) % travelWidth - horizontalPadding
        val y = fallProgress * travelHeight - maxRadius * 2f
        // Bias strongly toward small flakes; only a few particles should pass close enough to
        // become large foreground blobs.
        val radiusNoise = rainNoise(noiseIndex, 859).pow(2.10f)
        val radiusDp = style.minRadiusDp +
            (style.maxRadiusDp - style.minRadiusDp) * radiusNoise
        val radius = radiusDp.dp.toPx() * patternStyle.radiusScale
        val alpha = (style.alpha * patternStyle.alphaScale * pulse *
            (0.72f + rainNoise(noiseIndex, 863) * 0.28f)).coerceIn(0f, 1f)

        drawSnowFlakeSprite(
            center = Offset(x, y),
            radius = radius,
            alpha = alpha,
            softFocus = !grains && radius >= 1.65.dp.toPx(),
            rotationFraction = rainNoise(noiseIndex, 877),
            motionSkew = when (scene.windLevel) {
                WeatherWindLevel.Calm -> 0.06f
                WeatherWindLevel.Breezy -> 0.24f
                WeatherWindLevel.Windy -> 0.48f
            } + (rainNoise(noiseIndex, 883) - 0.5f) * 0.10f,
        )
    }
}

private fun DrawScope.drawSnowCardImpacts(
    scene: WeatherSceneSpec,
    motionProgress: Float,
    rhythmProgress: Float,
    edgeY: Float,
    cornerRadius: Float,
) {
    val grains = scene.precipitation == WeatherPrecipitation.SnowGrains
    val style = snowCardImpactStyle(
        intensity = scene.precipitationIntensity,
        grains = grains,
    )
    if (style.impactCount == 0 || size.width <= 0f || size.height <= 0f) return

    val patternStyle = snowPatternStyle(
        intensity = scene.precipitationIntensity,
        pattern = scene.precipitationPattern,
        windLevel = scene.windLevel,
    )
    val pulse = rainRhythmPulse(
        progress = rhythmProgress,
        pulseMin = patternStyle.pulseMin,
        pulseMax = patternStyle.pulseMax,
        phaseOffset = 0.17f,
    )
    val resolvedCornerRadius = cornerRadius.coerceIn(0f, size.width / 2f)

    repeat(13) { index ->
        val cellCenterFraction = (index + 0.5f) / 13f
        val clusterStrength = snowEdgeClusterStrength(cellCenterFraction)
        if (clusterStrength <= 0f) return@repeat
        val segmentWidth = (14f + rainNoise(index, 877) * 34f).dp.toPx()
        val jitter = (rainNoise(index, 881) - 0.5f) * size.width / 20f
        val centerX = (cellCenterFraction * size.width + jitter).coerceIn(0f, size.width)
        val startX = (centerX - segmentWidth / 2f).coerceAtLeast(0f)
        val endX = (centerX + segmentWidth / 2f).coerceAtMost(size.width)
        if (endX <= startX) return@repeat
        val path = roundedCardTopEdgePath(
            startX = startX,
            endX = endX,
            edgeY = edgeY,
            cornerRadius = resolvedCornerRadius,
        )
        val capWidth = (0.85f + rainNoise(index, 883) * 1.65f).dp.toPx() *
            style.capWidthScale
        drawPath(
            path = path,
            color = Color(0xFFB9D7E8).copy(
                alpha = style.snowCapAlpha * pulse * clusterStrength * 0.42f,
            ),
            style = Stroke(
                width = capWidth * 1.75f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        drawPath(
            path = path,
            color = Color.White.copy(
                alpha = style.snowCapAlpha * pulse * clusterStrength,
            ),
            style = Stroke(
                width = capWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }

    repeat(style.settledFlakeCount) { index ->
        val noiseIndex = 13_000 + index
        val x = size.width * (0.018f + rainNoise(noiseIndex, 887) * 0.964f)
        val clusterStrength = snowEdgeClusterStrength(x / size.width)
        if (clusterStrength <= 0f) return@repeat
        val impactY = roundedCardTopEdgeY(x, size.width, edgeY, resolvedCornerRadius)
        val radiusScale = if (grains) 0.62f else 1f
        val radius = (0.30f + rainNoise(noiseIndex, 907).pow(1.35f) * 0.92f).dp.toPx() * radiusScale
        drawCircle(
            color = Color.White.copy(
                alpha = style.alpha * pulse * clusterStrength * 0.56f,
            ),
            radius = radius,
            center = Offset(x, impactY - radius * 0.38f),
        )
    }

    val impactCount = (style.impactCount * patternStyle.countScale).roundToInt()
    repeat(impactCount) { index ->
        val noiseIndex = 14_000 + index
        val x = size.width * (0.02f + rainNoise(noiseIndex, 911) * 0.96f)
        val clusterStrength = snowEdgeClusterStrength(x / size.width)
        if (clusterStrength <= 0f) return@repeat
        val impactY = roundedCardTopEdgeY(x, size.width, edgeY, resolvedCornerRadius)
        val age = (motionProgress + rainNoise(noiseIndex, 919)) % 1f
        val radiusScale = if (grains) 0.58f else 1f
        val radius = (0.72f + rainNoise(noiseIndex, 929) * 1.15f).dp.toPx() * radiusScale

        if (age > 0.90f) {
            val approachProgress = (age - 0.90f) / 0.10f
            val distance = (7f + rainNoise(noiseIndex, 937) * 15f).dp.toPx() *
                style.impactEnergy
            val approachAlpha = style.alpha * pulse * clusterStrength *
                sin(approachProgress * PI.toFloat()).pow(0.65f)
            drawCircle(
                color = Color.White.copy(alpha = approachAlpha),
                radius = radius,
                center = Offset(x, impactY - distance * (1f - approachProgress)),
            )
        }

        val lifetime = 0.14f + rainNoise(noiseIndex, 941) * 0.08f
        if (age < lifetime) {
            val lifeProgress = age / lifetime
            val visibility = (1f - lifeProgress).pow(0.82f)
            val bounce = sin(lifeProgress * PI.toFloat()) * radius *
                (1.4f + rainNoise(noiseIndex, 947) * 1.8f) * style.impactEnergy
            val scatter = (rainNoise(noiseIndex, 953) - 0.5f) * radius * 2.8f *
                lifeProgress * style.impactEnergy
            val center = Offset(x + scatter, impactY - radius * 0.32f - bounce)
            val alpha = style.alpha * pulse * clusterStrength * visibility
            drawCircle(
                color = Color(0xFFD9F2FF).copy(alpha = alpha * 0.22f),
                radius = radius * 1.65f,
                center = center,
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = center,
            )
            if (!grains && index % 3 == 0) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.64f),
                    radius = radius * 0.24f,
                    center = center + Offset(radius * 1.25f, -radius * 0.55f),
                )
            }
        }
    }
}

internal fun snowGrainCount(intensity: WeatherIntensity): Int {
    return when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 345
        WeatherIntensity.Moderate -> 630
        WeatherIntensity.Heavy -> 960
    }
}

private fun DrawScope.drawSnowGrains(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
) {
    val count = snowGrainCount(intensity)
    if (count == 0) return
    val windShift = when (windLevel) {
        WeatherWindLevel.Calm -> width * 0.025f
        WeatherWindLevel.Breezy -> width * 0.095f
        WeatherWindLevel.Windy -> width * 0.18f
    }
    repeat(count) { index ->
        val depth = 0.48f + rainNoise(index, 307) * 0.52f
        val initialPhase = rainNoise(index, 311)
        val fallProgress = (initialPhase + motionProgress * (0.86f + depth * 0.72f)) % 1f
        val baseX = rainNoise(index, 313) * width
        val flutter = sin(
            motionProgress * PI.toFloat() * 2f + rainNoise(index, 317) * PI.toFloat() * 2f,
        ) * 9.dp.toPx() * depth
        val rawX = baseX + flutter - fallProgress * windShift * depth
        val x = (rawX % width + width) % width
        val y = fallProgress * height
        val radius = (0.72f + rainNoise(index, 331) * 0.92f).dp.toPx() * depth
        val alpha = 0.42f + depth * 0.36f

        drawCircle(
            color = Color(0xFFE7F7FF).copy(alpha = alpha * 0.18f),
            radius = radius * 1.65f,
            center = Offset(x, y),
        )
        drawCircle(
            color = Color(0xFFF7FCFF).copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.drawPrecipitation(
    scene: WeatherSceneSpec,
    width: Float,
    height: Float,
    motionProgress: Float,
    rainRhythmProgress: Float = motionProgress,
    rainSprite: ImageBitmap? = null,
) {
    when (scene.precipitation) {
        WeatherPrecipitation.None -> Unit
        WeatherPrecipitation.Drizzle -> drawRain(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            pattern = scene.precipitationPattern,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
            rainRhythmProgress = rainRhythmProgress,
            drizzle = true,
            freezing = scene.freezing,
            isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
            rainSprite = rainSprite,
        )
        WeatherPrecipitation.Rain -> drawRain(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            pattern = scene.precipitationPattern,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
            rainRhythmProgress = rainRhythmProgress,
            freezing = scene.freezing,
            isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
            rainSprite = rainSprite,
        )
        WeatherPrecipitation.Snow -> drawSnow(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            pattern = scene.precipitationPattern,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
        )
        WeatherPrecipitation.SnowGrains -> drawSnowGrains(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
        )
        WeatherPrecipitation.Sleet -> {
            drawRain(
                width = width,
                height = height,
                intensity = scene.precipitationIntensity,
                pattern = scene.precipitationPattern,
                windLevel = scene.windLevel,
                motionProgress = motionProgress,
                rainRhythmProgress = rainRhythmProgress,
                freezing = true,
                isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
                rainSprite = rainSprite,
            )
            drawIceParticles(
                width = width,
                height = height,
                intensity = scene.precipitationIntensity,
                isHail = false,
                windLevel = scene.windLevel,
                motionProgress = motionProgress,
            )
        }
        WeatherPrecipitation.Hail -> {
            drawRain(
                width = width,
                height = height,
                intensity = WeatherIntensity.Heavy,
                pattern = WeatherPrecipitationPattern.Showers,
                windLevel = scene.windLevel,
                motionProgress = motionProgress,
                rainRhythmProgress = rainRhythmProgress,
                isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
                rainSprite = rainSprite,
            )
            drawIceParticles(
                width = width,
                height = height,
                intensity = WeatherIntensity.Heavy,
                isHail = true,
                windLevel = scene.windLevel,
                motionProgress = motionProgress,
            )
        }
    }
}

internal fun iceParticleCount(
    intensity: WeatherIntensity,
    isHail: Boolean,
): Int {
    val base = when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 30
        WeatherIntensity.Moderate -> 54
        WeatherIntensity.Heavy -> 84
    }
    return if (isHail) (base * 1.22f).roundToInt() else base
}

private fun DrawScope.drawIceParticles(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    isHail: Boolean,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
) {
    val count = iceParticleCount(intensity = intensity, isHail = isHail)
    if (count == 0) return
    val windShift = when (windLevel) {
        WeatherWindLevel.Calm -> width * 0.025f
        WeatherWindLevel.Breezy -> width * 0.090f
        WeatherWindLevel.Windy -> width * 0.180f
    }
    repeat(count) { index ->
        val depth = 0.52f + rainNoise(index, 347) * 0.48f
        val initialPhase = rainNoise(index, 349)
        val fallScale = if (isHail) 1.25f + depth * 0.72f else 0.88f + depth * 0.48f
        val fallProgress = (initialPhase + motionProgress * fallScale) % 1f
        val baseX = rainNoise(index, 353) * width
        val rawX = baseX - fallProgress * windShift * depth
        val x = (rawX % width + width) % width
        val y = fallProgress * height
        val radiusDp = if (isHail) {
            2.2f + rainNoise(index, 359) * 2.4f
        } else {
            1.0f + rainNoise(index, 359) * 1.45f
        }
        val radius = radiusDp.dp.toPx() * depth
        val center = Offset(x, y)

        if (isHail) {
            drawLine(
                color = Color(0xFFD5F1FF).copy(alpha = 0.34f),
                start = center.copy(
                    x = x + radius * 0.72f,
                    y = y - radius * 3.1f,
                ),
                end = center.copy(
                    x = x + radius * 0.18f,
                    y = y - radius * 0.82f,
                ),
                strokeWidth = radius * 0.42f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = Color(0xFFBDEBFF).copy(alpha = if (isHail) 0.10f else 0.11f),
            radius = radius * if (isHail) 1.32f else 1.48f,
            center = center,
        )
        drawCircle(
            color = Color(0xFFEAF7FF).copy(alpha = if (isHail) 0.90f else 0.72f),
            radius = radius,
            center = center,
        )
        if (isHail) {
            drawCircle(
                color = Color.White.copy(alpha = 0.82f),
                radius = radius * 0.34f,
                center = center.copy(x = x - radius * 0.28f, y = y - radius * 0.28f),
            )
        }
    }
}

internal data class FogBankSpec(
    val centerXFraction: Float,
    val centerYFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
    val alpha: Float,
    val driftFraction: Float,
    val direction: Float,
    val phaseOffset: Float,
)

internal val fogBankSpecs = listOf(
    FogBankSpec(0.32f, 0.15f, 0.90f, 0.075f, 0.085f, 0.026f, 1f, 0.04f),
    FogBankSpec(0.58f, 0.28f, 1.24f, 0.115f, 0.120f, 0.018f, -1f, 0.26f),
    FogBankSpec(0.72f, 0.44f, 0.82f, 0.090f, 0.078f, 0.034f, 1f, 0.51f),
    FogBankSpec(0.43f, 0.61f, 1.42f, 0.155f, 0.135f, 0.022f, -1f, 0.70f),
    FogBankSpec(0.64f, 0.82f, 1.08f, 0.205f, 0.115f, 0.014f, 1f, 0.88f),
)

internal val fogOpacityProfile = listOf(
    0f to 1f,
    0.38f to 0.62f,
    0.72f to 0.16f,
    1f to 0f,
)

private fun DrawScope.drawFog(
    width: Float,
    height: Float,
    motionProgress: Float,
    isDaytime: Boolean,
) {
    val primaryTint = if (isDaytime) Color(0xFFF1F4F4) else Color(0xFFB9C6CD)
    val secondaryTint = if (isDaytime) Color(0xFFD5DEE1) else Color(0xFF8397A2)

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                primaryTint.copy(alpha = if (isDaytime) 0.035f else 0.020f),
                secondaryTint.copy(alpha = if (isDaytime) 0.105f else 0.070f),
            ),
            startY = height * 0.45f,
            endY = height,
        ),
        topLeft = Offset(0f, height * 0.45f),
        size = Size(width, height * 0.55f),
    )

    fogBankSpecs.forEach { bank ->
        val movement = sin((motionProgress + bank.phaseOffset) * 2f * PI.toFloat()) *
            width * bank.driftFraction * bank.direction
        val bankWidth = width * bank.widthFraction
        val bankHeight = height * bank.heightFraction
        val center = Offset(
            x = width * bank.centerXFraction + movement,
            y = height * bank.centerYFraction,
        )

        drawSoftFogBank(
            center = center,
            width = bankWidth,
            height = bankHeight,
            tint = primaryTint,
            alpha = bank.alpha,
        )
    }
}

private fun DrawScope.drawSoftFogBank(
    center: Offset,
    width: Float,
    height: Float,
    tint: Color,
    alpha: Float,
) {
    val radius = width * 0.5f
    val verticalScale = (height / width).coerceAtLeast(0.01f)
    val colorStops = fogOpacityProfile.map { (position, alphaMultiplier) ->
        position to tint.copy(alpha = alpha * alphaMultiplier)
    }.toTypedArray()

    withTransform({
        scale(
            scaleX = 1f,
            scaleY = verticalScale,
            pivot = center,
        )
    }) {
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = colorStops,
                center = center,
                radius = radius,
            ),
            center = center,
            radius = radius,
        )
    }
}

private fun DrawScope.drawThunderstorm(
    width: Float,
    height: Float,
    flash: Float,
    strikeIndex: Int = 2,
) {
    val visibility = lightningVisibility(flash)
    if (visibility <= 0f) return

    val strike = lightningStrikeSpecs[strikeIndex.coerceIn(0, lightningStrikeSpecs.lastIndex)]
    val brightness = (visibility * strike.brightnessScale).coerceAtMost(1f)
    val origin = Offset(
        x = width * strike.originXFraction,
        y = height * strike.originYFraction,
    )
    val glowRadius = width * 0.30f * strike.glowScale
    val glowCenter = origin.copy(y = origin.y + height * 0.045f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE9F3FF).copy(alpha = brightness * 0.18f),
                Color(0xFFB8D7FF).copy(alpha = brightness * 0.065f),
                Color.Transparent,
            ),
            center = glowCenter,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = glowCenter,
    )

    val mainBolt = lightningPath(origin, width, height, strike.mainPath)
    val branches = strike.branchPaths.map { points ->
        lightningPath(origin, width, height, points)
    }

    val roundedGlow = Stroke(
        width = 6.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    val roundedBody = Stroke(
        width = 2.6.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    val roundedCore = Stroke(
        width = 0.9.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    drawPath(
        path = mainBolt,
        color = Color(0xFFB9D9FF).copy(alpha = brightness * 0.10f),
        style = roundedGlow,
    )
    drawPath(
        path = mainBolt,
        color = Color(0xFFE5F2FF).copy(alpha = brightness * 0.56f),
        style = roundedBody,
    )
    drawPath(
        path = mainBolt,
        color = Color(0xFFFCFEFF).copy(alpha = brightness * 0.96f),
        style = roundedCore,
    )
    branches.forEachIndexed { index, branch ->
        val startsAtCloudBase = strike.branchPaths[index].first().yFraction <= 0.001f
        val branchVisibility = brightness * if (startsAtCloudBase) 0.92f else 0.74f
        drawPath(
            path = branch,
            color = Color(0xFFCAE3FF).copy(alpha = branchVisibility * 0.34f),
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        drawPath(
            path = branch,
            color = Color(0xFFF7FCFF).copy(alpha = branchVisibility * 0.78f),
            style = Stroke(
                width = 0.72.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private fun lightningPath(
    origin: Offset,
    width: Float,
    height: Float,
    points: List<LightningPathPoint>,
): Path {
    return Path().apply {
        points.forEachIndexed { index, point ->
            val x = origin.x + width * point.xFraction
            val y = origin.y + height * point.yFraction
            if (index == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
    }
}
