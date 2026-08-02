package cn.byronlab.weather.data.mapper;

import cn.byronlab.weather.data.db.entities.minimalist.AirQualityLive;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherLive;
import cn.byronlab.weather.domain.model.AirQuality;
import cn.byronlab.weather.domain.model.CurrentWeather;
import cn.byronlab.weather.domain.model.ForecastDay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyWeatherDomainMapper {

    public cn.byronlab.weather.domain.model.Weather map(
            cn.byronlab.weather.data.db.entities.minimalist.Weather source) {
        if (source == null) {
            return null;
        }

        return new cn.byronlab.weather.domain.model.Weather(
                safeString(source.getCityId()),
                safeString(source.getCityName()),
                safeString(source.getCityNameEn()),
                mapCurrentWeather(source.getWeatherLive()),
                mapForecasts(source.getWeatherForecasts()),
                mapAirQuality(source.getAirQualityLive()),
                mapLifeIndexes(source.getLifeIndexes())
        );
    }

    private CurrentWeather mapCurrentWeather(WeatherLive source) {
        if (source == null) {
            return null;
        }
        return new CurrentWeather(
                safeString(source.getCityId()),
                safeString(source.getWeather()),
                safeString(source.getTemp()),
                safeString(source.getHumidity()),
                safeString(source.getWind()),
                safeString(source.getWindSpeed()),
                source.getTime(),
                safeString(source.getWindPower()),
                safeString(source.getRain()),
                safeString(source.getFeelsTemperature()),
                safeString(source.getAirPressure())
        );
    }

    private List<ForecastDay> mapForecasts(List<WeatherForecast> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<ForecastDay> result = new ArrayList<>(source.size());
        for (WeatherForecast forecast : source) {
            if (forecast == null) {
                continue;
            }
            result.add(new ForecastDay(
                    safeString(forecast.getCityId()),
                    safeString(forecast.getWeather()),
                    safeString(forecast.getWeatherDay()),
                    safeString(forecast.getWeatherNight()),
                    forecast.getTempMax(),
                    forecast.getTempMin(),
                    safeString(forecast.getWind()),
                    safeString(forecast.getDate()),
                    safeString(forecast.getWeek()),
                    safeString(forecast.getPop()),
                    safeString(forecast.getUv()),
                    safeString(forecast.getVisibility()),
                    safeString(forecast.getHumidity()),
                    safeString(forecast.getPressure()),
                    safeString(forecast.getPrecipitation()),
                    safeString(forecast.getSunrise()),
                    safeString(forecast.getSunset()),
                    safeString(forecast.getMoonrise()),
                    safeString(forecast.getMoonset())
            ));
        }
        return result;
    }

    private AirQuality mapAirQuality(AirQualityLive source) {
        if (source == null) {
            return null;
        }
        return new AirQuality(
                safeString(source.getCityId()),
                source.getAqi(),
                source.getPm25(),
                source.getPm10(),
                safeString(source.getPublishTime()),
                safeString(source.getAdvice()),
                safeString(source.getCityRank()),
                safeString(source.getQuality()),
                safeString(source.getCo()),
                safeString(source.getSo2()),
                safeString(source.getNo2()),
                safeString(source.getO3()),
                safeString(source.getPrimary())
        );
    }

    private List<cn.byronlab.weather.domain.model.LifeIndex> mapLifeIndexes(
            List<cn.byronlab.weather.data.db.entities.minimalist.LifeIndex> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<cn.byronlab.weather.domain.model.LifeIndex> result = new ArrayList<>(source.size());
        for (cn.byronlab.weather.data.db.entities.minimalist.LifeIndex lifeIndex : source) {
            if (lifeIndex == null) {
                continue;
            }
            result.add(new cn.byronlab.weather.domain.model.LifeIndex(
                    safeString(lifeIndex.getCityId()),
                    safeString(lifeIndex.getName()),
                    safeString(lifeIndex.getIndex()),
                    safeString(lifeIndex.getDetails())
            ));
        }
        return result;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
