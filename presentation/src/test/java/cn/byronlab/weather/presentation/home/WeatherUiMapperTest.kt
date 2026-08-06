package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.AirQuality
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.ForecastDay
import cn.byronlab.weather.domain.model.HourlyForecast
import cn.byronlab.weather.domain.model.LifeIndex
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.model.WeatherCondition
import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.FallbackWeatherScene
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUiMapperTest {

    private val mapper = WeatherUiMapper()

    @Test
    fun mapBuildsHomeUiModelFromDomainWeather() {
        val weather = Weather(
            city = city(),
            currentWeather = CurrentWeather(
                "101010100", WeatherCondition.Clear, "26", "40", "东风", "3",
                0L, "3级", "0", "27", "1000",
            ),
            forecastDays = listOf(
                ForecastDay(
                    "101010100", WeatherCondition.Unknown, WeatherCondition.Clear,
                    WeatherCondition.Cloudy, 30, 20, "东风", "08.01", "周六",
                    "20", "强", "12", "40", "1000", "0", "05:10", "19:20", "", "",
                ),
            ),
            hourlyForecasts = hourlyForecasts(),
            airQuality = AirQuality(
                "101010100", 42, 8, 20, "", "适合户外活动", "", "", "", "", "", "", "PM2.5",
            ),
            lifeIndexes = listOf(LifeIndex("101010100", "穿衣", "舒适", "建议短袖")),
        )

        val uiModel = mapper.map(weather)

        assertEquals("北京", uiModel.cityName)
        assertEquals("26", uiModel.currentTemperature)
        assertEquals("27", uiModel.feelsLikeTemperature)
        assertEquals(30, uiModel.highTemperature)
        assertEquals(20, uiModel.lowTemperature)
        assertEquals(42, uiModel.aqi)
        assertEquals("主要污染物: PM2.5", uiModel.cityRank)
        assertEquals("PM2.5", uiModel.primaryPollutant)
        assertEquals("PM2.5", uiModel.pollutants.first().label)
        assertEquals("8", uiModel.pollutants.first().value)
        assertEquals("20", uiModel.pollutants[1].value)
        assertEquals(WeatherSceneSpec(), uiModel.scene)
        assertEquals("30°", uiModel.todaySummary.high)
        assertEquals("05:10", uiModel.todaySummary.sunrise)
        assertEquals(24, uiModel.hourlyForecasts.size)
        assertEquals("Now", uiModel.hourlyForecasts[0].time)
        assertEquals(10, uiModel.hourlyForecasts[0].temperature)
        assertEquals("17:00", uiModel.hourlyForecasts[1].time)
        assertEquals(11, uiModel.hourlyForecasts[1].temperature)
        assertEquals("19:00", uiModel.hourlyForecasts[3].time)
        assertEquals("小雨", uiModel.hourlyForecasts[3].conditionText)
        assertEquals(WeatherPrecipitation.Rain, uiModel.hourlyForecasts[3].scene.precipitation)
        assertEquals("01:00", uiModel.hourlyForecasts[9].time)
        assertEquals("15:00", uiModel.hourlyForecasts[23].time)
        assertEquals(33, uiModel.hourlyForecasts[23].temperature)
        assertEquals("晴转多云", uiModel.forecasts.first().conditionText)
        assertEquals("体感温度", uiModel.details.first().title)
        assertEquals("27°", uiModel.details.first().value)
        assertEquals("3 km/h", uiModel.details[2].value)
        assertEquals("东风", uiModel.details[2].subtitle)
        assertEquals("舒适", uiModel.lifeIndexes.first().level)
    }

    @Test
    fun mapPreservesNighttimeStateForSceneSelection() {
        val weather = Weather(
            city = city(),
            currentWeather = CurrentWeather(
                "101010100", WeatherCondition.Clear, "18", "40", "东风", "3",
                0L, "3级", "0", "18", "1000", isDay = false,
            ),
            airQuality = null,
        )

        val uiModel = mapper.map(weather)

        assertEquals(WeatherSkyPhase.Night, uiModel.scene.skyPhase)
        assertEquals(WeatherCloudCover.Clear, uiModel.scene.cloudCover)
    }

    @Test
    fun mapDistinguishesPartlyCloudyCloudyAndOvercastConditions() {
        val expectedCloudCovers = listOf(
            WeatherCondition.PartlyCloudy to WeatherCloudCover.PartlyCloudy,
            WeatherCondition.MainlyClear to WeatherCloudCover.MostlyClear,
            WeatherCondition.Cloudy to WeatherCloudCover.Cloudy,
            WeatherCondition.Overcast to WeatherCloudCover.Overcast,
            WeatherCondition.Fog to WeatherCloudCover.Overcast,
        )

        expectedCloudCovers.forEach { (condition, expectedCloudCover) ->
            val weather = Weather(
                city = city(),
                currentWeather = CurrentWeather(
                    "101010100", condition, "18", "40", "东风", "3",
                    0L, "3级", "0", "18", "1000",
                ),
                airQuality = null,
            )

            assertEquals(expectedCloudCover, mapper.map(weather).scene.cloudCover)
        }
    }

    @Test
    fun mapBuildsIndependentAtmosphereAndPrecipitationScenes() {
        val expectedScenes = listOf(
            WeatherCondition.Fog to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                atmosphere = WeatherAtmosphere.Fog,
            ),
            WeatherCondition.Drizzle to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Drizzle,
                precipitationIntensity = WeatherIntensity.Light,
            ),
            WeatherCondition.LightRain to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Light,
            ),
            WeatherCondition.ModerateRain to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
            ),
            WeatherCondition.ViolentRainShowers to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                windLevel = WeatherWindLevel.Windy,
            ),
            WeatherCondition.FreezingRain to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
                freezing = true,
            ),
            WeatherCondition.ThunderstormWithHail to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Hail,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                lightningIntensity = WeatherLightningIntensity.Frequent,
                windLevel = WeatherWindLevel.Windy,
            ),
            WeatherCondition.HeavySnow to WeatherSceneSpec(
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Heavy,
            ),
        )

        expectedScenes.forEach { (condition, expectedScene) ->
            val weather = Weather(
                city = city(),
                currentWeather = CurrentWeather(
                    "101010100", condition, "18", "40", "东风", "3",
                    0L, "3级", "0", "18", "1000",
                ),
                airQuality = null,
            )

            assertEquals(condition.name, expectedScene, mapper.map(weather).scene)
        }
    }

    @Test
    fun mapDistinguishesSteadyShoweryAndFreezingRain() {
        val steadyRain = sceneFor(WeatherCondition.ModerateRain)
        val rainShower = sceneFor(WeatherCondition.RainShowers)
        val freezingRain = sceneFor(WeatherCondition.FreezingRain)

        assertEquals(WeatherPrecipitationPattern.Steady, steadyRain.precipitationPattern)
        assertEquals(WeatherPrecipitationPattern.Showers, rainShower.precipitationPattern)
        assertEquals(WeatherCloudCover.Cloudy, rainShower.cloudCover)
        assertEquals(WeatherWindLevel.Breezy, rainShower.windLevel)
        assertTrue(freezingRain.freezing)
    }

    @Test
    fun mapDistinguishesSteadySnowSnowGrainsAndSnowShowers() {
        val steadySnow = sceneFor(WeatherCondition.ModerateSnow)
        val snowGrains = sceneFor(WeatherCondition.SnowGrains)
        val snowShower = sceneFor(WeatherCondition.SnowShowers)

        assertEquals(WeatherPrecipitation.Snow, steadySnow.precipitation)
        assertEquals(WeatherPrecipitationPattern.Steady, steadySnow.precipitationPattern)
        assertEquals(WeatherPrecipitation.SnowGrains, snowGrains.precipitation)
        assertEquals(WeatherPrecipitationPattern.Flurries, snowGrains.precipitationPattern)
        assertEquals(WeatherPrecipitation.Snow, snowShower.precipitation)
        assertEquals(WeatherPrecipitationPattern.Flurries, snowShower.precipitationPattern)
        assertEquals(WeatherWindLevel.Breezy, snowShower.windLevel)
    }

    @Test
    fun mapUsesCurrentWindSpeedToDriveSceneMotion() {
        assertEquals(WeatherWindLevel.Calm, sceneFor(WeatherCondition.Clear, windSpeed = "4").windLevel)
        assertEquals(WeatherWindLevel.Breezy, sceneFor(WeatherCondition.Clear, windSpeed = "18").windLevel)
        assertEquals(WeatherWindLevel.Windy, sceneFor(WeatherCondition.Clear, windSpeed = "36").windLevel)
    }

    @Test
    fun mapHandlesMissingOptionalWeatherBlocks() {
        val weather = Weather(
            city = city(),
            currentWeather = null,
            airQuality = null,
        )

        val uiModel = mapper.map(weather)

        assertEquals("", uiModel.currentTemperature)
        assertEquals(0, uiModel.aqi)
        assertEquals("主要污染物: --", uiModel.cityRank)
        assertEquals("", uiModel.primaryPollutant)
        assertTrue(uiModel.pollutants.all { it.value == "--" })
        assertEquals(FallbackWeatherScene, uiModel.scene)
        assertEquals("--", uiModel.todaySummary.high)
        assertTrue(uiModel.hourlyForecasts.isEmpty())
        assertTrue(uiModel.forecasts.isEmpty())
        assertTrue(uiModel.lifeIndexes.isEmpty())
    }

    @Test
    fun mapUsesDomainCityLocationData() {
        val weather = Weather(
            city = city(),
            currentWeather = null,
            airQuality = null,
        )

        val uiModel = mapper.map(weather)

        assertEquals("39.90°N  116.41°E", uiModel.coordinatesText)
        assertEquals("中国", uiModel.citySubtitle)
    }

    private fun city(): City = City(
        cityId = "101010100",
        name = "北京",
        nameEn = "Beijing",
        root = "中国",
        parent = "北京 · 中国",
        longitude = "116.4074",
        latitude = "39.9042",
    )

    private fun sceneFor(
        condition: WeatherCondition,
        windSpeed: String = "3",
    ): WeatherSceneSpec {
        val weather = Weather(
            city = city(),
            currentWeather = CurrentWeather(
                "101010100", condition, "18", "40", "东风", windSpeed,
                0L, "3级", "0", "18", "1000",
            ),
            airQuality = null,
        )
        return mapper.map(weather).scene
    }

    private fun hourlyForecasts(): List<HourlyForecast> {
        val dateTimes = List(24) { index ->
            val hour = (16 + index) % 24
            val date = if (index < 8) "2026-08-03" else "2026-08-04"
            "${date}T${hour.toString().padStart(2, '0')}:00"
        }
        return dateTimes.mapIndexed { index, dateTime ->
            HourlyForecast(
                cityId = "101010100",
                dateTime = dateTime,
                condition = if (index == 3) WeatherCondition.LightRain else WeatherCondition.Clear,
                temperature = 10 + index,
            )
        }
    }
}
