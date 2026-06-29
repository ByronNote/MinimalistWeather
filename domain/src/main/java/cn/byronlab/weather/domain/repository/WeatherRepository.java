package cn.byronlab.weather.domain.repository;

import cn.byronlab.weather.domain.model.Weather;
import cn.byronlab.weather.domain.result.DomainResult;

public interface WeatherRepository {

    DomainResult<Weather> getWeather(String cityId, boolean refreshNow);

    DomainResult<Weather> refreshWeather(String cityId);
}
