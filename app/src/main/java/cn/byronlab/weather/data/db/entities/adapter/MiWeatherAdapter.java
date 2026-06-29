package cn.byronlab.weather.data.db.entities.adapter;

import cn.byronlab.weather.library.util.DateConvertUtils;
import cn.byronlab.weather.data.db.entities.minimalist.AirQualityLive;
import cn.byronlab.weather.data.db.entities.minimalist.LifeIndex;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherLive;
import cn.byronlab.weather.data.http.entity.mi.MiAQI;
import cn.byronlab.weather.data.http.entity.mi.MiForecast;
import cn.byronlab.weather.data.http.entity.mi.MiIndex;
import cn.byronlab.weather.data.http.entity.mi.MiRealTime;
import cn.byronlab.weather.data.http.entity.mi.MiWeather;

import java.util.ArrayList;
import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/26
 */
public class MiWeatherAdapter extends WeatherAdapter {

    private final MiWeather miWeather;

    public MiWeatherAdapter(MiWeather miWeather) {
        this.miWeather = miWeather;
    }

    @Override
    public String getCityId() {
        if (miWeather == null) {
            return "";
        }
        MiRealTime realTime = miWeather.getRealTime();
        if (realTime != null && !isEmpty(realTime.getCityId())) {
            return realTime.getCityId();
        }
        MiForecast forecast = miWeather.getForecast();
        if (forecast != null && !isEmpty(forecast.getCityId())) {
            return forecast.getCityId();
        }
        MiAQI aqi = miWeather.getAqi();
        return aqi == null || aqi.getCityId() <= 0 ? "" : String.valueOf(aqi.getCityId());
    }

    @Override
    public String getCityName() {
        if (miWeather == null) {
            return "";
        }
        MiAQI aqi = miWeather.getAqi();
        if (aqi != null && !isEmpty(aqi.getCityName())) {
            return aqi.getCityName();
        }
        MiForecast forecast = miWeather.getForecast();
        return forecast == null ? "" : safeString(forecast.getCityName());
    }

    @Override
    public String getCityNameEn() {
        MiForecast forecast = miWeather == null ? null : miWeather.getForecast();
        return forecast == null ? "" : safeString(forecast.getCityEn());
    }

    @Override
    public WeatherLive getWeatherLive() {
        MiRealTime realTime = miWeather == null ? null : miWeather.getRealTime();
        if (realTime == null) {
            return new WeatherLive(getCityId(), "", "", "", "", "", 0L);
        }
        return new WeatherLive(getCityId(),
                safeString(realTime.getWeather()), safeString(realTime.getTemp()),
                safeString(realTime.getHumidity()), safeString(realTime.getWind()),
                safeString(realTime.getWindSpeed()), DateConvertUtils.dateToTimeStamp(realTime.getTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));
    }

    @Override
    public List<WeatherForecast> getWeatherForecasts() {

        List<WeatherForecast> weatherForecasts = new ArrayList<>();
        MiForecast miForecast = miWeather == null ? null : miWeather.getForecast();
        if (miForecast == null) {
            return weatherForecasts;
        }

        //TODO Forecast中的日期和星期还需要修改
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather1(), miForecast.getTemp1(), miForecast.getWind1(), miForecast.getDate());
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather2(), miForecast.getTemp2(), miForecast.getWind2(), miForecast.getDate());
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather3(), miForecast.getTemp3(), miForecast.getWind3(), miForecast.getDate());
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather4(), miForecast.getTemp4(), miForecast.getWind4(), miForecast.getDate());
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather5(), miForecast.getTemp5(), miForecast.getWind5(), miForecast.getDate());
        addForecast(weatherForecasts, miForecast.getCityId(), miForecast.getWeather6(), miForecast.getTemp6(), miForecast.getWind6(), miForecast.getDate());

        return weatherForecasts;
    }

    @Override
    public List<LifeIndex> getLifeIndexes() {
        List<LifeIndex> lifeIndexes = new ArrayList<>();
        String cityId = getCityId();
        if (miWeather == null || miWeather.getIndexList() == null) {
            return lifeIndexes;
        }
        for (MiIndex miIndex : miWeather.getIndexList()) {
            if (miIndex == null) {
                continue;
            }
            lifeIndexes.add(new LifeIndex(cityId, safeString(miIndex.getName()), safeString(miIndex.getIndex()), safeString(miIndex.getDetails())));
        }
        return lifeIndexes;
    }

    @Override
    public AirQualityLive getAirQualityLive() {
        MiAQI aqiEntity = miWeather == null ? null : miWeather.getAqi();
        AirQualityLive airQualityLive = new AirQualityLive();
        airQualityLive.setCityId(getCityId());
        airQualityLive.setAqi(aqiEntity == null ? 0 : aqiEntity.getAqi());
        airQualityLive.setPm25(aqiEntity == null ? 0 : aqiEntity.getPm25());
        airQualityLive.setPm10(aqiEntity == null ? 0 : aqiEntity.getPm10());
        airQualityLive.setAdvice("");
        airQualityLive.setCityRank("");
        airQualityLive.setQuality(aqiEntity == null ? "" : safeString(aqiEntity.getSrc()));
        return airQualityLive;
    }

    private void addForecast(List<WeatherForecast> weatherForecasts, String cityId, String weather, String temperature, String wind, String date) {
        String[] weathers = splitWeather(weather);
        int[] temps = splitTemperature(temperature);
        String safeDate = safeString(date);
        weatherForecasts.add(new WeatherForecast(safeString(cityId), safeString(weather), weathers[0],
                weathers[1], temps[0], temps[1], safeString(wind), safeDate, DateConvertUtils.convertDataToWeek(safeDate)));
    }

    /**
     * 拆分天气
     *
     * @param weather 如：晴转多云
     * @return {"晴", "多云"}
     */
    private String[] splitWeather(String weather) {

        if (weather == null) {
            return new String[]{"", ""};
        }
        if (weather.contains("转")) {
            String[] weathers = weather.split("转", 2);
            return new String[]{safeString(weathers[0]), weathers.length > 1 ? safeString(weathers[1]) : safeString(weathers[0])};
        } else {
            return new String[]{weather, weather};
        }
    }

    /**
     * 拆分气温
     *
     * @param temperature 如：5℃~-3℃
     * @return {5, 3}
     */
    private int[] splitTemperature(String temperature) {
        if (temperature == null) {
            return new int[]{0, 0};
        }
        temperature = temperature.replace("℃", "").trim();
        if (temperature.contains("~")) {
            String[] temps = temperature.split("~", 2);
            int min = parseInt(temps[0], 0);
            int max = temps.length > 1 ? parseInt(temps[1], min) : min;
            return new int[]{min, max};
        }
        int temp = parseInt(temperature, 0);
        return new int[]{temp, temp};
    }

    private int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return fallback;
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

}
