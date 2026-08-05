package cn.byronlab.weather.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class AddedCityListTest {

    @Test
    fun `temperature range keeps both high and low values`() {
        assertEquals(
            "最高 28°  最低 17°",
            addedCityTemperatureRange(highTemperature = 28, lowTemperature = 17),
        )
    }

    @Test
    fun `temperature range preserves missing values`() {
        assertEquals(
            "最高 --  最低 --",
            addedCityTemperatureRange(highTemperature = null, lowTemperature = null),
        )
        assertEquals(
            "最高 21°  最低 --",
            addedCityTemperatureRange(highTemperature = 21, lowTemperature = null),
        )
    }
}
