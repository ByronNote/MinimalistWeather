package cn.byronlab.weather.presentation.weatherui.icons

import cn.byronlab.weather.presentation.home.WeatherUiTestCatalog
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionIconTest {

    @Test
    fun `every weather test type resolves to a distinct icon`() {
        val dayIcons = WeatherUiTestCatalog.scenarios
            .filter { it.isDay }
            .associate { scenario ->
                scenario.weatherName to weatherConditionIconType(
                    WeatherUiTestCatalog.createPreview(scenario).scene,
                )
            }

        assertEquals(24, dayIcons.size)
        assertEquals(24, dayIcons.values.distinct().size)
    }

    @Test
    fun `every semantic icon keeps separate day and night scene variants`() {
        WeatherUiTestCatalog.scenarios
            .groupBy { it.weatherName }
            .values
            .forEach { scenarios ->
                val scenes = scenarios.map { WeatherUiTestCatalog.createPreview(it).scene }

                assertEquals(2, scenes.map { it.skyPhase }.distinct().size)
                assertEquals(setOf(WeatherSkyPhase.Day, WeatherSkyPhase.Night), scenes.map { it.skyPhase }.toSet())
                assertEquals(1, scenes.map(::weatherConditionIconType).distinct().size)
            }
    }
}
