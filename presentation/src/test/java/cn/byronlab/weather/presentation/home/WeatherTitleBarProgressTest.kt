package cn.byronlab.weather.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherTitleBarProgressTest {

    @Test
    fun `title bar starts fully expanded`() {
        assertEquals(0f, weatherTitleBarProgress(0, 0, 180), 0f)
        assertEquals(0f, weatherTitleBarBackgroundAlpha(0f), 0f)
    }

    @Test
    fun `title bar changes continuously through transition`() {
        assertEquals(0.5f, weatherTitleBarProgress(0, 90, 180), 0f)
        assertEquals(0.45f, weatherTitleBarBackgroundAlpha(0.5f), 0.001f)
    }

    @Test
    fun `title bar finishes collapsed after transition distance`() {
        assertEquals(1f, weatherTitleBarProgress(0, 180, 180), 0f)
        assertEquals(1f, weatherTitleBarProgress(1, 0, 180), 0f)
        assertEquals(0.90f, weatherTitleBarBackgroundAlpha(1f), 0f)
    }
}
