package com.byronzhang.android.weather.data.http.entity.mi;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 小米天气空气污染指数
 *
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiAQI {

    private String cityName;
    private int cityId;
    private String pubTime;//发布时间
    private int aqi;//空气质量指数
    private int co;//一氧化碳 浓度 (μg/m³)
    private int no2;//二氧化氮 浓度 (μg/m³)
    private int o3;//臭氧 浓度 (μg/m³)
    private int so2;//二氧化硫 浓度 (μg/m³)
    private int pm25;//PM2.5 浓度 (μg/m³)
    private int pm10;//PM10 浓度 (μg/m³)
    private String src;//来源，例如：中国环境监测总站
    private String primary;//首要污染物
    private String suggest;//建议，例如：空气质量可以接受，可能对少数异常敏感的人群健康有较弱影响
    private String pm25Desc;//例：PM2.5指的是直径小于或等于2.5微米的颗粒物，又称为细颗粒物
    private String pm10Desc;//例：PM10的主要来源是建筑活动和从地表扬起的尘土，含有氧化物矿物和其他成分
    private String coDesc;//例：一氧化碳是无色，无臭，无味气体，但吸入对人体有十分大的危害
    private String so2Desc;//例：二氧化硫是一种无色气体，当空气中SO2达到一定浓度时，空气中会有刺鼻的气味
    private String no2Desc;//例：二氧化氮的主要来源是燃烧过程产生，例如供热、发电以及机动车和船舶的发动机
    private String o3Desc;//例：空气中过多臭氧可能导致呼吸问题，引发哮喘，降低肺功能并引起肺部疾病，对人类健康影响较大

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getPubTime() {
        return pubTime;
    }

    public void setPubTime(String pubTime) {
        this.pubTime = pubTime;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public int getCo() {
        return co;
    }

    public void setCo(int co) {
        this.co = co;
    }

    public int getNo2() {
        return no2;
    }

    public void setNo2(int no2) {
        this.no2 = no2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public int getSo2() {
        return so2;
    }

    public void setSo2(int so2) {
        this.so2 = so2;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public int getPm10() {
        return pm10;
    }

    public void setPm10(int pm10) {
        this.pm10 = pm10;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getPm25Desc() {
        return pm25Desc;
    }

    public void setPm25Desc(String pm25Desc) {
        this.pm25Desc = pm25Desc;
    }

    public String getPm10Desc() {
        return pm10Desc;
    }

    public void setPm10Desc(String pm10Desc) {
        this.pm10Desc = pm10Desc;
    }

    public String getCoDesc() {
        return coDesc;
    }

    public void setCoDesc(String coDesc) {
        this.coDesc = coDesc;
    }

    public String getSo2Desc() {
        return so2Desc;
    }

    public void setSo2Desc(String so2Desc) {
        this.so2Desc = so2Desc;
    }

    public String getNo2Desc() {
        return no2Desc;
    }

    public void setNo2Desc(String no2Desc) {
        this.no2Desc = no2Desc;
    }

    public String getO3Desc() {
        return o3Desc;
    }

    public void setO3Desc(String o3Desc) {
        this.o3Desc = o3Desc;
    }

    @NonNull
    @Override
    public String toString() {
        return "MiAQI{" +
                "cityName='" + cityName + '\'' +
                ", cityId=" + cityId +
                ", pubTime='" + pubTime + '\'' +
                ", aqi=" + aqi +
                ", co=" + co +
                ", no2=" + no2 +
                ", o3=" + o3 +
                ", so2=" + so2 +
                ", pm25=" + pm25 +
                ", pm10=" + pm10 +
                ", src='" + src + '\'' +
                ", primary='" + primary + '\'' +
                ", suggest='" + suggest + '\'' +
                ", pm25Desc='" + pm25Desc + '\'' +
                ", pm10Desc='" + pm10Desc + '\'' +
                ", coDesc='" + coDesc + '\'' +
                ", so2Desc='" + so2Desc + '\'' +
                ", no2Desc='" + no2Desc + '\'' +
                ", o3Desc='" + o3Desc + '\'' +
                '}';
    }
}
