package cn.byronlab.weather.domain.model;

import java.util.Objects;

public final class ForecastDay {

    private final String cityId;
    private final String condition;
    private final String dayCondition;
    private final String nightCondition;
    private final int tempMax;
    private final int tempMin;
    private final String wind;
    private final String date;
    private final String week;
    private final String precipitationProbability;
    private final String uvLevel;
    private final String visibility;
    private final String humidity;
    private final String pressure;
    private final String precipitation;
    private final String sunrise;
    private final String sunset;
    private final String moonrise;
    private final String moonset;

    public ForecastDay(String cityId, String condition, String dayCondition, String nightCondition,
                       int tempMax, int tempMin, String wind, String date, String week,
                       String precipitationProbability, String uvLevel, String visibility,
                       String humidity, String pressure, String precipitation, String sunrise,
                       String sunset, String moonrise, String moonset) {
        this.cityId = cityId;
        this.condition = condition;
        this.dayCondition = dayCondition;
        this.nightCondition = nightCondition;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.wind = wind;
        this.date = date;
        this.week = week;
        this.precipitationProbability = precipitationProbability;
        this.uvLevel = uvLevel;
        this.visibility = visibility;
        this.humidity = humidity;
        this.pressure = pressure;
        this.precipitation = precipitation;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.moonrise = moonrise;
        this.moonset = moonset;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCondition() {
        return condition;
    }

    public String getDayCondition() {
        return dayCondition;
    }

    public String getNightCondition() {
        return nightCondition;
    }

    public int getTempMax() {
        return tempMax;
    }

    public int getTempMin() {
        return tempMin;
    }

    public String getWind() {
        return wind;
    }

    public String getDate() {
        return date;
    }

    public String getWeek() {
        return week;
    }

    public String getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getUvLevel() {
        return uvLevel;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public String getMoonrise() {
        return moonrise;
    }

    public String getMoonset() {
        return moonset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForecastDay)) {
            return false;
        }
        ForecastDay that = (ForecastDay) o;
        return tempMax == that.tempMax
                && tempMin == that.tempMin
                && Objects.equals(cityId, that.cityId)
                && Objects.equals(condition, that.condition)
                && Objects.equals(dayCondition, that.dayCondition)
                && Objects.equals(nightCondition, that.nightCondition)
                && Objects.equals(wind, that.wind)
                && Objects.equals(date, that.date)
                && Objects.equals(week, that.week)
                && Objects.equals(precipitationProbability, that.precipitationProbability)
                && Objects.equals(uvLevel, that.uvLevel)
                && Objects.equals(visibility, that.visibility)
                && Objects.equals(humidity, that.humidity)
                && Objects.equals(pressure, that.pressure)
                && Objects.equals(precipitation, that.precipitation)
                && Objects.equals(sunrise, that.sunrise)
                && Objects.equals(sunset, that.sunset)
                && Objects.equals(moonrise, that.moonrise)
                && Objects.equals(moonset, that.moonset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, condition, dayCondition, nightCondition, tempMax, tempMin,
                wind, date, week, precipitationProbability, uvLevel, visibility, humidity,
                pressure, precipitation, sunrise, sunset, moonrise, moonset);
    }
}
