package cn.byronlab.weather.feature.home;

import cn.byronlab.weather.data.db.entities.minimalist.Weather;
import cn.byronlab.weather.base.BasePresenter;
import cn.byronlab.weather.base.BaseView;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
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
