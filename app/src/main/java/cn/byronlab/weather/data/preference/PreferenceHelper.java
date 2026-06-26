package cn.byronlab.weather.data.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import cn.byronlab.weather.WeatherApplication;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/6
 */
public final class PreferenceHelper {

    private static final String TAG = "Preferences";

    private static final String SETTINGS_FILENAME = WeatherApplication.class.getPackage().getName();

    private static final List<ConfigurationListener> CONFIGURATION_LISTENERS = Collections.synchronizedList(new ArrayList<>());

    private PreferenceHelper() {
        super();
    }

    public static void loadDefaults() {
        //设置SharedPreferences默认值
        try {
            Map<WeatherSettings, Object> defaultPrefs = new HashMap<>();
            WeatherSettings[] values = WeatherSettings.values();
            for (WeatherSettings value : values) {
                defaultPrefs.put(value, value.getDefaultValue());
            }
            savePreferences(defaultPrefs, true);
        } catch (Exception ex) {
            Log.e(TAG, "Save default settings fails", ex);
        }
    }

    public static void addConfigurationListener(ConfigurationListener listener) {
        CONFIGURATION_LISTENERS.add(listener);
    }

    public static void removeConfigurationListener(ConfigurationListener listener) {
        CONFIGURATION_LISTENERS.remove(listener);
    }

    public static SharedPreferences getSharedPreferences() {
        return WeatherApplication.getInstance().getSharedPreferences(
                SETTINGS_FILENAME, Context.MODE_PRIVATE);
    }

    public static void savePreference(WeatherSettings pref, Object value) throws InvalidClassException {
        Map<WeatherSettings, Object> prefs = new HashMap<>();
        prefs.put(pref, value);
        savePreferences(prefs, false);
    }

    public static void savePreferences(Map<WeatherSettings, Object> prefs) throws InvalidClassException {

        savePreferences(prefs, false);
    }

    private static void savePreferences(Map<WeatherSettings, Object> prefs, boolean noSaveIfExists) throws InvalidClassException {

        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();

        for (WeatherSettings pref : prefs.keySet()) {

            Object value = prefs.get(pref);

            if (noSaveIfExists && sp.contains(pref.getId())) {
                continue;
            }

            if (value instanceof Boolean && pref.getDefaultValue() instanceof Boolean) {
                editor.putBoolean(pref.getId(), (Boolean) value);
            } else if (value instanceof String && pref.getDefaultValue() instanceof String) {
                editor.putString(pref.getId(), (String) value);
            } else if (value instanceof Set && pref.getDefaultValue() instanceof Set) {
                editor.putStringSet(pref.getId(), (Set<String>) value);
            } else if (value instanceof Integer && pref.getDefaultValue() instanceof Integer) {
                editor.putInt(pref.getId(), (Integer) value);
            } else if (value instanceof Float && pref.getDefaultValue() instanceof Float) {
                editor.putFloat(pref.getId(), (Float) value);
            } else if (value instanceof Long && pref.getDefaultValue() instanceof Long) {
                editor.putLong(pref.getId(), (Long) value);
            } else {
                //The object is not of the appropriate type
                String msg = String.format("%s: %s", pref.getId(), value.getClass().getName());
                Log.e(TAG, String.format("Configuration error. InvalidClassException: %s", msg));
                throw new InvalidClassException(msg);
            }
        }

        editor.apply();

        if (!CONFIGURATION_LISTENERS.isEmpty()) {
            for (WeatherSettings pref : prefs.keySet()) {
                Object value = prefs.get(pref);
                for (ConfigurationListener listener : CONFIGURATION_LISTENERS) {
                    listener.onConfigurationChanged(pref, value);
                }
            }
        }
    }
}
