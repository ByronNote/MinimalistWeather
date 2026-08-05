package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveAddedCityUseCaseTest {

    @Test
    fun blankCityIdReturnsInvalidInput() = runTest {
        val cityRepository = FakeCityRepository()
        val settingsRepository = FakeSettingsRepository("101")
        val useCase = RemoveAddedCityUseCase(cityRepository, settingsRepository)

        val result = useCase(" ")

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Type.INVALID_INPUT, (result as DomainResult.Failure).error.type)
        assertEquals(0, cityRepository.removeCallCount)
    }

    @Test
    fun deletingNonCurrentCityDoesNotChangeCurrentCity() = runTest {
        val cityRepository = FakeCityRepository(city("101"), city("102"))
        val settingsRepository = FakeSettingsRepository("102")
        val useCase = RemoveAddedCityUseCase(cityRepository, settingsRepository)

        val result = useCase("101")

        assertTrue(result is DomainResult.Success)
        assertEquals("102", settingsRepository.currentCityId)
        assertEquals(0, settingsRepository.setCallCount)
    }

    @Test
    fun deletingCurrentCitySwitchesToFirstRemainingCity() = runTest {
        val cityRepository = FakeCityRepository(city("101"), city("102"))
        val settingsRepository = FakeSettingsRepository("101")
        val useCase = RemoveAddedCityUseCase(cityRepository, settingsRepository)

        val result = useCase("101")

        assertTrue(result is DomainResult.Success)
        assertEquals("102", settingsRepository.currentCityId)
        assertEquals(1, settingsRepository.setCallCount)
    }

    @Test
    fun deletingLastCurrentCityClearsCurrentCity() = runTest {
        val cityRepository = FakeCityRepository(city("101"))
        val settingsRepository = FakeSettingsRepository("101")
        val useCase = RemoveAddedCityUseCase(cityRepository, settingsRepository)

        val result = useCase("101")

        assertTrue(result is DomainResult.Success)
        assertEquals("", settingsRepository.currentCityId)
    }

    private class FakeCityRepository(vararg cities: City) : CityRepository {

        private val addedCities = cities.toMutableList()
        var removeCallCount = 0

        override suspend fun searchCities(keyword: String): DomainResult<List<City>> {
            return DomainResult.success(addedCities.toList())
        }

        override suspend fun getPopularCities(): DomainResult<List<City>> {
            return DomainResult.success(addedCities.toList())
        }

        override fun resolveLocation(location: DeviceLocation): DomainResult<City> {
            return DomainResult.success(city("location"))
        }

        override fun observeAddedCities(): Flow<DomainResult<List<City>>> = flow {
            emit(getAddedCities())
        }

        override suspend fun getAddedCities(): DomainResult<List<City>> {
            return DomainResult.success(addedCities.toList())
        }

        override suspend fun addCity(cityId: String): DomainResult<Unit> {
            return DomainResult.success(Unit)
        }

        override suspend fun removeCity(cityId: String): DomainResult<Unit> {
            removeCallCount++
            addedCities.removeAll { it.cityId == cityId }
            return DomainResult.success(Unit)
        }

        override suspend fun getRecentCities() = DomainResult.success(emptyList<City>())

        override suspend fun recordRecentCity(cityId: String) = DomainResult.success(Unit)
    }

    private class FakeSettingsRepository(
        var currentCityId: String,
    ) : SettingsRepository {

        var setCallCount = 0

        override fun observeCurrentCityId(): Flow<DomainResult<String>> = flow {
            emit(getCurrentCityId())
        }

        override suspend fun getCurrentCityId(): DomainResult<String> {
            return DomainResult.success(currentCityId)
        }

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> {
            currentCityId = cityId
            setCallCount++
            return DomainResult.success(Unit)
        }

        override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> = flow {
            emit(getWeatherRefreshInterval())
        }

        override suspend fun getWeatherRefreshInterval() =
            DomainResult.success(WeatherRefreshInterval.ThirtyMinutes)

        override suspend fun setWeatherRefreshInterval(interval: WeatherRefreshInterval) =
            DomainResult.success(Unit)
    }
}

private fun city(cityId: String): City {
    return City(cityId, cityId, cityId, "", "", "", "")
}
