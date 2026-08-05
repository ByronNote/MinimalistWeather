package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.WeatherCondition
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoParserMapperTest {

    private val parser = OpenMeteoJsonParser()

    @Test
    fun parseGeocoding_returnsEmptyResultsWhenResultsMissing() {
        val response = parser.parseGeocoding("{}")

        assertTrue(response.results.isEmpty())
    }

    @Test(expected = SerializationException::class)
    fun parseForecast_rejectsMalformedNumericFields() {
        parser.parseForecast(
            """
                {
                  "current": {
                    "temperature_2m": "warm"
                  }
                }
            """.trimIndent(),
        )
    }

    @Test
    fun cityMapper_filtersInvalidRemoteCities() {
        val response = parser.parseGeocoding(
            """
                {
                  "generationtime_ms": 0.42,
                  "results": [
                    {
                      "id": 2643743,
                      "name": "伦敦",
                      "country": "英国",
                      "admin1": "英格兰",
                      "latitude": 51.5072,
                      "longitude": -0.1276,
                      "timezone": "Europe/London",
                      "population": 8982000
                    },
                    {
                      "id": 1,
                      "country": "无效",
                      "latitude": 10.0,
                      "longitude": 10.0
                    },
                    {
                      "id": 2,
                      "name": "无坐标"
                    }
                  ]
                }
            """.trimIndent(),
        )

        val cities = OpenMeteoCityMapper().map(response)

        assertEquals(1, cities.size)
        assertEquals("2643743", cities.first().geonameId)
        assertEquals("伦敦", cities.first().name)
        assertEquals("英国", cities.first().country)
        assertEquals("Europe/London", cities.first().timezone)
    }

    @Test
    fun weatherMapper_mapsForecastAndAirQualitySample() {
        val forecast = parser.parseForecast(
            """
                {
                  "current": {
                    "time": "2026-08-03T16:15",
                    "temperature_2m": 19.4,
                    "relative_humidity_2m": 83,
                    "apparent_temperature": 18.7,
                    "is_day": 1,
                    "precipitation": 0.3,
                    "rain": 0.1,
                    "weather_code": 61,
                    "surface_pressure": 1012.4,
                    "wind_speed_10m": 4.2,
                    "wind_direction_10m": 90
                  },
                  "hourly": {
                    "time": [
                      "2026-08-03T15:00",
                      "2026-08-03T16:00",
                      "2026-08-03T17:00",
                      "2026-08-03T18:00",
                      "2026-08-03T19:00"
                    ],
                    "temperature_2m": [17.2, 19.4, 20.1, 21.2, 20.8],
                    "weather_code": [3, 61, 61, 2, 0],
                    "is_day": [1, 1, 1, 1, 0]
                  },
                  "daily": {
                    "time": ["2026-08-03", "2026-08-04"],
                    "weather_code": [61, 3],
                    "temperature_2m_max": [25.2, 22.8],
                    "temperature_2m_min": [13.6, 12.2],
                    "precipitation_sum": [1.2, 0],
                    "precipitation_probability_max": [70, 10],
                    "uv_index_max": [5.6, 2.2],
                    "sunrise": ["2026-08-03T05:31", "2026-08-04T05:33"],
                    "sunset": ["2026-08-03T20:45", "2026-08-04T20:43"]
                  }
                }
            """.trimIndent(),
        )
        val airQuality = parser.parseAirQuality(
            """
                {
                  "current": {
                    "time": "2026-08-03T16:00",
                    "us_aqi": 77,
                    "pm10": 33.4,
                    "pm2_5": 42.8,
                    "carbon_monoxide": 188.2,
                    "nitrogen_dioxide": 12.1,
                    "sulphur_dioxide": 4.2,
                    "ozone": 62.6
                  }
                }
            """.trimIndent(),
        )
        val city = OpenMeteoCity(
            geonameId = "2643743",
            name = "伦敦",
            nameEn = "London",
            country = "英国",
            admin1 = "英格兰",
            latitude = 51.5072,
            longitude = -0.1276,
            timezone = "Europe/London",
        )

        val weather = OpenMeteoWeatherMapper().map(city, forecast, airQuality)
        val currentWeather = requireNotNull(weather.currentWeather)
        val mappedAirQuality = requireNotNull(weather.airQuality)

        assertEquals("伦敦", weather.cityName)
        assertEquals(WeatherCondition.LightRain, currentWeather.condition)
        assertEquals("19", currentWeather.temperature)
        assertEquals("83", currentWeather.humidity)
        assertEquals("东风", currentWeather.windDirection)
        assertEquals("3级", currentWeather.windPower)
        assertEquals(4, weather.hourlyForecasts.size)
        assertEquals("2026-08-03T16:00", weather.hourlyForecasts.first().dateTime)
        assertEquals(19, weather.hourlyForecasts.first().temperature)
        assertEquals(WeatherCondition.LightRain, weather.hourlyForecasts.first().condition)
        assertEquals(WeatherCondition.Clear, weather.hourlyForecasts.last().condition)
        assertTrue(weather.hourlyForecasts.first().isDay)
        assertEquals(false, weather.hourlyForecasts.last().isDay)
        assertEquals(2, weather.forecastDays.size)
        assertEquals("周一", weather.forecastDays.first().week)
        assertEquals(25, weather.forecastDays.first().tempMax)
        assertEquals("中等", weather.forecastDays.first().uvLevel)
        assertEquals(77, mappedAirQuality.aqi)
        assertEquals("良", mappedAirQuality.quality)
        assertEquals("PM2.5", mappedAirQuality.primaryPollutant)
        assertEquals(1, weather.lifeIndexes.size)
    }

    @Test
    fun weatherMapper_handlesMissingOptionalFields() {
        val forecast = parser.parseForecast(
            """
                {
                  "current": {},
                  "daily": {
                    "time": ["2026-08-03"]
                  }
                }
            """.trimIndent(),
        )
        val airQuality = parser.parseAirQuality("{}")
        val city = OpenMeteoCity(
            geonameId = "1816670",
            name = "北京",
            nameEn = "Beijing",
            country = "中国",
            admin1 = "北京",
            latitude = 39.9042,
            longitude = 116.4074,
            timezone = "Asia/Shanghai",
        )

        val weather = OpenMeteoWeatherMapper().map(city, forecast, airQuality)
        val currentWeather = requireNotNull(weather.currentWeather)
        val mappedAirQuality = requireNotNull(weather.airQuality)

        assertEquals("北京", weather.cityName)
        assertEquals(WeatherCondition.Unknown, currentWeather.condition)
        assertEquals("", currentWeather.temperature)
        assertTrue(weather.hourlyForecasts.isEmpty())
        assertEquals(1, weather.forecastDays.size)
        assertEquals(0, weather.forecastDays.first().tempMax)
        assertEquals(0, mappedAirQuality.aqi)
        assertEquals("", mappedAirQuality.quality)
        assertTrue(weather.lifeIndexes.isEmpty())
    }

    @Test
    fun weatherMapper_rejectsInvalidDatesAndNormalizesWindDirection() {
        val forecast = OpenMeteoForecastResponse(
            current = OpenMeteoCurrentForecastResponse(
                time = "invalid-date",
                windDirection10m = -90.0,
            ),
            daily = OpenMeteoDailyForecastResponse(time = listOf("2026-99-99")),
        )

        val weather = OpenMeteoWeatherMapper().map(
            city = OpenMeteoCityCatalog.defaultCity,
            forecast = forecast,
            airQuality = OpenMeteoAirQualityResponse(),
        )

        assertEquals(0L, weather.currentWeather?.observedAtMillis)
        assertEquals("西风", weather.currentWeather?.windDirection)
        assertEquals("", weather.forecastDays.single().week)
    }
}
