package cn.byronlab.weather.feature.home;

import cn.byronlab.weather.di.scope.ActivityScoped;
import cn.byronlab.weather.feature.home.HomePagePresenter;

import dagger.Module;
import dagger.Provides;

import cn.byronlab.weather.feature.home.HomePageContract;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link HomePagePresenter}
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Module
public class HomePageModule {

    private final HomePageContract.View view;

    public HomePageModule(HomePageContract.View view) {

        this.view = view;
    }

    @Provides
    @ActivityScoped
    HomePageContract.View provideHomePageContractView() {
        return view;
    }

}
