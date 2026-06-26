package cn.byronlab.weather.util.stetho;

import android.content.Context;

import cn.byronlab.weather.util.StethoHelper;

import okhttp3.OkHttpClient;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
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
