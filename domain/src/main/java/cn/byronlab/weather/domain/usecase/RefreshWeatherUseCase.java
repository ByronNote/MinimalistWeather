package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.Weather;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.repository.WeatherRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

public final class RefreshWeatherUseCase {

    private final WeatherRepository weatherRepository;
    private final SettingsRepository settingsRepository;

    public RefreshWeatherUseCase(WeatherRepository weatherRepository,
                                 SettingsRepository settingsRepository) {
        this.weatherRepository = weatherRepository;
        this.settingsRepository = settingsRepository;
    }

    public DomainResult<Weather> execute() {
        DomainResult<String> currentCityResult = settingsRepository.getCurrentCityId();
        if (currentCityResult.isFailure()) {
            return DomainResult.failure(currentCityResult.getError());
        }
        return execute(currentCityResult.getData());
    }

    public DomainResult<Weather> execute(String cityId) {
        String normalizedCityId = UseCasePreconditions.trim(cityId);
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.missingCurrentCity());
        }
        return weatherRepository.refreshWeather(normalizedCityId);
    }
}
