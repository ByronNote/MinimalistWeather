package com.byronzhang.android.weather.data.db.entities.adapter;

import com.byronzhang.android.library.util.DateConvertUtils;
import com.byronzhang.android.weather.data.db.entities.minimalist.AirQualityLive;
import com.byronzhang.android.weather.data.db.entities.minimalist.LifeIndex;
import com.byronzhang.android.weather.data.db.entities.minimalist.WeatherForecast;
import com.byronzhang.android.weather.data.db.entities.minimalist.WeatherLive;
import com.byronzhang.android.weather.data.http.entity.mi.MiAQI;
import com.byronzhang.android.weather.data.http.entity.mi.MiForecast;
import com.byronzhang.android.weather.data.http.entity.mi.MiIndex;
import com.byronzhang.android.weather.data.http.entity.mi.MiWeather;

import java.util.ArrayList;
import java.util.List;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/26
 */
public class MiWeatherAdapter extends WeatherAdapter {

    private final MiWeather miWeather;

    public MiWeatherAdapter(MiWeather miWeather) {
        this.miWeather = miWeather;
    }

    @Override
    public String getCityId() {
        return String.valueOf(miWeather.getAqi().getCityId());
    }

    @Override
    public String getCityName() {
        return miWeather.getAqi().getCityName();
    }

    @Override
    public String getCityNameEn() {
        return miWeather.getForecast().getCityEn();
    }

    @Override
    public WeatherLive getWeatherLive() {
        return new WeatherLive(miWeather.getRealTime().getCityId(),
                miWeather.getRealTime().getWeather(), miWeather.getRealTime().getTemp(),
                miWeather.getRealTime().getHumidity(), miWeather.getRealTime().getWind(),
                miWeather.getRealTime().getWindSpeed(), DateConvertUtils.dateToTimeStamp(miWeather.getRealTime().getTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));
    }

    @Override
    public List<WeatherForecast> getWeatherForecasts() {

        List<WeatherForecast> weatherForecasts = new ArrayList<>();
        MiForecast miForecast = miWeather.getForecast();

        //TODO Forecast中的日期和星期还需要修改
        String[] weathers1 = splitWeather(miForecast.getWeather1());
        int[] temps1 = splitTemperature(miForecast.getTemp1());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather1(), weathers1[0],
                weathers1[1], temps1[0], temps1[1], miForecast.getWind1(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        String[] weathers2 = splitWeather(miForecast.getWeather2());
        int[] temps2 = splitTemperature(miForecast.getTemp2());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather2(), weathers2[0],
                weathers2[1], temps2[0], temps2[1], miForecast.getWind2(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        String[] weathers3 = splitWeather(miForecast.getWeather3());
        int[] temps3 = splitTemperature(miForecast.getTemp3());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather3(), weathers3[0],
                weathers3[1], temps3[0], temps3[1], miForecast.getWind3(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        String[] weathers4 = splitWeather(miForecast.getWeather4());
        int[] temps4 = splitTemperature(miForecast.getTemp4());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather4(), weathers4[0],
                weathers4[1], temps4[0], temps4[1], miForecast.getWind4(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        String[] weathers5 = splitWeather(miForecast.getWeather5());
        int[] temps5 = splitTemperature(miForecast.getTemp5());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather5(), weathers5[0],
                weathers5[1], temps5[0], temps5[1], miForecast.getWind5(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        String[] weathers6 = splitWeather(miForecast.getWeather6());
        int[] temps6 = splitTemperature(miForecast.getTemp6());
        weatherForecasts.add(new WeatherForecast(miForecast.getCityId(), miForecast.getWeather6(), weathers6[0],
                weathers6[1], temps6[0], temps6[1], miForecast.getWind6(), miForecast.getDate(), DateConvertUtils.convertDataToWeek(miForecast.getDate())));

        return weatherForecasts;
    }

    @Override
    public List<LifeIndex> getLifeIndexes() {
        List<LifeIndex> lifeIndexes = new ArrayList<>();
        String cityId = miWeather.getForecast().getCityId();
        for (MiIndex miIndex : miWeather.getIndexList()) {
            lifeIndexes.add(new LifeIndex(cityId, miIndex.getName(), miIndex.getIndex(), miIndex.getDetails()));
        }
        return lifeIndexes;
    }

    @Override
    public AirQualityLive getAirQualityLive() {
        MiAQI aqiEntity = miWeather.getAqi();
        AirQualityLive airQualityLive = new AirQualityLive();
        airQualityLive.setCityId(String.valueOf(miWeather.getAqi().getCityId()));
        airQualityLive.setAqi(aqiEntity.getAqi());
        airQualityLive.setPm25(aqiEntity.getPm25());
        airQualityLive.setPm10(aqiEntity.getPm10());
        airQualityLive.setAdvice("");
        airQualityLive.setCityRank("");
        airQualityLive.setQuality(aqiEntity.getSrc());
        return airQualityLive;
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
            return weather.split("转");
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
        if (temperature != null && temperature.contains("~")) {
            if (temperature.contains("℃")) {
                temperature = temperature.replaceAll("℃", "");
            }
            String[] temps = temperature.split("~");
            return new int[]{Integer.parseInt(temps[0]), Integer.parseInt(temps[1])};
        } else {
            return null;
        }
    }

}
