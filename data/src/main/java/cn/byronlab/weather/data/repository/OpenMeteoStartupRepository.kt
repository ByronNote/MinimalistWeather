package cn.byronlab.weather.data.repository

import cn.byronlab.weather.data.openmeteo.OpenMeteoCityCatalog
import cn.byronlab.weather.domain.repository.AppStartupRepository
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class OpenMeteoStartupRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cityRepository: CityRepository,
) : AppStartupRepository {

    override suspend fun initialize(): DomainResult<Unit> {
        val cityId = when (val result = settingsRepository.getCurrentCityId()) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return result
        }
        val validCityId = if (OpenMeteoCityCatalog.resolve(cityId) != null) {
            cityId
        } else {
            OpenMeteoCityCatalog.defaultCityId
        }
        if (validCityId != cityId) {
            val setResult = settingsRepository.setCurrentCityId(validCityId)
            if (setResult is DomainResult.Failure) {
                return setResult
            }
        }
        return cityRepository.addCity(validCityId)
    }
}
