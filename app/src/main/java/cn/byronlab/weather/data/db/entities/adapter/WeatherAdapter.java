package cn.byronlab.weather.data.db.entities.adapter;

import cn.byronlab.weather.data.db.entities.minimalist.AirQualityLive;
import cn.byronlab.weather.data.db.entities.minimalist.LifeIndex;
import cn.byronlab.weather.data.db.entities.minimalist.Weather;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherLive;

import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public abstract class WeatherAdapter {

    public abstract String getCityId();

    public abstract String getCityName();

    public abstract String getCityNameEn();

    public abstract WeatherLive getWeatherLive();

    public abstract List<WeatherForecast> getWeatherForecasts();

    public abstract List<LifeIndex> getLifeIndexes();

    public abstract AirQualityLive getAirQualityLive();

    public Weather getWeather() {

        Weather weather = new Weather();
        weather.setCityId(getCityId());
        weather.setCityName(getCityName());
        weather.setCityNameEn(getCityNameEn());
        weather.setAirQualityLive(getAirQualityLive());
        weather.setWeatherForecasts(getWeatherForecasts());
        weather.setLifeIndexes(getLifeIndexes());
        weather.setWeatherLive(getWeatherLive());
        return weather;
    }
}
