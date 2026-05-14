package com.byronzhang.android.weather.feature.home.drawer;

import com.byronzhang.android.weather.di.scope.ActivityScoped;
import com.byronzhang.android.weather.feature.home.drawer.DrawerContract;

import dagger.Module;
import dagger.Provides;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
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
