package com.byronzhang.android.weather.util.stetho;

import android.content.Context;

import com.byronzhang.android.weather.util.StethoHelper;

import okhttp3.OkHttpClient;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/7/25
 */
public class ReleaseStethoHelper implements StethoHelper {

    @Override
    public void init(Context context) {

    }

    @Override
    public OkHttpClient.Builder addNetworkInterceptor(OkHttpClient.Builder builder) {
        return null;
    }
}
