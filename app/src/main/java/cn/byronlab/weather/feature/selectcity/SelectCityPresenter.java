package cn.byronlab.weather.feature.selectcity;

import android.content.Context;

import cn.byronlab.weather.data.db.dao.CityDao;
import cn.byronlab.weather.data.db.entities.City;
import cn.byronlab.weather.di.component.DaggerPresenterComponent;
import cn.byronlab.weather.di.module.ApplicationModule;
import cn.byronlab.weather.di.scope.ActivityScoped;

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
 */
@ActivityScoped
public final class SelectCityPresenter implements SelectCityContract.Presenter {

    private final SelectCityContract.View cityListView;

    private CompositeSubscription subscriptions;

    @Inject
    CityDao cityDao;

    @Inject
    SelectCityPresenter(Context context, SelectCityContract.View view) {

        this.cityListView = view;
        this.subscriptions = new CompositeSubscription();
        cityListView.setPresenter(this);

        DaggerPresenterComponent.builder()
                .applicationModule(new ApplicationModule(context))
                .build().inject(this);
    }

    @Override
    public void loadCities() {
        Subscription subscription = Observable.fromCallable(() -> {
                    List<City> cities = cityDao.queryCityList();
                    return cities == null ? Collections.<City>emptyList() : cities;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cityListView::displayCities, Throwable::printStackTrace);
        subscriptions.add(subscription);
    }

    @Override
    public void subscribe() {
        loadCities();
    }

    @Override
    public void unSubscribe() {
        subscriptions.clear();
    }
}
