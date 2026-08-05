package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.model.WeatherCondition
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCurrentWeatherUseCaseTest {

    @Test
    fun missingCurrentCityReturnsDomainError() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val settingsRepository = FakeSettingsRepository("")
        val useCase = GetCurrentWeatherUseCase(weatherRepository, settingsRepository)

        val result = useCase()

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Type.MISSING_CURRENT_CITY, (result as DomainResult.Failure).error.type)
        assertEquals(0, weatherRepository.callCount)
    }

    @Test
    fun currentCityIsTrimmedBeforeLoadingWeather() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val settingsRepository = FakeSettingsRepository(" 101010100 ")
        val useCase = GetCurrentWeatherUseCase(weatherRepository, settingsRepository)

        val result = useCase()

        assertTrue(result is DomainResult.Success)
        assertEquals("101010100", weatherRepository.lastCityId)
    }

    private class FakeWeatherRepository : WeatherRepository {

        var callCount = 0
        var lastCityId: String? = null

        override suspend fun getWeather(cityId: String, refreshNow: Boolean): DomainResult<Weather> {
            callCount++
            lastCityId = cityId
            return DomainResult.success(weather(cityId))
        }

        override suspend fun refreshWeather(cityId: String): DomainResult<Weather> {
            return getWeather(cityId, refreshNow = true)
        }
    }

    private class FakeSettingsRepository(
        private val currentCityId: String,
    ) : SettingsRepository {

        override fun observeCurrentCityId(): Flow<DomainResult<String>> = flow {
            emit(getCurrentCityId())
        }

        override suspend fun getCurrentCityId(): DomainResult<String> {
            return DomainResult.success(currentCityId)
        }

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> {
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

private fun weather(cityId: String): Weather {
    return Weather(
        city = City(cityId, "北京", "beijing", "", "", "", ""),
        currentWeather = CurrentWeather(
            cityId,
            WeatherCondition.Clear,
            "26",
            "40",
            "东风",
            "3",
            1L,
            "3级",
            "0",
            "26",
            "1000",
        ),
        airQuality = null,
    )
}
