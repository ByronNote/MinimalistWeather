package cn.byronlab.weather.domain.model;

import java.util.Objects;

public final class CurrentWeather {

    private final String cityId;
    private final String condition;
    private final String temperature;
    private final String humidity;
    private final String windDirection;
    private final String windSpeed;
    private final long observedAtMillis;
    private final String windPower;
    private final String rain;
    private final String feelsTemperature;
    private final String airPressure;

    public CurrentWeather(String cityId, String condition, String temperature, String humidity,
                          String windDirection, String windSpeed, long observedAtMillis,
                          String windPower, String rain, String feelsTemperature,
                          String airPressure) {
        this.cityId = cityId;
        this.condition = condition;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.observedAtMillis = observedAtMillis;
        this.windPower = windPower;
        this.rain = rain;
        this.feelsTemperature = feelsTemperature;
        this.airPressure = airPressure;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCondition() {
        return condition;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public long getObservedAtMillis() {
        return observedAtMillis;
    }

    public String getWindPower() {
        return windPower;
    }

    public String getRain() {
        return rain;
    }

    public String getFeelsTemperature() {
        return feelsTemperature;
    }

    public String getAirPressure() {
        return airPressure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CurrentWeather)) {
            return false;
        }
        CurrentWeather that = (CurrentWeather) o;
        return observedAtMillis == that.observedAtMillis
                && Objects.equals(cityId, that.cityId)
                && Objects.equals(condition, that.condition)
                && Objects.equals(temperature, that.temperature)
                && Objects.equals(humidity, that.humidity)
                && Objects.equals(windDirection, that.windDirection)
                && Objects.equals(windSpeed, that.windSpeed)
                && Objects.equals(windPower, that.windPower)
                && Objects.equals(rain, that.rain)
                && Objects.equals(feelsTemperature, that.feelsTemperature)
                && Objects.equals(airPressure, that.airPressure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, condition, temperature, humidity, windDirection, windSpeed,
                observedAtMillis, windPower, rain, feelsTemperature, airPressure);
    }

    @Override
    public String toString() {
        return "CurrentWeather{"
                + "cityId='" + cityId + '\''
                + ", condition='" + condition + '\''
                + ", temperature='" + temperature + '\''
                + ", humidity='" + humidity + '\''
                + ", windDirection='" + windDirection + '\''
                + ", windSpeed='" + windSpeed + '\''
                + ", observedAtMillis=" + observedAtMillis
                + ", windPower='" + windPower + '\''
                + ", rain='" + rain + '\''
                + ", feelsTemperature='" + feelsTemperature + '\''
                + ", airPressure='" + airPressure + '\''
                + '}';
    }
}
