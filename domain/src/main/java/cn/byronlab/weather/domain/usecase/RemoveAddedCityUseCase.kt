package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class RemoveAddedCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(cityId: String?): DomainResult<Unit> {
        val normalizedCityId = UseCasePreconditions.trim(cityId)
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.invalidInput("cityId must not be blank."))
        }

        val currentCityId = when (val result = settingsRepository.getCurrentCityId()) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return result
        }
        val removeResult = cityRepository.removeCity(normalizedCityId)
        if (removeResult is DomainResult.Failure || normalizedCityId != currentCityId) {
            return removeResult
        }
        val addedCities = when (val result = cityRepository.getAddedCities()) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> return result
        }
        return settingsRepository.setCurrentCityId(findFirstCityId(addedCities))
    }

    private fun findFirstCityId(cities: List<City>): String = cities.firstOrNull()?.cityId.orEmpty()
}
