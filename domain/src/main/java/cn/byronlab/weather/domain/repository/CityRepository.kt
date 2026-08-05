package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface CityRepository {

    suspend fun searchCities(keyword: String): DomainResult<List<City>>

    suspend fun getPopularCities(): DomainResult<List<City>>

    fun resolveLocation(location: DeviceLocation): DomainResult<City>

    fun observeAddedCities(): Flow<DomainResult<List<City>>>

    suspend fun getAddedCities(): DomainResult<List<City>>

    suspend fun addCity(cityId: String): DomainResult<Unit>

    suspend fun removeCity(cityId: String): DomainResult<Unit>

    suspend fun getRecentCities(): DomainResult<List<City>>

    suspend fun recordRecentCity(cityId: String): DomainResult<Unit>
}
