package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetCurrentCityUseCaseTest {

    @Test
    fun selectingCityUpdatesCurrentCityWithoutAddingIt() = runTest {
        val calls = mutableListOf<String>()
        val settingsRepository = FakeSettingsRepository(calls)

        val result = SetCurrentCityUseCase(settingsRepository)(" city ")

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf("select:city"), calls)
        assertEquals("city", settingsRepository.currentCityId)
    }

    @Test
    fun blankCityDoesNotChangeCurrentCity() = runTest {
        val calls = mutableListOf<String>()
        val settingsRepository = FakeSettingsRepository(calls)

        val result = SetCurrentCityUseCase(settingsRepository)(" ")

        assertTrue(result is DomainResult.Failure)
        assertTrue(calls.isEmpty())
        assertEquals("original", settingsRepository.currentCityId)
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
