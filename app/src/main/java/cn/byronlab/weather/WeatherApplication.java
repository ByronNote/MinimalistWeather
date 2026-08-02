package cn.byronlab.weather;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import cn.byronlab.weather.data.http.ApiClient;
import cn.byronlab.weather.data.http.ApiConstants;
import cn.byronlab.weather.data.http.configuration.ApiConfiguration;
import dagger.hilt.android.HiltAndroidApp;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/4
 */
@HiltAndroidApp
public class WeatherApplication extends Application {

    private static final String TAG = "WeatherApp";

    private static WeatherApplication weatherApplicationInstance;

    public static WeatherApplication getInstance() {

        return weatherApplicationInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate start");
        weatherApplicationInstance = this;

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

        //初始化Stetho
        BuildConfig.STETHO.init(this.getApplicationContext());

        //初始化ApiClient
        ApiConfiguration apiConfiguration = ApiConfiguration.builder()
                .dataSourceType(ApiConstants.WEATHER_DATA_SOURCE_TYPE_MI)
//                .dataSourceType(ApiConstants.WEATHER_DATA_SOURCE_TYPE_KNOW)
//                .dataSourceType(ApiConstants.WEATHER_DATA_SOURCE_TYPE_ENVIRONMENT_CLOUD)
                .build();
        ApiClient.init(apiConfiguration);
        Log.d(TAG, "onCreate end");
    }
}
