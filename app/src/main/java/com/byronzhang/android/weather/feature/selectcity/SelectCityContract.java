package com.byronzhang.android.weather.feature.selectcity;

import java.util.List;

import com.byronzhang.android.weather.data.db.entities.City;
import com.byronzhang.android.weather.base.BasePresenter;
import com.byronzhang.android.weather.base.BaseView;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 */
public interface SelectCityContract {

    interface View extends BaseView<Presenter> {

        void displayCities(List<City> cities);
    }

    interface Presenter extends BasePresenter {

        void loadCities();
    }
}
