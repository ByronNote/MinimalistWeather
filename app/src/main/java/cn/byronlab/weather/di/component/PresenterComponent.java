package cn.byronlab.weather.di.component;

import cn.byronlab.weather.di.module.ApplicationModule;
import cn.byronlab.weather.feature.home.drawer.DrawerMenuPresenter;
import cn.byronlab.weather.feature.selectcity.SelectCityPresenter;

import javax.inject.Singleton;

import dagger.Component;
import cn.byronlab.weather.feature.home.HomePagePresenter;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/12/2
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface PresenterComponent {

    void inject(HomePagePresenter presenter);

    void inject(SelectCityPresenter presenter);

    void inject(DrawerMenuPresenter presenter);
}
 
