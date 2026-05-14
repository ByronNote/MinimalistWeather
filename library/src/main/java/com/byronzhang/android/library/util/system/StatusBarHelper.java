package com.byronzhang.android.library.util.system;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * 适配4.4以上版本 MIUI6、Flyme 和其他 Android6.0 及以上版本状态栏字体颜色
 *
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/6/2
 */
public class StatusBarHelper {

    private static final int MIUI = 1;
    private static final int FLYME = 2;
    private static final int ANDROID_M = 3;
    private static final int OTHER = 4;

    @IntDef({MIUI, FLYME, ANDROID_M, OTHER})
    @Retention(RetentionPolicy.SOURCE)
    @interface SystemType {
    }

    public static void statusBarLightMode(Activity activity) {
        statusMode(activity, true);
    }

    public static int statusBarDarkMode(Activity activity) {
        return statusMode(activity, false);
    }

    private static int statusMode(Activity activity, boolean isFontColorDark) {
        @SystemType int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = ANDROID_M;
            } else if (isXiaomi()) {
                if (new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                    result = MIUI;
                }
            } else if (isMeizu()) {
                if (new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                    result = FLYME;
                }
            }
        }
        return result;
    }


    public static void statusBarLightMode(Activity activity, @SystemType int type) {
        statusBarMode(activity, type, true);

    }

    public static void statusBarDarkMode(Activity activity, @SystemType int type) {
        statusBarMode(activity, type, false);
    }

    private static void statusBarMode(Activity activity, @SystemType int type, boolean isFontColorDark) {
        if (type == MIUI) {
            new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark);
        } else if (type == FLYME) {
            new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark);
        }
        else if (type == ANDROID_M) {
            new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark);
        }
    }

    private static boolean isXiaomi() {
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer)) return false;
        manufacturer = manufacturer.toLowerCase(Locale.US);
        return manufacturer.contains("xiaomi") || manufacturer.contains("redmi");
    }

    private static boolean isMeizu() {
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer)) return false;
        manufacturer = manufacturer.toLowerCase(Locale.US);
        return manufacturer.contains("meizu");
    }
}
