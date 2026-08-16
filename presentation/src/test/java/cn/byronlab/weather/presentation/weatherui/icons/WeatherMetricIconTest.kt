package cn.byronlab.weather.presentation.weatherui.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMetricIconTest {

    @Test
    fun `weather detail cards have distinct semantic icons`() {
        val titles = listOf("体感温度", "湿度", "风速", "能见度", "气压", "紫外线指数")

        assertEquals(titles.size, titles.map(::weatherDetailIconType).distinct().size)
    }

    @Test
    fun `summary metrics have separate temperature and sun event icons`() {
        val summaryIcons = listOf(
            WeatherMetricIconType.HighTemperature,
            WeatherMetricIconType.LowTemperature,
            WeatherMetricIconType.Sunrise,
            WeatherMetricIconType.Sunset,
        )

        assertEquals(4, summaryIcons.distinct().size)
    }
}
