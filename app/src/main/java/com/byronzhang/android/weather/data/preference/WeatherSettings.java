package com.byronzhang.android.weather.data.preference;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 */
public enum WeatherSettings {

    /*默认配置项*/
    SETTINGS_FIRST_USE("first_use", Boolean.TRUE),

    SETTINGS_CURRENT_CITY_ID("current_city_id", "");

    private final String mId;
    private final Object mDefaultValue;

    WeatherSettings(String id, Object defaultValue) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    public String getId() {
        return this.mId;
    }

    public Object getDefaultValue() {
        return this.mDefaultValue;
    }

    public static WeatherSettings fromId(String id) {
        WeatherSettings[] values = values();
        for (WeatherSettings value : values) {
            if (value.mId.equals(id)) {
                return value;
            }
        }
        return null;
    }
}
