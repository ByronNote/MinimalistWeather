package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.AirQuality
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.ForecastDay
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
import cn.byronlab.weather.presentation.weatherui.model.WeatherWindLevel
import java.util.Locale
import javax.inject.Inject

class WeatherUiMapper @Inject constructor() {

    fun map(weather: Weather): WeatherUiModel {
        val current = weather.currentWeather
        val airQuality = weather.airQuality
        val firstForecast = weather.forecastDays.firstOrNull()
        val city = weather.city
        val scene = current?.let {
            it.condition.toWeatherScene(
                skyPhase = if (it.isDay) WeatherSkyPhase.Day else WeatherSkyPhase.Night,
                windLevel = it.windSpeed.toWeatherWindLevel(),
            )
        } ?: WeatherSceneSpec()

        return WeatherUiModel(
            cityId = city.cityId,
            cityName = city.name.ifBlank { city.cityId },
            citySubtitle = city.root.ifBlank { city.nameEn },
            coordinatesText = city.coordinatesText(),
            currentTemperature = current?.temperature.orEmpty(),
            currentCondition = current?.condition?.toDisplayText(current.isDay).orEmpty(),
            feelsLikeTemperature = current?.feelsTemperature.orEmpty(),
            highTemperature = firstForecast?.tempMax,
            lowTemperature = firstForecast?.tempMin,
            aqi = airQuality?.aqi ?: 0,
            airQuality = airQuality?.quality.orEmpty(),
            advice = airQuality?.advice.orEmpty(),
            cityRank = airQuality.cityRankText(),
            primaryPollutant = airQuality?.primaryPollutant.orEmpty(),
            pollutants = buildAirPollutants(airQuality),
            scene = scene,
            todaySummary = buildTodaySummary(firstForecast),
            hourlyForecasts = buildHourlyForecasts(weather),
            details = buildDetails(current, firstForecast),
            forecasts = weather.forecastDays.map { it.toUiModel() },
            lifeIndexes = weather.lifeIndexes.map {
                LifeIndexUiModel(
                    name = it.name,
                    level = it.level,
                    details = it.details,
                )
            },
        )
    }

    fun map(city: City): CityUiModel {
        val name = city.name.ifBlank { city.cityId }
        val allLocationParts = listOf(city.root, city.parent)
            .flatMap { it.split("·") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val subtitle = allLocationParts.filter { it != name }.ifEmpty {
            allLocationParts.take(1)
        }.ifEmpty {
            listOf(city.nameEn).filter { it.isNotBlank() && it != name }
        }
            .joinToString(" · ")
        return CityUiModel(
            cityId = city.cityId,
            name = name,
            subtitle = subtitle,
        )
    }

    private fun buildDetails(
        current: CurrentWeather?,
        forecast: ForecastDay?,
    ): List<WeatherDetailUiModel> = listOf(
        WeatherDetailUiModel("体感温度", current?.feelsTemperature.withSuffix("°")),
        WeatherDetailUiModel("湿度", current?.humidity.withSuffix("%")),
        WeatherDetailUiModel(
            title = "风速",
            value = current?.windSpeed.withSuffix("km/h"),
            subtitle = current?.windDirection.orEmpty(),
        ),
        WeatherDetailUiModel("能见度", forecast?.visibility.withSuffix("km")),
        WeatherDetailUiModel("气压", (current?.airPressure ?: forecast?.pressure).orDash()),
        WeatherDetailUiModel("紫外线指数", forecast?.uvLevel.orDash()),
    )

    private fun buildAirPollutants(airQuality: AirQuality?): List<AirPollutantUiModel> = listOf(
        AirPollutantUiModel("PM2.5", airQuality?.pm25.toPollutantText()),
        AirPollutantUiModel("PM10", airQuality?.pm10.toPollutantText()),
        AirPollutantUiModel("O₃", airQuality?.o3.orDash()),
        AirPollutantUiModel("NO₂", airQuality?.no2.orDash()),
        AirPollutantUiModel("SO₂", airQuality?.so2.orDash()),
        AirPollutantUiModel("CO", airQuality?.co.orDash()),
    )

    private fun buildTodaySummary(forecast: ForecastDay?): TodaySummaryUiModel {
        return TodaySummaryUiModel(
            high = forecast?.tempMax?.toString()?.withSuffix("°") ?: "--",
            low = forecast?.tempMin?.toString()?.withSuffix("°") ?: "--",
            sunrise = forecast?.sunrise.orDash(),
            sunset = forecast?.sunset.orDash(),
            rainProbability = forecast?.precipitationProbability.withSuffix("%"),
            uvIndex = forecast?.uvLevel.orDash(),
        )
    }

    private fun buildHourlyForecasts(weather: Weather): List<HourlyForecastUiModel> {
        return weather.hourlyForecasts
            .take(HOURLY_FORECAST_ITEM_COUNT)
            .mapIndexed { index, forecast ->
                HourlyForecastUiModel(
                    time = if (index == 0) "Now" else forecast.dateTime.timeLabel(),
                    conditionText = forecast.condition.toDisplayText(forecast.isDay),
                    temperature = forecast.temperature,
                    scene = forecast.condition.toWeatherScene(
                        skyPhase = if (forecast.isDay) WeatherSkyPhase.Day else WeatherSkyPhase.Night,
                    ),
                )
            }
    }

    private fun ForecastDay.toUiModel(): ForecastUiModel {
        val effectiveCondition = condition.takeUnless { it == WeatherCondition.Unknown }
        val displayCondition = effectiveCondition?.toDisplayText() ?: if (dayCondition == nightCondition) {
            dayCondition.toDisplayText()
        } else {
            "${dayCondition.toDisplayText()}转${nightCondition.toDisplayText()}"
        }
        val sceneCondition = effectiveCondition ?: mostSignificantCondition(dayCondition, nightCondition)
        return ForecastUiModel(
            week = week,
            date = date,
            conditionText = displayCondition,
            tempMax = tempMax,
            tempMin = tempMin,
            scene = sceneCondition.toWeatherScene(),
        )
    }

    private fun AirQuality?.cityRankText(): String {
        if (this == null) {
            return "主要污染物: --"
        }
        return cityRank.ifBlank {
            "主要污染物: ${primaryPollutant.ifBlank { "--" }}"
        }
    }

    private fun String?.withSuffix(suffix: String): String {
        val value = this.orEmpty()
        if (value.isBlank()) {
            return "--"
        }
        return if (suffix == "%" || suffix == "°") {
            "$value$suffix"
        } else {
            "$value $suffix"
        }
    }

    private fun String?.orDash(): String {
        return this.orEmpty().ifBlank { "--" }
    }

    private fun String.timeLabel(): String {
        return substringAfter("T", this).take(5)
    }

    private fun Int?.toPollutantText(): String {
        return this?.takeIf { it > 0 }?.toString() ?: "--"
    }

    private fun WeatherCondition.toWeatherScene(
        skyPhase: WeatherSkyPhase = WeatherSkyPhase.Day,
        windLevel: WeatherWindLevel = WeatherWindLevel.Calm,
    ): WeatherSceneSpec {
        return when (this) {
            WeatherCondition.ThunderstormWithHail -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Hail,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                lightningIntensity = WeatherLightningIntensity.Frequent,
                windLevel = windLevel.atLeast(WeatherWindLevel.Windy),
            )
            WeatherCondition.Thunderstorm -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                lightningIntensity = WeatherLightningIntensity.Occasional,
                windLevel = windLevel.atLeast(WeatherWindLevel.Windy),
            )
            WeatherCondition.FreezingDrizzle -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Drizzle,
                precipitationIntensity = WeatherIntensity.Light,
                freezing = true,
                windLevel = windLevel,
            )
            WeatherCondition.FreezingRain -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
                freezing = true,
                windLevel = windLevel,
            )
            WeatherCondition.ViolentRainShowers -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                windLevel = windLevel.atLeast(WeatherWindLevel.Windy),
            )
            WeatherCondition.HeavyRainShowers -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                windLevel = windLevel.atLeast(WeatherWindLevel.Breezy),
            )
            WeatherCondition.RainShowers -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
                precipitationPattern = WeatherPrecipitationPattern.Showers,
                windLevel = windLevel.atLeast(WeatherWindLevel.Breezy),
            )
            WeatherCondition.HeavyRain -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Heavy,
                windLevel = windLevel,
            )
            WeatherCondition.ModerateRain -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Moderate,
                windLevel = windLevel,
            )
            WeatherCondition.LightRain -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Rain,
                precipitationIntensity = WeatherIntensity.Light,
                windLevel = windLevel,
            )
            WeatherCondition.Drizzle -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Drizzle,
                precipitationIntensity = WeatherIntensity.Light,
                windLevel = windLevel,
            )
            WeatherCondition.HeavySnowShowers -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Heavy,
                precipitationPattern = WeatherPrecipitationPattern.Flurries,
                windLevel = windLevel.atLeast(WeatherWindLevel.Windy),
            )
            WeatherCondition.SnowShowers -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Moderate,
                precipitationPattern = WeatherPrecipitationPattern.Flurries,
                windLevel = windLevel.atLeast(WeatherWindLevel.Breezy),
            )
            WeatherCondition.HeavySnow -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Heavy,
                windLevel = windLevel,
            )
            WeatherCondition.ModerateSnow -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Moderate,
                windLevel = windLevel,
            )
            WeatherCondition.SnowGrains -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.SnowGrains,
                precipitationIntensity = WeatherIntensity.Light,
                precipitationPattern = WeatherPrecipitationPattern.Flurries,
                windLevel = windLevel.atLeast(WeatherWindLevel.Breezy),
            )
            WeatherCondition.LightSnow -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                precipitation = WeatherPrecipitation.Snow,
                precipitationIntensity = WeatherIntensity.Light,
                windLevel = windLevel,
            )
            WeatherCondition.Fog -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                atmosphere = WeatherAtmosphere.Fog,
                windLevel = windLevel,
            )
            WeatherCondition.Overcast -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Overcast,
                windLevel = windLevel,
            )
            WeatherCondition.MainlyClear -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.MostlyClear,
                windLevel = windLevel,
            )
            WeatherCondition.PartlyCloudy -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.PartlyCloudy,
                windLevel = windLevel,
            )
            WeatherCondition.Cloudy -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                windLevel = windLevel,
            )
            WeatherCondition.Clear -> WeatherSceneSpec(
                skyPhase = skyPhase,
                windLevel = windLevel,
            )
            WeatherCondition.Unknown -> WeatherSceneSpec(
                skyPhase = skyPhase,
                cloudCover = WeatherCloudCover.Cloudy,
                windLevel = windLevel,
            )
        }
    }

    private fun String.toWeatherWindLevel(): WeatherWindLevel {
        val speed = toDoubleOrNull() ?: return WeatherWindLevel.Calm
        return when {
            speed >= 28.0 -> WeatherWindLevel.Windy
            speed >= 12.0 -> WeatherWindLevel.Breezy
            else -> WeatherWindLevel.Calm
        }
    }

    private fun WeatherWindLevel.atLeast(minimum: WeatherWindLevel): WeatherWindLevel {
        return if (ordinal >= minimum.ordinal) this else minimum
    }

    private fun WeatherCondition.toDisplayText(isDay: Boolean = true): String {
        return when (this) {
            WeatherCondition.Clear -> if (isDay) "晴" else "晴夜"
            WeatherCondition.MainlyClear -> "大部晴朗"
            WeatherCondition.PartlyCloudy -> "少云"
            WeatherCondition.Cloudy -> "多云"
            WeatherCondition.Overcast -> "阴"
            WeatherCondition.Fog -> "雾"
            WeatherCondition.Drizzle -> "毛毛雨"
            WeatherCondition.FreezingDrizzle -> "冻毛毛雨"
            WeatherCondition.LightRain -> "小雨"
            WeatherCondition.ModerateRain -> "中雨"
            WeatherCondition.HeavyRain -> "大雨"
            WeatherCondition.FreezingRain -> "冻雨"
            WeatherCondition.LightSnow -> "小雪"
            WeatherCondition.ModerateSnow -> "中雪"
            WeatherCondition.HeavySnow -> "大雪"
            WeatherCondition.SnowGrains -> "雪粒"
            WeatherCondition.RainShowers -> "阵雨"
            WeatherCondition.HeavyRainShowers -> "强阵雨"
            WeatherCondition.ViolentRainShowers -> "暴雨"
            WeatherCondition.SnowShowers -> "阵雪"
            WeatherCondition.HeavySnowShowers -> "强阵雪"
            WeatherCondition.Thunderstorm -> "雷暴"
            WeatherCondition.ThunderstormWithHail -> "雷暴伴冰雹"
            WeatherCondition.Unknown -> "未知"
        }
    }

    private fun mostSignificantCondition(
        first: WeatherCondition,
        second: WeatherCondition,
    ): WeatherCondition {
        return if (first.scenePriority() >= second.scenePriority()) first else second
    }

    private fun WeatherCondition.scenePriority(): Int {
        return when (this) {
            WeatherCondition.Unknown -> 0
            WeatherCondition.Clear -> 1
            WeatherCondition.MainlyClear -> 2
            WeatherCondition.PartlyCloudy -> 3
            WeatherCondition.Cloudy -> 4
            WeatherCondition.Overcast -> 5
            WeatherCondition.Fog -> 6
            WeatherCondition.Drizzle -> 7
            WeatherCondition.FreezingDrizzle -> 8
            WeatherCondition.LightRain,
            WeatherCondition.LightSnow -> 9
            WeatherCondition.ModerateRain,
            WeatherCondition.RainShowers,
            WeatherCondition.ModerateSnow,
            WeatherCondition.SnowGrains,
            WeatherCondition.SnowShowers -> 10
            WeatherCondition.HeavyRain,
            WeatherCondition.FreezingRain,
            WeatherCondition.HeavyRainShowers,
            WeatherCondition.ViolentRainShowers,
            WeatherCondition.HeavySnow,
            WeatherCondition.HeavySnowShowers -> 11
            WeatherCondition.Thunderstorm -> 12
            WeatherCondition.ThunderstormWithHail -> 13
        }
    }

    private fun City.coordinatesText(): String {
        val latitude = latitude.toDoubleOrNull() ?: return ""
        val longitude = longitude.toDoubleOrNull() ?: return ""
        val latitudeHemisphere = if (latitude >= 0) "N" else "S"
        val longitudeHemisphere = if (longitude >= 0) "E" else "W"
        return String.format(
            Locale.US,
            "%.2f°%s  %.2f°%s",
            kotlin.math.abs(latitude),
            latitudeHemisphere,
            kotlin.math.abs(longitude),
            longitudeHemisphere,
        )
    }

    private companion object {
        const val HOURLY_FORECAST_ITEM_COUNT = 24
    }
}
