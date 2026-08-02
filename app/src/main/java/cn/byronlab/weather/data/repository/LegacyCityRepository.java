package cn.byronlab.weather.data.repository;

import cn.byronlab.weather.data.db.dao.CityDao;
import cn.byronlab.weather.data.db.dao.WeatherDao;
import cn.byronlab.weather.data.mapper.LegacyCityDomainMapper;
import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public final class LegacyCityRepository implements CityRepository {

    private final CityDao cityDao;
    private final WeatherDao weatherDao;
    private final LegacyCityDomainMapper mapper;

    @Inject
    public LegacyCityRepository(CityDao cityDao, WeatherDao weatherDao) {
        this.cityDao = cityDao;
        this.weatherDao = weatherDao;
        this.mapper = new LegacyCityDomainMapper();
    }

    @Override
    public DomainResult<List<City>> searchCities(String keyword) {
        try {
            List<cn.byronlab.weather.data.db.entities.City> legacyCities = cityDao.queryCityList();
            if (legacyCities == null || legacyCities.isEmpty()) {
                return DomainResult.success(Collections.emptyList());
            }

            String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
            List<City> result = new ArrayList<>();
            for (cn.byronlab.weather.data.db.entities.City legacyCity : legacyCities) {
                if (matches(legacyCity, normalizedKeyword)) {
                    City city = mapper.map(legacyCity);
                    if (city != null) {
                        result.add(city);
                    }
                }
            }
            return DomainResult.success(result);
        } catch (Throwable throwable) {
            return DomainResult.failure(toStorageError(throwable));
        }
    }

    @Override
    public DomainResult<List<City>> getSavedCities() {
        try {
            List<cn.byronlab.weather.data.db.entities.minimalist.Weather> weathers =
                    weatherDao.queryAllSaveCity();
            if (weathers == null || weathers.isEmpty()) {
                return DomainResult.success(Collections.emptyList());
            }

            List<City> result = new ArrayList<>(weathers.size());
            for (cn.byronlab.weather.data.db.entities.minimalist.Weather weather : weathers) {
                City city = mapper.map(weather);
                if (city != null) {
                    result.add(city);
                }
            }
            return DomainResult.success(result);
        } catch (SQLException exception) {
            return DomainResult.failure(toStorageError(exception));
        }
    }

    @Override
    public DomainResult<Unit> deleteSavedCity(String cityId) {
        try {
            weatherDao.deleteById(cityId);
            return DomainResult.success(Unit.INSTANCE);
        } catch (SQLException exception) {
            return DomainResult.failure(toStorageError(exception));
        }
    }

    private boolean matches(cn.byronlab.weather.data.db.entities.City city, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        return contains(city.getCityName(), keyword)
                || contains(city.getCityNameEn(), keyword)
                || contains(city.getParent(), keyword)
                || contains(city.getRoot(), keyword)
                || String.valueOf(city.getCityId()).contains(keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private DomainError toStorageError(Throwable throwable) {
        return new DomainError(DomainError.Type.STORAGE, "City storage failed.", throwable);
    }
}
