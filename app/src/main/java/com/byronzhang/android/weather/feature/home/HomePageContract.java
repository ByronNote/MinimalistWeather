package com.byronzhang.android.weather.feature.home;

import com.byronzhang.android.weather.data.db.entities.minimalist.Weather;
import com.byronzhang.android.weather.base.BasePresenter;
import com.byronzhang.android.weather.base.BaseView;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 */
public interface HomePageContract {

    interface View extends BaseView<Presenter> {

        void displayWeatherInformation(Weather weather);

        void showLoadError(String message);
    }

    interface Presenter extends BasePresenter {

        void loadWeather(String cityId, boolean refreshNow);
    }
}
