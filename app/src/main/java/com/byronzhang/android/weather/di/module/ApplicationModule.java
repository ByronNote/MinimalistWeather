package com.byronzhang.android.weather.di.module;

import android.content.Context;

import com.byronzhang.android.weather.WeatherApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author byronzhang (byron[at]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Module
public class ApplicationModule {

    private final Context context;

    public ApplicationModule(Context context) {

        this.context = context;
    }

    @Provides
    @Singleton
    WeatherApplication provideApplication() {

        return (WeatherApplication) context.getApplicationContext();
    }

    @Provides
    @Singleton
    Context provideContext() {

        return context;
    }
}
