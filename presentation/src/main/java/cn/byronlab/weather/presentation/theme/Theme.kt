package cn.byronlab.weather.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeatherColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    tertiary = Color(0xFFF97316),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFBA1A1A),
)

@Composable
fun MinimalistWeatherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WeatherColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
