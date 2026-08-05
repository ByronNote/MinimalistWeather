package cn.byronlab.weather.app.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.WeatherRefreshScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerWeatherRefreshScheduler @Inject constructor(
    @ApplicationContext context: Context,
) : WeatherRefreshScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun schedule(interval: WeatherRefreshInterval) {
        val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(
            interval.minutes,
            TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .addTag(WEATHER_REFRESH_WORK_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WEATHER_REFRESH_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        const val UNIQUE_WEATHER_REFRESH_WORK = "periodic-weather-refresh"
        const val WEATHER_REFRESH_WORK_TAG = "weather-refresh"
    }
}
