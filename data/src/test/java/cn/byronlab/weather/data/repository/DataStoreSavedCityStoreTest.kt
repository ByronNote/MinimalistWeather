package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.byronlab.weather.data.openmeteo.OpenMeteoCity
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCodec
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreCityStoreTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun addPersistsNewestCityFirstWithoutDuplicates() = runTest(dispatcher) {
        val store = DataStoreCityStore(FakePreferencesDataStore(), dispatcher)

        store.addCity("beijing")
        store.addCity("shanghai")
        store.addCity("beijing")

        val result = store.getAddedCityIds()
        assertTrue(result is DomainResult.Success)
        assertEquals(listOf("shanghai", "beijing"), (result as DomainResult.Success).data)
    }

    @Test
    fun removeOnlyDeletesRequestedCity() = runTest(dispatcher) {
        val store = DataStoreCityStore(FakePreferencesDataStore(), dispatcher)
        store.addCity("beijing")
        store.addCity("shanghai")

        store.removeCity("shanghai")

        assertEquals(
            listOf("beijing"),
            (store.getAddedCityIds() as DomainResult.Success).data,
        )
    }

    @Test
    fun equivalentCityPayloadsShareOnePersistedUniqueId() = runTest(dispatcher) {
        val store = DataStoreCityStore(FakePreferencesDataStore(), dispatcher)
        val localCityId = shanghaiCityId(latitude = 31.2304, name = "上海")
        val remoteCityId = shanghaiCityId(latitude = 31.2222, name = "上海市")

        store.addCity(localCityId)
        store.addCity(remoteCityId)

        assertEquals(
            listOf(remoteCityId),
            (store.getAddedCityIds() as DomainResult.Success).data,
        )

        store.removeCity(localCityId)

        assertTrue((store.getAddedCityIds() as DomainResult.Success).data.isEmpty())
    }

    @Test
    fun legacyIdListIsReadWithSemanticDuplicatesCollapsed() = runTest(dispatcher) {
        val localCityId = shanghaiCityId(latitude = 31.2304, name = "上海")
        val remoteCityId = shanghaiCityId(latitude = 31.2222, name = "上海市")
        val legacyPreferences = preferencesOf(
            stringPreferencesKey("saved_city_ids") to Json.encodeToString(
                listOf(localCityId, remoteCityId),
            ),
        )
        val store = DataStoreCityStore(FakePreferencesDataStore(legacyPreferences), dispatcher)

        assertEquals(
            listOf(localCityId),
            (store.getAddedCityIds() as DomainResult.Success).data,
        )
    }

    @Test
    fun recentCitiesAreNewestFirstDeduplicatedAndLimitedToNine() = runTest(dispatcher) {
        val store = DataStoreCityStore(FakePreferencesDataStore(), dispatcher)

        (1..11).forEach { store.recordRecentCity("city-$it") }
        store.recordRecentCity("city-5")

        assertEquals(
            listOf("city-5", "city-11", "city-10", "city-9", "city-8", "city-7", "city-6", "city-4", "city-3"),
            (store.getRecentCityIds() as DomainResult.Success).data,
        )
    }

    private class FakePreferencesDataStore(
        initialPreferences: Preferences = emptyPreferences(),
    ) : DataStore<Preferences> {

        private val state = MutableStateFlow(initialPreferences)

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }
}

private fun shanghaiCityId(latitude: Double, name: String): String {
    return OpenMeteoCityCodec.encode(
        OpenMeteoCity(
            geonameId = "1796236",
            name = name,
            nameEn = "Shanghai",
            country = "中国",
            admin1 = "上海市",
            latitude = latitude,
            longitude = 121.4737,
            timezone = "Asia/Shanghai",
        ),
    )
}
