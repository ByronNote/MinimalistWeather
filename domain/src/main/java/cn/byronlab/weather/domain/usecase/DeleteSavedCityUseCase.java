package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import java.util.List;

public final class DeleteSavedCityUseCase {

    private final CityRepository cityRepository;
    private final SettingsRepository settingsRepository;

    public DeleteSavedCityUseCase(CityRepository cityRepository, SettingsRepository settingsRepository) {
        this.cityRepository = cityRepository;
        this.settingsRepository = settingsRepository;
    }

    public DomainResult<Unit> execute(String cityId) {
        String normalizedCityId = UseCasePreconditions.trim(cityId);
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.invalidInput("cityId must not be blank."));
        }

        DomainResult<String> currentCityResult = settingsRepository.getCurrentCityId();
        if (currentCityResult.isFailure()) {
            return DomainResult.failure(currentCityResult.getError());
        }

        DomainResult<Unit> deleteResult = cityRepository.deleteSavedCity(normalizedCityId);
        if (deleteResult.isFailure()) {
            return deleteResult;
        }

        if (!normalizedCityId.equals(currentCityResult.getData())) {
            return deleteResult;
        }

        DomainResult<List<City>> savedCitiesResult = cityRepository.getSavedCities();
        if (savedCitiesResult.isFailure()) {
            return DomainResult.failure(savedCitiesResult.getError());
        }

        String nextCityId = findFirstCityId(savedCitiesResult.getData());
        return settingsRepository.setCurrentCityId(nextCityId);
    }

    private static String findFirstCityId(List<City> cities) {
        if (cities == null || cities.isEmpty()) {
            return "";
        }
        City firstCity = cities.get(0);
        return firstCity == null || firstCity.getCityId() == null ? "" : firstCity.getCityId();
    }
}
