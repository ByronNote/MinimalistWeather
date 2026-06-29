package cn.byronlab.weather.feature.home.drawer;

import android.content.Context;

import cn.byronlab.weather.data.db.dao.WeatherDao;
import cn.byronlab.weather.data.db.entities.minimalist.Weather;
import cn.byronlab.weather.data.preference.PreferenceHelper;
import cn.byronlab.weather.data.preference.WeatherSettings;
import cn.byronlab.weather.di.component.DaggerPresenterComponent;
import cn.byronlab.weather.di.module.ApplicationModule;
import cn.byronlab.weather.di.scope.ActivityScoped;

import java.io.InvalidClassException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/4/16
 */
@ActivityScoped
public final class DrawerMenuPresenter implements DrawerContract.Presenter {

    private DrawerContract.View view;


    private CompositeSubscription subscriptions;

    @Inject
    WeatherDao weatherDao;

    @Inject
    public DrawerMenuPresenter(Context context, DrawerContract.View view) {

        this.view = view;
        this.subscriptions = new CompositeSubscription();
        view.setPresenter(this);

        DaggerPresenterComponent.builder()
                .applicationModule(new ApplicationModule(context))
                .build().inject(this);
    }

    @Override
    public void subscribe() {
        loadSavedCities();
    }

    @Override
    public void unSubscribe() {
        subscriptions.clear();
    }

    @Override
    public void loadSavedCities() {

        Subscription subscription = Observable.fromCallable(() -> {
                    List<Weather> weathers = weatherDao.queryAllSaveCity();
                    return weathers == null ? Collections.<Weather>emptyList() : weathers;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weathers -> view.displaySavedCities(weathers), throwable -> {
                    throwable.printStackTrace();
                    view.displaySavedCities(Collections.emptyList());
                });
        subscriptions.add(subscription);

    }

    @Override
    public void deleteCity(String cityId) {

        Subscription subscription = Observable.fromCallable(() -> deleteCityFromDBAndReturnCurrentCityId(cityId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentCityId -> {
                    if (currentCityId == null)
                        return;
                    try {
                        PreferenceHelper.savePreference(WeatherSettings.SETTINGS_CURRENT_CITY_ID, currentCityId);
                    } catch (InvalidClassException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
        subscriptions.add(subscription);
    }

    @Override
    public void saveCurrentCityToPreference(String cityId) throws InvalidClassException{
        PreferenceHelper.savePreference(WeatherSettings.SETTINGS_CURRENT_CITY_ID, cityId);
    }

    private String deleteCityFromDBAndReturnCurrentCityId(String cityId) {
        String currentCityId = PreferenceHelper.getSharedPreferences().getString(WeatherSettings.SETTINGS_CURRENT_CITY_ID.getId(), "");
        try {
            weatherDao.deleteById(cityId);
            if (cityId.equals(currentCityId)) {//说明删除的是当前选择的城市，所以需要重新设置默认城市
                List<Weather> weatherList = weatherDao.queryAllSaveCity();
                if (weatherList != null && weatherList.size() > 0) {
                    currentCityId = weatherList.get(0).getCityId();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currentCityId;
    }


}
