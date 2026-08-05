package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.AirQuality
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.ForecastDay
import cn.byronlab.weather.domain.model.HourlyForecast
import cn.byronlab.weather.domain.model.LifeIndex
import cn.byronlab.weather.domain.model.Weather
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

internal class OpenMeteoWeatherMapper {

    fun map(
        city: OpenMeteoCity,
        forecast: OpenMeteoForecastResponse,
        airQuality: OpenMeteoAirQualityResponse,
    ): Weather {
        val domainCity = OpenMeteoCityCodec.toDomainCity(city)
        val cityId = domainCity.cityId
        val currentWeather = mapCurrentWeather(cityId, forecast.current)
        val forecastDays = mapForecastDays(cityId, forecast.daily)
        val hourlyForecasts = mapHourlyForecasts(cityId, forecast.current, forecast.hourly)
        val airQualityModel = mapAirQuality(cityId, airQuality.current)
        val lifeIndexes = mapLifeIndexes(cityId, forecastDays)
        return Weather(
            city = domainCity,
            currentWeather = currentWeather,
            forecastDays = forecastDays,
            hourlyForecasts = hourlyForecasts,
            airQuality = airQualityModel,
            lifeIndexes = lifeIndexes,
        )
    }

    private fun mapCurrentWeather(
        cityId: String,
        current: OpenMeteoCurrentForecastResponse?,
    ): CurrentWeather {
        val isDay = (current?.isDay ?: 1) == 1
        val windSpeed = current?.windSpeed10m
        return CurrentWeather(
            cityId = cityId,
            condition = OpenMeteoWeatherCodeMapper.toDomainCondition(current?.weatherCode),
            temperature = current?.temperature2m.toIntText(),
            humidity = current?.relativeHumidity2m.toIntText(),
            windDirection = windDirection(current?.windDirection10m),
            windSpeed = windSpeed.toDecimalText(),
            observedAtMillis = parseOpenMeteoDateTimeMillis(current?.time.orEmpty()),
            windPower = beaufortLevel(windSpeed),
            rain = (current?.precipitation ?: current?.rain ?: 0.0).toDecimalText(),
            feelsTemperature = current?.apparentTemperature.toIntText(),
            airPressure = current?.surfacePressure.toIntText(),
            isDay = isDay,
        )
    }

    private fun mapForecastDays(
        cityId: String,
        daily: OpenMeteoDailyForecastResponse?,
    ): List<ForecastDay> {
        return daily?.time.orEmpty().mapIndexed { index, date ->
            val code = daily?.weatherCode.valueAt(index)
            val condition = OpenMeteoWeatherCodeMapper.toDomainCondition(code)
            ForecastDay(
                cityId = cityId,
                condition = condition,
                dayCondition = condition,
                nightCondition = condition,
                tempMax = daily?.temperature2mMax.valueAt(index).toRoundedInt(),
                tempMin = daily?.temperature2mMin.valueAt(index).toRoundedInt(),
                wind = "",
                date = date,
                week = weekLabel(date),
                precipitationProbability = daily?.precipitationProbabilityMax.valueAt(index).toIntText(),
                uvLevel = uvLevel(daily?.uvIndexMax.valueAt(index)),
                visibility = "",
                humidity = "",
                pressure = "",
                precipitation = daily?.precipitationSum.valueAt(index).toDecimalText(),
                sunrise = timeOnly(daily?.sunrise.valueAt(index).orEmpty()),
                sunset = timeOnly(daily?.sunset.valueAt(index).orEmpty()),
                moonrise = "",
                moonset = "",
            )
        }
    }

    private fun mapHourlyForecasts(
        cityId: String,
        current: OpenMeteoCurrentForecastResponse?,
        hourly: OpenMeteoHourlyForecastResponse?,
    ): List<HourlyForecast> {
        val currentHour = current?.time
            ?.takeIf { it.length >= DATE_TIME_HOUR_LENGTH }
            ?.take(DATE_TIME_HOUR_LENGTH)
            ?.plus(":00")
        val firstCurrentOrFutureIndex = currentHour?.let { hour ->
            hourly?.time.orEmpty().indexOfFirst { it >= hour }.takeIf { it >= 0 }
        } ?: 0

        return hourly?.time.orEmpty()
            .drop(firstCurrentOrFutureIndex)
            .mapIndexedNotNull { relativeIndex, dateTime ->
                val index = firstCurrentOrFutureIndex + relativeIndex
                val temperature = hourly?.temperature2m.valueAt(index) ?: return@mapIndexedNotNull null
                val code = hourly?.weatherCode.valueAt(index) ?: return@mapIndexedNotNull null
                val isDay = hourly?.isDay.valueAt(index)?.let { it == 1 } ?: true
                HourlyForecast(
                    cityId = cityId,
                    dateTime = dateTime,
                    condition = OpenMeteoWeatherCodeMapper.toDomainCondition(code),
                    temperature = temperature.roundToInt(),
                    isDay = isDay,
                )
            }
    }

    private fun mapAirQuality(
        cityId: String,
        current: OpenMeteoCurrentAirQualityResponse?,
    ): AirQuality {
        val aqi = current?.usAqi.toRoundedInt()
        val pm25 = current?.pm25.toRoundedInt()
        val pm10 = current?.pm10.toRoundedInt()
        val quality = aqiQuality(aqi)
        return AirQuality(
            cityId = cityId,
            aqi = aqi,
            pm25 = pm25,
            pm10 = pm10,
            publishTime = timeOnly(current?.time.orEmpty()),
            advice = aqiAdvice(aqi),
            cityRank = "",
            quality = quality,
            co = current?.carbonMonoxide.toDecimalText(),
            so2 = current?.sulphurDioxide.toDecimalText(),
            no2 = current?.nitrogenDioxide.toDecimalText(),
            o3 = current?.ozone.toDecimalText(),
            primaryPollutant = primaryPollutant(pm25, pm10),
        )
    }

    private fun mapLifeIndexes(cityId: String, forecastDays: List<ForecastDay>): List<LifeIndex> {
        val uv = forecastDays.firstOrNull()?.uvLevel.orEmpty()
        if (uv.isBlank()) {
            return emptyList()
        }
        return listOf(
            LifeIndex(
                cityId = cityId,
                name = "紫外线",
                level = uv,
                details = uvAdvice(uv),
            ),
        )
    }

    private fun <T> List<T>?.valueAt(index: Int): T? {
        return this?.getOrNull(index)
    }

    private fun Double?.toIntText(): String {
        return this?.roundToInt()?.toString().orEmpty()
    }

    private fun Double?.toDecimalText(): String {
        val value = this ?: return ""
        return if (value % 1.0 == 0.0) {
            value.roundToInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    private fun Double?.toRoundedInt(): Int {
        return this?.roundToInt() ?: 0
    }

    private fun windDirection(degrees: Double?): String {
        val value = degrees ?: return ""
        val directions = arrayOf(
            "北风",
            "东北风",
            "东风",
            "东南风",
            "南风",
            "西南风",
            "西风",
            "西北风",
        )
        val normalizedDegrees = ((value % 360.0) + 360.0) % 360.0
        val index = ((normalizedDegrees + 22.5) / 45.0).toInt() % directions.size
        return directions[index]
    }

    private fun beaufortLevel(speedMetersPerSecond: Double?): String {
        val speed = speedMetersPerSecond ?: return ""
        val level = when {
            speed < 0.3 -> 0
            speed < 1.6 -> 1
            speed < 3.4 -> 2
            speed < 5.5 -> 3
            speed < 8.0 -> 4
            speed < 10.8 -> 5
            speed < 13.9 -> 6
            speed < 17.2 -> 7
            speed < 20.8 -> 8
            speed < 24.5 -> 9
            speed < 28.5 -> 10
            speed < 32.7 -> 11
            else -> 12
        }
        return "${level}级"
    }

    private fun aqiQuality(aqi: Int): String {
        return when {
            aqi <= 0 -> ""
            aqi <= 50 -> "优"
            aqi <= 100 -> "良"
            aqi <= 150 -> "轻度污染"
            aqi <= 200 -> "中度污染"
            aqi <= 300 -> "重度污染"
            else -> "严重污染"
        }
    }

    private fun aqiAdvice(aqi: Int): String {
        return when {
            aqi <= 0 -> ""
            aqi <= 50 -> "空气质量令人满意，适合户外活动。"
            aqi <= 100 -> "空气质量可接受，敏感人群可减少长时间户外活动。"
            aqi <= 150 -> "敏感人群应减少户外活动。"
            aqi <= 200 -> "建议减少户外活动，外出注意防护。"
            aqi <= 300 -> "建议避免长时间户外活动。"
            else -> "建议尽量留在室内，并做好空气防护。"
        }
    }

    private fun uvLevel(uvIndex: Double?): String {
        val uv = uvIndex ?: return ""
        return when {
            uv < 3.0 -> "低"
            uv < 6.0 -> "中等"
            uv < 8.0 -> "高"
            uv < 11.0 -> "很高"
            else -> "极高"
        }
    }

    private fun uvAdvice(level: String): String {
        return when (level) {
            "低" -> "紫外线较弱，正常外出即可。"
            "中等" -> "建议使用防晒用品。"
            "高" -> "建议减少正午时段暴露，并做好防晒。"
            "很高", "极高" -> "建议避免长时间暴露，外出需加强防晒。"
            else -> ""
        }
    }

    private fun primaryPollutant(pm25: Int, pm10: Int): String {
        return when {
            pm25 <= 0 && pm10 <= 0 -> ""
            pm25 >= pm10 -> "PM2.5"
            else -> "PM10"
        }
    }

    private fun weekLabel(date: String): String {
        val parsedDate = parseDate(date, DATE_FORMAT_PATTERN) ?: return ""
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            Calendar.SUNDAY -> "周日"
            else -> ""
        }
    }

    private fun parseOpenMeteoDateTimeMillis(value: String): Long {
        return parseDate(value, DATE_TIME_FORMAT_PATTERN)?.time ?: 0L
    }

    private fun parseDate(value: String, pattern: String): Date? {
        if (value.isBlank()) {
            return null
        }
        val parser = SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
        }
        val position = ParsePosition(0)
        return parser.parse(value, position)?.takeIf { position.index == value.length }
    }

    private fun timeOnly(value: String): String {
        return value.substringAfter("T", value)
    }

    private companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
        const val DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm"
        const val DATE_TIME_HOUR_LENGTH = 13
    }
}
