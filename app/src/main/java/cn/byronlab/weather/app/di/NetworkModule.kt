package cn.byronlab.weather.app.di

import cn.byronlab.weather.data.openmeteo.OpenMeteoClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10.seconds)
            .readTimeout(15.seconds)
            .writeTimeout(15.seconds)
            .build()

    @Provides
    @Singleton
    fun provideOpenMeteoClient(okHttpClient: OkHttpClient): OpenMeteoClient =
        OpenMeteoClient(okHttpClient)
}
