package com.byronzhang.android.weather.data.http.entity.mi;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiToday {

    private int cityCode;//城市ID
    private String date;//日期
    private int humidityMax;//最大湿度
    private int humidityMin;//最小湿度
    private int precipitationMax;//最大降水量
    private int precipitationMin;//最低降水量
    private int tempMax;//最高温度
    private int tempMin;//最低温度
    private String weatherEnd;
    private String weatherStart;
    private String windDirectionEnd;
    private String windDirectionStart;
    private int windMax;//最大风力
    private int windMin;//最小风力

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHumidityMax() {
        return humidityMax;
    }

    public void setHumidityMax(int humidityMax) {
        this.humidityMax = humidityMax;
    }

    public int getHumidityMin() {
        return humidityMin;
    }

    public void setHumidityMin(int humidityMin) {
        this.humidityMin = humidityMin;
    }

    public int getPrecipitationMax() {
        return precipitationMax;
    }

    public void setPrecipitationMax(int precipitationMax) {
        this.precipitationMax = precipitationMax;
    }

    public int getPrecipitationMin() {
        return precipitationMin;
    }

    public void setPrecipitationMin(int precipitationMin) {
        this.precipitationMin = precipitationMin;
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

    public String getWeatherEnd() {
        return weatherEnd;
    }

    public void setWeatherEnd(String weatherEnd) {
        this.weatherEnd = weatherEnd;
    }

    public String getWeatherStart() {
        return weatherStart;
    }

    public void setWeatherStart(String weatherStart) {
        this.weatherStart = weatherStart;
    }

    public String getWindDirectionEnd() {
        return windDirectionEnd;
    }

    public void setWindDirectionEnd(String windDirectionEnd) {
        this.windDirectionEnd = windDirectionEnd;
    }

    public String getWindDirectionStart() {
        return windDirectionStart;
    }

    public void setWindDirectionStart(String windDirectionStart) {
        this.windDirectionStart = windDirectionStart;
    }

    public int getWindMax() {
        return windMax;
    }

    public void setWindMax(int windMax) {
        this.windMax = windMax;
    }

    public int getWindMin() {
        return windMin;
    }

    public void setWindMin(int windMin) {
        this.windMin = windMin;
    }

    @Override
    public String toString() {
        return "MiToday{" +
                "cityCode='" + cityCode + '\'' +
                ", date='" + date + '\'' +
                ", humidityMax=" + humidityMax +
                ", humidityMin=" + humidityMin +
                ", precipitationMax=" + precipitationMax +
                ", precipitationMin=" + precipitationMin +
                ", tempMax=" + tempMax +
                ", tempMin=" + tempMin +
                ", weatherEnd='" + weatherEnd + '\'' +
                ", weatherStart='" + weatherStart + '\'' +
                ", windDirectionEnd='" + windDirectionEnd + '\'' +
                ", windDirectionStart='" + windDirectionStart + '\'' +
                ", windMax=" + windMax +
                ", windMin=" + windMin +
                '}';
    }
}
