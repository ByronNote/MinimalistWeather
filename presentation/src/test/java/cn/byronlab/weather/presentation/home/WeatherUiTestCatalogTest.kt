package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.WeatherCondition
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUiTestCatalogTest {

    @Test
    fun `catalog covers every supported weather condition`() {
        val coveredConditions = WeatherUiTestCatalog.scenarios.mapTo(mutableSetOf()) { it.condition }
        val supportedConditions = WeatherCondition.entries
            .filterNot { it == WeatherCondition.Unknown }
            .toSet()

        assertEquals(supportedConditions, coveredConditions)
    }

    @Test
    fun `every weather test type has separate day and night scenarios`() {
        val scenariosByWeather = WeatherUiTestCatalog.scenarios.groupBy { it.weatherName }

        assertEquals(24, scenariosByWeather.size)
        assertEquals(48, WeatherUiTestCatalog.scenarios.size)
        assertEquals(48, WeatherUiTestCatalog.scenarios.map { it.id }.distinct().size)
        scenariosByWeather.values.forEach { scenarios ->
            assertEquals(setOf(true, false), scenarios.mapTo(mutableSetOf()) { it.isDay })
        }
    }

    @Test
    fun `preview is deterministic and complete without live weather data`() {
        val nightScenario = WeatherUiTestCatalog.find("thunderstorm-hail-night")!!
        val preview = WeatherUiTestCatalog.createPreview(nightScenario)

        assertEquals("天气测试", preview.cityName)
        assertEquals("雷暴伴冰雹", preview.currentCondition)
        assertEquals(WeatherSkyPhase.Night, preview.scene.skyPhase)
        assertEquals(24, preview.hourlyForecasts.size)
        assertEquals(7, preview.forecasts.size)
        assertTrue(preview.details.isNotEmpty())
        assertTrue(preview.pollutants.isNotEmpty())
        assertTrue(preview.hourlyForecasts.all { it.scene.skyPhase == WeatherSkyPhase.Night })
    }
}
