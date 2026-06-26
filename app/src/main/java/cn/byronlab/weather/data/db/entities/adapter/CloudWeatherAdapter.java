package cn.byronlab.weather.data.db.entities.adapter;

import cn.byronlab.weather.library.util.DateConvertUtils;
import cn.byronlab.weather.data.db.entities.minimalist.AirQualityLive;
import cn.byronlab.weather.data.db.entities.minimalist.LifeIndex;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherLive;
import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudCityAirLive;
import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudForecast;
import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudWeatherLive;

import java.util.ArrayList;
import java.util.List;


/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/7/5
 */
public class CloudWeatherAdapter extends WeatherAdapter {

    private EnvironmentCloudWeatherLive cloudWeatherLive;
    private EnvironmentCloudForecast cloudForecast;
    private EnvironmentCloudCityAirLive cloudCityAirLive;

    public CloudWeatherAdapter(EnvironmentCloudWeatherLive cloudWeatherLive, EnvironmentCloudForecast cloudForecast, EnvironmentCloudCityAirLive cloudCityAirLive) {
        this.cloudWeatherLive = cloudWeatherLive;
        this.cloudForecast = cloudForecast;
        this.cloudCityAirLive = cloudCityAirLive;
    }

    @Override
    public String getCityId() {
        return cloudWeatherLive.getCityId();
    }

    @Override
    public String getCityName() {
        return cloudForecast.getCityName();
    }

    @Override
    public String getCityNameEn() {
        return null;
    }

    @Override
    public WeatherLive getWeatherLive() {

        WeatherLive weatherLive = new WeatherLive();
        weatherLive.setAirPressure(cloudWeatherLive.getAirPressure());
        weatherLive.setCityId(cloudWeatherLive.getCityId());
        weatherLive.setFeelsTemperature(cloudWeatherLive.getFeelsTemperature());
        weatherLive.setHumidity(cloudWeatherLive.getHumidity());
        weatherLive.setRain(cloudWeatherLive.getRain());
        weatherLive.setTemp(cloudWeatherLive.getTemperature());
        weatherLive.setTime(DateConvertUtils.dateToTimeStamp(cloudWeatherLive.getUpdateTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));
        weatherLive.setWeather(cloudWeatherLive.getPhenomena());
        weatherLive.setWind(cloudWeatherLive.getWindDirect());
        weatherLive.setWindPower(cloudWeatherLive.getWindPower());
        weatherLive.setWindSpeed(cloudWeatherLive.getWindSpeed());

        return weatherLive;
    }

    @Override
    public List<WeatherForecast> getWeatherForecasts() {

        List<WeatherForecast> weatherForecasts = new ArrayList<>();

        for (EnvironmentCloudForecast.ForecastEntity forecastEntity : cloudForecast.getForecast()) {

            WeatherForecast weatherForecast = new WeatherForecast();
            weatherForecast.setWind(forecastEntity.getWind().getDir());
            weatherForecast.setCityId(getCityId());
            weatherForecast.setHumidity(forecastEntity.getHum());
            weatherForecast.setMoonrise(forecastEntity.getAstro().getMr());
            weatherForecast.setMoonset(forecastEntity.getAstro().getMs());
            weatherForecast.setPop(forecastEntity.getPop());
            weatherForecast.setPrecipitation(forecastEntity.getPcpn());
            weatherForecast.setPressure(forecastEntity.getPres());
            weatherForecast.setSunrise(forecastEntity.getAstro().getSr());
            weatherForecast.setSunset(forecastEntity.getAstro().getSs());
            weatherForecast.setTempMax(Integer.parseInt(forecastEntity.getTmp().getMax()));
            weatherForecast.setTempMin(Integer.parseInt(forecastEntity.getTmp().getMin()));
            weatherForecast.setUv(forecastEntity.getUv());
            weatherForecast.setVisibility(forecastEntity.getVis());
//            weatherForecast.setWeather();
            weatherForecast.setWeatherDay(forecastEntity.getCond().getCond_d());
            weatherForecast.setWeatherNight(forecastEntity.getCond().getCond_n());
            weatherForecast.setWeek(DateConvertUtils.convertDataToWeek(forecastEntity.getDate()));
            weatherForecast.setDate(DateConvertUtils.convertDataToString(forecastEntity.getDate()));
            weatherForecasts.add(weatherForecast);
        }

        return weatherForecasts;
    }

    @Override
    public List<LifeIndex> getLifeIndexes() {

        EnvironmentCloudForecast.SuggestionEntity suggestionEntity = cloudForecast.getSuggestion();

        List<LifeIndex> indexList = new ArrayList<>();

        LifeIndex index1 = new LifeIndex();
        index1.setCityId(cloudForecast.getCityId());
        index1.setName("空气质量");
        index1.setIndex(suggestionEntity.getAir().getBrf());
        index1.setDetails(suggestionEntity.getAir().getTxt());
        indexList.add(index1);

        LifeIndex index2 = new LifeIndex();
        index2.setCityId(cloudForecast.getCityId());
        index2.setName("舒适度");
        index2.setIndex(suggestionEntity.getComf().getBrf());
        index2.setDetails(suggestionEntity.getComf().getTxt());
        indexList.add(index2);

        LifeIndex index3 = new LifeIndex();
        index3.setCityId(cloudForecast.getCityId());
        index3.setName("穿衣");
        index3.setIndex(suggestionEntity.getDrs().getBrf());
        index3.setDetails(suggestionEntity.getDrs().getTxt());
        indexList.add(index3);

        LifeIndex index4 = new LifeIndex();
        index4.setCityId(cloudForecast.getCityId());
        index4.setName("感冒");
        index4.setIndex(suggestionEntity.getFlu().getBrf());
        index4.setDetails(suggestionEntity.getFlu().getTxt());
        indexList.add(index4);

        LifeIndex index5 = new LifeIndex();
        index5.setCityId(cloudForecast.getCityId());
        index5.setName("运动");
        index5.setIndex(suggestionEntity.getSport().getBrf());
        index5.setDetails(suggestionEntity.getSport().getTxt());
        indexList.add(index5);

        LifeIndex index6 = new LifeIndex();
        index6.setCityId(cloudForecast.getCityId());
        index6.setName("旅游");
        index6.setIndex(suggestionEntity.getTrav().getBrf());
        index6.setDetails(suggestionEntity.getTrav().getTxt());
        indexList.add(index6);

        LifeIndex index7 = new LifeIndex();
        index7.setCityId(cloudForecast.getCityId());
        index7.setName("紫外线");
        index7.setIndex(suggestionEntity.getUv().getBrf());
        index7.setDetails(suggestionEntity.getUv().getTxt());
        indexList.add(index7);

        LifeIndex index8 = new LifeIndex();
        index8.setCityId(cloudForecast.getCityId());
        index8.setName("洗车");
        index8.setIndex(suggestionEntity.getCw().getBrf());
        index8.setDetails(suggestionEntity.getCw().getTxt());
        indexList.add(index8);

        return indexList;
    }

    @Override
    public AirQualityLive getAirQualityLive() {

        AirQualityLive airQualityLive = new AirQualityLive();
//        airQualityLive.setAdvice("");
        airQualityLive.setAqi(Integer.parseInt(cloudCityAirLive.getAqi()));
        airQualityLive.setCityId(cloudCityAirLive.getCityId());
//        airQualityLive.setCityRank("");
        airQualityLive.setCo(cloudCityAirLive.getCo());
        airQualityLive.setNo2(cloudCityAirLive.getNo2());
        airQualityLive.setO3(cloudCityAirLive.getO3());
        airQualityLive.setPm10(Integer.parseInt(cloudCityAirLive.getPm10()));
        airQualityLive.setPm25(Integer.parseInt(cloudCityAirLive.getPm25()));
        airQualityLive.setPrimary(cloudCityAirLive.getPrimary());
        airQualityLive.setPublishTime(cloudCityAirLive.getTime());
        airQualityLive.setQuality(getAqiQuality(airQualityLive.getAqi()));
        airQualityLive.setSo2(cloudCityAirLive.getSo2());
        return airQualityLive;
    }

    private String getAqiQuality(int aqi) {

        if (aqi <= 50) {
            return "优";
        } else if (aqi > 50 && aqi <= 100) {
            return "良";
        } else if (aqi > 100 && aqi <= 150) {
            return "轻度污染";
        } else if (aqi > 150 && aqi <= 200) {
            return "中度污染";
        } else if (aqi > 200 && aqi <= 300) {
            return "重度污染";
        } else if (aqi > 300 && aqi < 500) {
            return "严重污染";
        } else if (aqi >= 500) {
            return "污染爆表";
        }
        return null;
    }
}
