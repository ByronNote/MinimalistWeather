package cn.byronlab.weather.domain.repository;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.result.DomainResult;

import java.util.List;

public interface CityRepository {

    DomainResult<List<City>> searchCities(String keyword);

    DomainResult<List<City>> getSavedCities();

    DomainResult<Unit> deleteSavedCity(String cityId);
}
