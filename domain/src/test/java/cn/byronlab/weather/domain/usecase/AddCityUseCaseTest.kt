package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddCityUseCaseTest {

    @Test
    fun addingCityPersistsItBeforeSelectingIt() = runTest {
        val calls = mutableListOf<String>()
        val cityRepository = FakeCityRepository(calls)
        val settingsRepository = FakeSettingsRepository(calls)

        val result = AddCityUseCase(cityRepository, settingsRepository)(" city ")

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf("add:city", "select:city"), calls)
        assertEquals("city", settingsRepository.currentCityId)
    }

    @Test
    fun addFailureDoesNotChangeCurrentCity() = runTest {
        val calls = mutableListOf<String>()
        val cityRepository = FakeCityRepository(calls, addFails = true)
        val settingsRepository = FakeSettingsRepository(calls)

        val result = AddCityUseCase(cityRepository, settingsRepository)("city")

        assertTrue(result is DomainResult.Failure)
        assertEquals(listOf("add:city"), calls)
        assertEquals("original", settingsRepository.currentCityId)
    }

    private class FakeCityRepository(
        private val calls: MutableList<String>,
        private val addFails: Boolean = false,
    ) : CityRepository {

        override suspend fun searchCities(keyword: String) = DomainResult.success(emptyList<City>())

        override suspend fun getPopularCities() = DomainResult.success(emptyList<City>())

        override fun resolveLocation(location: DeviceLocation) = DomainResult.success(city("location"))

        override fun observeAddedCities(): Flow<DomainResult<List<City>>> =
            flowOf(DomainResult.success(emptyList()))

        override suspend fun getAddedCities() = DomainResult.success(emptyList<City>())

        override suspend fun addCity(cityId: String): DomainResult<Unit> {
            calls += "add:$cityId"
            return if (addFails) {
                DomainResult.failure(DomainError(DomainError.Type.STORAGE))
            } else {
                DomainResult.success(Unit)
            }
        }

        override suspend fun removeCity(cityId: String) = DomainResult.success(Unit)

        override suspend fun getRecentCities() = DomainResult.success(emptyList<City>())

        override suspend fun recordRecentCity(cityId: String) = DomainResult.success(Unit)
    }

    private class FakeSettingsRepository(
        private val calls: MutableList<String>,
    ) : SettingsRepository {
        var currentCityId = "original"

        override fun observeCurrentCityId(): Flow<DomainResult<String>> =
            flowOf(DomainResult.success(currentCityId))

        override suspend fun getCurrentCityId() = DomainResult.success(currentCityId)

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> {
            calls += "select:$cityId"
            currentCityId = cityId
            return DomainResult.success(Unit)
        }

        override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> =
            flowOf(DomainResult.success(WeatherRefreshInterval.ThirtyMinutes))

        override suspend fun getWeatherRefreshInterval() =
            DomainResult.success(WeatherRefreshInterval.ThirtyMinutes)

        override suspend fun setWeatherRefreshInterval(interval: WeatherRefreshInterval) =
            DomainResult.success(Unit)
    }
}

private fun city(cityId: String): City = City(cityId, cityId, cityId, "", "", "", "")
