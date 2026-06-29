package cn.byronlab.weather.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Weather {

    private final String cityId;
    private final String cityName;
    private final String cityNameEn;
    private final CurrentWeather currentWeather;
    private final List<ForecastDay> forecastDays;
    private final AirQuality airQuality;
    private final List<LifeIndex> lifeIndexes;

    public Weather(String cityId, String cityName, String cityNameEn,
                   CurrentWeather currentWeather, List<ForecastDay> forecastDays,
                   AirQuality airQuality, List<LifeIndex> lifeIndexes) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.cityNameEn = cityNameEn;
        this.currentWeather = currentWeather;
        this.forecastDays = immutableCopy(forecastDays);
        this.airQuality = airQuality;
        this.lifeIndexes = immutableCopy(lifeIndexes);
    }

    public String getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCityNameEn() {
        return cityNameEn;
    }

    public CurrentWeather getCurrentWeather() {
        return currentWeather;
    }

    public List<ForecastDay> getForecastDays() {
        return forecastDays;
    }

    public AirQuality getAirQuality() {
        return airQuality;
    }

    public List<LifeIndex> getLifeIndexes() {
        return lifeIndexes;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Weather)) {
            return false;
        }
        Weather weather = (Weather) o;
        return Objects.equals(cityId, weather.cityId)
                && Objects.equals(cityName, weather.cityName)
                && Objects.equals(cityNameEn, weather.cityNameEn)
                && Objects.equals(currentWeather, weather.currentWeather)
                && Objects.equals(forecastDays, weather.forecastDays)
                && Objects.equals(airQuality, weather.airQuality)
                && Objects.equals(lifeIndexes, weather.lifeIndexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, cityName, cityNameEn, currentWeather, forecastDays,
                airQuality, lifeIndexes);
    }
}
