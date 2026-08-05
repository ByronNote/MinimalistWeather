package cn.byronlab.weather.app.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import cn.byronlab.weather.domain.usecase.RefreshWeatherUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class WeatherRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WeatherRefreshWorkerEntryPoint::class.java,
        )
        return when (val result = entryPoint.refreshWeatherUseCase().invoke()) {
            is DomainResult.Success -> Result.success()
            is DomainResult.Failure -> when (result.error.type) {
                DomainError.Type.NETWORK,
                DomainError.Type.STORAGE,
                DomainError.Type.UNKNOWN -> Result.retry()
                DomainError.Type.MISSING_CURRENT_CITY,
                DomainError.Type.INVALID_INPUT,
                DomainError.Type.NOT_FOUND,
                DomainError.Type.LOCATION_PERMISSION_REQUIRED,
                DomainError.Type.LOCATION_UNAVAILABLE -> Result.failure()
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WeatherRefreshWorkerEntryPoint {
    fun refreshWeatherUseCase(): RefreshWeatherUseCase
}
