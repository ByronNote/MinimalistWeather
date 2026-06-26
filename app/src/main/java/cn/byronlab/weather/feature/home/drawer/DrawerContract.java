package cn.byronlab.weather.feature.home.drawer;

import cn.byronlab.weather.base.BasePresenter;
import cn.byronlab.weather.base.BaseView;
import cn.byronlab.weather.data.db.entities.minimalist.Weather;

import java.io.InvalidClassException;
import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/4/16
 */
public interface DrawerContract {

    interface View extends BaseView<DrawerMenuPresenter> {

        void displaySavedCities(List<Weather> weatherList);
    }

    interface Presenter extends BasePresenter {

        void loadSavedCities();

        void deleteCity(String cityId);

        void saveCurrentCityToPreference(String cityId) throws InvalidClassException;
    }
}
