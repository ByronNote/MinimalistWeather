package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.data.di.SettingsDataStore
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class DataStoreCityStore @Inject constructor(
    @param:SettingsDataStore
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun observeAddedCityIds(): Flow<DomainResult<List<String>>> {
        return dataStore.data
            .map<Preferences, DomainResult<List<String>>> { preferences ->
                DomainResult.success(preferences.cityIds(ADDED_CITY_IDS))
            }
            .catch { exception ->
                emit(DomainResult.failure(exception.toStorageError()))
            }
            .flowOn(ioDispatcher)
    }

    suspend fun getAddedCityIds(): DomainResult<List<String>> = withContext(ioDispatcher) {
        observeAddedCityIds().first()
    }

    suspend fun addCity(cityId: String): DomainResult<Unit> = update(ADDED_CITY_IDS) { cityIds ->
        if (cityId in cityIds) cityIds else listOf(cityId) + cityIds
    }

    suspend fun removeCity(cityId: String): DomainResult<Unit> = update(ADDED_CITY_IDS) { cityIds ->
        cityIds.filterNot { it == cityId }
    }

    suspend fun getRecentCityIds(): DomainResult<List<String>> = withContext(ioDispatcher) {
        try {
            DomainResult.success(dataStore.data.first().cityIds(RECENT_CITY_IDS))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    suspend fun recordRecentCity(cityId: String): DomainResult<Unit> = update(RECENT_CITY_IDS) { cityIds ->
        (listOf(cityId) + cityIds.filterNot { it == cityId }).take(MAX_RECENT_CITIES)
    }

    private suspend fun update(
        key: Preferences.Key<String>,
        transform: (List<String>) -> List<String>,
    ): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = Json.encodeToString(transform(preferences.cityIds(key)))
            }
            DomainResult.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    private fun Preferences.cityIds(key: Preferences.Key<String>): List<String> {
        val encoded = this[key].orEmpty()
        if (encoded.isBlank()) {
            return emptyList()
        }
        return Json.decodeFromString<List<String>>(encoded)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
    }

    private fun Throwable.toStorageError(): DomainError {
        return DomainError(
            DomainError.Type.STORAGE,
            "City data could not be persisted.",
            this,
        )
    }

    companion object {
        // Keep the legacy preference key so existing users retain their city list.
        private val ADDED_CITY_IDS = stringPreferencesKey("saved_city_ids")
        private val RECENT_CITY_IDS = stringPreferencesKey("recent_city_ids")
        private const val MAX_RECENT_CITIES = 9
    }
}
