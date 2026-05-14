package com.byronzhang.android.weather.feature.selectcity;

import dagger.Module;
import dagger.Provides;

import com.byronzhang.android.weather.di.scope.ActivityScoped;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@Module
public class SelectCityModule {

    private SelectCityContract.View view;

    public SelectCityModule(SelectCityContract.View view) {
        this.view = view;
    }

    @Provides
    @ActivityScoped
    SelectCityContract.View provideSelectCityContractView() {
        return view;
    }
}
