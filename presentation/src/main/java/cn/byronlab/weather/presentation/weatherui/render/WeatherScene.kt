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
                drawFog(size.width, size.height, cloudMotionProgress)
            }
            if (scene.lightningIntensity != WeatherLightningIntensity.None) {
                drawThunderstorm(
                    width = size.width,
                    height = size.height,
                    flash = lightningFlash,
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

private fun WeatherSceneSpec.precipitationMotionDurationMillis(): Int {
    return when (precipitation) {
        WeatherPrecipitation.None -> 12_000
        WeatherPrecipitation.Drizzle -> 2_200
        WeatherPrecipitation.Rain -> 1_250
        WeatherPrecipitation.Snow -> 4_600
        WeatherPrecipitation.SnowGrains -> 2_800
        WeatherPrecipitation.Sleet -> 2_500
        WeatherPrecipitation.Hail -> 1_500
    }
}

private fun WeatherSceneSpec.lightningAnimation() = when (lightningIntensity) {
    WeatherLightningIntensity.None -> keyframes {
        durationMillis = 8_000
        0f at 0
        0f at 8_000
    }
    WeatherLightningIntensity.Occasional -> keyframes {
        durationMillis = 6_800
        0f at 0
        0f at 4_900
        0.92f at 5_020
        0.08f at 5_150
        0.62f at 5_280
        0f at 5_460
        0f at 6_800
    }
    WeatherLightningIntensity.Frequent -> keyframes {
        durationMillis = 4_600
        0f at 0
        0f at 2_650
        1f at 2_760
        0.12f at 2_900
        0.82f at 3_020
        0f at 3_220
        0f at 4_600
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
        drawRect(Color(0xFFDCE4E9).copy(alpha = 0.18f), size = size)
        return
    }
    if (scene.lightningIntensity != WeatherLightningIntensity.None) {
        drawRect(Color(0xFF071426).copy(alpha = 0.48f), size = size)
        if (lightningFlash > 0f) {
            drawRect(
                Color(0xFFDDEBFF).copy(alpha = lightningFlash * 0.16f),
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
        drawRect(Color(0xFFB4C0C9).copy(alpha = 0.48f), size = size)
        drawFog(width, height, motionProgress = 0.35f)
        drawWeatherPhotoOverlays(style)
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
            drawCloud(Offset(width * 0.16f, height * 0.22f), 1.24f, Color.White.copy(alpha = 0.46f))
            drawCloud(Offset(width * 0.74f, height * 0.29f), 1.42f, Color.White.copy(alpha = 0.38f))
            drawCloud(Offset(width * 0.45f, height * 0.15f), 1.02f, Color.White.copy(alpha = 0.30f))
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

private fun DrawScope.drawRain(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    pattern: WeatherPrecipitationPattern,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
    drizzle: Boolean = false,
    freezing: Boolean = false,
) {
    val baseCount = when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> if (drizzle) 18 else 24
        WeatherIntensity.Moderate -> 40
        WeatherIntensity.Heavy -> 62
    }
    val count = if (pattern == WeatherPrecipitationPattern.Showers) {
        (baseCount * 0.86f).toInt()
    } else {
        baseCount
    }
    val length = when {
        drizzle -> 10.dp.toPx()
        intensity == WeatherIntensity.Heavy -> 31.dp.toPx()
        intensity == WeatherIntensity.Light -> 18.dp.toPx()
        else -> 24.dp.toPx()
    }
    val strokeWidth = when {
        drizzle -> 0.9.dp.toPx()
        intensity == WeatherIntensity.Heavy -> 1.8.dp.toPx()
        else -> 1.35.dp.toPx()
    }
    val alpha = when (intensity) {
        WeatherIntensity.None -> 0f
        WeatherIntensity.Light -> 0.30f
        WeatherIntensity.Moderate -> 0.42f
        WeatherIntensity.Heavy -> 0.54f
    }
    val safeWidth = width.toInt().coerceAtLeast(1)
    val travelHeight = height + length * 2f
    val showerPulse = if (pattern == WeatherPrecipitationPattern.Showers) {
        0.58f + 0.42f * ((sin(motionProgress * PI * 2.0).toFloat() + 1f) / 2f)
    } else {
        1f
    }
    val slant = when (windLevel) {
        WeatherWindLevel.Calm -> 0.26f
        WeatherWindLevel.Breezy -> 0.48f
        WeatherWindLevel.Windy -> 0.74f
    }
    repeat(count) { index ->
        if (pattern == WeatherPrecipitationPattern.Showers && (index * 37) % 10 >= 8) {
            return@repeat
        }
        val speedVariance = 0.88f + (index % 4) * 0.06f
        val startY = ((index * 97) % travelHeight.toInt().coerceAtLeast(1)).toFloat()
        val y = (startY + motionProgress * travelHeight * speedVariance) % travelHeight - length
        val windShift = motionProgress * width * when (windLevel) {
            WeatherWindLevel.Calm -> 0.01f
            WeatherWindLevel.Breezy -> 0.035f
            WeatherWindLevel.Windy -> 0.07f
        }
        val x = (((index * 41) % safeWidth).toFloat() - windShift + width) % width
        drawLine(
            color = (if (freezing) Color(0xFFE5F7FF) else Color(0xFFC7EAFF))
                .copy(alpha = alpha * showerPulse),
            start = Offset(x, y),
            end = Offset(x - length * slant, y + length),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
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
    val count = when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 22
        WeatherIntensity.Moderate -> 38
        WeatherIntensity.Heavy -> 58
    }
    val safeWidth = width.toInt().coerceAtLeast(1)
    val safeHeight = height.toInt().coerceAtLeast(1)
    val windAmplitude = when (windLevel) {
        WeatherWindLevel.Calm -> 8.dp.toPx()
        WeatherWindLevel.Breezy -> 20.dp.toPx()
        WeatherWindLevel.Windy -> 38.dp.toPx()
    }
    val showerPulse = if (pattern == WeatherPrecipitationPattern.Flurries) {
        0.66f + 0.34f * ((sin(motionProgress * PI * 2.0).toFloat() + 1f) / 2f)
    } else {
        1f
    }
    repeat(count) { index ->
        val baseX = ((index * 53) % safeWidth).toFloat()
        val baseY = ((index * 89) % safeHeight).toFloat()
        val fallProgress = (motionProgress * (0.72f + (index % 5) * 0.07f)) % 1f
        val y = (baseY + fallProgress * height) % height
        val drift = sin((motionProgress * PI * 2.0) + index * 0.73).toFloat() * windAmplitude
        val directionalShift = motionProgress * windAmplitude * if (windLevel == WeatherWindLevel.Windy) 1.8f else 0.7f
        val x = (baseX + drift - directionalShift + width) % width
        drawCircle(
            color = Color.White.copy(
                alpha = (if (intensity == WeatherIntensity.Heavy) 0.68f else 0.54f) * showerPulse,
            ),
            radius = (1.5f + (index % 3)).dp.toPx(),
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.drawSnowGrains(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    windLevel: WeatherWindLevel,
    motionProgress: Float,
) {
    val count = when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 28
        WeatherIntensity.Moderate -> 42
        WeatherIntensity.Heavy -> 56
    }
    val windShift = when (windLevel) {
        WeatherWindLevel.Calm -> width * 0.01f
        WeatherWindLevel.Breezy -> width * 0.04f
        WeatherWindLevel.Windy -> width * 0.08f
    }
    repeat(count) { index ->
        val x = (((index * 71) % width.toInt().coerceAtLeast(1)).toFloat() - motionProgress * windShift + width) % width
        val y = (((index * 113) % height.toInt().coerceAtLeast(1)).toFloat() + motionProgress * height) % height
        drawCircle(
            color = Color(0xFFF4FAFF).copy(alpha = 0.64f),
            radius = (0.9f + (index % 2) * 0.45f).dp.toPx(),
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
        )
        WeatherPrecipitation.Rain -> drawRain(
            width = width,
            height = height,
            intensity = scene.precipitationIntensity,
            pattern = scene.precipitationPattern,
            windLevel = scene.windLevel,
            motionProgress = motionProgress,
            freezing = scene.freezing,
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
            )
            drawIceParticles(
                width = width,
                height = height,
                intensity = scene.precipitationIntensity,
                isHail = false,
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
            )
            drawIceParticles(
                width = width,
                height = height,
                intensity = WeatherIntensity.Heavy,
                isHail = true,
                motionProgress = motionProgress,
            )
        }
    }
}

private fun DrawScope.drawIceParticles(
    width: Float,
    height: Float,
    intensity: WeatherIntensity,
    isHail: Boolean,
    motionProgress: Float,
) {
    val count = when (intensity) {
        WeatherIntensity.None -> 0
        WeatherIntensity.Light -> 12
        WeatherIntensity.Moderate -> 20
        WeatherIntensity.Heavy -> 32
    }
    val safeWidth = width.toInt().coerceAtLeast(1)
    val safeHeight = height.toInt().coerceAtLeast(1)
    repeat(count) { index ->
        val x = ((index * 67 + 23) % safeWidth).toFloat()
        val startY = ((index * 109 + 37) % safeHeight).toFloat()
        val y = (startY + motionProgress * height * if (isHail) 1.45f else 0.9f) % height
        val radius = if (isHail) {
            (2.2f + (index % 2)).dp.toPx()
        } else {
            (1.2f + (index % 2)).dp.toPx()
        }
        drawCircle(
            color = Color(0xFFEAF7FF).copy(alpha = if (isHail) 0.82f else 0.62f),
            radius = radius,
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.drawFog(
    width: Float,
    height: Float,
    motionProgress: Float,
) {
    val bandHeight = height * 0.12f
    listOf(0.16f, 0.30f, 0.47f, 0.66f, 0.84f).forEachIndexed { index, fraction ->
        val alpha = if (index % 2 == 0) 0.18f else 0.13f
        drawOval(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFF4F7F8).copy(alpha = alpha),
                    Color(0xFFE9EEF1).copy(alpha = alpha + 0.04f),
                    Color.Transparent,
                ),
            ),
            topLeft = Offset(
                x = -width * 0.18f + (motionProgress * 2f - 1f) * width * 0.035f * if (index % 2 == 0) 1f else -1f,
                y = height * fraction,
            ),
            size = Size(width * 1.36f, bandHeight),
        )
    }
}

private fun DrawScope.drawThunderstorm(
    width: Float,
    height: Float,
    flash: Float,
) {
    val origin = Offset(width * 0.78f, height * 0.14f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE8F1FF).copy(alpha = 0.08f + flash * 0.20f),
                Color.Transparent,
            ),
            center = origin,
            radius = width * 0.24f,
        ),
        radius = width * 0.24f,
        center = origin,
    )
    val bolt = Path().apply {
        moveTo(origin.x + width * 0.03f, origin.y)
        lineTo(origin.x - width * 0.025f, origin.y + height * 0.055f)
        lineTo(origin.x + width * 0.012f, origin.y + height * 0.055f)
        lineTo(origin.x - width * 0.038f, origin.y + height * 0.13f)
        lineTo(origin.x + width * 0.06f, origin.y + height * 0.035f)
        lineTo(origin.x + width * 0.018f, origin.y + height * 0.036f)
        close()
    }
    drawPath(bolt, Color(0xFFF4E9A8).copy(alpha = 0.08f + flash * 0.42f))
    drawPath(
        path = bolt,
        color = Color(0xFFFFF5BE).copy(alpha = 0.12f + flash * 0.72f),
        style = Stroke(width = 1.2.dp.toPx(), join = StrokeJoin.Round),
    )
}
