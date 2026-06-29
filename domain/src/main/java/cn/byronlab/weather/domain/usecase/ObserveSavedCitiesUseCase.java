package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.result.DomainResult;

import java.util.List;

public final class ObserveSavedCitiesUseCase {

    private final CityRepository cityRepository;

    public ObserveSavedCitiesUseCase(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public DomainResult<List<City>> execute() {
        return cityRepository.getSavedCities();
    }
}
