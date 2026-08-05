package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class GetRecentCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {

    suspend operator fun invoke(): DomainResult<List<City>> = cityRepository.getRecentCities()
}
