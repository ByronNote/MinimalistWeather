package com.byronzhang.android.weather.util;

import android.content.Context;

import okhttp3.OkHttpClient;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/7/25
 */
public interface StethoHelper {

    void init(Context context);

    OkHttpClient.Builder addNetworkInterceptor(OkHttpClient.Builder builder);
}
