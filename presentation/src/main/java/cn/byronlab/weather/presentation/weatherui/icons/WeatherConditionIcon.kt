package cn.byronlab.weather.presentation.weatherui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The semantic glyphs intentionally mirror the weather conditions supported by Open-Meteo.
 * Keeping this separate from the renderer makes it possible to verify that conditions never
 * silently collapse back to a generic cloud, drop, or snowflake.
 */
internal enum class WeatherConditionIconType {
    Clear,
    WindyClear,
    MainlyClear,
    PartlyCloudy,
    Cloudy,
    Overcast,
    Fog,
    Drizzle,
    FreezingDrizzle,
    LightRain,
    ModerateRain,
    HeavyRain,
    RainShowers,
    HeavyRainShowers,
    ViolentRainShowers,
    FreezingRain,
    LightSnow,
    ModerateSnow,
    HeavySnow,
    SnowGrains,
    SnowShowers,
    HeavySnowShowers,
    Sleet,
    Hail,
    Thunderstorm,
    ThunderstormWithHail,
}

internal fun weatherConditionIconType(scene: WeatherSceneSpec): WeatherConditionIconType {
    return when {
        scene.lightningIntensity == WeatherLightningIntensity.Frequent &&
            scene.precipitation == WeatherPrecipitation.Hail ->
            WeatherConditionIconType.ThunderstormWithHail
        scene.lightningIntensity != WeatherLightningIntensity.None -> WeatherConditionIconType.Thunderstorm
        scene.atmosphere == WeatherAtmosphere.Fog -> WeatherConditionIconType.Fog
        scene.freezing && scene.precipitation == WeatherPrecipitation.Drizzle ->
            WeatherConditionIconType.FreezingDrizzle
        scene.freezing && scene.precipitation == WeatherPrecipitation.Rain ->
            WeatherConditionIconType.FreezingRain
        scene.precipitation == WeatherPrecipitation.Drizzle -> WeatherConditionIconType.Drizzle
        scene.precipitation == WeatherPrecipitation.Rain &&
            scene.precipitationPattern == WeatherPrecipitationPattern.Showers -> when {
            scene.precipitationIntensity == WeatherIntensity.Heavy && scene.windLevel == WeatherWindLevel.Windy ->
                WeatherConditionIconType.ViolentRainShowers
            scene.precipitationIntensity == WeatherIntensity.Heavy -> WeatherConditionIconType.HeavyRainShowers
            else -> WeatherConditionIconType.RainShowers
        }
        scene.precipitation == WeatherPrecipitation.Rain -> when (scene.precipitationIntensity) {
            WeatherIntensity.Light -> WeatherConditionIconType.LightRain
            WeatherIntensity.Heavy -> WeatherConditionIconType.HeavyRain
            WeatherIntensity.None,
            WeatherIntensity.Moderate -> WeatherConditionIconType.ModerateRain
        }
        scene.precipitation == WeatherPrecipitation.Snow &&
            scene.precipitationPattern == WeatherPrecipitationPattern.Flurries ->
            if (scene.precipitationIntensity == WeatherIntensity.Heavy) {
                WeatherConditionIconType.HeavySnowShowers
            } else {
                WeatherConditionIconType.SnowShowers
            }
        scene.precipitation == WeatherPrecipitation.Snow -> when (scene.precipitationIntensity) {
            WeatherIntensity.Light -> WeatherConditionIconType.LightSnow
            WeatherIntensity.Heavy -> WeatherConditionIconType.HeavySnow
            WeatherIntensity.None,
            WeatherIntensity.Moderate -> WeatherConditionIconType.ModerateSnow
        }
        scene.precipitation == WeatherPrecipitation.SnowGrains -> WeatherConditionIconType.SnowGrains
        scene.precipitation == WeatherPrecipitation.Sleet -> WeatherConditionIconType.Sleet
        scene.precipitation == WeatherPrecipitation.Hail -> WeatherConditionIconType.Hail
        scene.cloudCover == WeatherCloudCover.Clear && scene.windLevel == WeatherWindLevel.Windy ->
            WeatherConditionIconType.WindyClear
        scene.cloudCover == WeatherCloudCover.Clear -> WeatherConditionIconType.Clear
        scene.cloudCover == WeatherCloudCover.MostlyClear -> WeatherConditionIconType.MainlyClear
        scene.cloudCover == WeatherCloudCover.PartlyCloudy -> WeatherConditionIconType.PartlyCloudy
        scene.cloudCover == WeatherCloudCover.Cloudy -> WeatherConditionIconType.Cloudy
        else -> WeatherConditionIconType.Overcast
    }
}

/**
 * A compact, multi-layer weather glyph. It is drawn rather than tinted as a single vector so
 * cloud depth, precipitation density, freezing, lightning, and day/night remain legible at 18dp.
 */
@Composable
internal fun WeatherConditionIcon(
    scene: WeatherSceneSpec,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawWeatherConditionIcon(
            type = weatherConditionIconType(scene),
            phase = scene.skyPhase,
        )
    }
}

private val Sun = Color(0xFFFFC857)
private val SunCore = Color(0xFFFFA93B)
private val Moon = Color(0xFFF1F4EF)
private val CloudLight = Color(0xFFF2F7FC)
private val CloudMid = Color(0xFFC7D5E2)
private val CloudDark = Color(0xFF879BAE)
private val CloudOutline = Color(0xFF637A90)
private val Rain = Color(0xFF55BDF3)
private val RainDeep = Color(0xFF278ED5)
private val Snow = Color(0xFFDDF5FF)
private val Ice = Color(0xFF79D5F2)
private val Lightning = Color(0xFFFFD85C)
private val Wind = Color(0xFF8DB8D3)

private data class GlyphGeometry(
    val origin: Offset,
    val unit: Float,
) {
    fun point(x: Float, y: Float): Offset = Offset(origin.x + x * unit, origin.y + y * unit)
    fun size(width: Float, height: Float): Size = Size(width * unit, height * unit)
}

private fun DrawScope.glyphGeometry(): GlyphGeometry {
    val side = min(size.width, size.height)
    return GlyphGeometry(
        origin = Offset((size.width - side) / 2f, (size.height - side) / 2f),
        unit = side / 100f,
    )
}

private fun DrawScope.drawWeatherConditionIcon(
    type: WeatherConditionIconType,
    phase: WeatherSkyPhase,
) {
    val g = glyphGeometry()
    when (type) {
        WeatherConditionIconType.Clear -> drawCelestial(
            g = g,
            phase = phase,
            center = g.point(50f, 50f),
            radius = (if (phase == WeatherSkyPhase.Night) 28f else 23f) * g.unit,
        )
        WeatherConditionIconType.WindyClear -> {
            drawCelestial(g, phase, center = g.point(40f, 40f), radius = 18f * g.unit)
            drawWind(g, y = 63f, strong = true)
        }
        WeatherConditionIconType.MainlyClear -> {
            drawCelestial(g, phase, center = g.point(38f, 38f), radius = 17f * g.unit)
            drawCloud(g, centerX = 57f, bottomY = 72f, scale = 0.78f, color = CloudLight)
        }
        WeatherConditionIconType.PartlyCloudy -> {
            drawCelestial(g, phase, center = g.point(34f, 34f), radius = 16f * g.unit)
            drawCloud(g, centerX = 57f, bottomY = 69f, scale = 0.94f, color = CloudLight)
        }
        WeatherConditionIconType.Cloudy -> {
            drawCloud(g, centerX = 45f, bottomY = 58f, scale = 0.75f, color = CloudMid)
            drawCloud(g, centerX = 56f, bottomY = 75f, scale = 0.98f, color = CloudLight)
        }
        WeatherConditionIconType.Overcast -> {
            drawCloud(g, centerX = 44f, bottomY = 57f, scale = 0.78f, color = CloudDark)
            drawCloud(g, centerX = 56f, bottomY = 75f, scale = 1.02f, color = CloudMid)
        }
        WeatherConditionIconType.Fog -> {
            drawCloud(g, centerX = 50f, bottomY = 58f, scale = 0.86f, color = CloudMid)
            drawFog(g)
        }
        WeatherConditionIconType.Drizzle -> {
            drawWeatherCloud(g, phase, CloudMid, revealSky = true)
            drawDrizzle(g, freezing = false)
        }
        WeatherConditionIconType.FreezingDrizzle -> {
            drawWeatherCloud(g, phase, CloudMid, revealSky = true)
            drawDrizzle(g, freezing = true)
        }
        WeatherConditionIconType.LightRain -> {
            drawWeatherCloud(g, phase, CloudMid)
            drawRain(g, count = 2, heavy = false, angled = false)
        }
        WeatherConditionIconType.ModerateRain -> {
            drawWeatherCloud(g, phase, CloudMid)
            drawRain(g, count = 3, heavy = false, angled = false)
        }
        WeatherConditionIconType.HeavyRain -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawRain(g, count = 4, heavy = true, angled = false)
        }
        WeatherConditionIconType.RainShowers -> {
            drawWeatherCloud(g, phase, CloudMid, revealSky = true)
            drawRain(g, count = 3, heavy = false, angled = true)
        }
        WeatherConditionIconType.HeavyRainShowers -> {
            drawWeatherCloud(g, phase, CloudDark, revealSky = true)
            drawRain(g, count = 4, heavy = true, angled = true)
        }
        WeatherConditionIconType.ViolentRainShowers -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawWind(g, y = 58f, strong = true)
            drawRain(g, count = 4, heavy = true, angled = true, yOffset = 5f)
        }
        WeatherConditionIconType.FreezingRain -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawRain(g, count = 3, heavy = false, angled = false)
            drawIceDiamond(g, center = g.point(78f, 82f), radius = 7f * g.unit)
        }
        WeatherConditionIconType.LightSnow -> {
            drawWeatherCloud(g, phase, CloudMid)
            drawSnow(g, count = 2, heavy = false, shower = false)
        }
        WeatherConditionIconType.ModerateSnow -> {
            drawWeatherCloud(g, phase, CloudMid)
            drawSnow(g, count = 3, heavy = false, shower = false)
        }
        WeatherConditionIconType.HeavySnow -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawSnow(g, count = 4, heavy = true, shower = false)
        }
        WeatherConditionIconType.SnowGrains -> {
            drawWeatherCloud(g, phase, CloudMid)
            drawSnowGrains(g)
        }
        WeatherConditionIconType.SnowShowers -> {
            drawWeatherCloud(g, phase, CloudMid, revealSky = true)
            drawSnow(g, count = 3, heavy = false, shower = true)
        }
        WeatherConditionIconType.HeavySnowShowers -> {
            drawWeatherCloud(g, phase, CloudDark, revealSky = true)
            drawWind(g, y = 59f, strong = true)
            drawSnow(g, count = 4, heavy = true, shower = true, yOffset = 5f)
        }
        WeatherConditionIconType.Sleet -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawRain(g, count = 2, heavy = false, angled = true)
            drawSnowflake(g, g.point(69f, 82f), 7f * g.unit, Ice, 3.2f * g.unit)
        }
        WeatherConditionIconType.Hail -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawHail(g, withLightning = false)
        }
        WeatherConditionIconType.Thunderstorm -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawLightning(g, centerX = 50f)
            drawRain(g, count = 2, heavy = true, angled = true, sideGap = true)
        }
        WeatherConditionIconType.ThunderstormWithHail -> {
            drawWeatherCloud(g, phase, CloudDark)
            drawLightning(g, centerX = 47f)
            drawHail(g, withLightning = true)
        }
    }
}

private fun DrawScope.drawWeatherCloud(
    g: GlyphGeometry,
    phase: WeatherSkyPhase,
    color: Color,
    revealSky: Boolean = false,
) {
    if (revealSky) {
        drawCelestial(g, phase, center = g.point(31f, 28f), radius = 12f * g.unit, compact = true)
    }
    drawCloud(g, centerX = 50f, bottomY = 63f, scale = 1f, color = color)
}

private fun DrawScope.drawCelestial(
    g: GlyphGeometry,
    phase: WeatherSkyPhase,
    center: Offset,
    radius: Float,
    compact: Boolean = false,
) {
    if (phase == WeatherSkyPhase.Day) {
        val rayStart = radius * 1.35f
        val rayEnd = radius * 1.70f
        val stroke = (radius * 0.13f).coerceAtLeast(1.4f * g.unit)
        repeat(if (compact) 6 else 8) { index ->
            val angle = (2.0 * PI * index / if (compact) 6 else 8).toFloat()
            drawLine(
                color = Sun,
                start = center + Offset(cos(angle) * rayStart, sin(angle) * rayStart),
                end = center + Offset(cos(angle) * rayEnd, sin(angle) * rayEnd),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = Sun, radius = radius, center = center)
        drawCircle(color = SunCore.copy(alpha = 0.65f), radius = radius * 0.58f, center = center)
    } else {
        val outerMoon = Path().apply {
            addOval(
                Rect(
                    center = center,
                    radius = radius,
                ),
            )
        }
        val cutoutCenter = center + Offset(radius * 0.48f, -radius * 0.22f)
        val cutoutRadius = radius * 0.84f
        val moonCutout = Path().apply {
            addOval(
                Rect(
                    center = cutoutCenter,
                    radius = cutoutRadius,
                ),
            )
        }
        val moonPath = Path.combine(
            operation = PathOperation.Difference,
            path1 = outerMoon,
            path2 = moonCutout,
        )
        drawPath(moonPath, Moon)
    }
}

private fun DrawScope.drawCloud(
    g: GlyphGeometry,
    centerX: Float,
    bottomY: Float,
    scale: Float,
    color: Color,
) {
    fun p(x: Float, y: Float): Offset = g.point(centerX + (x - 50f) * scale, bottomY + (y - 63f) * scale)
    val path = Path().apply {
        val start = p(24f, 63f)
        moveTo(start.x, start.y)
        val one = p(14f, 55f)
        val two = p(20f, 46f)
        cubicTo(one.x, one.y, one.x, one.y - 7f * g.unit * scale, two.x, two.y)
        val three = p(26f, 35f)
        val four = p(42f, 37f)
        cubicTo(three.x, three.y, p(35f, 34f).x, p(35f, 34f).y, four.x, four.y)
        val five = p(52f, 24f)
        val six = p(69f, 40f)
        cubicTo(five.x, five.y, p(68f, 25f).x, p(68f, 25f).y, six.x, six.y)
        val seven = p(85f, 47f)
        val eight = p(76f, 63f)
        cubicTo(seven.x, seven.y, p(86f, 59f).x, p(86f, 59f).y, eight.x, eight.y)
        close()
    }
    val outline = (2.7f * g.unit * scale).coerceAtLeast(1f)
    drawPath(path, color)
    drawPath(
        path = path,
        color = CloudOutline.copy(alpha = if (color == CloudLight) 0.72f else 0.9f),
        style = Stroke(width = outline, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    drawArc(
        color = Color.White.copy(alpha = if (color == CloudDark) 0.22f else 0.48f),
        startAngle = 205f,
        sweepAngle = 76f,
        useCenter = false,
        topLeft = p(39f, 29f),
        size = g.size(22f * scale, 22f * scale),
        style = Stroke(width = 2.4f * g.unit * scale, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawDrizzle(g: GlyphGeometry, freezing: Boolean) {
    val dots = listOf(g.point(34f, 76f), g.point(49f, 82f), g.point(64f, 76f))
    dots.forEach { drawCircle(color = Rain, radius = 3.6f * g.unit, center = it) }
    drawLine(
        color = RainDeep,
        start = g.point(43f, 71f),
        end = g.point(40f, 78f),
        strokeWidth = 2.8f * g.unit,
        cap = StrokeCap.Round,
    )
    if (freezing) {
        drawIceDiamond(g, g.point(73f, 84f), 6.5f * g.unit)
    }
}

private fun DrawScope.drawRain(
    g: GlyphGeometry,
    count: Int,
    heavy: Boolean,
    angled: Boolean,
    yOffset: Float = 0f,
    sideGap: Boolean = false,
) {
    val allX = when (count) {
        2 -> listOf(39f, 61f)
        3 -> listOf(31f, 50f, 69f)
        else -> listOf(27f, 42f, 58f, 73f)
    }.filterNot { sideGap && it in 42f..58f }
    allX.forEachIndexed { index, x ->
        val stagger = if (index % 2 == 0) 0f else 4f
        val start = g.point(x + if (angled) 4f else 0f, 71f + yOffset + stagger)
        val end = g.point(x - if (angled) 4f else 2f, (if (heavy) 91f else 87f) + yOffset + stagger)
        drawLine(
            color = if (heavy) RainDeep else Rain,
            start = start,
            end = end,
            strokeWidth = (if (heavy) 5f else 3.7f) * g.unit,
            cap = StrokeCap.Round,
        )
        if (heavy) {
            drawLine(
                color = Rain.copy(alpha = 0.85f),
                start = start,
                end = start + (end - start) * 0.42f,
                strokeWidth = 1.5f * g.unit,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.drawSnow(
    g: GlyphGeometry,
    count: Int,
    heavy: Boolean,
    shower: Boolean,
    yOffset: Float = 0f,
) {
    val positions = when (count) {
        2 -> listOf(39f to 79f, 63f to 83f)
        3 -> listOf(31f to 78f, 50f to 86f, 69f to 77f)
        else -> listOf(27f to 77f, 42f to 88f, 59f to 77f, 74f to 88f)
    }
    positions.forEach { (x, y) ->
        drawSnowflake(
            g = g,
            center = g.point(x - if (shower) 3f else 0f, y + yOffset),
            radius = (if (heavy) 6.2f else 5.2f) * g.unit,
            color = if (heavy) Ice else Snow,
            strokeWidth = (if (heavy) 2.8f else 2.35f) * g.unit,
        )
    }
}

private fun DrawScope.drawSnowflake(
    g: GlyphGeometry,
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
) {
    repeat(3) { index ->
        val angle = (PI * index / 3.0).toFloat()
        val delta = Offset(cos(angle) * radius, sin(angle) * radius)
        drawLine(
            color = color,
            start = center - delta,
            end = center + delta,
            strokeWidth = strokeWidth.coerceAtLeast(1.2f * g.unit),
            cap = StrokeCap.Round,
        )
    }
    drawCircle(color = color, radius = strokeWidth * 0.62f, center = center)
}

private fun DrawScope.drawSnowGrains(g: GlyphGeometry) {
    val grains = listOf(
        g.point(31f, 77f) to 3.6f,
        g.point(45f, 86f) to 4.2f,
        g.point(59f, 77f) to 3.4f,
        g.point(72f, 87f) to 3.8f,
    )
    grains.forEach { (center, radius) ->
        drawCircle(color = Ice, radius = radius * g.unit, center = center)
        drawCircle(
            color = CloudOutline.copy(alpha = 0.65f),
            radius = radius * g.unit,
            center = center,
            style = Stroke(width = 1.4f * g.unit),
        )
    }
}

private fun DrawScope.drawFog(g: GlyphGeometry) {
    listOf(
        Triple(20f, 78f, 66f),
        Triple(33f, 87f, 78f),
        Triple(22f, 96f, 59f),
    ).forEachIndexed { index, (startX, y, endX) ->
        drawLine(
            color = if (index == 1) CloudOutline else Wind,
            start = g.point(startX, y),
            end = g.point(endX, y),
            strokeWidth = 4f * g.unit,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawWind(g: GlyphGeometry, y: Float, strong: Boolean) {
    val width = if (strong) 65f else 52f
    val stroke = if (strong) 4.2f else 3.4f
    drawLine(
        color = Wind,
        start = g.point(18f, y),
        end = g.point(18f + width, y),
        strokeWidth = stroke * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Wind.copy(alpha = 0.78f),
        start = g.point(29f, y + 12f),
        end = g.point(73f, y + 12f),
        strokeWidth = 3.2f * g.unit,
        cap = StrokeCap.Round,
    )
    if (strong) {
        drawLine(
            color = Wind.copy(alpha = 0.62f),
            start = g.point(16f, y + 23f),
            end = g.point(55f, y + 23f),
            strokeWidth = 2.6f * g.unit,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawLightning(g: GlyphGeometry, centerX: Float) {
    val path = Path().apply {
        moveTo(g.point(centerX + 4f, 65f).x, g.point(centerX + 4f, 65f).y)
        lineTo(g.point(centerX - 10f, 82f).x, g.point(centerX - 10f, 82f).y)
        lineTo(g.point(centerX - 1f, 82f).x, g.point(centerX - 1f, 82f).y)
        lineTo(g.point(centerX - 7f, 98f).x, g.point(centerX - 7f, 98f).y)
        lineTo(g.point(centerX + 13f, 77f).x, g.point(centerX + 13f, 77f).y)
        lineTo(g.point(centerX + 4f, 77f).x, g.point(centerX + 4f, 77f).y)
        close()
    }
    drawPath(path, Lightning)
    drawPath(
        path = path,
        color = SunCore.copy(alpha = 0.78f),
        style = Stroke(width = 1.6f * g.unit, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawHail(g: GlyphGeometry, withLightning: Boolean) {
    val positions = if (withLightning) {
        listOf(g.point(27f, 78f), g.point(73f, 82f), g.point(31f, 92f), g.point(69f, 95f))
    } else {
        listOf(g.point(30f, 78f), g.point(50f, 86f), g.point(70f, 78f), g.point(39f, 95f), g.point(62f, 95f))
    }
    positions.forEachIndexed { index, center ->
        val radius = (if (index % 2 == 0) 4.4f else 3.7f) * g.unit
        drawCircle(color = Snow, radius = radius, center = center)
        drawCircle(
            color = Ice,
            radius = radius,
            center = center,
            style = Stroke(width = 1.8f * g.unit),
        )
        drawCircle(color = Color.White.copy(alpha = 0.75f), radius = radius * 0.28f, center = center - Offset(radius * 0.28f, radius * 0.28f))
    }
}

private fun DrawScope.drawIceDiamond(g: GlyphGeometry, center: Offset, radius: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.78f, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.78f, center.y)
        close()
    }
    drawPath(path, Snow)
    drawPath(path, Ice, style = Stroke(width = 2f * g.unit, join = StrokeJoin.Round))
    drawLine(
        color = Ice,
        start = center - Offset(0f, radius * 0.52f),
        end = center + Offset(0f, radius * 0.52f),
        strokeWidth = 1.4f * g.unit,
        cap = StrokeCap.Round,
    )
}
