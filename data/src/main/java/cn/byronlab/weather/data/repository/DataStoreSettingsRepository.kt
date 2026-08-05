package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.data.di.SettingsDataStore
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class DataStoreSettingsRepository @Inject constructor(
    @param:SettingsDataStore
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override fun observeCurrentCityId(): Flow<DomainResult<String>> {
        return dataStore.data
            .map<Preferences, DomainResult<String>> { preferences ->
                DomainResult.success(preferences[CURRENT_CITY_ID].orEmpty())
            }
            .catch { exception ->
                emit(DomainResult.failure(exception.toStorageError()))
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getCurrentCityId(): DomainResult<String> = withContext(ioDispatcher) {
        observeCurrentCityId().first()
    }

    override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[CURRENT_CITY_ID] = cityId
            }
            DomainResult.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> {
        return dataStore.data
            .map<Preferences, DomainResult<WeatherRefreshInterval>> { preferences ->
                DomainResult.success(preferences[WEATHER_REFRESH_INTERVAL].toWeatherRefreshInterval())
            }
            .catch { exception ->
                emit(DomainResult.failure(exception.toStorageError()))
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getWeatherRefreshInterval(): DomainResult<WeatherRefreshInterval> =
        withContext(ioDispatcher) {
            observeWeatherRefreshInterval().first()
        }

    override suspend fun setWeatherRefreshInterval(
        interval: WeatherRefreshInterval,
    ): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[WEATHER_REFRESH_INTERVAL] = interval.name
            }
            DomainResult.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    private fun Throwable.toStorageError(): DomainError {
        return DomainError(
            DomainError.Type.STORAGE,
            "Current city setting could not be saved.",
            this,
        )
    }

    companion object {
        const val LEGACY_SHARED_PREFERENCES_NAME = "cn.byronlab.weather"
        const val SETTINGS_DATASTORE_NAME = "weather_settings"

        private val CURRENT_CITY_ID = stringPreferencesKey("current_city_id")
        private val WEATHER_REFRESH_INTERVAL = stringPreferencesKey("weather_refresh_interval")
    }
}

private fun String?.toWeatherRefreshInterval(): WeatherRefreshInterval {
    return WeatherRefreshInterval.entries.firstOrNull { it.name == this }
        ?: WeatherRefreshInterval.ThirtyMinutes
}
