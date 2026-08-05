package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAddedCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {

    operator fun invoke(): Flow<DomainResult<List<City>>> = cityRepository.observeAddedCities()
}
