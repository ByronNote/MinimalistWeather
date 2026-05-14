package com.byronzhang.android.weather.data.http.entity.envicloud;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 城市实时空气质量
 *
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/2/16
 */
public class EnvironmentCloudCityAirLive {


    /**
     * citycode : 101020100
     * PM25 : 33
     * time : 2017021614
     * rdesc : Success
     * PM10 : 43
     * SO2 : 12.25
     * o3 : 51.58
     * NO2 : 53.17
     * primary : 颗粒物(PM10)
     * rcode : 200
     * CO : 0.77
     * AQI : 46
     */

    @JSONField(name = "rcode")
    private int requestCode;//结果吗

    @JSONField(name = "rdesc")
    private String requestDesc;//结果描述

    @JSONField(name = "citycode")
    private String cityId;//城市ID

    private String time;//时间(yyyyMMddHH)

    @JSONField(name = "AQI")
    private String aqi;//空气质量指数

    @JSONField(name = "PM25")
    private String pm25;//PM2.5浓度(μg/m3)

    @JSONField(name = "PM10")
    private String pm10;//PM10浓度(μg/m3)

    @JSONField(name = "CO")
    private String co;//一氧化碳浓度(mg/m3)

    @JSONField(name = "SO2")
    private String so2;//二氧化硫浓度(μg/m3)

    @JSONField(name = "NO2")
    private String no2;//二氧化氮浓度(μg/m3)

    private String o3;//臭氧浓度(μg/m3)

    private String primary;//首要污染物

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestDesc() {
        return requestDesc;
    }

    public void setRequestDesc(String requestDesc) {
        this.requestDesc = requestDesc;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getSo2() {
        return so2;
    }

    public void setSo2(String so2) {
        this.so2 = so2;
    }

    public String getNo2() {
        return no2;
    }

    public void setNo2(String no2) {
        this.no2 = no2;
    }

    public String getO3() {
        return o3;
    }

    public void setO3(String o3) {
        this.o3 = o3;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
