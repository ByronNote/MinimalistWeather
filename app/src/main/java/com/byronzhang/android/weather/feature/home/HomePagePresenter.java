package com.byronzhang.android.weather.feature.home;

import android.content.Context;

import java.net.SocketTimeoutException;

import com.byronzhang.android.library.util.RxSchedulerUtils;
import com.byronzhang.android.weather.data.db.dao.WeatherDao;
import com.byronzhang.android.weather.data.preference.PreferenceHelper;
import com.byronzhang.android.weather.data.preference.WeatherSettings;
import com.byronzhang.android.weather.data.repository.WeatherDataRepository;
import com.byronzhang.android.weather.di.component.DaggerPresenterComponent;
import com.byronzhang.android.weather.di.module.ApplicationModule;
import com.byronzhang.android.weather.di.scope.ActivityScoped;

import javax.inject.Inject;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 */
@ActivityScoped
public final class HomePagePresenter implements HomePageContract.Presenter {

    private final Context context;
    private final HomePageContract.View weatherView;

    private CompositeSubscription subscriptions;

    @Inject
    WeatherDao weatherDao;

    @Inject
    HomePagePresenter(Context context, HomePageContract.View view) {

        this.context = context;
        this.weatherView = view;
        this.subscriptions = new CompositeSubscription();
        weatherView.setPresenter(this);

        DaggerPresenterComponent.builder()
                .applicationModule(new ApplicationModule(context))
                .build().inject(this);
    }

    @Override
    public void subscribe() {
        String cityId = PreferenceHelper.getSharedPreferences().getString(WeatherSettings.SETTINGS_CURRENT_CITY_ID.getId(), "");
        loadWeather(cityId, false);
    }

    @Override
    public void loadWeather(String cityId, boolean refreshNow) {

        Subscription subscription = WeatherDataRepository.getWeather(context, cityId, weatherDao, refreshNow)
                .compose(RxSchedulerUtils.normalSchedulersTransformer())
                .subscribe(weatherView::displayWeatherInformation, throwable -> weatherView.showLoadError(toUserMessage(throwable)));
        subscriptions.add(subscription);
    }

    @Override
    public void unSubscribe() {
        subscriptions.clear();
    }

    private static String toUserMessage(Throwable throwable) {
        if (throwable == null) return "加载失败，请重试";
        if (isTimeout(throwable)) return "网络超时，请重试";
        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) return "加载失败，请重试";
        return message;
    }

    private static boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) return true;
            current = current.getCause();
        }
        return false;
    }
}
