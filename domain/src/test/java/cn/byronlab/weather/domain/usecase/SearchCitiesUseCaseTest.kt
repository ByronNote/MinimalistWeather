package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchCitiesUseCaseTest {

    @Test
    fun blankKeywordReturnsEmptyListWithoutCallingRepository() = runTest {
        val repository = FakeCityRepository()
        val useCase = SearchCitiesUseCase(repository)

        val result = useCase("  ")

        assertTrue(result is DomainResult.Success)
        assertTrue((result as DomainResult.Success).data.isEmpty())
        assertNull(repository.lastSearchKeyword)
    }

    @Test
    fun nonBlankKeywordIsTrimmedBeforeSearch() = runTest {
        val repository = FakeCityRepository()
        val useCase = SearchCitiesUseCase(repository)

        val result = useCase(" 北京 ")

        assertTrue(result is DomainResult.Success)
        assertEquals("北京", repository.lastSearchKeyword)
    }

    private class FakeCityRepository : CityRepository {

        var lastSearchKeyword: String? = null

        override suspend fun searchCities(keyword: String): DomainResult<List<City>> {
            lastSearchKeyword = keyword
            return DomainResult.success(listOf(City("101010100", "北京", "beijing", "", "", "116.4", "39.9")))
        }

        override suspend fun getPopularCities(): DomainResult<List<City>> {
            return DomainResult.success(emptyList())
        }

        override fun resolveLocation(location: DeviceLocation): DomainResult<City> {
            return DomainResult.success(City("location", location.cityName, location.cityName, "", "", "", ""))
        }

        override fun observeAddedCities(): Flow<DomainResult<List<City>>> = flow {
            emit(getAddedCities())
        }

        override suspend fun getAddedCities(): DomainResult<List<City>> {
            return DomainResult.success(emptyList())
        }

        override suspend fun addCity(cityId: String): DomainResult<Unit> {
            return DomainResult.success(Unit)
        }

        override suspend fun removeCity(cityId: String): DomainResult<Unit> {
            return DomainResult.success(Unit)
        }

        override suspend fun getRecentCities() = DomainResult.success(emptyList<City>())

        override suspend fun recordRecentCity(cityId: String) = DomainResult.success(Unit)
    }
}
