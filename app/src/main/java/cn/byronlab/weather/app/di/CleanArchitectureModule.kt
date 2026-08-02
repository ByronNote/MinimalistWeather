package cn.byronlab.weather.app.di

import android.content.Context
import cn.byronlab.weather.data.db.dao.CityDao
import cn.byronlab.weather.data.db.dao.WeatherDao
import cn.byronlab.weather.data.repository.LegacyCityRepository
import cn.byronlab.weather.data.repository.LegacySettingsRepository
import cn.byronlab.weather.data.repository.LegacyWeatherRepository
import cn.byronlab.weather.data.repository.StartupRepository
import cn.byronlab.weather.domain.repository.AppStartupRepository
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.usecase.DeleteSavedCityUseCase
import cn.byronlab.weather.domain.usecase.GetCurrentWeatherUseCase
import cn.byronlab.weather.domain.usecase.InitializeAppUseCase
import cn.byronlab.weather.domain.usecase.ObserveSavedCitiesUseCase
import cn.byronlab.weather.domain.usecase.RefreshWeatherUseCase
import cn.byronlab.weather.domain.usecase.SearchCitiesUseCase
import cn.byronlab.weather.domain.usecase.SetCurrentCityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CleanArchitectureModule {

    @Provides
    @Singleton
    fun provideWeatherDao(@ApplicationContext context: Context): WeatherDao = WeatherDao(context)

    @Provides
    @Singleton
    fun provideCityDao(@ApplicationContext context: Context): CityDao = CityDao(context)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        @ApplicationContext context: Context,
        weatherDao: WeatherDao,
    ): WeatherRepository = LegacyWeatherRepository(context, weatherDao)

    @Provides
    @Singleton
    fun provideCityRepository(
        cityDao: CityDao,
        weatherDao: WeatherDao,
    ): CityRepository = LegacyCityRepository(cityDao, weatherDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository = LegacySettingsRepository()

    @Provides
    @Singleton
    fun provideAppStartupRepository(): AppStartupRepository = StartupRepository()

    @Provides
    fun provideInitializeAppUseCase(repository: AppStartupRepository): InitializeAppUseCase =
        InitializeAppUseCase(repository)

    @Provides
    fun provideGetCurrentWeatherUseCase(
        weatherRepository: WeatherRepository,
        settingsRepository: SettingsRepository,
    ): GetCurrentWeatherUseCase = GetCurrentWeatherUseCase(weatherRepository, settingsRepository)

    @Provides
    fun provideRefreshWeatherUseCase(
        weatherRepository: WeatherRepository,
        settingsRepository: SettingsRepository,
    ): RefreshWeatherUseCase = RefreshWeatherUseCase(weatherRepository, settingsRepository)

    @Provides
    fun provideObserveSavedCitiesUseCase(repository: CityRepository): ObserveSavedCitiesUseCase =
        ObserveSavedCitiesUseCase(repository)

    @Provides
    fun provideSearchCitiesUseCase(repository: CityRepository): SearchCitiesUseCase =
        SearchCitiesUseCase(repository)

    @Provides
    fun provideSetCurrentCityUseCase(repository: SettingsRepository): SetCurrentCityUseCase =
        SetCurrentCityUseCase(repository)

    @Provides
    fun provideDeleteSavedCityUseCase(
        cityRepository: CityRepository,
        settingsRepository: SettingsRepository,
    ): DeleteSavedCityUseCase = DeleteSavedCityUseCase(cityRepository, settingsRepository)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
