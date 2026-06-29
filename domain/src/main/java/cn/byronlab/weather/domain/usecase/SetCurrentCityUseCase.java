package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

public final class SetCurrentCityUseCase {

    private final SettingsRepository settingsRepository;

    public SetCurrentCityUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public DomainResult<Unit> execute(String cityId) {
        String normalizedCityId = UseCasePreconditions.trim(cityId);
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.invalidInput("cityId must not be blank."));
        }
        return settingsRepository.setCurrentCityId(normalizedCityId);
    }
}
