package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.CurrentWeather
import cn.byronlab.weather.domain.model.DeviceLocation
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.model.WeatherCondition
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.repository.AppStartupRepository
import cn.byronlab.weather.domain.repository.CityRepository
import cn.byronlab.weather.domain.repository.CurrentLocationRepository
import cn.byronlab.weather.domain.repository.SettingsRepository
import cn.byronlab.weather.domain.repository.WeatherRepository
import cn.byronlab.weather.domain.repository.WeatherRefreshScheduler
import cn.byronlab.weather.domain.result.DomainResult
import cn.byronlab.weather.domain.usecase.AddCityUseCase
import cn.byronlab.weather.domain.usecase.ConfigureWeatherRefreshUseCase
import cn.byronlab.weather.domain.usecase.GetCurrentWeatherUseCase
import cn.byronlab.weather.domain.usecase.GetCurrentLocationCityUseCase
import cn.byronlab.weather.domain.usecase.GetPopularCitiesUseCase
import cn.byronlab.weather.domain.usecase.GetRecentCitiesUseCase
import cn.byronlab.weather.domain.usecase.InitializeAppUseCase
import cn.byronlab.weather.domain.usecase.ObserveAddedCitiesUseCase
import cn.byronlab.weather.domain.usecase.RecordRecentCityUseCase
import cn.byronlab.weather.domain.usecase.RefreshWeatherUseCase
import cn.byronlab.weather.domain.usecase.RemoveAddedCityUseCase
import cn.byronlab.weather.domain.usecase.SearchCitiesUseCase
import cn.byronlab.weather.domain.usecase.SetCurrentCityUseCase
import cn.byronlab.weather.domain.usecase.SetWeatherRefreshIntervalUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun appStartedLoadsWeatherAndAddedCities() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.weatherState is WeatherLoadState.Content)
        assertEquals("北京", state.weather?.cityName)
        assertEquals(listOf("北京"), state.addedCities.map { it.name })
        assertEquals("26", state.addedCityWeather["101010100"]?.currentTemperature)
        assertEquals(listOf("上海", "伦敦"), state.popularCities.map { it.name })
        assertEquals(1, fixture.weatherRepository.loadCallCountByCityId["101010100"])
    }

    @Test
    fun cityListsLoadWhileInitialWeatherIsPending() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val weatherGate = CompletableDeferred<Unit>()
        fixture.weatherRepository.nextLoadGate = weatherGate

        val viewModel = fixture.viewModel()
        runCurrent()

        assertEquals(listOf("上海", "伦敦"), viewModel.uiState.value.popularCities.map { it.name })
        assertTrue(viewModel.uiState.value.weatherState is WeatherLoadState.Loading)

        weatherGate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.weatherState is WeatherLoadState.Content)
    }

    @Test
    fun searchQueryIsDebouncedAndUpdatesResults() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.SearchOpened)
        viewModel.onEvent(HomeUiEvent.SearchQueryChanged(" 北京 "))

        assertEquals(HomeTab.Cities, viewModel.uiState.value.selectedTab)
        advanceTimeBy(249)
        assertTrue(viewModel.uiState.value.search.searching)
        assertTrue(viewModel.uiState.value.search.searchResults.isEmpty())

        advanceUntilIdle()

        val search = viewModel.uiState.value.search
        assertEquals(" 北京 ", search.searchQuery)
        assertEquals(false, search.searching)
        assertEquals(listOf("北京"), search.searchResults.map { it.name })
        assertEquals("北京", fixture.cityRepository.lastSearchKeyword)
    }

    @Test
    fun currentLocationIsIndependentFromSelectedWeatherCity() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.CurrentLocationRequested)
        advanceUntilIdle()

        val locationState = viewModel.uiState.value.currentLocation
        assertTrue(locationState is CurrentLocationUiState.Available)
        assertEquals("上海", (locationState as CurrentLocationUiState.Available).city.name)
        assertEquals("北京", viewModel.uiState.value.weather?.cityName)
    }

    @Test
    fun selectingUnaddedCityOpensPreviewWithoutChangingCurrentOrAddedCities() =
        runTest(mainDispatcherRule.testDispatcher) {
            val fixture = Fixture()
            val viewModel = fixture.viewModel()
            advanceUntilIdle()
            viewModel.onEvent(HomeUiEvent.TabSelected(HomeTab.Cities))
            val weatherGate = CompletableDeferred<Unit>()
            fixture.weatherRepository.nextLoadGate = weatherGate

            viewModel.onEvent(HomeUiEvent.CitySelected("1796236"))

            assertEquals(HomeTab.Home, viewModel.uiState.value.selectedTab)
            assertTrue(viewModel.uiState.value.weatherState is WeatherLoadState.Loading)
            runCurrent()
            assertEquals("101010100", fixture.settingsRepository.currentCityId)
            assertEquals(listOf("北京"), viewModel.uiState.value.addedCities.map { it.name })
            assertEquals("1796236", viewModel.uiState.value.cityPreview?.cityId)

            weatherGate.complete(Unit)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.weatherState is WeatherLoadState.Content)
        }

    @Test
    fun closingPreviewRestoresSearchAndPreviousWeather() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.SearchOpened)
        viewModel.onEvent(HomeUiEvent.SearchQueryChanged("上海"))
        advanceUntilIdle()
        viewModel.onEvent(HomeUiEvent.CitySelected("1796236"))
        advanceUntilIdle()

        assertEquals("1796236", viewModel.uiState.value.cityPreview?.cityId)
        assertEquals(listOf("上海"), viewModel.uiState.value.recentCities.map { it.name })

        viewModel.onEvent(HomeUiEvent.CityPreviewClosed)

        val state = viewModel.uiState.value
        assertTrue(state.search.searchOpen)
        assertEquals("上海", state.search.searchQuery)
        assertEquals("北京", state.weather?.cityName)
        assertEquals("101010100", fixture.settingsRepository.currentCityId)
    }

    @Test
    fun closingPreviewOpenedFromAllPopularCitiesReturnsToPopularCities() =
        runTest(mainDispatcherRule.testDispatcher) {
            val fixture = Fixture()
            val viewModel = fixture.viewModel()
            advanceUntilIdle()

            viewModel.onEvent(HomeUiEvent.TabSelected(HomeTab.HotCities))
            viewModel.onEvent(HomeUiEvent.CitySelected("2643743"))
            advanceUntilIdle()
            viewModel.onEvent(HomeUiEvent.CityPreviewClosed)

            assertEquals(HomeTab.HotCities, viewModel.uiState.value.selectedTab)
            assertEquals(false, viewModel.uiState.value.search.searchOpen)
            assertEquals(listOf("伦敦"), viewModel.uiState.value.recentCities.map { it.name })
            assertEquals("101010100", fixture.settingsRepository.currentCityId)
        }

    @Test
    fun addingPreviewCityAddsAndSelectsIt() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.TabSelected(HomeTab.Cities))
        viewModel.onEvent(HomeUiEvent.CitySelected("1796236"))
        advanceUntilIdle()
        viewModel.onEvent(HomeUiEvent.CityPreviewAdded)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.cityPreview)
        assertEquals("1796236", fixture.settingsRepository.currentCityId)
        assertEquals(listOf("上海", "北京"), state.addedCities.map { it.name })
        assertEquals("1796236", state.weather?.cityId)
    }

    @Test
    fun selectingAddedCitySkipsPreviewAndChangesCurrentCity() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        fixture.cityRepository.addCity("1796236")
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.CitySelected("1796236"))
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.cityPreview)
        assertEquals("1796236", fixture.settingsRepository.currentCityId)
        assertEquals("1796236", viewModel.uiState.value.weather?.cityId)
    }

    @Test
    fun selectingEquivalentSearchResultUsesAlreadyAddedCityUniqueId() =
        runTest(mainDispatcherRule.testDispatcher) {
            val fixture = Fixture()
            val viewModel = fixture.viewModel()
            advanceUntilIdle()

            viewModel.onEvent(HomeUiEvent.SearchOpened)
            viewModel.onEvent(HomeUiEvent.SearchQueryChanged("duplicate"))
            advanceUntilIdle()
            val equivalentCityId = viewModel.uiState.value.search.searchResults.single().cityId

            viewModel.onEvent(HomeUiEvent.CitySelected(equivalentCityId))
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.cityPreview)
            assertEquals("101010100", fixture.settingsRepository.currentCityId)
        }

    @Test
    fun foregroundRefreshesPeriodicallyAndImmediatelyAfterResume() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.AppEnteredForeground)
        runCurrent()
        assertEquals(0, fixture.weatherRepository.refreshCallCount)

        advanceTimeBy(WeatherRefreshInterval.ThirtyMinutes.minutes * 60_000L)
        runCurrent()
        assertEquals(1, fixture.weatherRepository.refreshCallCount)

        viewModel.onEvent(HomeUiEvent.AppEnteredBackground)
        viewModel.onEvent(HomeUiEvent.AppEnteredForeground)
        runCurrent()
        assertEquals(2, fixture.weatherRepository.refreshCallCount)
        viewModel.onEvent(HomeUiEvent.AppEnteredBackground)
    }

    @Test
    fun backgroundStopsPeriodicWeatherRefresh() = runTest(mainDispatcherRule.testDispatcher) {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.AppEnteredForeground)
        viewModel.onEvent(HomeUiEvent.AppEnteredBackground)
        advanceTimeBy(WeatherRefreshInterval.ThirtyMinutes.minutes * 60_000L * 2)
        runCurrent()

        assertEquals(0, fixture.weatherRepository.refreshCallCount)
    }

    @Test
    fun refreshIntervalSettingIsPersistedAndReschedulesBackgroundWork() =
        runTest(mainDispatcherRule.testDispatcher) {
            val fixture = Fixture()
            val viewModel = fixture.viewModel()
            advanceUntilIdle()

            assertEquals(WeatherRefreshInterval.ThirtyMinutes, viewModel.uiState.value.settings.weatherRefreshInterval)
            viewModel.onEvent(
                HomeUiEvent.WeatherRefreshIntervalSelected(WeatherRefreshInterval.OneHour),
            )
            advanceUntilIdle()

            assertEquals(WeatherRefreshInterval.OneHour, viewModel.uiState.value.settings.weatherRefreshInterval)
            assertEquals(WeatherRefreshInterval.OneHour, fixture.settingsRepository.refreshInterval)
            assertEquals(WeatherRefreshInterval.OneHour, fixture.scheduler.lastScheduledInterval)
        }

    private class Fixture {
        val cityRepository = FakeCityRepository()
        val weatherRepository = FakeWeatherRepository()
        val settingsRepository = FakeSettingsRepository("101010100")
        private val currentLocationRepository = FakeCurrentLocationRepository()
        private val startupRepository = FakeAppStartupRepository()
        val scheduler = FakeWeatherRefreshScheduler()

        fun viewModel(): HomeViewModel {
            return HomeViewModel(
                initializeAppUseCase = InitializeAppUseCase(startupRepository),
                getCurrentWeatherUseCase = GetCurrentWeatherUseCase(weatherRepository, settingsRepository),
                getCurrentLocationCityUseCase = GetCurrentLocationCityUseCase(
                    currentLocationRepository,
                    cityRepository,
                ),
                refreshWeatherUseCase = RefreshWeatherUseCase(weatherRepository, settingsRepository),
                observeAddedCitiesUseCase = ObserveAddedCitiesUseCase(cityRepository),
                searchCitiesUseCase = SearchCitiesUseCase(cityRepository),
                getPopularCitiesUseCase = GetPopularCitiesUseCase(cityRepository),
                getRecentCitiesUseCase = GetRecentCitiesUseCase(cityRepository),
                recordRecentCityUseCase = RecordRecentCityUseCase(cityRepository),
                setCurrentCityUseCase = SetCurrentCityUseCase(settingsRepository),
                addCityUseCase = AddCityUseCase(cityRepository, settingsRepository),
                removeAddedCityUseCase = RemoveAddedCityUseCase(cityRepository, settingsRepository),
                configureWeatherRefreshUseCase = ConfigureWeatherRefreshUseCase(settingsRepository, scheduler),
                setWeatherRefreshIntervalUseCase = SetWeatherRefreshIntervalUseCase(settingsRepository, scheduler),
                mapper = WeatherUiMapper(),
            )
        }
    }

    private class FakeAppStartupRepository : AppStartupRepository {
        override suspend fun initialize(): DomainResult<Unit> {
            return DomainResult.success(Unit)
        }
    }

    private class FakeWeatherRepository : WeatherRepository {
        var refreshCallCount: Int = 0
        var nextLoadGate: CompletableDeferred<Unit>? = null
        val loadCallCountByCityId = mutableMapOf<String, Int>()

        override suspend fun getWeather(cityId: String, refreshNow: Boolean): DomainResult<Weather> {
            loadCallCountByCityId[cityId] = loadCallCountByCityId.getOrDefault(cityId, 0) + 1
            nextLoadGate?.let { gate ->
                nextLoadGate = null
                gate.await()
            }
            return DomainResult.success(weather(cityId))
        }

        override suspend fun refreshWeather(cityId: String): DomainResult<Weather> {
            refreshCallCount += 1
            return getWeather(cityId, refreshNow = true)
        }
    }

    private class FakeCityRepository : CityRepository {
        var lastSearchKeyword: String? = null

        private val availableCities = listOf(
            city("101010100", "北京", uniqueId = "beijing"),
            city("beijing-alternate-payload", "北京市", uniqueId = "beijing"),
            city("1796236", "上海", uniqueId = "shanghai"),
            city("2643743", "伦敦", uniqueId = "london"),
        ).associateBy(City::cityId)
        private val addedCities = MutableStateFlow(listOf(availableCities.getValue("101010100")))
        private var recentCities = emptyList<City>()

        override suspend fun searchCities(keyword: String): DomainResult<List<City>> {
            lastSearchKeyword = keyword
            val cityId = if (keyword == "duplicate") "beijing-alternate-payload" else "101010100"
            return DomainResult.success(listOf(availableCities.getValue(cityId)))
        }

        override suspend fun getPopularCities(): DomainResult<List<City>> {
            return DomainResult.success(listOf(availableCities.getValue("1796236"), availableCities.getValue("2643743")))
        }

        override fun resolveLocation(location: DeviceLocation): DomainResult<City> {
            return DomainResult.success(city("device-location", location.cityName, uniqueId = "shanghai"))
        }

        override fun observeAddedCities(): Flow<DomainResult<List<City>>> = addedCities.map { cities ->
            DomainResult.success(cities)
        }

        override suspend fun getAddedCities(): DomainResult<List<City>> {
            return DomainResult.success(addedCities.value)
        }

        override suspend fun addCity(cityId: String): DomainResult<Unit> {
            val city = availableCities[cityId]
                ?: return DomainResult.success(Unit)
            addedCities.value = listOf(city) + addedCities.value.filterNot { it.cityId == cityId }
            return DomainResult.success(Unit)
        }

        override suspend fun removeCity(cityId: String): DomainResult<Unit> {
            addedCities.value = addedCities.value.filterNot { it.cityId == cityId }
            return DomainResult.success(Unit)
        }

        override suspend fun getRecentCities(): DomainResult<List<City>> = DomainResult.success(recentCities)

        override suspend fun recordRecentCity(cityId: String): DomainResult<Unit> {
            availableCities[cityId]?.let { city ->
                recentCities = listOf(city) + recentCities.filterNot { it.cityId == cityId }
            }
            return DomainResult.success(Unit)
        }
    }

    private class FakeCurrentLocationRepository : CurrentLocationRepository {
        override suspend fun getCurrentLocation(): DomainResult<DeviceLocation> {
            return DomainResult.success(
                DeviceLocation(31.2304, 121.4737, "上海", "中国", "上海"),
            )
        }
    }

    private class FakeSettingsRepository(
        var currentCityId: String,
    ) : SettingsRepository {

        var refreshInterval: WeatherRefreshInterval = WeatherRefreshInterval.ThirtyMinutes

        override fun observeCurrentCityId(): Flow<DomainResult<String>> = flow {
            emit(getCurrentCityId())
        }

        override suspend fun getCurrentCityId(): DomainResult<String> {
            return DomainResult.success(currentCityId)
        }

        override suspend fun setCurrentCityId(cityId: String): DomainResult<Unit> {
            currentCityId = cityId
            return DomainResult.success(Unit)
        }

        override fun observeWeatherRefreshInterval(): Flow<DomainResult<WeatherRefreshInterval>> = flow {
            emit(getWeatherRefreshInterval())
        }

        override suspend fun getWeatherRefreshInterval(): DomainResult<WeatherRefreshInterval> {
            return DomainResult.success(refreshInterval)
        }

        override suspend fun setWeatherRefreshInterval(
            interval: WeatherRefreshInterval,
        ): DomainResult<Unit> {
            refreshInterval = interval
            return DomainResult.success(Unit)
        }
    }

    private class FakeWeatherRefreshScheduler : WeatherRefreshScheduler {
        var lastScheduledInterval: WeatherRefreshInterval? = null

        override fun schedule(interval: WeatherRefreshInterval) {
            lastScheduledInterval = interval
        }
    }
}

private fun city(cityId: String, name: String, uniqueId: String = cityId): City {
    return City(cityId, name, name.lowercase(), "", "", "", "", uniqueId)
}

private fun weather(cityId: String): Weather {
    return Weather(
        city = city(cityId, "北京"),
        currentWeather = CurrentWeather(
            cityId,
            WeatherCondition.Clear,
            "26",
            "40",
            "东风",
            "3",
            1L,
            "3级",
            "0",
            "26",
            "1000",
        ),
        airQuality = null,
    )
}
