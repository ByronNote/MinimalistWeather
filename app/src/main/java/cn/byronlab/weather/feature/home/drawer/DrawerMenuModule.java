package cn.byronlab.weather.feature.home.drawer;

import cn.byronlab.weather.di.scope.ActivityScoped;
import cn.byronlab.weather.feature.home.drawer.DrawerContract;

import dagger.Module;
import dagger.Provides;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Module
public class DrawerMenuModule {

    private DrawerContract.View view;

    public DrawerMenuModule(DrawerContract.View view) {
        this.view = view;
    }

    @Provides
    @ActivityScoped
    DrawerContract.View provideCityManagerContactView() {
        return view;
    }
}
