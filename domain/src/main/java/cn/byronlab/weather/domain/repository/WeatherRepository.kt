package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.result.DomainResult

interface WeatherRepository {

    suspend fun getWeather(cityId: String, refreshNow: Boolean): DomainResult<Weather>

    suspend fun refreshWeather(cityId: String): DomainResult<Weather>
}
