package cn.byronlab.weather.data.http.entity.mi;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 小米天气未来7天天气预报
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiForecast {

    @JSONField(name = "city")
    private String cityName;
    @JSONField(name = "city_en")
    private String cityEn;
    @JSONField(name = "cityid")
    private String cityId;
    private String date;
    @JSONField(name = "date_y")
    private String dateY;
    private String fl1;
    private String fl2;
    private String fl3;
    private String fl4;
    private String fl5;
    private String fl6;

    private String temp1;//11℃~4℃
    private String temp2;
    private String temp3;
    private String temp4;
    private String temp5;
    private String temp6;
    private String tempF1;
    private String tempF2;
    private String tempF3;
    private String tempF4;
    private String tempF5;
    private String tempF6;
    private String weather1;
    private String weather2;
    private String weather3;
    private String weather4;
    private String weather5;
    private String weather6;
    private String week;
    private String wind1;
    private String wind2;
    private String wind3;
    private String wind4;
    private String wind5;
    private String wind6;

    public String getCityEn() {
        return cityEn;
    }

    public void setCityEn(String cityEn) {
        this.cityEn = cityEn;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateY() {
        return dateY;
    }

    public void setDateY(String dateY) {
        this.dateY = dateY;
    }

    public String getFl1() {
        return fl1;
    }

    public void setFl1(String fl1) {
        this.fl1 = fl1;
    }

    public String getFl2() {
        return fl2;
    }

    public void setFl2(String fl2) {
        this.fl2 = fl2;
    }

    public String getFl3() {
        return fl3;
    }

    public void setFl3(String fl3) {
        this.fl3 = fl3;
    }

    public String getFl4() {
        return fl4;
    }

    public void setFl4(String fl4) {
        this.fl4 = fl4;
    }

    public String getFl5() {
        return fl5;
    }

    public void setFl5(String fl5) {
        this.fl5 = fl5;
    }

    public String getFl6() {
        return fl6;
    }

    public void setFl6(String fl6) {
        this.fl6 = fl6;
    }

    public String getTemp1() {
        return temp1;
    }

    public void setTemp1(String temp1) {
        this.temp1 = temp1;
    }

    public String getTemp2() {
        return temp2;
    }

    public void setTemp2(String temp2) {
        this.temp2 = temp2;
    }

    public String getTemp3() {
        return temp3;
    }

    public void setTemp3(String temp3) {
        this.temp3 = temp3;
    }

    public String getTemp4() {
        return temp4;
    }

    public void setTemp4(String temp4) {
        this.temp4 = temp4;
    }

    public String getTemp5() {
        return temp5;
    }

    public void setTemp5(String temp5) {
        this.temp5 = temp5;
    }

    public String getTemp6() {
        return temp6;
    }

    public void setTemp6(String temp6) {
        this.temp6 = temp6;
    }

    public String getTempF1() {
        return tempF1;
    }

    public void setTempF1(String tempF1) {
        this.tempF1 = tempF1;
    }

    public String getTempF2() {
        return tempF2;
    }

    public void setTempF2(String tempF2) {
        this.tempF2 = tempF2;
    }

    public String getTempF3() {
        return tempF3;
    }

    public void setTempF3(String tempF3) {
        this.tempF3 = tempF3;
    }

    public String getTempF4() {
        return tempF4;
    }

    public void setTempF4(String tempF4) {
        this.tempF4 = tempF4;
    }

    public String getTempF5() {
        return tempF5;
    }

    public void setTempF5(String tempF5) {
        this.tempF5 = tempF5;
    }

    public String getTempF6() {
        return tempF6;
    }

    public void setTempF6(String tempF6) {
        this.tempF6 = tempF6;
    }

    public String getWeather1() {
        return weather1;
    }

    public void setWeather1(String weather1) {
        this.weather1 = weather1;
    }

    public String getWeather2() {
        return weather2;
    }

    public void setWeather2(String weather2) {
        this.weather2 = weather2;
    }

    public String getWeather3() {
        return weather3;
    }

    public void setWeather3(String weather3) {
        this.weather3 = weather3;
    }

    public String getWeather4() {
        return weather4;
    }

    public void setWeather4(String weather4) {
        this.weather4 = weather4;
    }

    public String getWeather5() {
        return weather5;
    }

    public void setWeather5(String weather5) {
        this.weather5 = weather5;
    }

    public String getWeather6() {
        return weather6;
    }

    public void setWeather6(String weather6) {
        this.weather6 = weather6;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWind1() {
        return wind1;
    }

    public void setWind1(String wind1) {
        this.wind1 = wind1;
    }

    public String getWind2() {
        return wind2;
    }

    public void setWind2(String wind2) {
        this.wind2 = wind2;
    }

    public String getWind3() {
        return wind3;
    }

    public void setWind3(String wind3) {
        this.wind3 = wind3;
    }

    public String getWind4() {
        return wind4;
    }

    public void setWind4(String wind4) {
        this.wind4 = wind4;
    }

    public String getWind5() {
        return wind5;
    }

    public void setWind5(String wind5) {
        this.wind5 = wind5;
    }

    public String getWind6() {
        return wind6;
    }

    public void setWind6(String wind6) {
        this.wind6 = wind6;
    }

    @Override
    public String toString() {
        return "MiForecast{" +
                "cityName='" + cityName + '\'' +
                ", cityEn='" + cityEn + '\'' +
                ", cityId='" + cityId + '\'' +
                ", date='" + date + '\'' +
                ", dateY='" + dateY + '\'' +
                ", fl1='" + fl1 + '\'' +
                ", fl2='" + fl2 + '\'' +
                ", fl3='" + fl3 + '\'' +
                ", fl4='" + fl4 + '\'' +
                ", fl5='" + fl5 + '\'' +
                ", fl6='" + fl6 + '\'' +
                ", temp1='" + temp1 + '\'' +
                ", temp2='" + temp2 + '\'' +
                ", temp3='" + temp3 + '\'' +
                ", temp4='" + temp4 + '\'' +
                ", temp5='" + temp5 + '\'' +
                ", temp6='" + temp6 + '\'' +
                ", tempF1='" + tempF1 + '\'' +
                ", tempF2='" + tempF2 + '\'' +
                ", tempF3='" + tempF3 + '\'' +
                ", tempF4='" + tempF4 + '\'' +
                ", tempF5='" + tempF5 + '\'' +
                ", tempF6='" + tempF6 + '\'' +
                ", weather1='" + weather1 + '\'' +
                ", weather2='" + weather2 + '\'' +
                ", weather3='" + weather3 + '\'' +
                ", weather4='" + weather4 + '\'' +
                ", weather5='" + weather5 + '\'' +
                ", weather6='" + weather6 + '\'' +
                ", week='" + week + '\'' +
                ", wind1='" + wind1 + '\'' +
                ", wind2='" + wind2 + '\'' +
                ", wind3='" + wind3 + '\'' +
                ", wind4='" + wind4 + '\'' +
                ", wind5='" + wind5 + '\'' +
                ", wind6='" + wind6 + '\'' +
                '}';
    }
}
