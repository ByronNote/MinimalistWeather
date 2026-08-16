package cn.byronlab.weather.presentation.weatherui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal enum class WeatherMetricIconType {
    HighTemperature,
    LowTemperature,
    Sunrise,
    Sunset,
    FeelsLike,
    Humidity,
    Wind,
    Visibility,
    Pressure,
    UvIndex,
}

internal fun weatherDetailIconType(title: String): WeatherMetricIconType {
    return when (title) {
        "体感温度" -> WeatherMetricIconType.FeelsLike
        "湿度" -> WeatherMetricIconType.Humidity
        "风速" -> WeatherMetricIconType.Wind
        "能见度" -> WeatherMetricIconType.Visibility
        "气压" -> WeatherMetricIconType.Pressure
        else -> WeatherMetricIconType.UvIndex
    }
}

@Composable
internal fun WeatherMetricIcon(
    type: WeatherMetricIconType,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val side = min(size.width, size.height)
        val origin = Offset((size.width - side) / 2f, (size.height - side) / 2f)
        val g = MetricGeometry(origin = origin, unit = side / 100f)
        when (type) {
            WeatherMetricIconType.HighTemperature -> drawTemperature(g, tint, rising = true, sensed = false)
            WeatherMetricIconType.LowTemperature -> drawTemperature(g, tint, rising = false, sensed = false)
            WeatherMetricIconType.Sunrise -> drawSunEvent(g, tint, rising = true)
            WeatherMetricIconType.Sunset -> drawSunEvent(g, tint, rising = false)
            WeatherMetricIconType.FeelsLike -> drawTemperature(g, tint, rising = null, sensed = true)
            WeatherMetricIconType.Humidity -> drawHumidity(g, tint)
            WeatherMetricIconType.Wind -> drawWind(g, tint)
            WeatherMetricIconType.Visibility -> drawVisibility(g, tint)
            WeatherMetricIconType.Pressure -> drawPressure(g, tint)
            WeatherMetricIconType.UvIndex -> drawUvIndex(g, tint)
        }
    }
}

private data class MetricGeometry(
    val origin: Offset,
    val unit: Float,
) {
    fun point(x: Float, y: Float): Offset = Offset(origin.x + x * unit, origin.y + y * unit)
}

private fun DrawScope.drawTemperature(
    g: MetricGeometry,
    color: Color,
    rising: Boolean?,
    sensed: Boolean,
) {
    val stroke = 7f * g.unit
    val bulbCenter = g.point(if (sensed) 43f else 40f, 73f)
    drawLine(
        color = color.copy(alpha = 0.72f),
        start = g.point(if (sensed) 43f else 40f, 25f),
        end = bulbCenter,
        strokeWidth = 20f * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.92f),
        start = g.point(if (sensed) 43f else 40f, 27f),
        end = g.point(if (sensed) 43f else 40f, 68f),
        strokeWidth = 10f * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = g.point(if (sensed) 43f else 40f, 47f),
        end = bulbCenter,
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    drawCircle(color = color, radius = 14f * g.unit, center = bulbCenter)
    drawCircle(
        color = color.copy(alpha = 0.92f),
        radius = 22f * g.unit,
        center = bulbCenter,
        style = Stroke(width = 4f * g.unit),
    )

    if (sensed) {
        listOf(28f, 44f, 60f).forEachIndexed { index, y ->
            val path = Path().apply {
                moveTo(g.point(67f, y).x, g.point(67f, y).y)
                cubicTo(
                    g.point(83f, y - 7f).x,
                    g.point(83f, y - 7f).y,
                    g.point(83f, y + 7f).x,
                    g.point(83f, y + 7f).y,
                    g.point(68f, y + 9f).x,
                    g.point(68f, y + 9f).y,
                )
            }
            drawPath(
                path = path,
                color = color.copy(alpha = 0.9f - index * 0.15f),
                style = Stroke(width = 4f * g.unit, cap = StrokeCap.Round),
            )
        }
    } else if (rising != null) {
        val arrowX = 72f
        val startY = if (rising) 70f else 28f
        val endY = if (rising) 28f else 70f
        drawLine(
            color = color,
            start = g.point(arrowX, startY),
            end = g.point(arrowX, endY),
            strokeWidth = 6f * g.unit,
            cap = StrokeCap.Round,
        )
        val direction = if (rising) 1f else -1f
        drawLine(
            color = color,
            start = g.point(arrowX, endY),
            end = g.point(arrowX - 10f, endY + 10f * direction),
            strokeWidth = 6f * g.unit,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = g.point(arrowX, endY),
            end = g.point(arrowX + 10f, endY + 10f * direction),
            strokeWidth = 6f * g.unit,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSunEvent(g: MetricGeometry, color: Color, rising: Boolean) {
    val center = g.point(43f, 63f)
    val radius = 17f * g.unit
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = center - Offset(radius, radius),
        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
        style = Stroke(width = 6f * g.unit, cap = StrokeCap.Round),
    )
    listOf(-1f, 0f, 1f).forEach { direction ->
        val angle = (-90f + direction * 42f) * PI.toFloat() / 180f
        drawLine(
            color = color.copy(alpha = 0.82f),
            start = center + Offset(cos(angle) * radius * 1.38f, sin(angle) * radius * 1.38f),
            end = center + Offset(cos(angle) * radius * 1.72f, sin(angle) * radius * 1.72f),
            strokeWidth = 4f * g.unit,
            cap = StrokeCap.Round,
        )
    }
    drawLine(
        color = color,
        start = g.point(11f, 64f),
        end = g.point(74f, 64f),
        strokeWidth = 5f * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color.copy(alpha = 0.52f),
        start = g.point(21f, 78f),
        end = g.point(65f, 78f),
        strokeWidth = 4f * g.unit,
        cap = StrokeCap.Round,
    )
    val arrowX = 84f
    val arrowStartY = if (rising) 69f else 25f
    val arrowEndY = if (rising) 25f else 69f
    drawLine(
        color = color,
        start = g.point(arrowX, arrowStartY),
        end = g.point(arrowX, arrowEndY),
        strokeWidth = 5.5f * g.unit,
        cap = StrokeCap.Round,
    )
    val direction = if (rising) 1f else -1f
    drawLine(
        color = color,
        start = g.point(arrowX, arrowEndY),
        end = g.point(arrowX - 9f, arrowEndY + 9f * direction),
        strokeWidth = 5.5f * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = g.point(arrowX, arrowEndY),
        end = g.point(arrowX + 9f, arrowEndY + 9f * direction),
        strokeWidth = 5.5f * g.unit,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawHumidity(g: MetricGeometry, color: Color) {
    val path = Path().apply {
        moveTo(g.point(50f, 10f).x, g.point(50f, 10f).y)
        cubicTo(
            g.point(43f, 28f).x,
            g.point(43f, 28f).y,
            g.point(24f, 47f).x,
            g.point(24f, 47f).y,
            g.point(24f, 66f).x,
            g.point(24f, 66f).y,
        )
        cubicTo(
            g.point(24f, 86f).x,
            g.point(24f, 86f).y,
            g.point(40f, 95f).x,
            g.point(40f, 95f).y,
            g.point(50f, 95f).x,
            g.point(50f, 95f).y,
        )
        cubicTo(
            g.point(71f, 95f).x,
            g.point(71f, 95f).y,
            g.point(78f, 78f).x,
            g.point(78f, 78f).y,
            g.point(76f, 64f).x,
            g.point(76f, 64f).y,
        )
        cubicTo(
            g.point(73f, 45f).x,
            g.point(73f, 45f).y,
            g.point(57f, 28f).x,
            g.point(57f, 28f).y,
            g.point(50f, 10f).x,
            g.point(50f, 10f).y,
        )
        close()
    }
    drawPath(path, color.copy(alpha = 0.24f))
    drawPath(path, color, style = Stroke(width = 5f * g.unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawArc(
        color = Color.White.copy(alpha = 0.78f),
        startAngle = 130f,
        sweepAngle = 72f,
        useCenter = false,
        topLeft = g.point(35f, 48f),
        size = androidx.compose.ui.geometry.Size(29f * g.unit, 31f * g.unit),
        style = Stroke(width = 5f * g.unit, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawWind(g: MetricGeometry, color: Color) {
    val paths = listOf(
        listOf(g.point(12f, 29f), g.point(66f, 29f), g.point(78f, 20f), g.point(89f, 29f), g.point(80f, 40f)),
        listOf(g.point(8f, 51f), g.point(72f, 51f)),
        listOf(g.point(20f, 73f), g.point(68f, 73f), g.point(78f, 82f), g.point(68f, 91f)),
    )
    paths.forEachIndexed { index, points ->
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }
        drawPath(
            path = path,
            color = color.copy(alpha = 1f - index * 0.16f),
            style = Stroke(width = 6f * g.unit, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun DrawScope.drawVisibility(g: MetricGeometry, color: Color) {
    val eye = Path().apply {
        moveTo(g.point(7f, 51f).x, g.point(7f, 51f).y)
        cubicTo(
            g.point(28f, 20f).x,
            g.point(28f, 20f).y,
            g.point(72f, 20f).x,
            g.point(72f, 20f).y,
            g.point(93f, 51f).x,
            g.point(93f, 51f).y,
        )
        cubicTo(
            g.point(72f, 82f).x,
            g.point(72f, 82f).y,
            g.point(28f, 82f).x,
            g.point(28f, 82f).y,
            g.point(7f, 51f).x,
            g.point(7f, 51f).y,
        )
        close()
    }
    drawPath(eye, color.copy(alpha = 0.18f))
    drawPath(eye, color, style = Stroke(width = 5f * g.unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(color = color.copy(alpha = 0.25f), radius = 17f * g.unit, center = g.point(50f, 51f))
    drawCircle(color = color, radius = 10f * g.unit, center = g.point(50f, 51f))
    drawCircle(color = Color.White.copy(alpha = 0.86f), radius = 3f * g.unit, center = g.point(46f, 47f))
}

private fun DrawScope.drawPressure(g: MetricGeometry, color: Color) {
    val center = g.point(50f, 54f)
    val radius = 37f * g.unit
    drawCircle(color = color.copy(alpha = 0.16f), radius = radius, center = center)
    drawArc(
        color = color,
        startAngle = 145f,
        sweepAngle = 250f,
        useCenter = false,
        topLeft = center - Offset(radius, radius),
        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
        style = Stroke(width = 6f * g.unit, cap = StrokeCap.Round),
    )
    repeat(7) { index ->
        val angle = (150f + index * 40f) * PI.toFloat() / 180f
        val start = center + Offset(cos(angle) * radius * 0.72f, sin(angle) * radius * 0.72f)
        val end = center + Offset(cos(angle) * radius * 0.88f, sin(angle) * radius * 0.88f)
        drawLine(color, start, end, 3.2f * g.unit, StrokeCap.Round)
    }
    val needleAngle = 318f * PI.toFloat() / 180f
    drawLine(
        color = color,
        start = center,
        end = center + Offset(cos(needleAngle) * radius * 0.62f, sin(needleAngle) * radius * 0.62f),
        strokeWidth = 5f * g.unit,
        cap = StrokeCap.Round,
    )
    drawCircle(color = color, radius = 7f * g.unit, center = center)
    drawLine(
        color = color.copy(alpha = 0.64f),
        start = g.point(32f, 94f),
        end = g.point(68f, 94f),
        strokeWidth = 4f * g.unit,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawUvIndex(g: MetricGeometry, color: Color) {
    val center = g.point(43f, 42f)
    val radius = 17f * g.unit
    repeat(8) { index ->
        val angle = (2f * PI.toFloat() * index / 8f)
        drawLine(
            color = color,
            start = center + Offset(cos(angle) * radius * 1.42f, sin(angle) * radius * 1.42f),
            end = center + Offset(cos(angle) * radius * 1.78f, sin(angle) * radius * 1.78f),
            strokeWidth = 4f * g.unit,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(color = color.copy(alpha = 0.26f), radius = radius, center = center)
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 5f * g.unit))
    val shield = Path().apply {
        moveTo(g.point(67f, 48f).x, g.point(67f, 48f).y)
        lineTo(g.point(88f, 56f).x, g.point(88f, 56f).y)
        lineTo(g.point(86f, 75f).x, g.point(86f, 75f).y)
        cubicTo(
            g.point(83f, 87f).x,
            g.point(83f, 87f).y,
            g.point(74f, 93f).x,
            g.point(74f, 93f).y,
            g.point(67f, 96f).x,
            g.point(67f, 96f).y,
        )
        cubicTo(
            g.point(60f, 92f).x,
            g.point(60f, 92f).y,
            g.point(50f, 85f).x,
            g.point(50f, 85f).y,
            g.point(49f, 73f).x,
            g.point(49f, 73f).y,
        )
        lineTo(g.point(48f, 56f).x, g.point(48f, 56f).y)
        close()
    }
    drawPath(shield, color.copy(alpha = 0.26f))
    drawPath(shield, color, style = Stroke(width = 5f * g.unit, join = StrokeJoin.Round))
    drawLine(
        color = color,
        start = g.point(59f, 71f),
        end = g.point(65f, 79f),
        strokeWidth = 4.5f * g.unit,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = g.point(65f, 79f),
        end = g.point(78f, 65f),
        strokeWidth = 4.5f * g.unit,
        cap = StrokeCap.Round,
    )
}
