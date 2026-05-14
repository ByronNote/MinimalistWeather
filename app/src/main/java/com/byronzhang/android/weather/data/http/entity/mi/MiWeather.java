package com.byronzhang.android.weather.data.http.entity.mi;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiWeather {

    private MiForecast forecast;
    @JSONField(name = "realtime")
    private MiRealTime realTime;
    private MiAQI aqi;
    @JSONField(name = "index")
    private List<MiIndex> indexList;
    private MiToday today;
    private MiToday yesterday;
    private String updateTime;//更新时间

    public MiAQI getAqi() {
        return aqi;
    }

    public void setAqi(MiAQI aqi) {
        this.aqi = aqi;
    }

    public MiForecast getForecast() {
        return forecast;
    }

    public void setForecast(MiForecast forecast) {
        this.forecast = forecast;
    }

    public List<MiIndex> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<MiIndex> indexList) {
        this.indexList = indexList;
    }

    public MiRealTime getRealTime() {
        return realTime;
    }

    public void setRealTime(MiRealTime realTime) {
        this.realTime = realTime;
    }

    public MiToday getToday() {
        return today;
    }

    public void setToday(MiToday today) {
        this.today = today;
    }

    public MiToday getYesterday() {
        return yesterday;
    }

    public void setYesterday(MiToday yesterday) {
        this.yesterday = yesterday;
    }

    @Override
    public String toString() {
        return "MiWeather{" +
                "forecast=" + forecast +
                ", realTime=" + realTime +
                ", aqi=" + aqi +
                ", indexList=" + indexList +
                ", today=" + today +
                ", yesterday=" + yesterday +
                '}';
    }
}
