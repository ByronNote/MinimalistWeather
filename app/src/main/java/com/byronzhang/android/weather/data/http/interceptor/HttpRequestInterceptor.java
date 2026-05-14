package com.byronzhang.android.weather.data.http.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class HttpRequestInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return null;
    }
}
