package cn.byronlab.weather.presentation.home

data class HomeUiState(
    val initializing: Boolean = true,
    val loadingWeather: Boolean = false,
    val refreshing: Boolean = false,
    val weather: WeatherUiModel? = null,
    val savedCities: List<CityUiModel> = emptyList(),
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CityUiModel> = emptyList(),
    val errorMessage: String? = null,
)

data class WeatherUiModel(
    val cityId: String,
    val cityName: String,
    val currentTemperature: String,
    val currentCondition: String,
    val publishTime: String,
    val aqi: Int,
    val airQuality: String,
    val advice: String,
    val cityRank: String,
    val details: List<WeatherDetailUiModel>,
    val forecasts: List<ForecastUiModel>,
    val lifeIndexes: List<LifeIndexUiModel>,
)

data class ForecastUiModel(
    val week: String,
    val date: String,
    val condition: String,
    val tempMax: Int,
    val tempMin: Int,
)

data class WeatherDetailUiModel(
    val title: String,
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
)

sealed interface HomeUiEvent {
    data object AppStarted : HomeUiEvent
    data object RefreshClicked : HomeUiEvent
    data object SearchOpened : HomeUiEvent
    data object SearchClosed : HomeUiEvent
    data class SearchQueryChanged(val query: String) : HomeUiEvent
    data class CitySelected(val cityId: String) : HomeUiEvent
    data class SavedCityDeleted(val cityId: String) : HomeUiEvent
    data object ErrorShown : HomeUiEvent
}
