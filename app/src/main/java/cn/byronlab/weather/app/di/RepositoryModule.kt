package cn.byronlab.weather.app.di

import cn.byronlab.weather.data.repository.DataStoreSettingsRepository
import cn.byronlab.weather.data.repository.OpenMeteoCityRepository
import cn.byronlab.weather.data.repository.OpenMeteoStartupRepository
import cn.byronlab.weather.data.repository.OpenMeteoWeatherRepository
import cn.byronlab.weather.app.background.WorkManagerWeatherRefreshScheduler
import cn.byronlab.weather.app.location.AndroidCurrentLocationRepository
import cn.byronlab.weather.domain.repository.AppStartupRepository
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.CurrentLocationRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.repository.WeatherRefreshScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        implementation: OpenMeteoWeatherRepository,
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRefreshScheduler(
        implementation: WorkManagerWeatherRefreshScheduler,
    ): WeatherRefreshScheduler

    @Binds
    @Singleton
    abstract fun bindCityRepository(
        implementation: OpenMeteoCityRepository,
    ): CityRepository

    @Binds
    @Singleton
    abstract fun bindCurrentLocationRepository(
        implementation: AndroidCurrentLocationRepository,
    ): CurrentLocationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: DataStoreSettingsRepository,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAppStartupRepository(
        implementation: OpenMeteoStartupRepository,
    ): AppStartupRepository
}
