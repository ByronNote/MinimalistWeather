package cn.byronlab.weather.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeatherColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006B55),
    onPrimary = Color.White,
    secondary = Color(0xFF705D00),
    onSecondary = Color.White,
    tertiary = Color(0xFF8E3D4F),
    surface = Color(0xFFFAFCF8),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFE6EAE3),
    onSurfaceVariant = Color(0xFF43483F),
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
