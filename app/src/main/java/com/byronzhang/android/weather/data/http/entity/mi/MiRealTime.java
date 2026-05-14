package com.byronzhang.android.weather.data.http.entity.mi;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiRealTime {

    @JSONField(name = "SD")
    private String humidity;//湿度
    @JSONField(name = "WD")
    private String wind;//风向
    @JSONField(name = "WS")
    private String windSpeed;//风速
    @JSONField(name = "cityid")
    private String cityId;
    private String temp;//温度
    private String time;//发布时间
    private String weather;//天气情况

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    @NonNull
    @Override
    public String toString() {
        return "MiRealTime{" +
                "humidity='" + humidity + '\'' +
                ", wind='" + wind + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", cityId='" + cityId + '\'' +
                ", temp='" + temp + '\'' +
                ", time='" + time + '\'' +
                ", weather='" + weather + '\'' +
                '}';
    }
}
