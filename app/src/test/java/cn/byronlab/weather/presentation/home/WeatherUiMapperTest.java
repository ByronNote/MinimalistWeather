package cn.byronlab.weather.presentation.home;

import cn.byronlab.weather.domain.model.AirQuality;
import cn.byronlab.weather.domain.model.CurrentWeather;
import cn.byronlab.weather.domain.model.ForecastDay;
import cn.byronlab.weather.domain.model.LifeIndex;
import cn.byronlab.weather.domain.model.Weather;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeatherUiMapperTest {

    @Test
    public void mapBuildsHomeUiModelFromDomainWeather() {
        WeatherUiMapper mapper = new WeatherUiMapper();
        Weather weather = new Weather(
                "101010100",
                "北京",
                "beijing",
                new CurrentWeather("101010100", "晴", "26", "40", "东风", "3",
                        0L, "3级", "0", "27", "1000"),
                Collections.singletonList(new ForecastDay("101010100", "", "晴", "多云",
                        30, 20, "东风", "08.01", "周六", "20", "强",
                        "12", "40", "1000", "0", "05:10", "19:20", "", "")),
                new AirQuality("101010100", 42, 8, 20, "", "适合户外活动",
                        "", "", "", "", "", "", "PM2.5"),
                Collections.singletonList(new LifeIndex("101010100", "穿衣", "舒适", "建议短袖"))
        );

        WeatherUiModel uiModel = mapper.map(weather);

        assertEquals("北京", uiModel.getCityName());
        assertEquals("26", uiModel.getCurrentTemperature());
        assertEquals(42, uiModel.getAqi());
        assertEquals("首要污染物: PM2.5", uiModel.getCityRank());
        assertEquals("晴转多云", uiModel.getForecasts().get(0).getCondition());
        assertEquals("体感温度", uiModel.getDetails().get(0).getTitle());
        assertEquals("27°C", uiModel.getDetails().get(0).getValue());
        assertEquals("舒适", uiModel.getLifeIndexes().get(0).getLevel());
    }

    @Test
    public void mapHandlesMissingOptionalWeatherBlocks() {
        WeatherUiMapper mapper = new WeatherUiMapper();
        Weather weather = new Weather("101010100", "北京", "beijing",
                null, null, null, null);

        WeatherUiModel uiModel = mapper.map(weather);

        assertEquals("", uiModel.getCurrentTemperature());
        assertEquals(0, uiModel.getAqi());
        assertEquals("首要污染物: ", uiModel.getCityRank());
        assertTrue(uiModel.getForecasts().isEmpty());
        assertTrue(uiModel.getLifeIndexes().isEmpty());
    }
}
