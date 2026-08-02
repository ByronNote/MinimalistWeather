package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.AirQuality
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.ForecastDay
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.library.util.DateConvertUtils
import javax.inject.Inject

class WeatherUiMapper @Inject constructor() {

    fun map(weather: Weather): WeatherUiModel {
        val current = weather.currentWeather
        val airQuality = weather.airQuality
        val firstForecast = weather.forecastDays.firstOrNull()

        return WeatherUiModel(
            cityId = weather.cityId.orEmpty(),
            cityName = weather.cityName.orEmpty().ifBlank { weather.cityId.orEmpty() },
            currentTemperature = current?.temperature.orEmpty(),
            currentCondition = current?.condition.orEmpty(),
            publishTime = current.publishTimeText(),
            aqi = airQuality?.aqi ?: 0,
            airQuality = airQuality?.quality.orEmpty(),
            advice = airQuality?.advice.orEmpty(),
            cityRank = airQuality.cityRankText(),
            details = buildDetails(current, firstForecast),
            forecasts = weather.forecastDays.map { it.toUiModel() },
            lifeIndexes = weather.lifeIndexes.map {
                LifeIndexUiModel(
                    name = it.name.orEmpty(),
                    level = it.level.orEmpty(),
                    details = it.details.orEmpty(),
                )
            },
        )
    }

    fun map(city: City): CityUiModel {
        val name = city.name.orEmpty().ifBlank { city.cityId.orEmpty() }
        val subtitle = listOf(city.parent, city.nameEn)
            .map { it.orEmpty() }
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(" · ")
        return CityUiModel(
            cityId = city.cityId.orEmpty(),
            name = name,
            subtitle = subtitle,
        )
    }

    private fun buildDetails(
        current: CurrentWeather?,
        forecast: ForecastDay?,
    ): List<WeatherDetailUiModel> = listOf(
        WeatherDetailUiModel("体感温度", "${current?.feelsTemperature.orEmpty()}°C"),
        WeatherDetailUiModel("湿度", "${current?.humidity.orEmpty()}%"),
        WeatherDetailUiModel("紫外线指数", forecast?.uvLevel.orEmpty()),
        WeatherDetailUiModel("降水量", "${current?.rain.orEmpty()}mm"),
        WeatherDetailUiModel("降水概率", "${forecast?.precipitationProbability.orEmpty()}%"),
        WeatherDetailUiModel("能见度", "${forecast?.visibility.orEmpty()}km"),
    )

    private fun ForecastDay.toUiModel(): ForecastUiModel {
        val condition = condition.orEmpty().ifBlank {
            if (dayCondition.orEmpty() == nightCondition.orEmpty()) {
                dayCondition.orEmpty()
            } else {
                "${dayCondition.orEmpty()}转${nightCondition.orEmpty()}"
            }
        }
        return ForecastUiModel(
            week = week.orEmpty(),
            date = date.orEmpty(),
            condition = condition,
            tempMax = tempMax,
            tempMin = tempMin,
        )
    }

    private fun CurrentWeather?.publishTimeText(): String {
        val timestamp = this?.observedAtMillis ?: return ""
        if (timestamp <= 0L) {
            return ""
        }
        return DateConvertUtils.timeStampToDate(
            timestamp,
            DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM,
        )
    }

    private fun AirQuality?.cityRankText(): String {
        if (this == null) {
            return "首要污染物: "
        }
        return cityRank.orEmpty().ifBlank {
            "首要污染物: ${primaryPollutant.orEmpty()}"
        }
    }
}
