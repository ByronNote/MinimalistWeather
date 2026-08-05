package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun observeCurrentCityId(): Flow<DomainResult<String>>

    suspend fun getCurrentCityId(): DomainResult<String>

    suspend fun setCurrentCityId(cityId: String): DomainResult<Unit>

    fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>>

    suspend fun getWeatherRefreshInterval(): DomainResult<WeatherRefreshInterval>

    suspend fun setWeatherRefreshInterval(interval: WeatherRefreshInterval): DomainResult<Unit>
}
