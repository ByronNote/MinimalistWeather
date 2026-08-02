package cn.byronlab.weather.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.byronlab.weather.app.di.IoDispatcher
import cn.byronlab.weather.domain.model.City
import cn.byronlab.weather.domain.model.Weather
import cn.byronlab.weather.domain.result.DomainError
import cn.byronlab.weather.domain.result.DomainResult
import cn.byronlab.weather.domain.usecase.DeleteSavedCityUseCase
import cn.byronlab.weather.domain.usecase.GetCurrentWeatherUseCase
import cn.byronlab.weather.domain.usecase.InitializeAppUseCase
import cn.byronlab.weather.domain.usecase.ObserveSavedCitiesUseCase
import cn.byronlab.weather.domain.usecase.RefreshWeatherUseCase
import cn.byronlab.weather.domain.usecase.SearchCitiesUseCase
import cn.byronlab.weather.domain.usecase.SetCurrentCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val setCurrentCityUseCase: SetCurrentCityUseCase,
    private val deleteSavedCityUseCase: DeleteSavedCityUseCase,
    private val mapper: WeatherUiMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        onEvent(HomeUiEvent.AppStarted)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.AppStarted -> initialize()
            HomeUiEvent.RefreshClicked -> loadWeather(refresh = true)
            HomeUiEvent.SearchOpened -> _uiState.update {
                it.copy(searchOpen = true, searchQuery = "", searchResults = emptyList(), errorMessage = null)
            }
            HomeUiEvent.SearchClosed -> _uiState.update {
                it.copy(searchOpen = false, searchQuery = "", searchResults = emptyList())
            }
            is HomeUiEvent.SearchQueryChanged -> searchCities(event.query)
            is HomeUiEvent.CitySelected -> selectCity(event.cityId)
            is HomeUiEvent.SavedCityDeleted -> deleteSavedCity(event.cityId)
            HomeUiEvent.ErrorShown -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            _uiState.update { it.copy(initializing = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { initializeAppUseCase.execute() }
            if (result.isFailure) {
                _uiState.update {
                    it.copy(initializing = false, errorMessage = result.error.toUserMessage())
                }
                return@launch
            }
            loadSavedCities()
            loadWeather(refresh = false)
        }
    }

    private fun loadWeather(refresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    initializing = false,
                    loadingWeather = !refresh && it.weather == null,
                    refreshing = refresh,
                    errorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                if (refresh) {
                    refreshWeatherUseCase.execute()
                } else {
                    getCurrentWeatherUseCase.execute()
                }
            }
            applyWeatherResult(result)
        }
    }

    private fun loadWeather(cityId: String, refresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    initializing = false,
                    loadingWeather = true,
                    refreshing = refresh,
                    errorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                if (refresh) {
                    refreshWeatherUseCase.execute(cityId)
                } else {
                    getCurrentWeatherUseCase.execute(cityId, false)
                }
            }
            applyWeatherResult(result)
            loadSavedCities()
        }
    }

    private fun applyWeatherResult(result: DomainResult<Weather>) {
        if (result.isFailure) {
            _uiState.update {
                it.copy(
                    initializing = false,
                    loadingWeather = false,
                    refreshing = false,
                    errorMessage = result.error.toUserMessage(),
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                initializing = false,
                loadingWeather = false,
                refreshing = false,
                weather = mapper.map(result.data),
                errorMessage = null,
            )
        }
    }

    private fun loadSavedCities() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { observeSavedCitiesUseCase.execute() }
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.error.toUserMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(savedCities = result.data.toCityUiModels())
            }
        }
    }

    private fun searchCities(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { searchCitiesUseCase.execute(query) }
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.error.toUserMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(searchResults = result.data.toCityUiModels())
            }
        }
    }

    private fun selectCity(cityId: String) {
        viewModelScope.launch {
            val setCurrentCityResult = withContext(ioDispatcher) {
                setCurrentCityUseCase.execute(cityId)
            }
            if (setCurrentCityResult.isFailure) {
                _uiState.update { it.copy(errorMessage = setCurrentCityResult.error.toUserMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(searchOpen = false, searchQuery = "", searchResults = emptyList())
            }
            loadWeather(cityId = cityId, refresh = false)
        }
    }

    private fun deleteSavedCity(cityId: String) {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { deleteSavedCityUseCase.execute(cityId) }
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.error.toUserMessage()) }
                return@launch
            }
            loadSavedCities()
            loadWeather(refresh = false)
        }
    }

    private fun List<City>?.toCityUiModels(): List<CityUiModel> {
        return this.orEmpty().map(mapper::map)
    }

    private fun DomainError?.toUserMessage(): String {
        return when (this?.type) {
            DomainError.Type.MISSING_CURRENT_CITY -> "请选择城市"
            DomainError.Type.INVALID_INPUT -> "城市参数无效"
            DomainError.Type.NOT_FOUND -> "没有找到天气数据"
            DomainError.Type.NETWORK -> "网络异常，请重试"
            DomainError.Type.STORAGE -> "本地数据异常，请重试"
            DomainError.Type.UNKNOWN, null -> this?.message?.takeIf { it.isNotBlank() } ?: "操作失败，请重试"
        }
    }
}
