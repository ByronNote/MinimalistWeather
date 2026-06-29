package cn.byronlab.weather;

import android.content.Intent;
import android.os.Bundle;

import cn.byronlab.weather.base.BaseActivity;
import cn.byronlab.weather.library.util.system.StatusBarHelper;
import cn.byronlab.weather.feature.home.MainActivity;
import cn.byronlab.weather.data.db.CityDatabaseHelper;
import cn.byronlab.weather.data.preference.PreferenceHelper;
import cn.byronlab.weather.data.preference.WeatherSettings;

import java.io.InvalidClassException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 */
public class WelcomeActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.statusBarLightMode(this);

        Observable.fromCallable(() -> {
                    initAppData();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> gotoMainPage(), throwable -> {
                    throwable.printStackTrace();
                    gotoMainPage();
                });

    }

    private void gotoMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // 修复 Android 9.0 下 Activity 跳转动画导致的启动页闪屏的问题
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * 初始化应用数据
     */
    private void initAppData() {
        PreferenceHelper.loadDefaults();
        //TODO 测试，待删除
        if (PreferenceHelper.getSharedPreferences().getBoolean(WeatherSettings.SETTINGS_FIRST_USE.getId(), false)) {
            try {
                PreferenceHelper.savePreference(WeatherSettings.SETTINGS_CURRENT_CITY_ID, "101020100");
                PreferenceHelper.savePreference(WeatherSettings.SETTINGS_FIRST_USE, false);
            } catch (InvalidClassException e) {
                e.printStackTrace();
            }
        }
        CityDatabaseHelper.importCityDB();
    }
}
