package cn.byronlab.weather.data.http.entity.mi;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 * {
 *     "aqi": "52",
 *     "date": "2026-02-03T12:00:00+08:00",
 *     "status": 0,
 *     "sunRise": "2026-02-03T07:22:00+08:00",
 *     "sunSet": "2026-02-03T17:35:00+08:00",
 *     "tempMax": "8",
 *     "tempMin": "-2",
 *     "weatherEnd": "1",
 *     "weatherStart": "0",
 *     "windDircEnd": "149.0",
 *     "windDircStart": "149.0",
 *     "windSpeedEnd": "4.0",
 *     "windSpeedStart": "4.0"
 * }
 */
public class MiYesterday {

    private String aqi; // 空气质量指数（AQI）
    private String date; // 日期时间（ISO 8601，例如：2026-02-03T12:00:00+08:00）
    private Integer status; // 状态码（接口返回的 status 字段）
    private String sunRise; // 日出时间（ISO 8601）
    private String sunSet; // 日落时间（ISO 8601）
    private String tempMax; // 最高温度
    private String tempMin; // 最低温度
    private String weatherEnd; // 当日结束时的天气代码
    private String weatherStart; // 当日开始时的天气代码
    private String windDircEnd; // 当日结束时的风向角度
    private String windDircStart; // 当日开始时的风向角度
    private String windSpeedEnd; // 当日结束时的风速
    private String windSpeedStart; // 当日开始时的风速

    public MiYesterday() {}

    public String getAqi() {
        return aqi;
    }

    public String getDate() {
        return date;
    }

    public Integer getStatus() {
        return status;
    }

    public String getSunRise() {
        return sunRise;
    }

    public String getSunSet() {
        return sunSet;
    }

    public String getTempMax() {
        return tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public String getWeatherEnd() {
        return weatherEnd;
    }

    public String getWeatherStart() {
        return weatherStart;
    }

    public String getWindDircEnd() {
        return windDircEnd;
    }

    public String getWindDircStart() {
        return windDircStart;
    }

    public String getWindSpeedEnd() {
        return windSpeedEnd;
    }

    public String getWindSpeedStart() {
        return windSpeedStart;
    }


    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setSunRise(String sunRise) {
        this.sunRise = sunRise;
    }

    public void setSunSet(String sunSet) {
        this.sunSet = sunSet;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public void setWeatherEnd(String weatherEnd) {
        this.weatherEnd = weatherEnd;
    }

    public void setWeatherStart(String weatherStart) {
        this.weatherStart = weatherStart;
    }

    public void setWindDircEnd(String windDircEnd) {
        this.windDircEnd = windDircEnd;
    }

    public void setWindDircStart(String windDircStart) {
        this.windDircStart = windDircStart;
    }

    public void setWindSpeedEnd(String windSpeedEnd) {
        this.windSpeedEnd = windSpeedEnd;
    }

    public void setWindSpeedStart(String windSpeedStart) {
        this.windSpeedStart = windSpeedStart;
    }

}
