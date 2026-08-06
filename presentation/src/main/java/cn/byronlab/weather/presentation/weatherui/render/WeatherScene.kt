package cn.byronlab.weather.presentation.weatherui.render

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

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

@Composable
private fun WeatherSceneContent(scene: WeatherSceneSpec) {
    val style = weatherVisualStyle(scene)
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
        WeatherPrecipitation.Drizzle -> 3_200
        WeatherPrecipitation.Rain -> when {
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Showers &&
                windLevel == WeatherWindLevel.Windy -> 1_480
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Showers -> 1_720
            precipitationIntensity == WeatherIntensity.None -> 12_000
            precipitationIntensity == WeatherIntensity.Light -> 2_800
            precipitationIntensity == WeatherIntensity.Moderate -> 2_400
            else -> 2_050
        }
        WeatherPrecipitation.Snow -> when {
            precipitationIntensity == WeatherIntensity.Heavy &&
                precipitationPattern == WeatherPrecipitationPattern.Flurries -> 2_350
            precipitationIntensity == WeatherIntensity.Heavy -> 3_350
            precipitationIntensity == WeatherIntensity.Moderate &&
                precipitationPattern == WeatherPrecipitationPattern.Flurries -> 3_150
            precipitationIntensity == WeatherIntensity.Moderate -> 4_100
            else -> 5_200
        }
        WeatherPrecipitation.SnowGrains -> 2_550
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
    val (primaryAlpha, secondaryAlpha) = when (scene.precipitationIntensity) {
        WeatherIntensity.None -> 0.42f to 0.24f
        WeatherIntensity.Light -> 0.52f to 0.28f
        WeatherIntensity.Moderate -> 0.72f to 0.46f
        WeatherIntensity.Heavy -> 0.82f to 0.58f
    }
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
                    alpha = 0.34f,
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
                WeatherIntensity.None -> 0.20f
                WeatherIntensity.Light -> 0.25f
                WeatherIntensity.Moderate -> 0.36f
                WeatherIntensity.Heavy -> 0.44f
            }
            val showerAdjustment = if (scene.precipitationPattern == WeatherPrecipitationPattern.Showers) {
                -0.04f
            } else {
                0f
            }
            drawRect(Color(0xFF132C47).copy(alpha = (alpha + showerAdjustment).coerceAtLeast(0f)), size = size)
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

internal val rainLayerSpecs = listOf(
    RainLayerSpec(0.50f, 0.38f, 0.38f, 0.56f, 1),
    RainLayerSpec(0.33f, 0.68f, 0.62f, 0.80f, 2),
    RainLayerSpec(0.17f, 1.00f, 0.96f, 1.00f, 3),
)

internal fun rainVisualStyle(
    intensity: WeatherIntensity,
    drizzle: Boolean,
): RainVisualStyle {
    return when (intensity) {
        WeatherIntensity.None -> RainVisualStyle(
            dropCount = 0,
            lengthDp = 0f,
            alpha = 0f,
            veilAlpha = 0f,
        )
        WeatherIntensity.Light -> if (drizzle) {
            RainVisualStyle(
                dropCount = 120,
                lengthDp = 7f,
                alpha = 0.38f,
                veilAlpha = 0.008f,
            )
        } else {
            RainVisualStyle(
                dropCount = 145,
                lengthDp = 12.5f,
                alpha = 0.45f,
                veilAlpha = 0.014f,
            )
        }
        WeatherIntensity.Moderate -> RainVisualStyle(
            dropCount = 205,
            lengthDp = 17f,
            alpha = 0.55f,
            veilAlpha = 0.026f,
        )
        WeatherIntensity.Heavy -> RainVisualStyle(
            dropCount = 280,
            lengthDp = 22f,
            alpha = 0.65f,
            veilAlpha = 0.044f,
        )
    }
}

internal fun rainPatternStyle(
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
): RainPatternStyle {
    if (pattern != WeatherPrecipitationPattern.Showers) {
        return RainPatternStyle(
            countScale = 1f,
            lengthScale = 1f,
            alphaScale = 1f,
            veilScale = 1f,
            pulseMin = 1f,
            pulseMax = 1f,
        )
    }
    return when {
        intensity == WeatherIntensity.Heavy && windLevel == WeatherWindLevel.Windy -> RainPatternStyle(
            countScale = 1.48f,
            lengthScale = 1.24f,
            alphaScale = 1.18f,
            veilScale = 1.90f,
            pulseMin = 1.00f,
            pulseMax = 1.32f,
        )
        intensity == WeatherIntensity.Heavy -> RainPatternStyle(
            countScale = 1.20f,
            lengthScale = 1.10f,
            alphaScale = 1.08f,
            veilScale = 1.40f,
            pulseMin = 0.94f,
            pulseMax = 1.18f,
        )
        else -> RainPatternStyle(
            countScale = 1f,
            lengthScale = 1f,
            alphaScale = 1f,
            veilScale = 1.08f,
            pulseMin = 0.88f,
            pulseMax = 1.10f,
        )
    }
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
    drizzle: Boolean = false,
    freezing: Boolean = false,
    isDaytime: Boolean,
) {
    val style = rainVisualStyle(intensity = intensity, drizzle = drizzle)
    if (style.dropCount == 0) return

    val patternStyle = rainPatternStyle(
        intensity = intensity,
        pattern = pattern,
        windLevel = windLevel,
    )
    val showerPulse = if (pattern == WeatherPrecipitationPattern.Showers) {
        val pulseProgress = (sin(motionProgress * PI * 2.0).toFloat() + 1f) / 2f
        patternStyle.pulseMin + (patternStyle.pulseMax - patternStyle.pulseMin) * pulseProgress
    } else {
        1f
    }
    val baseSkew = when (windLevel) {
        WeatherWindLevel.Calm -> 0.06f
        WeatherWindLevel.Breezy -> 0.16f
        WeatherWindLevel.Windy -> 0.28f
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
                    alpha = style.veilAlpha * patternStyle.veilScale * showerPulse * 0.48f,
                ),
                1f to rainColor.copy(
                    alpha = style.veilAlpha * patternStyle.veilScale * showerPulse,
                ),
            ),
            startY = 0f,
            endY = height,
        ),
        size = Size(width, height),
    )

    rainLayerSpecs.forEachIndexed { layerIndex, layer ->
        val count = (style.dropCount * patternStyle.countScale * layer.countFraction).roundToInt()
        val baseLength = style.lengthDp.dp.toPx() * patternStyle.lengthScale * layer.lengthScale
        val maxLength = baseLength * 1.20f
        val travelHeight = height + maxLength * 2f
        val horizontalPadding = maxLength * 2f
        val travelWidth = width + horizontalPadding * 2f

        repeat(count) { dropIndex ->
            val noiseIndex = layerIndex * 1_000 + dropIndex
            val initialPhase = rainNoise(noiseIndex, 17)
            val fallProgress = (initialPhase + motionProgress * layer.fallCycles) % 1f
            val length = baseLength * (0.72f + rainNoise(noiseIndex, 31) * 0.48f)
            val skew = baseSkew * (0.86f + rainNoise(noiseIndex, 47) * 0.28f)
            val baseX = rainNoise(noiseIndex, 71) * travelWidth - horizontalPadding
            val rawX = baseX - fallProgress * height * skew
            val x = ((rawX + horizontalPadding) % travelWidth + travelWidth) % travelWidth - horizontalPadding
            val y = fallProgress * travelHeight - maxLength
            val strokeWidth = layer.strokeWidthDp.dp.toPx() *
                (0.84f + rainNoise(noiseIndex, 89) * 0.30f)
            val dropAlpha = style.alpha * patternStyle.alphaScale * layer.alphaScale * showerPulse *
                (0.72f + rainNoise(noiseIndex, 107) * 0.28f)
            val start = Offset(x, y)
            val end = Offset(x - length * skew, y + length)

            drawLine(
                color = rainColor.copy(alpha = dropAlpha * 0.78f),
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            if (layerIndex == rainLayerSpecs.lastIndex && dropIndex % 2 == 0) {
                val headStart = Offset(
                    x = start.x + (end.x - start.x) * 0.62f,
                    y = start.y + (end.y - start.y) * 0.62f,
                )
                drawLine(
                    color = rainColor.copy(alpha = dropAlpha * 0.40f),
                    start = headStart,
                    end = end,
                    strokeWidth = strokeWidth * 0.58f,
                    cap = StrokeCap.Round,
                )
            }
        }
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

internal val snowLayerSpecs = listOf(
    SnowLayerSpec(0.52f, 0.62f, 0.54f, 0.72f, 0.68f),
    SnowLayerSpec(0.32f, 0.88f, 0.78f, 0.96f, 0.94f),
    SnowLayerSpec(0.16f, 1.18f, 1.00f, 1.22f, 1.24f),
)

internal fun snowVisualStyle(intensity: WeatherIntensity): SnowVisualStyle {
    return when (intensity) {
        WeatherIntensity.None -> SnowVisualStyle(0, 0f, 0f, 0f, 0f)
        WeatherIntensity.Light -> SnowVisualStyle(78, 0.95f, 2.4f, 0.64f, 0.012f)
        WeatherIntensity.Moderate -> SnowVisualStyle(138, 1.05f, 3.0f, 0.74f, 0.025f)
        WeatherIntensity.Heavy -> SnowVisualStyle(220, 1.15f, 3.5f, 0.84f, 0.046f)
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
            countScale = 1.45f,
            radiusScale = 1.12f,
            alphaScale = 1.14f,
            veilScale = 1.80f,
            driftScale = 1.45f,
            pulseMin = 0.96f,
            pulseMax = 1.30f,
        )
        intensity == WeatherIntensity.Heavy -> SnowPatternStyle(
            countScale = 1.28f,
            radiusScale = 1.08f,
            alphaScale = 1.08f,
            veilScale = 1.45f,
            driftScale = 1.25f,
            pulseMin = 0.90f,
            pulseMax = 1.22f,
        )
        else -> SnowPatternStyle(
            countScale = 1.12f,
            radiusScale = 1.04f,
            alphaScale = 1.04f,
            veilScale = 1.22f,
            driftScale = 1.15f,
            pulseMin = 0.84f,
            pulseMax = 1.14f,
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

            if (layerIndex == snowLayerSpecs.lastIndex) {
                drawCircle(
                    color = Color(0xFFDFF4FF).copy(alpha = alpha * 0.16f),
                    radius = radius * 1.85f,
                    center = Offset(x, y),
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y),
            )
            if (layerIndex == snowLayerSpecs.lastIndex && flakeIndex % 3 == 0) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.72f),
                    radius = radius * 0.42f,
                    center = Offset(x - radius * 0.22f, y - radius * 0.22f),
                )
            }
        }
    }
}

internal fun snowGrainCount(intensity: WeatherIntensity): Int {
    return when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 96
        WeatherIntensity.Moderate -> 136
        WeatherIntensity.Heavy -> 182
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
            drizzle = true,
            freezing = scene.freezing,
            isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
        )
        WeatherPrecipitation.Rain -> drawRain(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            pattern = scene.precipitationPattern,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
            freezing = scene.freezing,
            isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
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
                freezing = true,
                isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
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
                isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
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
