package cn.byronlab.weather.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cn.byronlab.weather.data.di.SettingsDataStore
import cn.byronlab.weather.data.di.WeatherCacheDataStore
import cn.byronlab.weather.data.repository.DataStoreSettingsRepository
import cn.byronlab.weather.data.repository.DataStoreWeatherCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.weatherSettingsDataStore by preferencesDataStore(
    name = DataStoreSettingsRepository.SETTINGS_DATASTORE_NAME,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context,
                DataStoreSettingsRepository.LEGACY_SHARED_PREFERENCES_NAME,
            ),
        )
    },
)

private val Context.weatherCacheDataStore by preferencesDataStore(
    name = DataStoreWeatherCache.CACHE_DATASTORE_NAME,
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.weatherSettingsDataStore

    @Provides
    @Singleton
    @WeatherCacheDataStore
    fun provideWeatherCacheDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.weatherCacheDataStore
}
