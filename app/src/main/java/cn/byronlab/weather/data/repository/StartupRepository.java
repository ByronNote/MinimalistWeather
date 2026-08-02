package cn.byronlab.weather.data.repository;

import cn.byronlab.weather.data.db.CityDatabaseHelper;
import cn.byronlab.weather.data.preference.PreferenceHelper;
import cn.byronlab.weather.data.preference.WeatherSettings;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.AppStartupRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import java.io.InvalidClassException;

import javax.inject.Inject;

public final class StartupRepository implements AppStartupRepository {

    @Inject
    public StartupRepository() {
    }

    @Override
    public DomainResult<Unit> initialize() {
        try {
            PreferenceHelper.loadDefaults();
            if (PreferenceHelper.getSharedPreferences()
                    .getBoolean(WeatherSettings.SETTINGS_FIRST_USE.getId(), false)) {
                PreferenceHelper.savePreference(WeatherSettings.SETTINGS_CURRENT_CITY_ID, "101020100");
                PreferenceHelper.savePreference(WeatherSettings.SETTINGS_FIRST_USE, false);
            }
            CityDatabaseHelper.importCityDB();
            return DomainResult.success(Unit.INSTANCE);
        } catch (InvalidClassException exception) {
            return DomainResult.failure(new DomainError(
                    DomainError.Type.STORAGE,
                    "Startup preferences could not be initialized.",
                    exception
            ));
        } catch (Throwable throwable) {
            return DomainResult.failure(DomainError.unknown(throwable));
        }
    }
}
