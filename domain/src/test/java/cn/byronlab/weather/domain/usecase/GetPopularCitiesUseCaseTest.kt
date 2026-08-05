package cn.byronlab.weather.domain.usecase

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPopularCitiesUseCaseTest {

    @Test
    fun delegatesToRepository() = runTest {
        val repository = FakeCityRepository(listOf(City("1", "上海", "Shanghai", "中国", "上海", "121.4", "31.2")))
        val useCase = GetPopularCitiesUseCase(repository)

        val result = useCase()

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf("上海"), (result as DomainResult.Success).data.map { it.name })
        assertEquals(1, repository.callCount)
    }

    private class FakeCityRepository(
        private val popularCities: List<City>,
    ) : CityRepository {

        var callCount = 0

        override suspend fun searchCities(keyword: String): DomainResult<List<City>> {
            return DomainResult.success(emptyList())
        }

        override suspend fun getPopularCities(): DomainResult<List<City>> {
            callCount++
            return DomainResult.success(popularCities)
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
