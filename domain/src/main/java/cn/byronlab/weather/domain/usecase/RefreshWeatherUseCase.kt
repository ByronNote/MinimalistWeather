package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class RefreshWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(): DomainResult<Weather> {
        return when (val currentCityResult = settingsRepository.getCurrentCityId()) {
            is DomainResult.Success -> invoke(currentCityResult.data)
            is DomainResult.Failure -> currentCityResult
        }
    }

    suspend operator fun invoke(cityId: String?): DomainResult<Weather> {
        val normalizedCityId = UseCasePreconditions.trim(cityId)
        if (UseCasePreconditions.isBlank(normalizedCityId)) {
            return DomainResult.failure(DomainError.missingCurrentCity())
        }
        return weatherRepository.refreshWeather(normalizedCityId)
    }
}
