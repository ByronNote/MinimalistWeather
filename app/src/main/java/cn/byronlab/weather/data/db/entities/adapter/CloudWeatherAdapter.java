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
        if (cloudWeatherLive != null && !isEmpty(cloudWeatherLive.getCityId())) {
            return cloudWeatherLive.getCityId();
        }
        if (cloudForecast != null && !isEmpty(cloudForecast.getCityId())) {
            return cloudForecast.getCityId();
        }
        return cloudCityAirLive == null ? "" : safeString(cloudCityAirLive.getCityId());
    }

    @Override
    public String getCityName() {
        return cloudForecast == null ? "" : safeString(cloudForecast.getCityName());
    }

    @Override
    public String getCityNameEn() {
        return null;
    }

    @Override
    public WeatherLive getWeatherLive() {

        WeatherLive weatherLive = new WeatherLive();
        if (cloudWeatherLive == null) {
            weatherLive.setCityId(getCityId());
            return weatherLive;
        }
        weatherLive.setAirPressure(safeString(cloudWeatherLive.getAirPressure()));
        weatherLive.setCityId(getCityId());
        weatherLive.setFeelsTemperature(safeString(cloudWeatherLive.getFeelsTemperature()));
        weatherLive.setHumidity(safeString(cloudWeatherLive.getHumidity()));
        weatherLive.setRain(safeString(cloudWeatherLive.getRain()));
        weatherLive.setTemp(safeString(cloudWeatherLive.getTemperature()));
        weatherLive.setTime(DateConvertUtils.dateToTimeStamp(cloudWeatherLive.getUpdateTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));
        weatherLive.setWeather(safeString(cloudWeatherLive.getPhenomena()));
        weatherLive.setWind(safeString(cloudWeatherLive.getWindDirect()));
        weatherLive.setWindPower(safeString(cloudWeatherLive.getWindPower()));
        weatherLive.setWindSpeed(safeString(cloudWeatherLive.getWindSpeed()));

        return weatherLive;
    }

    @Override
    public List<WeatherForecast> getWeatherForecasts() {

        List<WeatherForecast> weatherForecasts = new ArrayList<>();

        if (cloudForecast == null || cloudForecast.getForecast() == null) {
            return weatherForecasts;
        }

        for (EnvironmentCloudForecast.ForecastEntity forecastEntity : cloudForecast.getForecast()) {
            if (forecastEntity == null) {
                continue;
            }
            EnvironmentCloudForecast.ForecastEntity.WindEntity wind = forecastEntity.getWind();
            EnvironmentCloudForecast.ForecastEntity.AstroEntity astro = forecastEntity.getAstro();
            EnvironmentCloudForecast.ForecastEntity.TmpEntity tmp = forecastEntity.getTmp();
            EnvironmentCloudForecast.ForecastEntity.CondEntity cond = forecastEntity.getCond();

            WeatherForecast weatherForecast = new WeatherForecast();
            weatherForecast.setWind(wind == null ? "" : safeString(wind.getDir()));
            weatherForecast.setCityId(getCityId());
            weatherForecast.setHumidity(safeString(forecastEntity.getHum()));
            weatherForecast.setMoonrise(astro == null ? "" : safeString(astro.getMr()));
            weatherForecast.setMoonset(astro == null ? "" : safeString(astro.getMs()));
            weatherForecast.setPop(safeString(forecastEntity.getPop()));
            weatherForecast.setPrecipitation(safeString(forecastEntity.getPcpn()));
            weatherForecast.setPressure(safeString(forecastEntity.getPres()));
            weatherForecast.setSunrise(astro == null ? "" : safeString(astro.getSr()));
            weatherForecast.setSunset(astro == null ? "" : safeString(astro.getSs()));
            weatherForecast.setTempMax(tmp == null ? 0 : parseInt(tmp.getMax(), 0));
            weatherForecast.setTempMin(tmp == null ? 0 : parseInt(tmp.getMin(), 0));
            weatherForecast.setUv(safeString(forecastEntity.getUv()));
            weatherForecast.setVisibility(safeString(forecastEntity.getVis()));
//            weatherForecast.setWeather();
            weatherForecast.setWeatherDay(cond == null ? "" : safeString(cond.getCond_d()));
            weatherForecast.setWeatherNight(cond == null ? "" : safeString(cond.getCond_n()));
            weatherForecast.setWeek(DateConvertUtils.convertDataToWeek(forecastEntity.getDate()));
            weatherForecast.setDate(DateConvertUtils.convertDataToString(forecastEntity.getDate()));
            weatherForecasts.add(weatherForecast);
        }

        return weatherForecasts;
    }

    @Override
    public List<LifeIndex> getLifeIndexes() {

        EnvironmentCloudForecast.SuggestionEntity suggestionEntity = cloudForecast == null ? null : cloudForecast.getSuggestion();

        List<LifeIndex> indexList = new ArrayList<>();
        if (suggestionEntity == null) {
            return indexList;
        }

        if (suggestionEntity.getAir() != null) {
            addLifeIndex(indexList, "空气质量", suggestionEntity.getAir().getBrf(), suggestionEntity.getAir().getTxt());
        }
        if (suggestionEntity.getComf() != null) {
            addLifeIndex(indexList, "舒适度", suggestionEntity.getComf().getBrf(), suggestionEntity.getComf().getTxt());
        }
        if (suggestionEntity.getDrs() != null) {
            addLifeIndex(indexList, "穿衣", suggestionEntity.getDrs().getBrf(), suggestionEntity.getDrs().getTxt());
        }
        if (suggestionEntity.getFlu() != null) {
            addLifeIndex(indexList, "感冒", suggestionEntity.getFlu().getBrf(), suggestionEntity.getFlu().getTxt());
        }
        if (suggestionEntity.getSport() != null) {
            addLifeIndex(indexList, "运动", suggestionEntity.getSport().getBrf(), suggestionEntity.getSport().getTxt());
        }
        if (suggestionEntity.getTrav() != null) {
            addLifeIndex(indexList, "旅游", suggestionEntity.getTrav().getBrf(), suggestionEntity.getTrav().getTxt());
        }
        if (suggestionEntity.getUv() != null) {
            addLifeIndex(indexList, "紫外线", suggestionEntity.getUv().getBrf(), suggestionEntity.getUv().getTxt());
        }
        if (suggestionEntity.getCw() != null) {
            addLifeIndex(indexList, "洗车", suggestionEntity.getCw().getBrf(), suggestionEntity.getCw().getTxt());
        }

        return indexList;
    }

    @Override
    public AirQualityLive getAirQualityLive() {

        AirQualityLive airQualityLive = new AirQualityLive();
        if (cloudCityAirLive == null) {
            airQualityLive.setCityId(getCityId());
            airQualityLive.setQuality("");
            return airQualityLive;
        }
//        airQualityLive.setAdvice("");
        airQualityLive.setAqi(parseInt(cloudCityAirLive.getAqi(), 0));
        airQualityLive.setCityId(getCityId());
//        airQualityLive.setCityRank("");
        airQualityLive.setCo(safeString(cloudCityAirLive.getCo()));
        airQualityLive.setNo2(safeString(cloudCityAirLive.getNo2()));
        airQualityLive.setO3(safeString(cloudCityAirLive.getO3()));
        airQualityLive.setPm10(parseInt(cloudCityAirLive.getPm10(), 0));
        airQualityLive.setPm25(parseInt(cloudCityAirLive.getPm25(), 0));
        airQualityLive.setPrimary(safeString(cloudCityAirLive.getPrimary()));
        airQualityLive.setPublishTime(safeString(cloudCityAirLive.getTime()));
        airQualityLive.setQuality(getAqiQuality(airQualityLive.getAqi()));
        airQualityLive.setSo2(safeString(cloudCityAirLive.getSo2()));
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
        return "";
    }

    private void addLifeIndex(List<LifeIndex> indexList, String name, String index, String details) {
        LifeIndex lifeIndex = new LifeIndex();
        lifeIndex.setCityId(getCityId());
        lifeIndex.setName(name);
        lifeIndex.setIndex(safeString(index));
        lifeIndex.setDetails(safeString(details));
        indexList.add(lifeIndex);
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
        return value == null || "null".equalsIgnoreCase(value) ? "" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
