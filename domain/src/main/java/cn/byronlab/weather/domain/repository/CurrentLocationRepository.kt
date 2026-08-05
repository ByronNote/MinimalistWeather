package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.result.DomainResult

interface CurrentLocationRepository {

    suspend fun getCurrentLocation(): DomainResult<DeviceLocation>
}
