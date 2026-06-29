package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.repository.SettingsRepository;
import cn.byronlab.weather.domain.result.DomainError;
import cn.byronlab.weather.domain.result.DomainResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeleteSavedCityUseCaseTest {

    @Test
    public void blankCityIdReturnsInvalidInput() {
        FakeCityRepository cityRepository = new FakeCityRepository();
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository("101");
        DeleteSavedCityUseCase useCase = new DeleteSavedCityUseCase(cityRepository, settingsRepository);

        DomainResult<Unit> result = useCase.execute(" ");

        assertTrue(result.isFailure());
        assertEquals(DomainError.Type.INVALID_INPUT, result.getError().getType());
        assertEquals(0, cityRepository.deleteCallCount);
    }

    @Test
    public void deletingNonCurrentCityDoesNotChangeCurrentCity() {
        FakeCityRepository cityRepository = new FakeCityRepository(city("101"), city("102"));
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository("102");
        DeleteSavedCityUseCase useCase = new DeleteSavedCityUseCase(cityRepository, settingsRepository);

        DomainResult<Unit> result = useCase.execute("101");

        assertTrue(result.isSuccess());
        assertEquals("102", settingsRepository.currentCityId);
        assertEquals(0, settingsRepository.setCallCount);
    }

    @Test
    public void deletingCurrentCitySwitchesToFirstRemainingCity() {
        FakeCityRepository cityRepository = new FakeCityRepository(city("101"), city("102"));
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository("101");
        DeleteSavedCityUseCase useCase = new DeleteSavedCityUseCase(cityRepository, settingsRepository);

        DomainResult<Unit> result = useCase.execute("101");

        assertTrue(result.isSuccess());
        assertEquals("102", settingsRepository.currentCityId);
        assertEquals(1, settingsRepository.setCallCount);
    }

    @Test
    public void deletingLastCurrentCityClearsCurrentCity() {
        FakeCityRepository cityRepository = new FakeCityRepository(city("101"));
        FakeSettingsRepository settingsRepository = new FakeSettingsRepository("101");
        DeleteSavedCityUseCase useCase = new DeleteSavedCityUseCase(cityRepository, settingsRepository);

        DomainResult<Unit> result = useCase.execute("101");

        assertTrue(result.isSuccess());
        assertEquals("", settingsRepository.currentCityId);
    }

    private static City city(String cityId) {
        return new City(cityId, cityId, cityId, "", "", "", "");
    }

    private static final class FakeCityRepository implements CityRepository {

        private final List<City> savedCities = new ArrayList<>();
        private int deleteCallCount;

        private FakeCityRepository(City... cities) {
            savedCities.addAll(Arrays.asList(cities));
        }

        @Override
        public DomainResult<List<City>> searchCities(String keyword) {
            return DomainResult.success(new ArrayList<>(savedCities));
        }

        @Override
        public DomainResult<List<City>> getSavedCities() {
            return DomainResult.success(new ArrayList<>(savedCities));
        }

        @Override
        public DomainResult<Unit> deleteSavedCity(String cityId) {
            deleteCallCount++;
            for (int i = 0; i < savedCities.size(); i++) {
                if (cityId.equals(savedCities.get(i).getCityId())) {
                    savedCities.remove(i);
                    break;
                }
            }
            return DomainResult.success(Unit.INSTANCE);
        }
    }

    private static final class FakeSettingsRepository implements SettingsRepository {

        private String currentCityId;
        private int setCallCount;

        private FakeSettingsRepository(String currentCityId) {
            this.currentCityId = currentCityId;
        }

        @Override
        public DomainResult<String> getCurrentCityId() {
            return DomainResult.success(currentCityId);
        }

        @Override
        public DomainResult<Unit> setCurrentCityId(String cityId) {
            currentCityId = cityId;
            setCallCount++;
            return DomainResult.success(Unit.INSTANCE);
        }
    }
}
