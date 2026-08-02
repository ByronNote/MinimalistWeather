package cn.byronlab.weather.data.repository;

import android.content.Context;

import cn.byronlab.weather.data.db.dao.WeatherDao;
import cn.byronlab.weather.data.mapper.LegacyWeatherDomainMapper;
import cn.byronlab.weather.domain.model.Weather;
import cn.byronlab.weather.domain.repository.WeatherRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.inject.Inject;

public final class LegacyWeatherRepository implements WeatherRepository {

    private final Context context;
    private final WeatherDao weatherDao;
    private final LegacyWeatherDomainMapper mapper;

    @Inject
    public LegacyWeatherRepository(Context context, WeatherDao weatherDao) {
        this.context = context;
        this.weatherDao = weatherDao;
        this.mapper = new LegacyWeatherDomainMapper();
    }

    @Override
    public DomainResult<Weather> getWeather(String cityId, boolean refreshNow) {
        return loadWeather(cityId, refreshNow);
    }

    @Override
    public DomainResult<Weather> refreshWeather(String cityId) {
        return loadWeather(cityId, true);
    }

    private DomainResult<Weather> loadWeather(String cityId, boolean refreshNow) {
        try {
            cn.byronlab.weather.data.db.entities.minimalist.Weather legacyWeather =
                    WeatherDataRepository.getWeather(context, cityId, weatherDao, refreshNow)
                            .toBlocking()
                            .lastOrDefault(null);
            Weather weather = mapper.map(legacyWeather);
            if (weather == null) {
                return DomainResult.failure(new DomainError(
                        DomainError.Type.NOT_FOUND,
                        "Weather data was not found."
                ));
            }
            return DomainResult.success(weather);
        } catch (Throwable throwable) {
            return DomainResult.failure(toDomainError(throwable));
        }
    }

    private DomainError toDomainError(Throwable throwable) {
        if (hasCause(throwable, SocketTimeoutException.class)
                || hasCause(throwable, UnknownHostException.class)) {
            return new DomainError(DomainError.Type.NETWORK, "Weather network request failed.", throwable);
        }
        if (hasCause(throwable, SQLException.class)) {
            return new DomainError(DomainError.Type.STORAGE, "Weather storage failed.", throwable);
        }
        return DomainError.unknown(throwable);
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> target) {
        Throwable current = throwable;
        while (current != null) {
            if (target.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
