package com.byronzhang.android.weather.data.db.entities.minimalist;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author byronzhang (byron[at]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
@DatabaseTable(tableName = "LifeIndex")
public class LifeIndex {

    public static final String ID_FIELD_NAME = "_id";
    public static final String CITY_ID_FIELD_NAME = "cityId";
    public static final String NAME_ID_FIELD_NAME = "name";
    public static final String INDEX_ID_FIELD_NAME = "index";
    public static final String DETAILS_ID_FIELD_NAME = "details";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    private long id;//数据库自增长ID
    @DatabaseField(columnName = CITY_ID_FIELD_NAME)
    private String cityId;
    @DatabaseField(columnName = NAME_ID_FIELD_NAME)
    private String name;
    @DatabaseField(columnName = INDEX_ID_FIELD_NAME)
    private String index;
    @DatabaseField(columnName = DETAILS_ID_FIELD_NAME)
    private String details;

    public LifeIndex() {
    }

    public LifeIndex(String cityId, String name, String index, String details) {

        this.cityId = cityId;
        this.name = name;
        this.index = index;
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LifeIndex{" +
                "id=" + id +
                ", cityId=" + cityId +
                ", name='" + name + '\'' +
                ", index='" + index + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
