package cn.byronlab.weather.presentation.home

import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec

data class HomeUiState(
    val weatherState: WeatherLoadState = WeatherLoadState.Initializing,
    val weather: WeatherUiModel? = null,
    val addedCities: List<CityUiModel> = emptyList(),
    val addedCityWeather: Map<String, WeatherUiModel> = emptyMap(),
    val recentCities: List<CityUiModel> = emptyList(),
    val popularCities: List<CityUiModel> = emptyList(),
    val currentLocation: CurrentLocationUiState = CurrentLocationUiState.NotRequested,
    val search: CitySearchUiState = CitySearchUiState(),
    val selectedTab: HomeTab = HomeTab.Home,
    val settings: SettingsUiState = SettingsUiState(),
    val cityPreview: CityPreviewUiState? = null,
)

data class CityPreviewUiState(
    val cityId: String,
    val origin: CityPreviewOrigin,
    val previousWeather: WeatherUiModel?,
    val previousWeatherState: WeatherLoadState,
    val previousSearchState: CitySearchUiState,
)

enum class CityPreviewOrigin {
    Cities,
    Search,
    HotCities,
}

sealed interface WeatherLoadState {
    data object Initializing : WeatherLoadState
    data object Loading : WeatherLoadState
    data class Empty(val message: String = "请选择城市") : WeatherLoadState
    data class Content(val refreshing: Boolean = false) : WeatherLoadState
}

sealed interface CurrentLocationUiState {
    data object NotRequested : CurrentLocationUiState
    data object Loading : CurrentLocationUiState
    data class Available(val city: CityUiModel) : CurrentLocationUiState
    data class Unavailable(val message: String) : CurrentLocationUiState
}

enum class HomeTab {
    Home,
    Cities,
    HotCities,
    Settings,
}

data class CitySearchUiState(
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CityUiModel> = emptyList(),
    val searching: Boolean = false,
)

data class SettingsUiState(
    val theme: String = "System",
    val temperatureUnit: String = "°C",
    val windSpeedUnit: String = "km/h",
    val notificationsEnabled: Boolean = true,
    val weatherRefreshInterval: WeatherRefreshInterval = WeatherRefreshInterval.ThirtyMinutes,
)

data class WeatherUiModel(
    val cityId: String,
    val cityName: String,
    val citySubtitle: String,
    val coordinatesText: String,
    val currentTemperature: String,
    val currentCondition: String,
    val feelsLikeTemperature: String,
    val highTemperature: Int?,
    val lowTemperature: Int?,
    val aqi: Int,
    val airQuality: String,
    val advice: String,
    val cityRank: String,
    val primaryPollutant: String,
    val pollutants: List<AirPollutantUiModel>,
    val scene: WeatherSceneSpec,
    val todaySummary: TodaySummaryUiModel,
    val hourlyForecasts: List<HourlyForecastUiModel>,
    val details: List<WeatherDetailUiModel>,
    val forecasts: List<ForecastUiModel>,
    val lifeIndexes: List<LifeIndexUiModel>,
)

data class TodaySummaryUiModel(
    val high: String,
    val low: String,
    val sunrise: String,
    val sunset: String,
    val rainProbability: String,
    val uvIndex: String,
)

data class HourlyForecastUiModel(
    val time: String,
    val conditionText: String,
    val temperature: Int,
    val scene: WeatherSceneSpec,
)

data class ForecastUiModel(
    val week: String,
    val date: String,
    val conditionText: String,
    val tempMax: Int,
    val tempMin: Int,
    val scene: WeatherSceneSpec,
)

data class WeatherDetailUiModel(
    val title: String,
    val value: String,
    val subtitle: String = "",
)

data class AirPollutantUiModel(
    val label: String,
    val value: String,
)

data class LifeIndexUiModel(
    val name: String,
    val level: String,
    val details: String,
)

data class CityUiModel(
    val cityId: String,
    val name: String,
    val subtitle: String,
    val uniqueId: String = cityId,
)

sealed interface HomeUiEvent {
    data object AppStarted : HomeUiEvent
    data object AppEnteredForeground : HomeUiEvent
    data object AppEnteredBackground : HomeUiEvent
    data object RefreshClicked : HomeUiEvent
    data class TabSelected(val tab: HomeTab) : HomeUiEvent
    data object SearchOpened : HomeUiEvent
    data object SearchClosed : HomeUiEvent
    data class SearchQueryChanged(val query: String) : HomeUiEvent
    data class CitySelected(val cityId: String) : HomeUiEvent
    data object CityPreviewClosed : HomeUiEvent
    data object CityPreviewAdded : HomeUiEvent
    data object CurrentLocationRequested : HomeUiEvent
    data object CurrentLocationPermissionDenied : HomeUiEvent
    data class AddedCityRemoved(val cityId: String) : HomeUiEvent
    data object ThemeSettingClicked : HomeUiEvent
    data object TemperatureUnitClicked : HomeUiEvent
    data object WindSpeedUnitClicked : HomeUiEvent
    data class WeatherRefreshIntervalSelected(val interval: WeatherRefreshInterval) : HomeUiEvent
    data class NotificationsChanged(val enabled: Boolean) : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class ShowMessage(val message: String) : HomeUiEffect
}
