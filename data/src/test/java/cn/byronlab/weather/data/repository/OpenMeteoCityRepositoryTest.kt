package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCatalog
import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCodec
import cn.byronlab.weather.data.openmeteo.OpenMeteoClient
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class OpenMeteoCityRepositoryTest {

    @Test
    fun getPopularCities_returnsLocalCatalogCitiesWithEncodedIds() = runTest {
        val repository = OpenMeteoCityRepository(
            client = OpenMeteoClient(OkHttpClient()),
            cityStore = DataStoreCityStore(
                FakePreferencesDataStore(),
                UnconfinedTestDispatcher(testScheduler),
            ),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = repository.getPopularCities()

        assertTrue(result is DomainResult.Success)
        val cities = (result as DomainResult.Success).data
        assertEquals(OpenMeteoCityCatalog.popularCities.size, cities.size)
        assertEquals(
            OpenMeteoCityCodec.encode(OpenMeteoCityCatalog.popularCities.first()),
            cities.first().cityId,
        )
    }

    @Test
    fun observeAddedCities_tracksPersistedCityChanges() = runTest {
        val cityStore = DataStoreCityStore(
            FakePreferencesDataStore(),
            UnconfinedTestDispatcher(testScheduler),
        )
        val repository = OpenMeteoCityRepository(
            client = OpenMeteoClient(OkHttpClient()),
            cityStore = cityStore,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )
        val emissions = mutableListOf<DomainResult<List<City>>>()

        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeAddedCities().take(2).toList(emissions)
        }
        repository.addCity(OpenMeteoCityCatalog.defaultCityId)
        collectionJob.join()

        assertTrue((emissions.first() as DomainResult.Success).data.isEmpty())
        assertEquals(
            OpenMeteoCityCatalog.defaultCity.name,
            (emissions.last() as DomainResult.Success).data.single().name,
        )
    }

    @Test
    fun resolveLocation_createsWeatherCompatibleCity() = runTest {
        val repository = OpenMeteoCityRepository(
            client = OpenMeteoClient(OkHttpClient()),
            cityStore = DataStoreCityStore(
                FakePreferencesDataStore(),
                UnconfinedTestDispatcher(testScheduler),
            ),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = repository.resolveLocation(
            DeviceLocation(31.2304, 121.4737, "上海", "中国", "上海"),
        )

        assertTrue(result is DomainResult.Success)
        val city = (result as DomainResult.Success).data
        assertEquals("上海", city.name)
        assertEquals("31.2304", city.latitude)
        assertTrue(OpenMeteoCityCodec.decode(city.cityId) != null)
    }

    @Test(expected = CancellationException::class)
    fun searchCities_rethrowsCancellation() = runTest {
        val cancellingClient = OkHttpClient.Builder()
            .addInterceptor { throw CancellationException("cancelled") }
            .build()
        val repository = OpenMeteoCityRepository(
            client = OpenMeteoClient(cancellingClient),
            cityStore = DataStoreCityStore(
                FakePreferencesDataStore(),
                UnconfinedTestDispatcher(testScheduler),
            ),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        repository.searchCities("no-local-city-matches-this")
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
