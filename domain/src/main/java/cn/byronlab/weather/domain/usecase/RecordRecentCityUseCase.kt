package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class RecordRecentCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {

    suspend operator fun invoke(cityId: String?): DomainResult<Unit> {
        val normalizedCityId = UseCasePreconditions.trim(cityId)
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.invalidInput("cityId must not be blank."))
        }
        return cityRepository.recordRecentCity(normalizedCityId)
    }
}
