package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRefreshScheduler
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRefreshConfigurationUseCaseTest {

    @Test
    fun configureSchedulesPersistedInterval() = runTest {
        val settings = FakeSettingsRepository(WeatherRefreshInterval.OneHour)
        val scheduler = FakeScheduler()

        val result = ConfigureWeatherRefreshUseCase(settings, scheduler)()

        assertTrue(result is DomainResult.Success)
        assertEquals(WeatherRefreshInterval.OneHour, scheduler.interval)
    }

    @Test
    fun setPersistsAndReschedulesInterval() = runTest {
        val settings = FakeSettingsRepository(WeatherRefreshInterval.ThirtyMinutes)
        val scheduler = FakeScheduler()

        val result = SetWeatherRefreshIntervalUseCase(settings, scheduler)(WeatherRefreshInterval.ThreeHours)

        assertTrue(result is DomainResult.Success)
        assertEquals(WeatherRefreshInterval.ThreeHours, settings.interval)
        assertEquals(WeatherRefreshInterval.ThreeHours, scheduler.interval)
    }

    private class FakeSettingsRepository(
        var interval: WeatherRefreshInterval,
    ) : SettingsRepository {
        override fun observeCurrentCityId(): Flow<DomainResult<String>> = flowOf(DomainResult.success("city"))

        override suspend fun getCurrentCityId(): DomainResult<String> = DomainResult.success("city")

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> = DomainResult.success(Unit)

        override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> =
            flowOf(DomainResult.success(interval))

        override suspend fun getWeatherRefreshInterval(): DomainResult<WeatherRefreshInterval> =
            DomainResult.success(interval)

        override suspend fun setWeatherRefreshInterval(
            interval: WeatherRefreshInterval,
        ): DomainResult<Unit> {
            this.interval = interval
            return DomainResult.success(Unit)
        }
    }

    private class FakeScheduler : WeatherRefreshScheduler {
        var interval: WeatherRefreshInterval? = null

        override fun schedule(interval: WeatherRefreshInterval) {
            this.interval = interval
        }
    }
}
