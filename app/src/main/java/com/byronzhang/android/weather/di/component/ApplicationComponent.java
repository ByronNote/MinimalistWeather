package com.byronzhang.android.weather.di.component;

import android.content.Context;

import com.byronzhang.android.weather.WeatherApplication;
import com.byronzhang.android.weather.di.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    WeatherApplication getApplication();

    Context getContext();
}
