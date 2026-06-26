package cn.byronlab.weather.feature.home;

import cn.byronlab.weather.di.component.ApplicationComponent;
import cn.byronlab.weather.di.scope.ActivityScoped;

import dagger.Component;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/29
 */
@ActivityScoped
@Component(modules = HomePageModule.class, dependencies = ApplicationComponent.class)
public interface HomePageComponent {

    void inject(MainActivity mainActivity);
}
