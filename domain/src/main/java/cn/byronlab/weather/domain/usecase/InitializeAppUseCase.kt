package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.repository.AppStartupRepository
import cn.byronlab.weather.domain.result.DomainResult
import javax.inject.Inject

class InitializeAppUseCase @Inject constructor(
    private val repository: AppStartupRepository,
) {

    suspend operator fun invoke(): DomainResult<Unit> = repository.initialize()
}
