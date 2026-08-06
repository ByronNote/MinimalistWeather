package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.byronlab.weather.data.di.IoDispatcher
import cn.byronlab.weather.data.di.SettingsDataStore
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCodec
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
import kotlinx.serialization.Serializable
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
                DomainResult.success(
                    preferences.cityRecords(ADDED_CITY_RECORDS, LEGACY_ADDED_CITY_IDS)
                        .map(PersistedCityRef::cityId),
                )
            }
            .catch { exception ->
                emit(DomainResult.failure(exception.toStorageError()))
            }
            .flowOn(ioDispatcher)
    }

    suspend fun getAddedCityIds(): DomainResult<List<String>> = withContext(ioDispatcher) {
        observeAddedCityIds().first()
    }

    suspend fun addCity(cityId: String): DomainResult<Unit> =
        update(ADDED_CITY_RECORDS, LEGACY_ADDED_CITY_IDS) { cities ->
            val city = cityRef(cityId)
            if (cities.any { it.uniqueId == city.uniqueId }) {
                cities.map { existing ->
                    if (existing.uniqueId == city.uniqueId) city else existing
                }
            } else {
                listOf(city) + cities
            }
    }

    suspend fun removeCity(cityId: String): DomainResult<Unit> =
        update(ADDED_CITY_RECORDS, LEGACY_ADDED_CITY_IDS) { cities ->
            val uniqueId = OpenMeteoCityCodec.uniqueId(cityId)
            cities.filterNot { it.uniqueId == uniqueId }
    }

    suspend fun getRecentCityIds(): DomainResult<List<String>> = withContext(ioDispatcher) {
        try {
            DomainResult.success(
                dataStore.data.first()
                    .cityRecords(RECENT_CITY_RECORDS, LEGACY_RECENT_CITY_IDS)
                    .map(PersistedCityRef::cityId),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    suspend fun recordRecentCity(cityId: String): DomainResult<Unit> =
        update(RECENT_CITY_RECORDS, LEGACY_RECENT_CITY_IDS) { cities ->
            val city = cityRef(cityId)
            (listOf(city) + cities.filterNot { it.uniqueId == city.uniqueId }).take(MAX_RECENT_CITIES)
        }

    private suspend fun update(
        recordsKey: Preferences.Key<String>,
        legacyIdsKey: Preferences.Key<String>,
        transform: (List<PersistedCityRef>) -> List<PersistedCityRef>,
    ): DomainResult<Unit> = withContext(ioDispatcher) {
        try {
            dataStore.edit { preferences ->
                preferences[recordsKey] = Json.encodeToString(
                    transform(preferences.cityRecords(recordsKey, legacyIdsKey)),
                )
                preferences.remove(legacyIdsKey)
            }
            DomainResult.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DomainResult.failure(exception.toStorageError())
        }
    }

    private fun Preferences.cityRecords(
        recordsKey: Preferences.Key<String>,
        legacyIdsKey: Preferences.Key<String>,
    ): List<PersistedCityRef> {
        val records = this[recordsKey].orEmpty()
        if (records.isNotBlank()) {
            return Json.decodeFromString<List<PersistedCityRef>>(records)
                .mapNotNull { record ->
                    val cityId = record.cityId.trim()
                    if (cityId.isEmpty()) {
                        null
                    } else {
                        PersistedCityRef(
                            uniqueId = record.uniqueId.trim().ifBlank {
                                OpenMeteoCityCodec.uniqueId(cityId)
                            },
                            cityId = cityId,
                        )
                    }
                }
                .distinctBy(PersistedCityRef::uniqueId)
        }
        return legacyCityIds(legacyIdsKey)
            .map(::cityRef)
            .distinctBy(PersistedCityRef::uniqueId)
    }

    private fun Preferences.legacyCityIds(key: Preferences.Key<String>): List<String> {
        val encoded = this[key].orEmpty()
        if (encoded.isBlank()) return emptyList()
        return Json.decodeFromString<List<String>>(encoded)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
    }

    private fun cityRef(cityId: String): PersistedCityRef {
        return PersistedCityRef(
            uniqueId = OpenMeteoCityCodec.uniqueId(cityId),
            cityId = cityId,
        )
    }

    private fun Throwable.toStorageError(): DomainError {
        return DomainError(
            DomainError.Type.STORAGE,
            "City data could not be persisted.",
            this,
        )
    }

    companion object {
        private val ADDED_CITY_RECORDS = stringPreferencesKey("added_city_records_v2")
        private val RECENT_CITY_RECORDS = stringPreferencesKey("recent_city_records_v2")
        // Read old ID-only lists once and migrate them on the next write.
        private val LEGACY_ADDED_CITY_IDS = stringPreferencesKey("saved_city_ids")
        private val LEGACY_RECENT_CITY_IDS = stringPreferencesKey("recent_city_ids")
        private const val MAX_RECENT_CITIES = 9
    }
}

@Serializable
internal data class PersistedCityRef(
    val uniqueId: String,
    val cityId: String,
)
