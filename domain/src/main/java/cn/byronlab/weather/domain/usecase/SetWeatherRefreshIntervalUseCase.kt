package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRefreshScheduler
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class SetWeatherRefreshIntervalUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduler: WeatherRefreshScheduler,
) {

    suspend operator fun invoke(interval: WeatherRefreshInterval): DomainResult<Unit> {
        return when (val result = settingsRepository.setWeatherRefreshInterval(interval)) {
            is DomainResult.Failure -> result
            is DomainResult.Success -> try {
                scheduler.schedule(interval)
                result
            } catch (exception: Exception) {
                DomainResult.failure(
                    DomainError(
                        DomainError.Type.UNKNOWN,
                        "Weather refresh could not be scheduled.",
                        exception,
                    ),
                )
            }
        }
    }
}
