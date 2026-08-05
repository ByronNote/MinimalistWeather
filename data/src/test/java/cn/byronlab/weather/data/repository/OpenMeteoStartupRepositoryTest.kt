package cn.byronlab.weather.data.repository

import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCatalog
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoStartupRepositoryTest {

    @Test
    fun initialize_setsDefaultCityWhenCurrentCityIsBlank() = runTest {
        val settingsRepository = FakeSettingsRepository("")
        val cityRepository = FakeCityRepository()
        val repository = OpenMeteoStartupRepository(settingsRepository, cityRepository)

        val result = repository.initialize()

        assertTrue(result is DomainResult.Success)
        assertEquals(OpenMeteoCityCatalog.defaultCityId, settingsRepository.cityId)
        assertEquals(settingsRepository.cityId, cityRepository.addedCityId)
    }

    @Test
    fun initialize_replacesLegacyCityId() = runTest {
        val settingsRepository = FakeSettingsRepository("101010100")
        val cityRepository = FakeCityRepository()
        val repository = OpenMeteoStartupRepository(settingsRepository, cityRepository)

        val result = repository.initialize()

        assertTrue(result is DomainResult.Success)
        assertEquals(OpenMeteoCityCatalog.defaultCityId, settingsRepository.cityId)
        assertEquals(settingsRepository.cityId, cityRepository.addedCityId)
    }

    @Test
    fun initialize_keepsValidOpenMeteoCityId() = runTest {
        val settingsRepository = FakeSettingsRepository(OpenMeteoCityCatalog.popularCities[1].let {
            cn.byronlab.weather.data.openmeteo.OpenMeteoCityCodec.encode(it)
        })
        val originalCityId = settingsRepository.cityId
        val cityRepository = FakeCityRepository()
        val repository = OpenMeteoStartupRepository(settingsRepository, cityRepository)

        val result = repository.initialize()

        assertTrue(result is DomainResult.Success)
        assertEquals(originalCityId, settingsRepository.cityId)
        assertEquals(originalCityId, cityRepository.addedCityId)
    }

    private class FakeCityRepository : CityRepository {
        var addedCityId: String? = null

        override suspend fun searchCities(keyword: String) = DomainResult.success(emptyList<City>())

        override suspend fun getPopularCities() = DomainResult.success(emptyList<City>())

        override fun resolveLocation(location: DeviceLocation) =
            DomainResult.success(City("location", location.cityName, location.cityName, "", "", "", ""))

        override fun observeAddedCities(): Flow<DomainResult<List<City>>> = flowOf(getAddedCitiesResult())

        override suspend fun getAddedCities(): DomainResult<List<City>> = getAddedCitiesResult()

        override suspend fun addCity(cityId: String): DomainResult<Unit> {
            addedCityId = cityId
            return DomainResult.success(Unit)
        }

        override suspend fun removeCity(cityId: String) = DomainResult.success(Unit)

        override suspend fun getRecentCities() = DomainResult.success(emptyList<City>())

        override suspend fun recordRecentCity(cityId: String) = DomainResult.success(Unit)

        private fun getAddedCitiesResult(): DomainResult<List<City>> = DomainResult.success(emptyList())
    }

    private class FakeSettingsRepository(
        var cityId: String,
    ) : SettingsRepository {

        override fun observeCurrentCityId(): Flow<DomainResult<String>> {
            return flowOf(DomainResult.success(cityId))
        }

        override suspend fun getCurrentCityId(): DomainResult<String> {
            return DomainResult.success(cityId)
        }

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> {
            this.cityId = cityId
            return DomainResult.success(Unit)
        }

        override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> {
            return flowOf(DomainResult.success(WeatherRefreshInterval.ThirtyMinutes))
        }

        override suspend fun getWeatherRefreshInterval() =
            DomainResult.success(WeatherRefreshInterval.ThirtyMinutes)

        override suspend fun setWeatherRefreshInterval(interval: WeatherRefreshInterval) =
            DomainResult.success(Unit)
    }
}
