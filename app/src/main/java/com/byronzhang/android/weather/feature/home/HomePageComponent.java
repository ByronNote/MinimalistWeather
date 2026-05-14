package com.byronzhang.android.weather.feature.home;

import com.byronzhang.android.weather.di.component.ApplicationComponent;
import com.byronzhang.android.weather.di.scope.ActivityScoped;

import dagger.Component;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/29
 */
@ActivityScoped
@Component(modules = HomePageModule.class, dependencies = ApplicationComponent.class)
public interface HomePageComponent {

    void inject(MainActivity mainActivity);
}
