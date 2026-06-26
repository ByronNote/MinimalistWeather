package cn.byronlab.weather.feature.selectcity;

import java.util.List;

import cn.byronlab.weather.data.db.entities.City;
import cn.byronlab.weather.base.BasePresenter;
import cn.byronlab.weather.base.BaseView;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 */
public interface SelectCityContract {

    interface View extends BaseView<Presenter> {

        void displayCities(List<City> cities);
    }

    interface Presenter extends BasePresenter {

        void loadCities();
    }
}
