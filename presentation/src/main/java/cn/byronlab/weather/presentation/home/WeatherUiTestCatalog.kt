package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.AirQuality
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.ForecastDay
import cn.byronlab.weather.domain.model.HourlyForecast
import cn.byronlab.weather.domain.model.LifeIndex
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.model.WeatherCondition

internal data class WeatherUiTestScenario(
    val id: String,
    val name: String,
    val weatherName: String,
    val category: String,
    val description: String,
    val condition: WeatherCondition,
    val isDay: Boolean = true,
    val temperature: Int,
    val windSpeed: Int = 8,
)

internal object WeatherUiTestCatalog {
    val scenarios: List<WeatherUiTestScenario> = listOf(
        condition("clear", "晴朗", "晴云", "天体、薄云与天空渐变", WeatherCondition.Clear, 28),
        condition("mainly-clear", "大部晴朗", "晴云", "少量薄云与柔和天光", WeatherCondition.MainlyClear, 27),
        condition("partly-cloudy", "少云", "晴云", "明暗云层的分层组合", WeatherCondition.PartlyCloudy, 25),
        condition("cloudy", "多云", "晴云", "较高云量与缓慢漂移", WeatherCondition.Cloudy, 23),
        condition("overcast", "阴", "晴云", "低明度阴云与压低的天空", WeatherCondition.Overcast, 20),
        condition("clear-windy", "晴朗 · 大风", "晴云", "验证强风下云层运动速度", WeatherCondition.Clear, 24, 36),
        condition("fog", "雾", "能见度", "雾带、低对比度和远景遮罩", WeatherCondition.Fog, 16),
        condition("drizzle", "毛毛雨", "降雨", "细密、低强度持续降水", WeatherCondition.Drizzle, 19),
        condition("freezing-drizzle", "冻毛毛雨", "降雨", "冻结细雨与冷色调", WeatherCondition.FreezingDrizzle, 0),
        condition("light-rain", "小雨", "降雨", "轻量持续雨线", WeatherCondition.LightRain, 18),
        condition("moderate-rain", "中雨", "降雨", "中等密度持续雨线", WeatherCondition.ModerateRain, 17),
        condition("heavy-rain", "大雨", "降雨", "高密度持续降雨", WeatherCondition.HeavyRain, 16),
        condition("rain-showers", "阵雨", "降雨", "阵性节奏与微风", WeatherCondition.RainShowers, 21),
        condition("heavy-rain-showers", "强阵雨", "降雨", "高强度阵雨与增强风速", WeatherCondition.HeavyRainShowers, 19),
        condition("violent-rain-showers", "暴雨", "降雨", "极强阵雨与快速运动", WeatherCondition.ViolentRainShowers, 18),
        condition("freezing-rain", "冻雨", "降雨", "冻结雨滴与冷色阴云", WeatherCondition.FreezingRain, -1),
        condition("light-snow", "小雪", "冰雪", "稀疏、缓慢落雪", WeatherCondition.LightSnow, -2),
        condition("moderate-snow", "中雪", "冰雪", "中等密度持续落雪", WeatherCondition.ModerateSnow, -4),
        condition("heavy-snow", "大雪", "冰雪", "高密度持续落雪", WeatherCondition.HeavySnow, -7),
        condition("snow-grains", "雪粒", "冰雪", "细小雪粒与飘雪轨迹", WeatherCondition.SnowGrains, -3),
        condition("snow-showers", "阵雪", "冰雪", "阵性飘雪与微风", WeatherCondition.SnowShowers, -5),
        condition("heavy-snow-showers", "强阵雪", "冰雪", "强飘雪与快速风场", WeatherCondition.HeavySnowShowers, -9),
        condition("thunderstorm", "雷暴", "强对流", "阵雨、积雨云与间歇闪电", WeatherCondition.Thunderstorm, 22),
        condition("thunderstorm-hail", "雷暴伴冰雹", "强对流", "冰雹、强风与频繁闪电", WeatherCondition.ThunderstormWithHail, 17),
    ).flatMap { condition ->
        listOf(condition.toScenario(isDay = true), condition.toScenario(isDay = false))
    }

    private val previews: Map<String, WeatherUiModel> by lazy(LazyThreadSafetyMode.NONE) {
        scenarios.associate { scenario ->
            scenario.id to WeatherUiMapper().map(createWeather(scenario))
        }
    }

    fun find(id: String?): WeatherUiTestScenario? = scenarios.firstOrNull { it.id == id }

    fun createPreview(scenario: WeatherUiTestScenario): WeatherUiModel {
        return previews.getValue(scenario.id)
    }

    private fun createWeather(scenario: WeatherUiTestScenario): Weather {
        val cityId = "weather-ui-test"
        val hourlyTemperatures = listOf(0, -1, -1, 0, 1, 2, 3, 3, 2, 2, 1, 0, -1, -2, -2, -1, 0, 1, 1, 0, -1, -1, 0, 1)
        val weeks = listOf("今天", "周四", "周五", "周六", "周日", "周一", "周二")

        return Weather(
            city = City(
                cityId = cityId,
                name = "天气测试",
                nameEn = "Weather UI Lab",
                root = "Debug 场景",
                parent = "",
                longitude = "",
                latitude = "",
            ),
            currentWeather = CurrentWeather(
                cityId = cityId,
                condition = scenario.condition,
                temperature = scenario.temperature.toString(),
                humidity = "68",
                windDirection = "西北风",
                windSpeed = scenario.windSpeed.toString(),
                observedAtMillis = 0L,
                windPower = "3",
                rain = "0",
                feelsTemperature = (scenario.temperature - 1).toString(),
                airPressure = "1012",
                isDay = scenario.isDay,
            ),
            forecastDays = weeks.mapIndexed { index, week ->
                ForecastDay(
                    cityId = cityId,
                    condition = scenario.condition,
                    dayCondition = scenario.condition,
                    nightCondition = scenario.condition,
                    tempMax = scenario.temperature + 4 - (index % 3),
                    tempMin = scenario.temperature - 5 - (index % 2),
                    wind = "${scenario.windSpeed} km/h",
                    date = "08-${(5 + index).toString().padStart(2, '0')}",
                    week = week,
                    precipitationProbability = if (scenario.condition.hasPrecipitation()) "78" else "12",
                    uvLevel = if (scenario.isDay) "4 中等" else "0",
                    visibility = if (scenario.condition == WeatherCondition.Fog) "2" else "10",
                    humidity = "68",
                    pressure = "1012",
                    precipitation = if (scenario.condition.hasPrecipitation()) "8" else "0",
                    sunrise = "05:42",
                    sunset = "19:08",
                    moonrise = "21:10",
                    moonset = "07:20",
                )
            },
            hourlyForecasts = hourlyTemperatures.mapIndexed { index, delta ->
                HourlyForecast(
                    cityId = cityId,
                    dateTime = "2026-08-05T${((6 + index) % 24).toString().padStart(2, '0')}:00",
                    condition = scenario.condition,
                    temperature = scenario.temperature + delta,
                    isDay = scenario.isDay,
                )
            },
            airQuality = AirQuality(
                cityId = cityId,
                aqi = 45,
                pm25 = 18,
                pm10 = 28,
                publishTime = "",
                advice = "空气质量令人满意，适合户外活动",
                cityRank = "主要污染物: PM2.5",
                quality = "优",
                co = "0.4",
                so2 = "6",
                no2 = "14",
                o3 = "72",
                primaryPollutant = "PM2.5",
            ),
            lifeIndexes = listOf(
                LifeIndex(cityId, "穿衣", "舒适", "适合日常着装"),
                LifeIndex(cityId, "运动", "适宜", "适合户外活动"),
            ),
        )
    }

    private fun condition(
        id: String,
        name: String,
        category: String,
        description: String,
        condition: WeatherCondition,
        temperature: Int,
        windSpeed: Int = 8,
    ) = WeatherUiTestCondition(
        id = id,
        name = name,
        category = category,
        description = description,
        condition = condition,
        temperature = temperature,
        windSpeed = windSpeed,
    )
}

private data class WeatherUiTestCondition(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val condition: WeatherCondition,
    val temperature: Int,
    val windSpeed: Int,
) {
    fun toScenario(isDay: Boolean): WeatherUiTestScenario {
        val phaseId = if (isDay) "day" else "night"
        val phaseName = if (isDay) "白天" else "夜间"
        return WeatherUiTestScenario(
            id = "$id-$phaseId",
            name = "$name · $phaseName",
            weatherName = name,
            category = category,
            description = description,
            condition = condition,
            isDay = isDay,
            temperature = if (isDay) temperature else temperature - 5,
            windSpeed = windSpeed,
        )
    }
}

private fun WeatherCondition.hasPrecipitation(): Boolean = when (this) {
    WeatherCondition.Clear,
    WeatherCondition.MainlyClear,
    WeatherCondition.PartlyCloudy,
    WeatherCondition.Cloudy,
    WeatherCondition.Overcast,
    WeatherCondition.Fog,
    WeatherCondition.Unknown -> false

    else -> true
}
