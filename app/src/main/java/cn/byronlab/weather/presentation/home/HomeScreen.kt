package cn.byronlab.weather.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SavedCitiesDrawer(
                cities = state.savedCities,
                onSearchClick = {
                    scope.launch { drawerState.close() }
                    onEvent(HomeUiEvent.SearchOpened)
                },
                onCityClick = {
                    scope.launch { drawerState.close() }
                    onEvent(HomeUiEvent.CitySelected(it))
                },
                onDeleteClick = { onEvent(HomeUiEvent.SavedCityDeleted(it)) },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = state.weather?.cityName ?: "天气",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "城市")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEvent(HomeUiEvent.RefreshClicked) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        IconButton(onClick = { onEvent(HomeUiEvent.SearchOpened) }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                when {
                    state.initializing || state.loadingWeather && state.weather == null -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.weather == null -> {
                        EmptyWeather(
                            message = state.errorMessage ?: "请选择城市",
                            onSearchClick = { onEvent(HomeUiEvent.SearchOpened) },
                        )
                    }
                    else -> {
                        WeatherContent(
                            weather = state.weather,
                            refreshing = state.refreshing || state.loadingWeather,
                            errorMessage = state.errorMessage,
                        )
                    }
                }
            }
        }
    }

    if (state.searchOpen) {
        SearchCityDialog(
            query = state.searchQuery,
            results = state.searchResults,
            onQueryChange = { onEvent(HomeUiEvent.SearchQueryChanged(it)) },
            onCityClick = { onEvent(HomeUiEvent.CitySelected(it)) },
            onDismiss = { onEvent(HomeUiEvent.SearchClosed) },
        )
    }
}

@Composable
private fun WeatherContent(
    weather: WeatherUiModel,
    refreshing: Boolean,
    errorMessage: String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (refreshing) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        }
        if (!errorMessage.isNullOrBlank()) {
            item { Text(text = errorMessage, color = MaterialTheme.colorScheme.error) }
        }
        item { CurrentWeatherCard(weather = weather) }
        item { AirQualityCard(weather = weather) }
        item { SectionTitle("详情") }
        items(weather.details) { detail -> DetailRow(detail) }
        item { SectionTitle("预报") }
        items(weather.forecasts) { forecast -> ForecastRow(forecast) }
        item { SectionTitle("生活指数") }
        items(weather.lifeIndexes) { lifeIndex -> LifeIndexRow(lifeIndex) }
    }
}

@Composable
private fun CurrentWeatherCard(weather: WeatherUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = weather.currentCondition, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${weather.currentTemperature}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (weather.publishTime.isNotBlank()) {
                Text(
                    text = "发布时间 ${weather.publishTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AirQualityCard(weather: WeatherUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "空气质量", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = weather.aqi.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = weather.airQuality.ifBlank { "AQI" })
            }
            if (weather.advice.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = weather.advice)
            }
            Text(
                text = weather.cityRank,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun DetailRow(detail: WeatherDetailUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = detail.title)
        Text(text = detail.value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ForecastRow(forecast: ForecastUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = forecast.week, modifier = Modifier.weight(0.9f))
        Text(text = forecast.date, modifier = Modifier.weight(1f))
        Text(text = forecast.condition, modifier = Modifier.weight(1.4f))
        Text(
            text = "${forecast.tempMin}° / ${forecast.tempMax}°",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun LifeIndexRow(lifeIndex: LifeIndexUiModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = lifeIndex.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Text(text = lifeIndex.level)
            }
            if (lifeIndex.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lifeIndex.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyWeather(
    message: String,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onSearchClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择城市")
        }
    }
}

@Composable
private fun SavedCitiesDrawer(
    cities: List<CityUiModel>,
    onSearchClick: () -> Unit,
    onCityClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    ModalDrawerSheet {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "城市",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Add, contentDescription = "添加城市")
            }
        }
        HorizontalDivider()
        LazyColumn {
            items(cities, key = { it.cityId }) { city ->
                SavedCityRow(
                    city = city,
                    onCityClick = onCityClick,
                    onDeleteClick = onDeleteClick,
                )
            }
        }
    }
}

@Composable
private fun SavedCityRow(
    city: CityUiModel,
    onCityClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            modifier = Modifier.weight(1f),
            onClick = { onCityClick(city.cityId) },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = city.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (city.subtitle.isNotBlank()) {
                    Text(
                        text = city.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        IconButton(onClick = { onDeleteClick(city.cityId) }) {
            Icon(Icons.Default.Delete, contentDescription = "删除")
        }
    }
}

@Composable
private fun SearchCityDialog(
    query: String,
    results: List<CityUiModel>,
    onQueryChange: (String) -> Unit,
    onCityClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "关闭")
            }
        },
        title = { Text("搜索城市") },
        text = {
            Column {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("城市名、拼音或编号") },
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(results, key = { it.cityId }) { city ->
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onCityClick(city.cityId) },
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = city.name)
                                if (city.subtitle.isNotBlank()) {
                                    Text(
                                        text = city.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}
