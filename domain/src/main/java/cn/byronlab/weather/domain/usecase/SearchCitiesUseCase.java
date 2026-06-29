package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.result.DomainResult;

import java.util.Collections;
import java.util.List;

public final class SearchCitiesUseCase {

    private final CityRepository cityRepository;

    public SearchCitiesUseCase(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public DomainResult<List<City>> execute(String keyword) {
        String normalizedKeyword = UseCasePreconditions.trim(keyword);
        if (normalizedKeyword.isEmpty()) {
            return DomainResult.success(Collections.emptyList());
        }
        return cityRepository.searchCities(normalizedKeyword);
    }
}
