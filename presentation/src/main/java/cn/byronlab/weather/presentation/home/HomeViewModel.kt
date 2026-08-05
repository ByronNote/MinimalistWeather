package cn.byronlab.weather.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.domain.result.DomainError
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getCurrentLocationCityUseCase: GetCurrentLocationCityUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val observeAddedCitiesUseCase: ObserveAddedCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val getPopularCitiesUseCase: GetPopularCitiesUseCase,
    private val getRecentCitiesUseCase: GetRecentCitiesUseCase,
    private val recordRecentCityUseCase: RecordRecentCityUseCase,
    private val setCurrentCityUseCase: SetCurrentCityUseCase,
    private val addCityUseCase: AddCityUseCase,
    private val removeAddedCityUseCase: RemoveAddedCityUseCase,
    private val configureWeatherRefreshUseCase: ConfigureWeatherRefreshUseCase,
    private val setWeatherRefreshIntervalUseCase: SetWeatherRefreshIntervalUseCase,
    private val mapper: WeatherUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<HomeUiEffect> = _uiEffect.asSharedFlow()

    private var weatherJob: Job? = null
    private var automaticWeatherJob: Job? = null
    private var autoRefreshLoopJob: Job? = null
    private var addedCitiesJob: Job? = null
    private var addedCitiesWeatherJob: Job? = null
    private var currentLocationJob: Job? = null
    private var searchJob: Job? = null
    private var settingsJob: Job? = null
    private var appInForeground = false
    private var hasEnteredForeground = false

    init {
        onEvent(HomeUiEvent.AppStarted)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.AppStarted -> initialize()
            HomeUiEvent.AppEnteredForeground -> startAutomaticRefresh()
            HomeUiEvent.AppEnteredBackground -> stopAutomaticRefresh()
            HomeUiEvent.RefreshClicked -> loadWeather(refresh = true)
            is HomeUiEvent.TabSelected -> _uiState.update {
                it.copy(
                    selectedTab = event.tab,
                    search = if (event.tab == HomeTab.HotCities) {
                        it.search.copy(searchOpen = false)
                    } else {
                        it.search
                    },
                )
            }
            HomeUiEvent.SearchOpened -> _uiState.update {
                it.copy(
                    selectedTab = HomeTab.Cities,
                    search = CitySearchUiState(searchOpen = true),
                )
            }
            HomeUiEvent.SearchClosed -> _uiState.update {
                it.copy(search = CitySearchUiState())
            }
            is HomeUiEvent.SearchQueryChanged -> searchCities(event.query)
            is HomeUiEvent.CitySelected -> selectCity(event.cityId)
            HomeUiEvent.CityPreviewClosed -> closeCityPreview()
            HomeUiEvent.CityPreviewAdded -> addPreviewCity()
            HomeUiEvent.CurrentLocationRequested -> loadCurrentLocation()
            HomeUiEvent.CurrentLocationPermissionDenied -> _uiState.update {
                it.copy(
                    currentLocation = CurrentLocationUiState.Unavailable(
                        "需要定位权限才能获取当前城市",
                    ),
                )
            }
            is HomeUiEvent.AddedCityRemoved -> removeAddedCity(event.cityId)
            HomeUiEvent.ThemeSettingClicked -> _uiState.update {
                it.copy(settings = it.settings.copy(theme = it.settings.theme.nextOf("System", "Light", "Dark")))
            }
            HomeUiEvent.TemperatureUnitClicked -> _uiState.update {
                it.copy(settings = it.settings.copy(temperatureUnit = it.settings.temperatureUnit.nextOf("°C", "°F")))
            }
            HomeUiEvent.WindSpeedUnitClicked -> _uiState.update {
                it.copy(settings = it.settings.copy(windSpeedUnit = it.settings.windSpeedUnit.nextOf("km/h", "mph")))
            }
            is HomeUiEvent.WeatherRefreshIntervalSelected -> updateWeatherRefreshInterval(event.interval)
            is HomeUiEvent.NotificationsChanged -> _uiState.update {
                it.copy(settings = it.settings.copy(notificationsEnabled = event.enabled))
            }
        }
    }

    private fun initialize() {
        weatherJob?.cancel()
        automaticWeatherJob = null
        weatherJob = viewModelScope.launch {
            _uiState.update { it.copy(weatherState = WeatherLoadState.Initializing) }
            val result = initializeAppUseCase()
            when (result) {
                is DomainResult.Failure -> {
                    val message = result.error.toUserMessage()
                    _uiState.update {
                        it.copy(weatherState = WeatherLoadState.Empty(message))
                    }
                    emitMessage(message)
                    return@launch
                }
                is DomainResult.Success -> Unit
            }
            configureWeatherRefresh()
            observeAddedCities()
            loadPopularCities()
            loadRecentCities()
            loadWeatherInCurrentJob(cityId = null, refresh = false)
        }
    }

    private suspend fun loadPopularCities() {
        when (val result = getPopularCitiesUseCase()) {
            is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
            is DomainResult.Success -> _uiState.update {
                it.copy(popularCities = result.data.toCityUiModels())
            }
        }
    }

    private fun loadWeather(refresh: Boolean) {
        weatherJob?.cancel()
        automaticWeatherJob = null
        weatherJob = viewModelScope.launch {
            loadWeatherInCurrentJob(cityId = null, refresh = refresh)
        }
    }

    private fun startAutomaticRefresh() {
        if (appInForeground) {
            return
        }
        appInForeground = true
        val refreshOnResume = hasEnteredForeground
        hasEnteredForeground = true
        restartAutomaticRefreshLoop(refreshImmediately = refreshOnResume)
    }

    private fun restartAutomaticRefreshLoop(refreshImmediately: Boolean) {
        autoRefreshLoopJob?.cancel()
        autoRefreshLoopJob = viewModelScope.launch {
            if (refreshImmediately) {
                requestAutomaticRefresh()
            }
            while (appInForeground) {
                val intervalMillis = _uiState.value.settings.weatherRefreshInterval.minutes * 60_000L
                delay(intervalMillis)
                requestAutomaticRefresh()
            }
        }
    }

    private fun stopAutomaticRefresh() {
        if (!appInForeground) {
            return
        }
        appInForeground = false
        autoRefreshLoopJob?.cancel()
        autoRefreshLoopJob = null
        automaticWeatherJob?.let { job ->
            job.cancel()
            if (weatherJob === job) {
                weatherJob = null
            }
        }
        automaticWeatherJob = null
    }

    private fun requestAutomaticRefresh() {
        if (weatherJob?.isActive == true || _uiState.value.cityPreview != null) {
            return
        }
        val job = viewModelScope.launch {
            loadWeatherInCurrentJob(
                cityId = null,
                refresh = true,
                silent = true,
            )
        }
        automaticWeatherJob = job
        weatherJob = job
    }

    private suspend fun configureWeatherRefresh() {
        when (val result = configureWeatherRefreshUseCase()) {
            is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
            is DomainResult.Success -> {
                _uiState.update {
                    it.copy(settings = it.settings.copy(weatherRefreshInterval = result.data))
                }
                if (appInForeground) {
                    restartAutomaticRefreshLoop(refreshImmediately = false)
                }
            }
        }
    }

    private fun updateWeatherRefreshInterval(interval: WeatherRefreshInterval) {
        if (interval == _uiState.value.settings.weatherRefreshInterval) {
            return
        }
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            when (val result = setWeatherRefreshIntervalUseCase(interval)) {
                is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(settings = it.settings.copy(weatherRefreshInterval = interval))
                    }
                    if (appInForeground) {
                        restartAutomaticRefreshLoop(refreshImmediately = false)
                    }
                }
            }
        }
    }

    private suspend fun loadWeatherInCurrentJob(
        cityId: String?,
        refresh: Boolean,
        silent: Boolean = false,
    ) {
        if (!silent) {
            _uiState.update {
                val hasWeather = it.weather != null
                it.copy(
                    weatherState = if (refresh && hasWeather) {
                        WeatherLoadState.Content(refreshing = true)
                    } else {
                        WeatherLoadState.Loading
                    },
                )
            }
        }
        val result = if (cityId == null) {
            if (refresh) {
                refreshWeatherUseCase()
            } else {
                getCurrentWeatherUseCase()
            }
        } else if (refresh) {
            refreshWeatherUseCase(cityId)
        } else {
            getCurrentWeatherUseCase(cityId, refreshNow = false)
        }
        applyWeatherResult(result, notifyFailure = !silent)
    }

    private fun applyWeatherResult(
        result: DomainResult<Weather>,
        notifyFailure: Boolean = true,
    ) {
        when (result) {
            is DomainResult.Failure -> {
                val message = result.error.toUserMessage()
                _uiState.update {
                    it.copy(
                        weatherState = if (it.weather == null) {
                            WeatherLoadState.Empty(message)
                        } else {
                            WeatherLoadState.Content(refreshing = false)
                        },
                    )
                }
                if (notifyFailure) {
                    emitMessage(message)
                }
            }
            is DomainResult.Success -> {
                val mappedWeather = mapper.map(result.data)
                _uiState.update {
                    val isAddedCity = it.addedCities.any { city -> city.cityId == mappedWeather.cityId }
                    it.copy(
                        weatherState = WeatherLoadState.Content(refreshing = false),
                        weather = mappedWeather,
                        addedCityWeather = if (isAddedCity) {
                            it.addedCityWeather + (mappedWeather.cityId to mappedWeather)
                        } else {
                            it.addedCityWeather
                        },
                    )
                }
            }
        }
    }

    private fun observeAddedCities() {
        addedCitiesJob?.cancel()
        addedCitiesJob = viewModelScope.launch {
            observeAddedCitiesUseCase().collect { result ->
                handleAddedCitiesResult(result)
            }
        }
    }

    private fun handleAddedCitiesResult(result: DomainResult<List<City>>) {
        when (result) {
            is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
            is DomainResult.Success -> {
                val cities = result.data
                val cityIds = cities.mapTo(mutableSetOf(), City::cityId)
                _uiState.update {
                    it.copy(
                        addedCities = cities.toCityUiModels(),
                        addedCityWeather = it.addedCityWeather.filterKeys(cityIds::contains),
                    )
                }
                loadAddedCityWeather(cities)
            }
        }
    }

    private fun loadAddedCityWeather(cities: List<City>) {
        addedCitiesWeatherJob?.cancel()
        if (cities.isEmpty()) {
            addedCitiesWeatherJob = null
            return
        }
        addedCitiesWeatherJob = viewModelScope.launch {
            val weatherByCityId = supervisorScope {
                cities.map { city ->
                    async {
                        when (val result = getCurrentWeatherUseCase(city.cityId, refreshNow = false)) {
                            is DomainResult.Success -> city.cityId to mapper.map(result.data)
                            is DomainResult.Failure -> null
                        }
                    }
                }.awaitAll().filterNotNull().toMap()
            }
            val activeCityIds = _uiState.value.addedCities.mapTo(mutableSetOf(), CityUiModel::cityId)
            _uiState.update {
                it.copy(
                    addedCityWeather = (it.addedCityWeather + weatherByCityId)
                        .filterKeys(activeCityIds::contains),
                )
            }
        }
    }

    private suspend fun loadRecentCities() {
        when (val result = getRecentCitiesUseCase()) {
            is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
            is DomainResult.Success -> _uiState.update {
                it.copy(recentCities = result.data.toCityUiModels())
            }
        }
    }

    private fun searchCities(query: String) {
        val normalizedQuery = query.trim()
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                search = it.search.copy(
                    searchQuery = query,
                    searchResults = if (normalizedQuery.isEmpty()) emptyList() else it.search.searchResults,
                    searching = normalizedQuery.isNotEmpty(),
                ),
            )
        }
        if (normalizedQuery.isEmpty()) {
            return
        }

        searchJob = viewModelScope.launch {
            delay(250)
            val result = searchCitiesUseCase(normalizedQuery)
            when (result) {
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(search = it.search.copy(searching = false)) }
                    emitMessage(result.error.toUserMessage())
                }
                is DomainResult.Success -> _uiState.update {
                    if (it.search.searchQuery.trim() == normalizedQuery) {
                        it.copy(
                            search = it.search.copy(
                                searchResults = result.data.toCityUiModels(),
                                searching = false,
                            ),
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun selectCity(cityId: String) {
        val state = _uiState.value
        val origin = when {
            state.search.searchOpen -> CityPreviewOrigin.Search
            state.selectedTab == HomeTab.HotCities -> CityPreviewOrigin.HotCities
            else -> CityPreviewOrigin.Cities
        }
        if (origin != CityPreviewOrigin.Cities) {
            recordRecentCity(cityId)
        }
        if (state.addedCities.any { it.cityId == cityId }) {
            selectAddedCity(cityId)
        } else {
            previewCity(cityId = cityId, origin = origin)
        }
    }

    private fun selectAddedCity(cityId: String) {
        weatherJob?.cancel()
        automaticWeatherJob = null
        _uiState.update {
            it.copy(
                selectedTab = HomeTab.Home,
                search = CitySearchUiState(),
                weatherState = WeatherLoadState.Loading,
                cityPreview = null,
            )
        }
        weatherJob = viewModelScope.launch {
            val setCurrentCityResult = setCurrentCityUseCase(cityId)
            if (setCurrentCityResult is DomainResult.Failure) {
                _uiState.update {
                    it.copy(
                        weatherState = if (it.weather == null) {
                            WeatherLoadState.Empty(setCurrentCityResult.error.toUserMessage())
                        } else {
                            WeatherLoadState.Content(refreshing = false)
                        },
                    )
                }
                emitMessage(setCurrentCityResult.error.toUserMessage())
                return@launch
            }
            loadWeatherInCurrentJob(cityId = cityId, refresh = false)
        }
    }

    private fun previewCity(cityId: String, origin: CityPreviewOrigin) {
        weatherJob?.cancel()
        automaticWeatherJob = null
        val state = _uiState.value
        val preview = CityPreviewUiState(
            cityId = cityId,
            origin = origin,
            previousWeather = state.weather,
            previousWeatherState = if (state.weather != null) {
                WeatherLoadState.Content(refreshing = false)
            } else {
                state.weatherState
            },
            previousSearchState = state.search,
        )
        _uiState.update {
            it.copy(
                selectedTab = HomeTab.Home,
                search = it.search.copy(searchOpen = false),
                weather = null,
                weatherState = WeatherLoadState.Loading,
                cityPreview = preview,
            )
        }
        weatherJob = viewModelScope.launch {
            val result = getCurrentWeatherUseCase(cityId, refreshNow = false)
            if (_uiState.value.cityPreview?.cityId == cityId) {
                applyWeatherResult(result)
            }
        }
    }

    private fun closeCityPreview() {
        val preview = _uiState.value.cityPreview ?: return
        weatherJob?.cancel()
        automaticWeatherJob = null
        _uiState.update {
            it.copy(
                selectedTab = if (preview.origin == CityPreviewOrigin.HotCities) {
                    HomeTab.HotCities
                } else {
                    HomeTab.Cities
                },
                search = if (preview.origin == CityPreviewOrigin.Search) {
                    preview.previousSearchState.copy(searchOpen = true)
                } else {
                    CitySearchUiState()
                },
                weather = preview.previousWeather,
                weatherState = preview.previousWeatherState,
                cityPreview = null,
            )
        }
    }

    private fun addPreviewCity() {
        val preview = _uiState.value.cityPreview ?: return
        weatherJob?.cancel()
        automaticWeatherJob = null
        weatherJob = viewModelScope.launch {
            when (val result = addCityUseCase(preview.cityId)) {
                is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
                is DomainResult.Success -> {
                    val previewWeather = _uiState.value.weather?.takeIf { it.cityId == preview.cityId }
                    _uiState.update {
                        it.copy(
                            selectedTab = HomeTab.Home,
                            search = CitySearchUiState(),
                            weather = previewWeather,
                            weatherState = if (previewWeather == null) {
                                WeatherLoadState.Loading
                            } else {
                                WeatherLoadState.Content(refreshing = false)
                            },
                            cityPreview = null,
                        )
                    }
                    if (previewWeather == null) {
                        loadWeatherInCurrentJob(cityId = preview.cityId, refresh = false)
                    }
                }
            }
        }
    }

    private fun recordRecentCity(cityId: String) {
        viewModelScope.launch {
            when (val result = recordRecentCityUseCase(cityId)) {
                is DomainResult.Failure -> emitMessage(result.error.toUserMessage())
                is DomainResult.Success -> loadRecentCities()
            }
        }
    }

    private fun loadCurrentLocation() {
        if (currentLocationJob?.isActive == true) {
            return
        }
        _uiState.update { it.copy(currentLocation = CurrentLocationUiState.Loading) }
        currentLocationJob = viewModelScope.launch {
            when (val result = getCurrentLocationCityUseCase()) {
                is DomainResult.Failure -> _uiState.update {
                    it.copy(
                        currentLocation = CurrentLocationUiState.Unavailable(
                            result.error.toUserMessage(),
                        ),
                    )
                }
                is DomainResult.Success -> _uiState.update {
                    it.copy(
                        currentLocation = CurrentLocationUiState.Available(
                            mapper.map(result.data),
                        ),
                    )
                }
            }
        }
    }

    private fun removeAddedCity(cityId: String) {
        weatherJob?.cancel()
        automaticWeatherJob = null
        weatherJob = viewModelScope.launch {
            val result = removeAddedCityUseCase(cityId)
            if (result is DomainResult.Failure) {
                emitMessage(result.error.toUserMessage())
                return@launch
            }
            loadWeatherInCurrentJob(cityId = null, refresh = false)
        }
    }

    private fun List<City>.toCityUiModels(): List<CityUiModel> {
        return map(mapper::map)
    }

    private fun DomainError.toUserMessage(): String {
        return when (type) {
            DomainError.Type.MISSING_CURRENT_CITY -> "请选择城市"
            DomainError.Type.INVALID_INPUT -> "城市参数无效"
            DomainError.Type.NOT_FOUND -> "没有找到天气数据"
            DomainError.Type.NETWORK -> "网络异常，请重试"
            DomainError.Type.STORAGE -> "本地数据异常，请重试"
            DomainError.Type.LOCATION_PERMISSION_REQUIRED -> "需要定位权限才能获取当前城市"
            DomainError.Type.LOCATION_UNAVAILABLE -> "暂时无法获取当前城市，请重试"
            DomainError.Type.UNKNOWN -> message?.takeIf { it.isNotBlank() } ?: "操作失败，请重试"
        }
    }

    private fun emitMessage(message: String) {
        _uiEffect.tryEmit(HomeUiEffect.ShowMessage(message))
    }

    private fun String.nextOf(vararg values: String): String {
        val currentIndex = values.indexOf(this)
        return values[(currentIndex + 1).floorMod(values.size)]
    }

    private fun Int.floorMod(other: Int): Int {
        return ((this % other) + other) % other
    }

}
