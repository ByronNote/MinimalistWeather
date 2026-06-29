package cn.byronlab.weather.domain.model;

import java.util.Objects;

public final class AirQuality {

    private final String cityId;
    private final int aqi;
    private final int pm25;
    private final int pm10;
    private final String publishTime;
    private final String advice;
    private final String cityRank;
    private final String quality;
    private final String co;
    private final String so2;
    private final String no2;
    private final String o3;
    private final String primaryPollutant;

    public AirQuality(String cityId, int aqi, int pm25, int pm10, String publishTime,
                      String advice, String cityRank, String quality, String co, String so2,
                      String no2, String o3, String primaryPollutant) {
        this.cityId = cityId;
        this.aqi = aqi;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.publishTime = publishTime;
        this.advice = advice;
        this.cityRank = cityRank;
        this.quality = quality;
        this.co = co;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.primaryPollutant = primaryPollutant;
    }

    public String getCityId() {
        return cityId;
    }

    public int getAqi() {
        return aqi;
    }

    public int getPm25() {
        return pm25;
    }

    public int getPm10() {
        return pm10;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public String getAdvice() {
        return advice;
    }

    public String getCityRank() {
        return cityRank;
    }

    public String getQuality() {
        return quality;
    }

    public String getCo() {
        return co;
    }

    public String getSo2() {
        return so2;
    }

    public String getNo2() {
        return no2;
    }

    public String getO3() {
        return o3;
    }

    public String getPrimaryPollutant() {
        return primaryPollutant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AirQuality)) {
            return false;
        }
        AirQuality that = (AirQuality) o;
        return aqi == that.aqi
                && pm25 == that.pm25
                && pm10 == that.pm10
                && Objects.equals(cityId, that.cityId)
                && Objects.equals(publishTime, that.publishTime)
                && Objects.equals(advice, that.advice)
                && Objects.equals(cityRank, that.cityRank)
                && Objects.equals(quality, that.quality)
                && Objects.equals(co, that.co)
                && Objects.equals(so2, that.so2)
                && Objects.equals(no2, that.no2)
                && Objects.equals(o3, that.o3)
                && Objects.equals(primaryPollutant, that.primaryPollutant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, aqi, pm25, pm10, publishTime, advice, cityRank, quality,
                co, so2, no2, o3, primaryPollutant);
    }
}
