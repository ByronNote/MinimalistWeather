package com.byronzhang.android.weather.di.component;

import com.byronzhang.android.weather.di.module.ApplicationModule;
import com.byronzhang.android.weather.feature.home.drawer.DrawerMenuPresenter;
import com.byronzhang.android.weather.feature.selectcity.SelectCityPresenter;

import javax.inject.Singleton;

import dagger.Component;
import com.byronzhang.android.weather.feature.home.HomePagePresenter;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/12/2
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface PresenterComponent {

    void inject(HomePagePresenter presenter);

    void inject(SelectCityPresenter presenter);

    void inject(DrawerMenuPresenter presenter);
}
 
