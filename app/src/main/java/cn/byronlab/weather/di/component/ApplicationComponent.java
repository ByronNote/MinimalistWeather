package cn.byronlab.weather.di.component;

import android.content.Context;

import cn.byronlab.weather.WeatherApplication;
import cn.byronlab.weather.di.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    WeatherApplication getApplication();

    Context getContext();
}
