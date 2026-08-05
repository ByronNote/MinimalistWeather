package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {

    suspend operator fun invoke(keyword: String?): DomainResult<List<City>> {
        val normalizedKeyword = UseCasePreconditions.trim(keyword)
        if (normalizedKeyword.isEmpty()) {
            return DomainResult.success(emptyList())
        }
        return cityRepository.searchCities(normalizedKeyword)
    }
}
