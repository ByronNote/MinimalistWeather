package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.CurrentWeather;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.model.Weather;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.repository.WeatherRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetCurrentWeatherUseCaseTest {

    @Test
    public void missingCurrentCityReturnsDomainError() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository("");
        GetCurrentWeatherUseCase useCase = new GetCurrentWeatherUseCase(
                weatherRepository,
                settingsRepository
        );

        DomainResult<Weather> result = useCase.execute();

        assertTrue(result.isFailure());
        assertEquals(DomainError.Type.MISSING_CURRENT_CITY, result.getError().getType());
        assertEquals(0, weatherRepository.callCount);
    }

    @Test
    public void currentCityIsTrimmedBeforeLoadingWeather() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository(" 101010100 ");
        GetCurrentWeatherUseCase useCase = new GetCurrentWeatherUseCase(
                weatherRepository,
                settingsRepository
        );

        DomainResult<Weather> result = useCase.execute();

        assertTrue(result.isSuccess());
        assertEquals("101010100", weatherRepository.lastCityId);
    }

    private static final class FakeWeatherRepository implements WeatherRepository {

        private int callCount;
        private String lastCityId;

        @Override
        public DomainResult<Weather> getWeather(String cityId, boolean refreshNow) {
            callCount++;
            lastCityId = cityId;
            return DomainResult.success(new Weather(
                    cityId,
                    "北京",
                    "beijing",
                    new CurrentWeather(cityId, "晴", "26", "40", "东风", "3", 1L,
                            "3级", "0", "26", "1000"),
                    Collections.emptyList(),
                    null,
                    Collections.emptyList()
            ));
        }

        @Override
        public DomainResult<Weather> refreshWeather(String cityId) {
            return getWeather(cityId, true);
        }
    }

    private static final class FakeSettingsRepository implements SettingsRepository {

        private final String currentCityId;

        private FakeSettingsRepository(String currentCityId) {
            this.currentCityId = currentCityId;
        }

        @Override
        public DomainResult<String> getCurrentCityId() {
            return DomainResult.success(currentCityId);
        }

        @Override
        public DomainResult<Unit> setCurrentCityId(String cityId) {
            return DomainResult.success(Unit.INSTANCE);
        }
    }
}
