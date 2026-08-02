package cn.byronlab.weather.data.repository;

import cn.byronlab.weather.data.preference.PreferenceHelper;
import cn.byronlab.weather.data.preference.WeatherSettings;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import java.io.InvalidClassException;

import javax.inject.Inject;

public final class LegacySettingsRepository implements SettingsRepository {

    @Inject
    public LegacySettingsRepository() {
    }

    @Override
    public DomainResult<String> getCurrentCityId() {
        String cityId = PreferenceHelper.getSharedPreferences()
                .getString(WeatherSettings.SETTINGS_CURRENT_CITY_ID.getId(), "");
        return DomainResult.success(cityId == null ? "" : cityId);
    }

    @Override
    public DomainResult<Unit> setCurrentCityId(String cityId) {
        try {
            PreferenceHelper.savePreference(WeatherSettings.SETTINGS_CURRENT_CITY_ID, cityId);
            return DomainResult.success(Unit.INSTANCE);
        } catch (InvalidClassException exception) {
            return DomainResult.failure(new DomainError(
                    DomainError.Type.STORAGE,
                    "Current city setting could not be saved.",
                    exception
            ));
        }
    }
}
