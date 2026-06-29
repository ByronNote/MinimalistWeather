package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.City;
import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.CityRepository;
import cn.byronlab.weather.domain.result.DomainResult;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SearchCitiesUseCaseTest {

    @Test
    public void blankKeywordReturnsEmptyListWithoutCallingRepository() {
        FakeCityRepository repository = new FakeCityRepository();
        SearchCitiesUseCase useCase = new SearchCitiesUseCase(repository);

        DomainResult<List<City>> result = useCase.execute("  ");

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        assertNull(repository.lastSearchKeyword);
    }

    @Test
    public void nonBlankKeywordIsTrimmedBeforeSearch() {
        FakeCityRepository repository = new FakeCityRepository();
        SearchCitiesUseCase useCase = new SearchCitiesUseCase(repository);

        DomainResult<List<City>> result = useCase.execute(" 北京 ");

        assertTrue(result.isSuccess());
        assertEquals("北京", repository.lastSearchKeyword);
    }

    private static final class FakeCityRepository implements CityRepository {

        private String lastSearchKeyword;

        @Override
        public DomainResult<List<City>> searchCities(String keyword) {
            lastSearchKeyword = keyword;
            return DomainResult.success(Collections.singletonList(
                    new City("101010100", "北京", "beijing", "", "", "116.4", "39.9")
            ));
        }

        @Override
        public DomainResult<List<City>> getSavedCities() {
            return DomainResult.success(Collections.emptyList());
        }

        @Override
        public DomainResult<Unit> deleteSavedCity(String cityId) {
            return DomainResult.success(Unit.INSTANCE);
        }
    }
}
