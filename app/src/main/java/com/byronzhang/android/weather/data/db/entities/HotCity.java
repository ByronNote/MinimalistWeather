package com.byronzhang.android.weather.data.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/6/21
 */
@DatabaseTable(tableName = "HotCity")
public class HotCity {

    public static final String ID_FIELD_NAME = "_id";
    public static final String CITY_NAME_FIELD_NAME = "name";
    public static final String CITY_ID_FIELD_NAME = "posID";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    private int id;
    @DatabaseField(columnName = CITY_ID_FIELD_NAME)
    private int cityId;
    @DatabaseField(columnName = CITY_NAME_FIELD_NAME)
    private String cityName;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
