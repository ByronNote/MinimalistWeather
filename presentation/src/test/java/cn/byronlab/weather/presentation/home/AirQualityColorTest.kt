package cn.byronlab.weather.presentation.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AirQualityColorTest {

    @Test
    fun `gauge color follows the same gradient positions as the scale`() {
        assertEquals(Color(0xFF51E0D0), airQualityColor(0))
        assertEquals(Color(0xFF63D59D), airQualityColor(60))
        assertEquals(Color(0xFFFFD166), airQualityColor(120))
        assertEquals(Color(0xFFFF9F5A), airQualityColor(180))
        assertEquals(Color(0xFFFF6B6B), airQualityColor(240))
        assertEquals(Color(0xFFE957C2), airQualityColor(300))
    }

    @Test
    fun `aqi values outside the scale are clamped to its endpoints`() {
        assertEquals(Color(0xFF51E0D0), airQualityColor(-1))
        assertEquals(Color(0xFFE957C2), airQualityColor(500))
    }
}
