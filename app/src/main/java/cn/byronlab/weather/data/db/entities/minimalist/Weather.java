package cn.byronlab.weather.data.db.entities.minimalist;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
@DatabaseTable(tableName = "Weather")
public class Weather {

    public static final String CITY_ID_FIELD_NAME = "cityId";
    public static final String CITY_NAME_FIELD_NAME = "cityName";
    public static final String CITY_NAME_EN_FIELD_NAME = "cityNameEn";

    @DatabaseField(columnName = CITY_ID_FIELD_NAME, id = true)
    private String cityId;
    @DatabaseField(columnName = CITY_NAME_FIELD_NAME)
    private String cityName;
    @DatabaseField(columnName = CITY_NAME_EN_FIELD_NAME)
    private String cityNameEn;

    private WeatherLive weatherLive;

    private List<WeatherForecast> weatherForecasts;

    private AirQualityLive airQualityLive;

    private List<LifeIndex> lifeIndexes;

    public AirQualityLive getAirQualityLive() {
        return airQualityLive;
    }

    public void setAirQualityLive(AirQualityLive airQualityLive) {
        this.airQualityLive = airQualityLive;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityNameEn() {
        return cityNameEn;
    }

    public void setCityNameEn(String cityNameEn) {
        this.cityNameEn = cityNameEn;
    }

    public List<WeatherForecast> getWeatherForecasts() {
        return weatherForecasts;
    }

    public void setWeatherForecasts(List<WeatherForecast> weatherForecasts) {
        this.weatherForecasts = weatherForecasts;
    }

    public List<LifeIndex> getLifeIndexes() {
        return lifeIndexes;
    }

    public void setLifeIndexes(List<LifeIndex> lifeIndexes) {
        this.lifeIndexes = lifeIndexes;
    }

    public WeatherLive getWeatherLive() {
        return weatherLive;
    }

    public void setWeatherLive(WeatherLive weatherLive) {
        this.weatherLive = weatherLive;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "aqi=" + airQualityLive +
                ", cityId='" + cityId + '\'' +
                ", cityName='" + cityName + '\'' +
                ", cityNameEn='" + cityNameEn + '\'' +
                ", realTime=" + weatherLive +
                ", forecasts=" + weatherForecasts +
                ", lifeIndexes=" + lifeIndexes +
                '}';
    }
}
