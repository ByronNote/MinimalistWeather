package cn.byronlab.weather.data.db.entities.minimalist;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 天气预报
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
@DatabaseTable(tableName = "WeatherForecast")
public class WeatherForecast {

    public static final String ID_FIELD_NAME = "_id";
    public static final String CITY_ID_FIELD_NAME = "cityId";
    public static final String WEATHER_FIELD_NAME = "weather";
    public static final String WEATHER_DAY_FIELD_NAME = "weatherDay";
    public static final String WEATHER_NIGHT_FIELD_NAME = "weatherNight";
    public static final String TEMP_MAX_FIELD_NAME = "tempMax";
    public static final String TEMP_MIN_FIELD_NAME = "tempMin";
    public static final String WIND_FIELD_NAME = "wind";
    public static final String DATE_FIELD_NAME = "date";
    public static final String WEEK_FIELD_NAME = "week";

    public static final String POP_FIELD_NAME = "pop";
    public static final String UV_FIELD_NAME = "uv";
    public static final String VISIBILITY_FIELD_NAME = "visibility";
    public static final String HUMIDITY_FIELD_NAME = "humidity";
    public static final String PRESSURE_FIELD_NAME = "pressure";
    public static final String PRECIPITATION_FIELD_NAME = "precipitation";
    public static final String SUNRISE_FIELD_NAME = "sunrise";
    public static final String SUNSET_FIELD_NAME = "sunset";
    public static final String MOONRISE_FIELD_NAME = "moonrise";
    public static final String MOONSET_FIELD_NAME = "moonset";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    private long id;//数据库自增长ID
    @DatabaseField(columnName = CITY_ID_FIELD_NAME)
    private String cityId;
    @DatabaseField(columnName = WEATHER_FIELD_NAME)
    private String weather;
    @DatabaseField(columnName = WEATHER_DAY_FIELD_NAME)
    private String weatherDay;
    @DatabaseField(columnName = WEATHER_NIGHT_FIELD_NAME)
    private String weatherNight;
    @DatabaseField(columnName = TEMP_MAX_FIELD_NAME)
    private int tempMax;
    @DatabaseField(columnName = TEMP_MIN_FIELD_NAME)
    private int tempMin;
    @DatabaseField(columnName = WIND_FIELD_NAME)
    private String wind;
    @DatabaseField(columnName = DATE_FIELD_NAME)
    private String date;
    @DatabaseField(columnName = WEEK_FIELD_NAME)
    private String week; //周一，周二，...

    @DatabaseField(columnName = POP_FIELD_NAME)
    private String pop;//降水概率(%)
    @DatabaseField(columnName = UV_FIELD_NAME)
    private String uv;//紫外线级别
    @DatabaseField(columnName = VISIBILITY_FIELD_NAME)
    private String visibility;//能见度(km)
    @DatabaseField(columnName = HUMIDITY_FIELD_NAME)
    private String humidity;//相对湿度(%)
    @DatabaseField(columnName = PRESSURE_FIELD_NAME)
    private String pressure;//气压(hPa)
    @DatabaseField(columnName = PRECIPITATION_FIELD_NAME)
    private String precipitation;//降水量(mm)
    @DatabaseField(columnName = SUNRISE_FIELD_NAME)
    private String sunrise;//日出
    @DatabaseField(columnName = SUNSET_FIELD_NAME)
    private String sunset;//日落
    @DatabaseField(columnName = MOONRISE_FIELD_NAME)
    private String moonrise;//月升
    @DatabaseField(columnName = MOONSET_FIELD_NAME)
    private String moonset;//月落

    public WeatherForecast() {
    }

    public WeatherForecast(String cityId, String weather, String weatherDay, String weatherNight,
                           int tempMax, int tempMin, String wind, String data, String week) {

        this.cityId = cityId;
        this.weather = weather;
        this.weatherDay = weatherDay;
        this.weatherNight = weatherNight;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.wind = wind;
        this.date = data;
        this.week = week;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }

    public int getTempMin() {
        return tempMin;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWeatherDay() {
        return weatherDay;
    }

    public void setWeatherDay(String weatherDay) {
        this.weatherDay = weatherDay;
    }

    public String getWeatherNight() {
        return weatherNight;
    }

    public void setWeatherNight(String weatherNight) {
        this.weatherNight = weatherNight;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getPop() {
        return pop;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getMoonrise() {
        return moonrise;
    }

    public void setMoonrise(String moonrise) {
        this.moonrise = moonrise;
    }

    public String getMoonset() {
        return moonset;
    }

    public void setMoonset(String moonset) {
        this.moonset = moonset;
    }

    @Override
    public String toString() {
        return "WeatherForecast{" +
                "id=" + id +
                ", cityId='" + cityId + '\'' +
                ", weather='" + weather + '\'' +
                ", weatherDay='" + weatherDay + '\'' +
                ", weatherNight='" + weatherNight + '\'' +
                ", tempMax=" + tempMax +
                ", tempMin=" + tempMin +
                ", wind='" + wind + '\'' +
                ", date='" + date + '\'' +
                ", week='" + week + '\'' +
                ", pop='" + pop + '\'' +
                ", uv='" + uv + '\'' +
                ", visibility='" + visibility + '\'' +
                ", humidity='" + humidity + '\'' +
                ", pressure='" + pressure + '\'' +
                ", precipitation='" + precipitation + '\'' +
                ", sunrise='" + sunrise + '\'' +
                ", sunset='" + sunset + '\'' +
                ", moonrise='" + moonrise + '\'' +
                ", moonset='" + moonset + '\'' +
                '}';
    }
}
