package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.CurrentLocationRepository
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class GetCurrentLocationCityUseCase @Inject constructor(
    private val currentLocationRepository: CurrentLocationRepository,
    private val cityRepository: CityRepository,
) {

    suspend operator fun invoke(): DomainResult<City> {
        return when (val result = currentLocationRepository.getCurrentLocation()) {
            is DomainResult.Failure -> result
            is DomainResult.Success -> cityRepository.resolveLocation(result.data)
        }
    }
}
