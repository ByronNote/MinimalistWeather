package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun getCurrentCityId_returnsBlankByDefault() = runTest(dispatcher) {
        val repository = DataStoreSettingsRepository(FakePreferencesDataStore(), dispatcher)

        val result = repository.getCurrentCityId()

        assertTrue(result is DomainResult.Success)
        assertEquals("", (result as DomainResult.Success).data)
    }

    @Test
    fun setCurrentCityId_persistsCityId() = runTest(dispatcher) {
        val repository = DataStoreSettingsRepository(FakePreferencesDataStore(), dispatcher)

        val setResult = repository.setCurrentCityId("openmeteo-city-id")
        val getResult = repository.getCurrentCityId()

        assertTrue(setResult is DomainResult.Success)
        assertTrue(getResult is DomainResult.Success)
        assertEquals("openmeteo-city-id", (getResult as DomainResult.Success).data)
    }

    @Test
    fun observeCurrentCityId_emitsPersistedValue() = runTest(dispatcher) {
        val repository = DataStoreSettingsRepository(FakePreferencesDataStore(), dispatcher)
        repository.setCurrentCityId("london")

        val result = repository.observeCurrentCityId().first()

        assertTrue(result is DomainResult.Success)
        assertEquals("london", (result as DomainResult.Success).data)
    }

    @Test
    fun weatherRefreshIntervalDefaultsToThirtyMinutesAndPersistsChanges() = runTest(dispatcher) {
        val repository = DataStoreSettingsRepository(FakePreferencesDataStore(), dispatcher)

        val defaultResult = repository.getWeatherRefreshInterval()
        repository.setWeatherRefreshInterval(WeatherRefreshInterval.OneHour)
        val updatedResult = repository.getWeatherRefreshInterval()

        assertEquals(
            WeatherRefreshInterval.ThirtyMinutes,
            (defaultResult as DomainResult.Success).data,
        )
        assertEquals(
            WeatherRefreshInterval.OneHour,
            (updatedResult as DomainResult.Success).data,
        )
    }

    @Test(expected = CancellationException::class)
    fun setCurrentCityId_rethrowsCancellation() = runTest(dispatcher) {
        val repository = DataStoreSettingsRepository(
            FakePreferencesDataStore(updateFailure = CancellationException("cancelled")),
            dispatcher,
        )

        repository.setCurrentCityId("london")
    }

    private class FakePreferencesDataStore(
        initialPreferences: Preferences = emptyPreferences(),
        private val updateFailure: Throwable? = null,
    ) : DataStore<Preferences> {

        private val state = MutableStateFlow(initialPreferences)

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            updateFailure?.let { throw it }
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }
}
