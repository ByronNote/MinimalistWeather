package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
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
