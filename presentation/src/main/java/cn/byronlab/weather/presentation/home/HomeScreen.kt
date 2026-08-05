package cn.byronlab.weather.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.byronlab.weather.domain.model.WeatherRefreshInterval
import cn.byronlab.weather.presentation.R
import cn.byronlab.weather.presentation.weatherui.icons.weatherConditionIcon
import cn.byronlab.weather.presentation.weatherui.icons.weatherConditionIconTint
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.render.WeatherScene
import cn.byronlab.weather.presentation.weatherui.render.drawWeatherThumbnailScene
import cn.byronlab.weather.presentation.weatherui.tokens.WeatherVisualStyle
import cn.byronlab.weather.presentation.weatherui.tokens.weatherVisualStyle
import kotlin.math.roundToInt

private val LightCanvasTop = Color(0xFFF8FBFF)
private val LightCanvasBottom = Color(0xFFF1F6FC)
private val LightScreenInk = Color(0xFF0F1E33)
private val LightScreenMuted = Color(0xFF64748B)
private val LightScreenBorder = Color(0xFFE5EBF3)
private val AirQualityScaleColors = listOf(
    Color(0xFF51E0D0),
    Color(0xFF63D59D),
    Color(0xFFFFD166),
    Color(0xFFFF9F5A),
    Color(0xFFFF6B6B),
    Color(0xFFE957C2),
)
private val PageTransitionEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val HourlyForecastColumnWidth = 64.dp

internal enum class HomePage(val depth: Int) {
    Home(depth = 0),
    Cities(depth = 1),
    Settings(depth = 1),
    HotCities(depth = 2),
    Search(depth = 2),
}

internal enum class HomePageTransition {
    LocationPickerOpen,
    LocationPickerClose,
    Forward,
    Backward,
    Crossfade,
}

internal fun homePageTransition(
    initialPage: HomePage,
    targetPage: HomePage,
): HomePageTransition {
    return when {
        initialPage == HomePage.Home && targetPage == HomePage.Cities ->
            HomePageTransition.LocationPickerOpen
        initialPage == HomePage.Cities && targetPage == HomePage.Home ->
            HomePageTransition.LocationPickerClose
        targetPage.depth > initialPage.depth -> HomePageTransition.Forward
        targetPage.depth < initialPage.depth -> HomePageTransition.Backward
        else -> HomePageTransition.Crossfade
    }
}

internal fun weatherTitleBarProgress(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    transitionDistance: Int,
): Float {
    if (firstVisibleItemIndex > 0) return 1f
    if (transitionDistance <= 0) return if (firstVisibleItemScrollOffset > 0) 1f else 0f
    return (firstVisibleItemScrollOffset.toFloat() / transitionDistance).coerceIn(0f, 1f)
}

internal fun weatherTitleBarBackgroundAlpha(progress: Float): Float {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val easedProgress = clampedProgress * clampedProgress * (3f - 2f * clampedProgress)
    return 0.90f * easedProgress
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + ((stop - start) * fraction)

private fun HomeUiState.currentPage(): HomePage {
    if (search.searchOpen) return HomePage.Search
    return when (selectedTab) {
        HomeTab.Home -> HomePage.Home
        HomeTab.Cities -> HomePage.Cities
        HomeTab.HotCities -> HomePage.HotCities
        HomeTab.Settings -> HomePage.Settings
    }
}

internal fun HomePage.usesDarkStatusBarIcons(): Boolean = this != HomePage.Home

private fun Modifier.lightScreenBackground(): Modifier = background(
    Brush.verticalGradient(listOf(LightCanvasTop, LightCanvasBottom)),
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onDarkStatusBarIconsChanged: (Boolean) -> Unit = {},
    locationPermissionGranted: Boolean = false,
    onLocationPermissionRequest: () -> Unit = {},
    debugToolsEnabled: Boolean = false,
    weatherUiTestLauncherEnabled: Boolean = false,
    onWeatherUiTestLauncherEnabledChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentPage = state.currentPage()
    var activeWeatherUiTestScenarioId by rememberSaveable { mutableStateOf<String?>(null) }
    var showWeatherUiTestDialog by rememberSaveable { mutableStateOf(false) }
    val activeWeatherUiTestScenario = if (debugToolsEnabled && weatherUiTestLauncherEnabled) {
        WeatherUiTestCatalog.find(activeWeatherUiTestScenarioId)
    } else {
        null
    }

    BackHandler(enabled = state.cityPreview != null) {
        viewModel.onEvent(HomeUiEvent.CityPreviewClosed)
    }

    BackHandler(enabled = activeWeatherUiTestScenario != null) {
        activeWeatherUiTestScenarioId = null
    }

    LaunchedEffect(debugToolsEnabled, weatherUiTestLauncherEnabled) {
        if (!debugToolsEnabled || !weatherUiTestLauncherEnabled) {
            activeWeatherUiTestScenarioId = null
            showWeatherUiTestDialog = false
        }
    }

    LifecycleStartEffect(viewModel) {
        viewModel.onEvent(HomeUiEvent.AppEnteredForeground)
        onStopOrDispose {
            viewModel.onEvent(HomeUiEvent.AppEnteredBackground)
        }
    }

    LaunchedEffect(currentPage) {
        onDarkStatusBarIconsChanged(currentPage.usesDarkStatusBarIcons())
    }

    LaunchedEffect(currentPage, locationPermissionGranted, state.currentLocation) {
        if (currentPage == HomePage.Cities && state.currentLocation is CurrentLocationUiState.NotRequested) {
            if (locationPermissionGranted) {
                viewModel.onEvent(HomeUiEvent.CurrentLocationRequested)
            } else {
                onLocationPermissionRequest()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        debugToolsEnabled = debugToolsEnabled,
        weatherUiTestLauncherEnabled = weatherUiTestLauncherEnabled,
        activeWeatherUiTestScenario = activeWeatherUiTestScenario,
        showWeatherUiTestDialog = showWeatherUiTestDialog,
        onShowWeatherUiTestDialog = { showWeatherUiTestDialog = true },
        onDismissWeatherUiTestDialog = { showWeatherUiTestDialog = false },
        onWeatherUiTestScenarioSelected = { scenario ->
            activeWeatherUiTestScenarioId = scenario.id
            showWeatherUiTestDialog = false
        },
        onStopWeatherUiTesting = {
            activeWeatherUiTestScenarioId = null
            showWeatherUiTestDialog = false
        },
        onWeatherUiTestLauncherEnabledChanged = { enabled ->
            if (!enabled) {
                activeWeatherUiTestScenarioId = null
                showWeatherUiTestDialog = false
            }
            onWeatherUiTestLauncherEnabledChanged(enabled)
        },
        onCurrentLocationClick = {
            when (val locationState = state.currentLocation) {
                is CurrentLocationUiState.Available -> {
                    viewModel.onEvent(HomeUiEvent.CitySelected(locationState.city.cityId))
                }
                CurrentLocationUiState.Loading -> Unit
                CurrentLocationUiState.NotRequested,
                is CurrentLocationUiState.Unavailable -> {
                    if (locationPermissionGranted) {
                        viewModel.onEvent(HomeUiEvent.CurrentLocationRequested)
                    } else {
                        onLocationPermissionRequest()
                    }
                }
            }
        },
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    debugToolsEnabled: Boolean,
    weatherUiTestLauncherEnabled: Boolean,
    activeWeatherUiTestScenario: WeatherUiTestScenario?,
    showWeatherUiTestDialog: Boolean,
    onShowWeatherUiTestDialog: () -> Unit,
    onDismissWeatherUiTestDialog: () -> Unit,
    onWeatherUiTestScenarioSelected: (WeatherUiTestScenario) -> Unit,
    onStopWeatherUiTesting: () -> Unit,
    onWeatherUiTestLauncherEnabledChanged: (Boolean) -> Unit,
    onCurrentLocationClick: () -> Unit,
) {
    val currentPage = state.currentPage()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AnimatedContent(
                targetState = currentPage,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    when (homePageTransition(initialState, targetState)) {
                        HomePageTransition.LocationPickerOpen -> {
                            val enter = slideInVertically(
                                animationSpec = tween(durationMillis = 360, easing = PageTransitionEasing),
                                initialOffsetY = { height -> height },
                            )
                            val holdCurrentPage = ExitTransition.KeepUntilTransitionsFinished
                            (enter togetherWith holdCurrentPage).apply {
                                targetContentZIndex = 1f
                            }
                        }

                        HomePageTransition.LocationPickerClose -> {
                            val exit = slideOutVertically(
                                animationSpec = tween(durationMillis = 320, easing = PageTransitionEasing),
                                targetOffsetY = { height -> height },
                            )
                            (EnterTransition.None togetherWith exit).apply {
                                targetContentZIndex = -1f
                            }
                        }

                        HomePageTransition.Forward -> {
                            val enter = slideInHorizontally(
                                animationSpec = tween(durationMillis = 320, easing = PageTransitionEasing),
                                initialOffsetX = { width -> width },
                            )
                            val holdCurrentPage = ExitTransition.KeepUntilTransitionsFinished
                            (enter togetherWith holdCurrentPage).apply {
                                targetContentZIndex = 1f
                            }
                        }

                        HomePageTransition.Backward -> {
                            val exit = slideOutHorizontally(
                                animationSpec = tween(durationMillis = 300, easing = PageTransitionEasing),
                                targetOffsetX = { width -> width },
                            )
                            (EnterTransition.None togetherWith exit).apply {
                                targetContentZIndex = -1f
                            }
                        }

                        HomePageTransition.Crossfade -> {
                            val enter = fadeIn(
                                animationSpec = tween(durationMillis = 220, easing = PageTransitionEasing),
                            )
                            val holdCurrentPage = ExitTransition.KeepUntilTransitionsFinished
                            (enter togetherWith holdCurrentPage).apply {
                                targetContentZIndex = 1f
                            }
                        }
                    }
                },
                contentAlignment = Alignment.Center,
                label = "HomePageTransition",
            ) { page ->
                when (page) {
                    HomePage.Home -> HomeWeatherTab(
                        state = state,
                        onEvent = onEvent,
                        activeWeatherUiTestScenario = activeWeatherUiTestScenario,
                        showWeatherUiTestLauncher = debugToolsEnabled && weatherUiTestLauncherEnabled,
                        onShowWeatherUiTestDialog = onShowWeatherUiTestDialog,
                        onStopWeatherUiTesting = onStopWeatherUiTesting,
                    )
                    HomePage.Cities -> CitiesTab(
                        state = state,
                        onEvent = onEvent,
                        onCurrentLocationClick = onCurrentLocationClick,
                    )
                    HomePage.HotCities -> HotCitiesTab(popularCities = state.popularCities, onEvent = onEvent)
                    HomePage.Settings -> SettingsTab(
                        state = state,
                        onEvent = onEvent,
                        debugToolsEnabled = debugToolsEnabled,
                        weatherUiTestLauncherEnabled = weatherUiTestLauncherEnabled,
                        onWeatherUiTestLauncherEnabledChanged = onWeatherUiTestLauncherEnabledChanged,
                    )
                    HomePage.Search -> SearchCityScreen(
                        query = state.search.searchQuery,
                        results = state.search.searchResults,
                        searching = state.search.searching,
                        recentCities = state.recentCities,
                        popularCities = state.popularCities,
                        addedCityIds = state.addedCities.mapTo(mutableSetOf()) { it.cityId },
                        onPopularCityClick = { onShowcaseCityClick(it, onEvent) },
                        onAllPopularCitiesClick = {
                            onEvent(HomeUiEvent.TabSelected(HomeTab.HotCities))
                        },
                        onQueryChange = { onEvent(HomeUiEvent.SearchQueryChanged(it)) },
                        onCityClick = { onEvent(HomeUiEvent.CitySelected(it)) },
                        onDismiss = { onEvent(HomeUiEvent.SearchClosed) },
                    )
                }
            }
            if (debugToolsEnabled && showWeatherUiTestDialog) {
                WeatherUiTestDialog(
                    activeScenario = activeWeatherUiTestScenario,
                    onDismiss = onDismissWeatherUiTestDialog,
                    onScenarioSelected = onWeatherUiTestScenarioSelected,
                    onStopTesting = onStopWeatherUiTesting,
                )
            }
        }
    }
}

@Composable
private fun HomeWeatherTab(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    activeWeatherUiTestScenario: WeatherUiTestScenario?,
    showWeatherUiTestLauncher: Boolean,
    onShowWeatherUiTestDialog: () -> Unit,
    onStopWeatherUiTesting: () -> Unit,
) {
    val testWeather = remember(activeWeatherUiTestScenario?.id) {
        activeWeatherUiTestScenario?.let(WeatherUiTestCatalog::createPreview)
    }
    val weather = testWeather ?: state.weather
    val scene = weather?.scene ?: WeatherSceneSpec()
    val style = weatherVisualStyle(scene)

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherScene(
            scene = scene,
            modifier = Modifier.fillMaxSize(),
        )
        if (testWeather != null) {
            WeatherDashboard(
                weather = testWeather,
                refreshing = false,
                style = style,
                previewMode = false,
                weatherUiTestMode = true,
                onMenu = { onEvent(HomeUiEvent.TabSelected(HomeTab.Settings)) },
                onLocation = {},
                onSearch = {},
            )
        } else when (val loadState = state.weatherState) {
            WeatherLoadState.Initializing,
            WeatherLoadState.Loading -> WeatherLoading(style = style)

            is WeatherLoadState.Empty -> EmptyWeather(
                message = loadState.message,
                style = style,
                onSearchClick = { onEvent(HomeUiEvent.TabSelected(HomeTab.Cities)) },
            )

            is WeatherLoadState.Content -> {
                if (weather == null) {
                    EmptyWeather(
                        message = "请选择城市",
                        style = style,
                        onSearchClick = { onEvent(HomeUiEvent.TabSelected(HomeTab.Cities)) },
                    )
                } else {
                    WeatherDashboard(
                        weather = weather,
                        refreshing = loadState.refreshing,
                        style = style,
                        previewMode = state.cityPreview != null,
                        weatherUiTestMode = false,
                        onMenu = { onEvent(HomeUiEvent.TabSelected(HomeTab.Settings)) },
                        onLocation = { onEvent(HomeUiEvent.TabSelected(HomeTab.Cities)) },
                        onSearch = { onEvent(HomeUiEvent.TabSelected(HomeTab.Cities)) },
                    )
                }
            }
        }
        if (state.cityPreview != null) {
            CityPreviewControls(
                style = style,
                onClose = { onEvent(HomeUiEvent.CityPreviewClosed) },
                onAdd = { onEvent(HomeUiEvent.CityPreviewAdded) },
            )
        }
        if (showWeatherUiTestLauncher && state.cityPreview == null) {
            WeatherUiTestLauncher(
                activeScenario = activeWeatherUiTestScenario,
                style = style,
                onClick = onShowWeatherUiTestDialog,
                onStopTesting = onStopWeatherUiTesting,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun WeatherDashboard(
    weather: WeatherUiModel,
    refreshing: Boolean,
    style: WeatherVisualStyle,
    previewMode: Boolean,
    weatherUiTestMode: Boolean,
    onMenu: () -> Unit,
    onLocation: () -> Unit,
    onSearch: () -> Unit,
) {
    val listState = rememberLazyListState()
    val transitionDistance = with(LocalDensity.current) { 180.dp.roundToPx() }
    val titleBarProgress by remember(listState, transitionDistance) {
        derivedStateOf {
            weatherTitleBarProgress(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
                transitionDistance = transitionDistance,
            )
        }
    }
    var temperatureSourceBounds by remember(weather.cityName) { mutableStateOf<Rect?>(null) }
    var temperatureTargetBounds by remember(weather.cityName) { mutableStateOf<Rect?>(null) }
    val temperatureTransitionReady = temperatureSourceBounds != null && temperatureTargetBounds != null

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 156.dp,
                end = 20.dp,
                bottom = if (previewMode || weatherUiTestMode) 104.dp else 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                WeatherHero(
                    weather = weather,
                    style = style,
                    temperatureVisible = !temperatureTransitionReady,
                    onTemperaturePositioned = { temperatureSourceBounds = it },
                )
            }
            if (refreshing) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.dp)),
                        color = style.accent,
                        trackColor = style.panelColor.copy(alpha = 0.28f),
                    )
                }
            }
            item {
                TodaySummaryPanel(weather = weather, style = style)
            }
            item {
                HourlyForecastPanel(weather = weather, style = style)
            }
            item {
                DailyForecastPanel(forecasts = weather.forecasts.take(7), style = style)
            }
            item {
                AirQualityPanel(weather = weather, style = style)
            }
            item {
                MoreDetailsPanel(
                    weather = weather,
                    style = style,
                )
            }
        }

        WeatherTitleBar(
            weather = weather,
            refreshing = refreshing,
            style = style,
            progress = titleBarProgress,
            previewMode = previewMode,
            onTemperatureTargetPositioned = { temperatureTargetBounds = it },
            onLocation = onLocation,
            onSearch = onSearch,
            onMenu = onMenu,
        )
        if (temperatureTransitionReady) {
            TransitioningTemperature(
                temperature = weather.currentTemperature.toTemperatureText(),
                sourceBounds = requireNotNull(temperatureSourceBounds),
                targetBounds = requireNotNull(temperatureTargetBounds),
                progress = titleBarProgress,
                style = style,
            )
        }
    }
}

@Composable
private fun WeatherTitleBar(
    weather: WeatherUiModel,
    refreshing: Boolean,
    style: WeatherVisualStyle,
    progress: Float,
    previewMode: Boolean,
    onTemperatureTargetPositioned: (Rect) -> Unit,
    onLocation: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit,
) {
    val easedProgress = progress * progress * (3f - 2f * progress)
    val titleBarHeight = lerp(start = 136f, stop = 64f, fraction = easedProgress).dp
    val secondaryContentAlpha = (1f - (progress * 2.35f)).coerceIn(0f, 1f)
    val backgroundAlpha = weatherTitleBarBackgroundAlpha(progress)
    val backgroundStart = style.panelColor.copy(alpha = backgroundAlpha)
    val backgroundEnd = androidx.compose.ui.graphics.lerp(
        style.panelColor.copy(alpha = 1f),
        style.backgroundColors.first().copy(alpha = 1f),
        0.16f,
    ).copy(alpha = backgroundAlpha)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = style.panelContent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.background(
                Brush.horizontalGradient(listOf(backgroundStart, backgroundEnd)),
            ),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(titleBarHeight),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 206.dp)
                        .graphicsLayer {
                            translationY = lerp(12.dp.toPx(), 13.5.dp.toPx(), easedProgress)
                        }
                        .then(if (previewMode) Modifier else Modifier.clickable(onClick = onLocation)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = weather.cityName,
                            color = style.content,
                            fontSize = lerp(32f, 22f, easedProgress).sp,
                            lineHeight = lerp(40f, 28f, easedProgress).sp,
                            fontWeight = FontWeight(lerp(700f, 600f, easedProgress).roundToInt()),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!previewMode) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "选择城市",
                                tint = style.content.copy(alpha = 0.86f),
                                modifier = Modifier.size(lerp(24f, 20f, easedProgress).dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.graphicsLayer {
                            alpha = secondaryContentAlpha
                            translationY = -8.dp.toPx() * easedProgress
                        },
                    ) {
                        if (weather.citySubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = weather.citySubtitle,
                                color = style.content.copy(alpha = 0.84f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (weather.coordinatesText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = style.content.copy(alpha = 0.88f),
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = weather.coordinatesText,
                                    color = style.content.copy(alpha = 0.84f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                if (!previewMode) Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .offset(y = (-1).dp),
                    ) {
                        Text(
                            text = weather.currentTemperature.toTemperatureText(),
                            modifier = Modifier
                                .alpha(0f)
                                .onGloballyPositioned {
                                    onTemperatureTargetPositioned(it.boundsInRoot())
                                },
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                    SearchAddHeaderActionButton(
                        enabled = !refreshing,
                        style = style,
                        onClick = onSearch,
                    )
                    HeaderActionButton(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        style = style,
                        onClick = onMenu,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = style.panelContent.copy(alpha = 0.14f * easedProgress),
                )
            }
        }
    }
}

@Composable
private fun TransitioningTemperature(
    temperature: String,
    sourceBounds: Rect,
    targetBounds: Rect,
    progress: Float,
    style: WeatherVisualStyle,
) {
    val easedProgress = progress * progress * (3f - 2f * progress)
    val horizontalProgress = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f).let {
        it * it * (3f - 2f * it)
    }
    val x = lerp(sourceBounds.left, targetBounds.left, horizontalProgress)
    val y = lerp(sourceBounds.top, targetBounds.top, easedProgress)

    Text(
        text = temperature,
        modifier = Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
        color = androidx.compose.ui.graphics.lerp(style.content, style.panelContent, easedProgress),
        fontSize = lerp(92f, 22f, easedProgress).sp,
        lineHeight = lerp(96f, 28f, easedProgress).sp,
        fontWeight = FontWeight(lerp(300f, 400f, easedProgress).roundToInt()),
        letterSpacing = 0.sp,
        maxLines = 1,
    )
}

@Composable
private fun HeaderActionButton(
    imageVector: ImageVector,
    contentDescription: String,
    style: WeatherVisualStyle,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(
        modifier = Modifier.size(48.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = style.content.copy(alpha = if (enabled) 0.96f else 0.46f),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun SearchAddHeaderActionButton(
    style: WeatherVisualStyle,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(
        modifier = Modifier.size(48.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "添加城市",
                tint = style.content.copy(alpha = if (enabled) 0.96f else 0.46f),
                modifier = Modifier
                    .size(27.dp)
                    .align(Alignment.BottomStart),
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopEnd),
                tint = style.content.copy(alpha = if (enabled) 0.96f else 0.46f),
            )
        }
    }
}

@Composable
private fun CityPreviewControls(
    style: WeatherVisualStyle,
    onClose: () -> Unit,
    onAdd: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            shape = CircleShape,
            color = style.panelColor.copy(alpha = 0.72f),
            contentColor = style.content,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭城市预览",
                )
            }
        }
        Button(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(vertical = 15.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加城市",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WeatherHero(
    weather: WeatherUiModel,
    style: WeatherVisualStyle,
    temperatureVisible: Boolean,
    onTemperaturePositioned: (Rect) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 216.dp)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = weather.currentTemperature.toTemperatureText(),
            modifier = Modifier.onGloballyPositioned {
                onTemperaturePositioned(it.boundsInRoot())
            },
            color = if (temperatureVisible) style.content else Color.Transparent,
            fontSize = 92.sp,
            lineHeight = 96.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.sp,
        )
        Text(
            text = weather.currentCondition.ifBlank { "--" },
            color = style.content,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "体感温度 ${weather.feelsLikeTemperature.ifBlank { "--" }}°",
            color = style.content.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TodaySummaryPanel(
    weather: WeatherUiModel,
    style: WeatherVisualStyle,
) {
    WeatherPanel(style = style) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SummaryMetric(
                icon = SummaryMetricIcon.High,
                label = "最高",
                value = weather.todaySummary.high,
                tint = style.warmAccent,
                modifier = Modifier.weight(1f),
                style = style,
            )
            SummaryMetric(
                icon = SummaryMetricIcon.Low,
                label = "最低",
                value = weather.todaySummary.low,
                tint = style.coolAccent,
                modifier = Modifier.weight(1f),
                style = style,
            )
            SummaryMetric(
                icon = SummaryMetricIcon.Sunrise,
                label = "日出",
                value = weather.todaySummary.sunrise,
                tint = style.warmAccent,
                modifier = Modifier.weight(1f),
                style = style,
            )
            SummaryMetric(
                icon = SummaryMetricIcon.Sunset,
                label = "日落",
                value = weather.todaySummary.sunset,
                tint = style.warmAccent,
                modifier = Modifier.weight(1f),
                style = style,
            )
        }
    }
}

private enum class SummaryMetricIcon {
    High,
    Low,
    Sunrise,
    Sunset,
}

@Composable
private fun SummaryMetric(
    icon: SummaryMetricIcon,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
    style: WeatherVisualStyle,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (icon) {
                SummaryMetricIcon.High -> Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = tint.copy(alpha = 0.92f),
                )
                SummaryMetricIcon.Low -> Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = tint.copy(alpha = 0.92f),
                )
                SummaryMetricIcon.Sunrise -> SunEventIcon(
                    rising = true,
                    tint = tint.copy(alpha = 0.92f),
                )
                SummaryMetricIcon.Sunset -> SunEventIcon(
                    rising = false,
                    tint = tint.copy(alpha = 0.92f),
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = style.panelContent,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = style.panelContent.copy(alpha = 0.82f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SunEventIcon(
    rising: Boolean,
    tint: Color,
) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val strokeWidth = 1.55.dp.toPx()
        val horizonY = size.height * 0.68f
        val centerX = size.width * 0.45f
        val sunRadius = size.width * 0.19f
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - sunRadius, horizonY - sunRadius),
            size = Size(sunRadius * 2f, sunRadius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.08f, horizonY),
            end = Offset(size.width * 0.82f, horizonY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        val arrowX = size.width * 0.82f
        val arrowStartY = if (rising) size.height * 0.60f else size.height * 0.24f
        val arrowEndY = if (rising) size.height * 0.24f else size.height * 0.60f
        drawLine(
            color = tint,
            start = Offset(arrowX, arrowStartY),
            end = Offset(arrowX, arrowEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        val arrowDirection = if (rising) 1f else -1f
        drawLine(
            color = tint,
            start = Offset(arrowX, arrowEndY),
            end = Offset(arrowX - size.width * 0.10f, arrowEndY + size.height * 0.10f * arrowDirection),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(arrowX, arrowEndY),
            end = Offset(arrowX + size.width * 0.10f, arrowEndY + size.height * 0.10f * arrowDirection),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun HourlyForecastPanel(
    weather: WeatherUiModel,
    style: WeatherVisualStyle,
) {
    val hourlyForecasts = weather.hourlyForecasts
    val hourlyScrollState = rememberScrollState()
    val hourlyContentWidth = HourlyForecastColumnWidth * hourlyForecasts.size

    WeatherPanel(style = style) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)) {
            ForecastPanelTitle(
                text = "每小时预报",
                style = style,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hourlyScrollState)
                    .padding(horizontal = 12.dp),
            ) {
                Row(modifier = Modifier.width(hourlyContentWidth)) {
                    hourlyForecasts.forEachIndexed { index, forecast ->
                        HourlyForecastItem(
                            forecast = forecast,
                            selected = index == 0,
                            style = style,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                TemperatureChart(
                    forecasts = hourlyForecasts,
                    accent = style.panelContent,
                    modifier = Modifier
                        .width(hourlyContentWidth)
                        .height(48.dp),
                )
            }
        }
    }
}

@Composable
private fun ForecastPanelTitle(
    text: String,
    style: WeatherVisualStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = style.panelContent.copy(alpha = 0.94f),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun HourlyForecastItem(
    forecast: HourlyForecastUiModel,
    selected: Boolean,
    style: WeatherVisualStyle,
) {
    Column(
        modifier = Modifier
            .width(HourlyForecastColumnWidth)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) style.glassChipColor else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (forecast.time == "Now") "现在" else forecast.time,
            style = MaterialTheme.typography.labelMedium,
            color = style.panelContent.copy(alpha = 0.92f),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Icon(
            imageVector = weatherConditionIcon(forecast.scene),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = weatherConditionIconTint(forecast.scene, style),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${forecast.temperature}°",
            style = MaterialTheme.typography.titleSmall,
            color = style.panelContent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TemperatureChart(
    forecasts: List<HourlyForecastUiModel>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val temperatures = forecasts.map { it.temperature }
    if (temperatures.isEmpty()) {
        return
    }
    Canvas(modifier = modifier) {
        val minTemperature = temperatures.minOrNull() ?: 0
        val maxTemperature = temperatures.maxOrNull() ?: minTemperature
        val temperatureRange = maxTemperature - minTemperature
        val displayRange = temperatureRange.coerceAtLeast(8)
        val temperatureCenter = (minTemperature + maxTemperature) / 2f
        val topPadding = 10.dp.toPx()
        val bottomPadding = 10.dp.toPx()
        val chartHeight = size.height - topPadding - bottomPadding
        val chartCenterY = topPadding + chartHeight / 2f
        val columnWidth = size.width / temperatures.size
        val points = temperatures.mapIndexed { index, value ->
            val x = columnWidth * (index + 0.5f)
            val y = chartCenterY - ((value - temperatureCenter) / displayRange) * chartHeight
            Offset(x, y)
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.zipWithNext().forEach { (start, end) ->
                val controlDistance = (end.x - start.x) / 3f
                cubicTo(
                    start.x + controlDistance,
                    start.y,
                    end.x - controlDistance,
                    end.y,
                    end.x,
                    end.y,
                )
            }
        }
        drawPath(
            path = path,
            color = accent,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        points.forEach { point ->
            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun DailyForecastPanel(
    forecasts: List<ForecastUiModel>,
    style: WeatherVisualStyle,
) {
    WeatherPanel(style = style) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            ForecastPanelTitle(text = "未来7天预报", style = style)
            Spacer(modifier = Modifier.height(7.dp))
            val minTemperature = forecasts.minOfOrNull { it.tempMin } ?: 0
            val maxTemperature = forecasts.maxOfOrNull { it.tempMax } ?: minTemperature
            forecasts.forEachIndexed { index, forecast ->
                DailyForecastRow(
                    forecast = forecast,
                    isToday = index == 0,
                    minTemperature = minTemperature,
                    maxTemperature = maxTemperature,
                    style = style,
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    forecast: ForecastUiModel,
    isToday: Boolean,
    minTemperature: Int,
    maxTemperature: Int,
    style: WeatherVisualStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isToday) "今天" else forecast.week.ifBlank { forecast.date },
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = style.panelContent,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
        )
        Icon(
            imageVector = weatherConditionIcon(forecast.scene),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = weatherConditionIconTint(forecast.scene, style),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = forecast.conditionText.ifBlank { "--" },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = style.panelContent.copy(alpha = 0.82f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${forecast.tempMin}°",
            modifier = Modifier.width(34.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = style.panelContent,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
        TemperatureRangeBar(
            low = forecast.tempMin,
            high = forecast.tempMax,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            accent = style.warmAccent,
            modifier = Modifier
                .width(82.dp)
                .height(20.dp)
                .padding(horizontal = 7.dp),
        )
        Text(
            text = "${forecast.tempMax}°",
            modifier = Modifier.width(34.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = style.panelContent,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun TemperatureRangeBar(
    low: Int,
    high: Int,
    minTemperature: Int,
    maxTemperature: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val range = (maxTemperature - minTemperature).takeIf { it > 0 } ?: 1
        val startFraction = (low - minTemperature).toFloat() / range
        val endFraction = (high - minTemperature).toFloat() / range
        val y = size.height / 2f
        drawLine(
            color = Color.White.copy(alpha = 0.24f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF74E0C1), Color(0xFFFFD56A), Color(0xFFFF8A4C), Color(0xFFE986D8)),
            ),
            start = Offset(size.width * startFraction, y),
            end = Offset(size.width * endFraction, y),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = accent,
            radius = 3.dp.toPx(),
            center = Offset(size.width * endFraction, y),
        )
    }
}

@Composable
private fun AirQualityPanel(
    weather: WeatherUiModel,
    style: WeatherVisualStyle,
) {
    WeatherPanel(style = style) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            ForecastPanelTitle(text = "空气质量", style = style)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AirQualityGauge(
                    aqi = weather.aqi,
                    quality = weather.airQuality,
                    style = style,
                    modifier = Modifier.size(96.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "主要污染物",
                        style = MaterialTheme.typography.labelLarge,
                        color = style.panelContent.copy(alpha = 0.88f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weather.pollutants.take(6).forEach { pollutant ->
                            PollutantMetric(
                                pollutant = pollutant,
                                highlighted = pollutant.label.toPlainPollutantLabel() == weather.primaryPollutant,
                                style = style,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AirQualityScale(aqi = weather.aqi, style = style)
        }
    }
}

@Composable
private fun AirQualityGauge(
    aqi: Int,
    quality: String,
    style: WeatherVisualStyle,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val diameter = minOf(size.width, size.height) - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)
            val startAngle = 140f
            val sweepAngle = 260f
            drawArc(
                color = style.panelContent.copy(alpha = 0.14f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = airQualityColor(aqi),
                startAngle = startAngle,
                sweepAngle = sweepAngle * (aqi.coerceIn(0, 300) / 300f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(
            modifier = Modifier.offset(y = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = aqi.takeIf { it > 0 }?.toString() ?: "--",
                style = MaterialTheme.typography.headlineMedium,
                color = style.panelContent,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = quality.ifBlank { "暂无" },
                style = MaterialTheme.typography.labelMedium,
                color = airQualityColor(aqi),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PollutantMetric(
    pollutant: AirPollutantUiModel,
    highlighted: Boolean,
    style: WeatherVisualStyle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = pollutant.label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) style.warmAccent else style.panelContent.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = pollutant.value,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = style.panelContent,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AirQualityScale(
    aqi: Int,
    style: WeatherVisualStyle,
) {
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
        ) {
            val y = size.height / 2f
            drawLine(
                brush = Brush.horizontalGradient(AirQualityScaleColors),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            val position = (aqi.coerceIn(0, 300) / 300f) * size.width
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(position, y))
            drawCircle(color = airQualityColor(aqi), radius = 3.dp.toPx(), center = Offset(position, y))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("0", "50", "100", "150", "200", "300+").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = style.panelContent.copy(alpha = 0.58f),
                )
            }
        }
    }
}

@Composable
private fun MoreDetailsPanel(
    weather: WeatherUiModel,
    style: WeatherVisualStyle,
) {
    WeatherPanel(style = style) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            ForecastPanelTitle(text = "详细信息", style = style)
            Spacer(modifier = Modifier.height(10.dp))
            val detailRows = weather.details.chunked(3)
            detailRows.forEachIndexed { rowIndex, details ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    details.forEach { detail ->
                        DetailTile(
                            detail = detail,
                            style = style,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - details.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIndex != detailRows.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailTile(
    detail: WeatherDetailUiModel,
    style: WeatherVisualStyle,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(124.dp),
        shape = RoundedCornerShape(14.dp),
        color = style.glassChipColor.copy(alpha = 0.12f),
        contentColor = style.panelContent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = detailIcon(detail.title),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = detailIconTint(detail.title, style),
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = detail.title,
                style = MaterialTheme.typography.labelMedium,
                color = style.panelContent.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detail.value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = style.panelContent,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (detail.subtitle.isNotBlank()) {
                Text(
                    text = detail.subtitle,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = style.panelContent.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CitiesTab(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onCurrentLocationClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .lightScreenBackground(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                LocationScreenTopBar(
                    title = "选择城市",
                    navigationIcon = Icons.Default.Close,
                    navigationDescription = "返回首页",
                    onNavigationClick = { onEvent(HomeUiEvent.TabSelected(HomeTab.Home)) },
                )
            }
            item {
                SearchBarButton(
                    placeholder = "搜索城市或地区",
                    onClick = { onEvent(HomeUiEvent.SearchOpened) },
                )
            }
            item { SectionLabel(text = "当前定位", dark = true) }
            item {
                CurrentLocationCard(
                    locationState = state.currentLocation,
                    currentWeather = state.weather,
                    onClick = onCurrentLocationClick,
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel(
                        text = "已添加城市",
                        dark = true,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${state.addedCities.size} 个城市",
                        color = LightScreenMuted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (state.addedCities.isEmpty()) {
                item {
                    EmptyAddedCitiesCard(onSearchClick = { onEvent(HomeUiEvent.SearchOpened) })
                }
            } else {
                item {
                    AddedCitiesList(
                        cities = state.addedCities,
                        weatherByCityId = state.addedCityWeather,
                        currentWeather = state.weather,
                        onCityClick = { cityId -> onEvent(HomeUiEvent.CitySelected(cityId)) },
                        onDeleteClick = { cityId -> onEvent(HomeUiEvent.AddedCityRemoved(cityId)) },
                    )
                }
            }
            item {
                AddCityButton(onClick = { onEvent(HomeUiEvent.SearchOpened) })
            }
        }
    }
}

@Composable
private fun LocationScreenTopBar(
    title: String,
    navigationIcon: ImageVector,
    navigationDescription: String,
    onNavigationClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigationClick) {
            Icon(
                imageVector = navigationIcon,
                contentDescription = navigationDescription,
                tint = LightScreenInk,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = LightScreenInk,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun SearchBarButton(
    placeholder: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFEAF1F9),
        contentColor = LightScreenMuted,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (dark) LightScreenInk else Color.White,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CurrentLocationCard(
    locationState: CurrentLocationUiState,
    currentWeather: WeatherUiModel?,
    onClick: () -> Unit,
) {
    val locationCity = (locationState as? CurrentLocationUiState.Available)?.city
    val locationWeather = currentWeather?.takeIf { it.cityId == locationCity?.cityId }
    val cityName = when (locationState) {
        is CurrentLocationUiState.Available -> locationState.city.name
        is CurrentLocationUiState.Unavailable -> "当前位置"
        CurrentLocationUiState.Loading,
        CurrentLocationUiState.NotRequested -> "当前位置"
    }
    val subtitle = when (locationState) {
        is CurrentLocationUiState.Available -> locationState.city.subtitle
        is CurrentLocationUiState.Unavailable -> locationState.message
        CurrentLocationUiState.Loading -> "定位中"
        CurrentLocationUiState.NotRequested -> "点击获取当前城市"
    }
    LocationWeatherRow(
        leadingIcon = Icons.Default.Place,
        leadingTint = Color(0xFF3B82F6),
        cityName = cityName,
        subtitle = subtitle,
        temperature = locationWeather?.currentTemperature?.toTemperatureText() ?: "--",
        scene = locationWeather?.scene ?: WeatherSceneSpec(),
        selected = true,
        onClick = onClick,
    )
}

@Composable
private fun AddedCitiesList(
    cities: List<CityUiModel>,
    weatherByCityId: Map<String, WeatherUiModel>,
    currentWeather: WeatherUiModel?,
    onCityClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column {
            cities.forEachIndexed { index, city ->
                val weather = weatherByCityId[city.cityId]
                    ?: currentWeather?.takeIf { it.cityId == city.cityId }
                AddedCityListItem(
                    city = city,
                    weather = weather,
                    selected = currentWeather?.cityId == city.cityId,
                    onClick = { onCityClick(city.cityId) },
                    onDeleteClick = { onDeleteClick(city.cityId) },
                )
                if (index != cities.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = LightScreenBorder,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddedCityListItem(
    city: CityUiModel,
    weather: WeatherUiModel?,
    selected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val scene = weather?.scene ?: WeatherSceneSpec()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFFF2F7FD) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = city.name,
                    modifier = Modifier.weight(1f, fill = false),
                    color = LightScreenInk,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFFE0EEFC),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Text(
                            text = "当前",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            color = Color(0xFF2878C8),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (city.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = city.subtitle,
                    color = LightScreenMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (weather == null) Icons.Default.Schedule else weatherConditionIcon(scene),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (weather == null) {
                        LightScreenMuted
                    } else {
                        weatherConditionIconTint(scene, weatherVisualStyle(scene))
                    },
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = weather?.currentCondition?.ifBlank { "天气数据加载中" }
                        ?: "天气数据加载中",
                    color = Color(0xFF475569),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = weather?.currentTemperature?.toTemperatureText() ?: "--",
                color = LightScreenInk,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = addedCityTemperatureRange(
                    highTemperature = weather?.highTemperature,
                    lowTemperature = weather?.lowTemperature,
                ),
                color = LightScreenMuted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.size(40.dp))
        } else {
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onDeleteClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除${city.name}",
                    modifier = Modifier.size(19.dp),
                    tint = Color(0xFF94A3B8),
                )
            }
        }
    }
}

internal fun addedCityTemperatureRange(
    highTemperature: Int?,
    lowTemperature: Int?,
): String {
    val high = highTemperature?.let { "$it°" } ?: "--"
    val low = lowTemperature?.let { "$it°" } ?: "--"
    return "最高 $high  最低 $low"
}

@Composable
private fun LocationWeatherRow(
    leadingIcon: ImageVector?,
    leadingTint: Color,
    cityName: String,
    subtitle: String,
    temperature: String,
    scene: WeatherSceneSpec,
    selected: Boolean,
    trailingDelete: Boolean = false,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = if (selected) 4.dp else 1.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = leadingTint,
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cityName,
                    color = Color(0xFF0F172A),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = weatherConditionIcon(scene),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = weatherConditionIconTint(scene, weatherVisualStyle(scene)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = temperature,
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (trailingDelete) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFF94A3B8),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAddedCitiesCard(onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "尚未添加城市",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onSearchClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("添加城市")
            }
        }
    }
}

@Composable
private fun AddCityButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEAF3FF),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF0F2A4A),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加城市",
                color = Color(0xFF0F2A4A),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FeaturedCityCard(
    city: ShowcaseCity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = weatherVisualStyle(city.scene)
    val landmarkImageRes = landmarkImageForCity(city.name)
    Surface(
        modifier = modifier
            .width(104.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.28f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (landmarkImageRes != null) {
                Image(
                    painter = painterResource(landmarkImageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(style.backgroundColors)),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawWeatherThumbnailScene(city.scene, style)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.32f),
                                0.28f to Color.Transparent,
                                0.58f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.74f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = city.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = city.country,
                        color = Color.White.copy(alpha = 0.84f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Icon(
                        imageVector = weatherConditionIcon(city.scene),
                        contentDescription = null,
                        tint = weatherConditionIconTint(city.scene, style),
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${city.temperature}°",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun HotCitiesTab(
    popularCities: List<CityUiModel>,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val sections = hotCitySections(popularCities)
    var expandedSections by remember { mutableStateOf(defaultExpandedHotCitySections()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .lightScreenBackground()
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 6.dp),
        ) {
            LocationScreenTopBar(
                title = "全球热门城市",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationDescription = "返回搜索城市",
                onNavigationClick = { onEvent(HomeUiEvent.SearchOpened) },
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            sections.forEachIndexed { sectionIndex, section ->
                val expanded = section.title in expandedSections
                stickyHeader(key = section.title) {
                    Box(
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                placementSpec = tween(durationMillis = 300, easing = PageTransitionEasing),
                                fadeOutSpec = null,
                            )
                            .fillMaxWidth()
                            .background(LightCanvasTop)
                            .padding(vertical = 4.dp),
                    ) {
                        HotCitySectionHeader(
                            title = section.title,
                            cityCount = section.cities.size,
                            expanded = expanded,
                            onToggle = {
                                expandedSections = toggleHotCitySection(
                                    expandedSections = expandedSections,
                                    sectionTitle = section.title,
                                )
                            },
                        )
                    }
                }
                if (expanded) {
                    items(
                        items = section.cities.chunked(3),
                        key = { row -> "$sectionIndex:${row.joinToString { it.cityId.ifBlank { it.name } }}" },
                    ) { cities ->
                        Row(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(durationMillis = 220, easing = PageTransitionEasing),
                                placementSpec = tween(durationMillis = 300, easing = PageTransitionEasing),
                                fadeOutSpec = tween(durationMillis = 160, easing = PageTransitionEasing),
                            ),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            cities.forEach { city ->
                                HotCityGridCard(
                                    city = city,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onShowcaseCityClick(city, onEvent) },
                                )
                            }
                            repeat(3 - cities.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else if (sectionIndex != sections.lastIndex) {
                    item(key = "${section.title}:divider") {
                        HorizontalDivider(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(durationMillis = 180, easing = PageTransitionEasing),
                                placementSpec = tween(durationMillis = 300, easing = PageTransitionEasing),
                                fadeOutSpec = tween(durationMillis = 120, easing = PageTransitionEasing),
                            ),
                            color = Color(0xFFE5EAF2),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HotCitySectionHeader(
    title: String,
    cityCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220, easing = PageTransitionEasing),
        label = "HotCitySectionArrow",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle,
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEAF2FC),
        border = BorderStroke(1.dp, Color(0xFFD8E5F4)),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = Color(0xFF0F2A4A),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color.White.copy(alpha = 0.82f),
            ) {
                Text(
                    text = "$cityCount 个城市",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "收起$title" else "展开$title",
                tint = Color(0xFF42658D),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .graphicsLayer { rotationZ = arrowRotation },
            )
        }
    }
}

@Composable
private fun HotCityGridCard(
    city: ShowcaseCity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            CityLandmarkGridImage(city = city)
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                Text(
                    text = city.name,
                    color = Color(0xFF0F172A),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = city.country,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CityLandmarkGridImage(city: ShowcaseCity) {
    val style = weatherVisualStyle(city.scene)
    val landmarkImageRes = hotCityThumbnailImageForCity(city.name)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Brush.verticalGradient(style.backgroundColors)),
        contentAlignment = Alignment.Center,
    ) {
        if (landmarkImageRes != null) {
            Image(
                painter = painterResource(landmarkImageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = weatherConditionIcon(city.scene),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = weatherConditionIconTint(city.scene, style),
            )
        }
    }
}

@Composable
private fun SearchCityScreen(
    query: String,
    results: List<CityUiModel>,
    searching: Boolean,
    recentCities: List<CityUiModel>,
    popularCities: List<CityUiModel>,
    addedCityIds: Set<String>,
    onPopularCityClick: (ShowcaseCity) -> Unit,
    onAllPopularCitiesClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCityClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .lightScreenBackground(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = LightScreenInk,
                        )
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() },
                        ),
                        placeholder = {
                            Text(
                                text = "搜索城市或地区",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                            )
                        },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清空",
                                        tint = Color(0xFF94A3B8),
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEFF3F8),
                            unfocusedContainerColor = Color(0xFFEFF3F8),
                            disabledContainerColor = Color(0xFFEFF3F8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
            if (searching) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.dp)),
                        color = Color(0xFF3B82F6),
                        trackColor = Color(0xFFE5EAF2),
                    )
                }
            }
            if (query.isBlank()) {
                item {
                    SearchSectionTitle(text = "搜索历史")
                }
                if (recentCities.isEmpty()) {
                    item {
                        EmptySearchHint(text = "暂无搜索历史")
                    }
                } else {
                    item {
                        SearchHistoryGrid(
                            cities = recentCities,
                            addedCityIds = addedCityIds,
                            onCityClick = onCityClick,
                        )
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SearchSectionTitle(
                            text = "全球热门城市",
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onAllPopularCitiesClick) {
                            Text(
                                text = "全部",
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                item {
                    val featuredCities = featuredCityCards(popularCities)
                    val featuredCityListState = remember { LazyListState() }
                    val showStartEdgeFade by remember(featuredCityListState) {
                        derivedStateOf { featuredCityListState.canScrollBackward }
                    }
                    val showEndEdgeFade by remember(featuredCityListState) {
                        derivedStateOf { featuredCityListState.canScrollForward }
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(
                            state = featuredCityListState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            flingBehavior = rememberSnapFlingBehavior(featuredCityListState),
                        ) {
                            items(
                                items = featuredCities,
                                key = { it.cityId.ifBlank { it.name } },
                            ) { city ->
                                FeaturedCityCard(
                                    city = city,
                                    onClick = { onPopularCityClick(city) },
                                )
                            }
                        }
                        if (showStartEdgeFade) {
                            CarouselEdgeFade(
                                alignStart = true,
                                modifier = Modifier.align(Alignment.CenterStart),
                            )
                        }
                        if (showEndEdgeFade) {
                            CarouselEdgeFade(
                                alignStart = false,
                                modifier = Modifier.align(Alignment.CenterEnd),
                            )
                        }
                    }
                }
            } else if (results.isEmpty() && !searching) {
                item {
                    SearchSectionTitle(text = "搜索结果")
                }
                item {
                    EmptySearchHint(text = "没有找到匹配城市")
                }
            } else {
                item {
                    SearchSectionTitle(text = "搜索结果")
                }
                items(results, key = { it.cityId }) { city ->
                    SearchResultRow(
                        city = city,
                        isAdded = city.cityId in addedCityIds,
                        onClick = { onCityClick(city.cityId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselEdgeFade(
    alignStart: Boolean,
    modifier: Modifier = Modifier,
) {
    val edgeColor = LightCanvasBottom.copy(alpha = 0.98f)
    Box(
        modifier = modifier
            .width(18.dp)
            .height(160.dp)
            .background(
                Brush.horizontalGradient(
                    colors = if (alignStart) {
                        listOf(edgeColor, Color.Transparent)
                    } else {
                        listOf(Color.Transparent, edgeColor)
                    },
                ),
            ),
    )
}

@Composable
private fun SearchHistoryGrid(
    cities: List<CityUiModel>,
    addedCityIds: Set<String>,
    onCityClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cities.chunked(3).forEach { rowCities ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCities.forEach { city ->
                    SearchHistoryCard(
                        city = city,
                        isAdded = city.cityId in addedCityIds,
                        onClick = { onCityClick(city.cityId) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - rowCities.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryCard(
    city: CityUiModel,
    isAdded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = city.name,
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = showcaseCountryForCity(city.name).ifBlank { city.subtitle.countryLabel() },
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (isAdded) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                            shape = CircleShape,
                        ),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAdded) "已添加" else "未添加",
                    color = if (isAdded) Color(0xFF2563EB) else Color(0xFF94A3B8),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SearchSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = LightScreenInk,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SearchResultRow(
    city: CityUiModel,
    isAdded: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    color = Color(0xFF0F172A),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = city.subtitle,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isAdded) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFE8F2FF),
                    contentColor = Color(0xFF2563EB),
                ) {
                    Text(
                        text = "已添加",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFB7C4D7),
                )
            }
        }
    }
}

@Composable
private fun EmptySearchHint(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFEAF2FC),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = Color(0xFF5681B5),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = LightScreenMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class ShowcaseCity(
    val cityId: String,
    val name: String,
    val country: String,
    val temperature: Int,
    val scene: WeatherSceneSpec,
)

private data class ShowcaseCitySection(
    val title: String,
    val cities: List<ShowcaseCity>,
)

private data class ShowcaseWeather(
    val temperature: Int,
    val scene: WeatherSceneSpec,
)

private val ShowcaseClearScene = WeatherSceneSpec()
private val ShowcaseCloudyScene = WeatherSceneSpec(cloudCover = WeatherCloudCover.Cloudy)
private val ShowcaseRainScene = WeatherSceneSpec(
    cloudCover = WeatherCloudCover.Cloudy,
    precipitation = WeatherPrecipitation.Rain,
    precipitationIntensity = WeatherIntensity.Light,
)

internal fun featuredCityNames(): List<String> = listOf(
    "北京",
    "上海",
    "伦敦",
    "巴黎",
    "柏林",
    "纽约",
    "洛杉矶",
    "东京",
    "首尔",
)

private fun featuredCityCards(popularCities: List<CityUiModel>): List<ShowcaseCity> {
    val byName = popularCities.associateBy { it.name }
    val fallbacksByName = fallbackShowcaseCities().associateBy { it.name }
    return featuredCityNames().mapNotNull { name ->
        byName[name]?.let(::toShowcaseCity) ?: fallbacksByName[name]
    }
}

private fun landmarkImageForCity(name: String): Int? = when (name) {
    "北京" -> R.drawable.city_landmark_beijing
    "上海" -> R.drawable.city_landmark_shanghai
    "伦敦" -> R.drawable.city_landmark_london
    "巴黎" -> R.drawable.city_landmark_paris
    "柏林" -> R.drawable.city_thumb_berlin
    "纽约" -> R.drawable.city_landmark_new_york
    "洛杉矶" -> R.drawable.city_landmark_los_angeles
    "东京" -> R.drawable.city_landmark_tokyo
    "首尔" -> R.drawable.city_landmark_seoul
    else -> null
}

private val HotCityThumbnailResources = mapOf(
    "北京" to R.drawable.city_thumb_beijing,
    "上海" to R.drawable.city_thumb_shanghai,
    "广州" to R.drawable.city_thumb_guangzhou,
    "深圳" to R.drawable.city_thumb_shenzhen,
    "香港" to R.drawable.city_thumb_hong_kong,
    "台北" to R.drawable.city_thumb_taipei,
    "东京" to R.drawable.city_thumb_tokyo,
    "首尔" to R.drawable.city_thumb_seoul,
    "新加坡" to R.drawable.city_thumb_singapore,
    "曼谷" to R.drawable.city_thumb_bangkok,
    "纽约" to R.drawable.city_thumb_new_york,
    "洛杉矶" to R.drawable.city_thumb_los_angeles,
    "旧金山" to R.drawable.city_thumb_san_francisco,
    "伦敦" to R.drawable.city_thumb_london,
    "巴黎" to R.drawable.city_thumb_paris,
    "柏林" to R.drawable.city_thumb_berlin,
    "罗马" to R.drawable.city_thumb_rome,
    "马德里" to R.drawable.city_thumb_madrid,
    "悉尼" to R.drawable.city_thumb_sydney,
    "墨尔本" to R.drawable.city_thumb_melbourne,
    "多伦多" to R.drawable.city_thumb_toronto,
    "温哥华" to R.drawable.city_thumb_vancouver,
    "迪拜" to R.drawable.city_thumb_dubai,
    "伊斯坦布尔" to R.drawable.city_thumb_istanbul,
    "莫斯科" to R.drawable.city_thumb_moscow,
    "开罗" to R.drawable.city_thumb_cairo,
    "约翰内斯堡" to R.drawable.city_thumb_johannesburg,
    "里约热内卢" to R.drawable.city_thumb_rio_de_janeiro,
    "圣保罗" to R.drawable.city_thumb_sao_paulo,
    "墨西哥城" to R.drawable.city_thumb_mexico_city,
)

private fun hotCityThumbnailImageForCity(name: String): Int? = HotCityThumbnailResources[name]

internal fun hotCityThumbnailNames(): Set<String> = HotCityThumbnailResources.keys

private fun hotCitySections(popularCities: List<CityUiModel>): List<ShowcaseCitySection> {
    val cities = popularCities.map(::toShowcaseCity).ifEmpty { fallbackShowcaseCities() }
    val groupedCities = cities.groupBy { continentForCountry(it.country) }
    return listOf("亚洲", "欧洲", "北美洲", "南美洲", "大洋洲", "非洲").map { title ->
        ShowcaseCitySection(
            title = title,
            cities = groupedCities[title].orEmpty(),
        )
    }
}

internal fun defaultExpandedHotCitySections(): Set<String> = setOf(
    "亚洲",
    "欧洲",
    "北美洲",
    "南美洲",
    "大洋洲",
    "非洲",
)

internal fun toggleHotCitySection(
    expandedSections: Set<String>,
    sectionTitle: String,
): Set<String> {
    return if (sectionTitle in expandedSections) {
        expandedSections - sectionTitle
    } else {
        expandedSections + sectionTitle
    }
}

private fun toShowcaseCity(city: CityUiModel): ShowcaseCity {
    val weather = showcaseWeatherForCity(city.name)
    return ShowcaseCity(
        cityId = city.cityId,
        name = city.name,
        country = showcaseCountryForCity(city.name).ifBlank { city.subtitle.countryLabel() },
        temperature = weather.temperature,
        scene = weather.scene,
    )
}

private fun fallbackShowcaseCities(): List<ShowcaseCity> = listOf(
    ShowcaseCity("", "北京", "中国", 18, ShowcaseCloudyScene),
    ShowcaseCity("", "上海", "中国", 26, ShowcaseClearScene),
    ShowcaseCity("", "伦敦", "英国", 15, ShowcaseCloudyScene),
    ShowcaseCity("", "巴黎", "法国", 21, ShowcaseClearScene),
    ShowcaseCity("", "纽约", "美国", 23, ShowcaseClearScene),
    ShowcaseCity("", "洛杉矶", "美国", 24, ShowcaseClearScene),
    ShowcaseCity("", "东京", "日本", 17, ShowcaseRainScene),
    ShowcaseCity("", "首尔", "韩国", 16, ShowcaseCloudyScene),
    ShowcaseCity("", "新加坡", "新加坡", 27, ShowcaseCloudyScene),
    ShowcaseCity("", "柏林", "德国", 17, ShowcaseCloudyScene),
    ShowcaseCity("", "罗马", "意大利", 23, ShowcaseClearScene),
    ShowcaseCity("", "马德里", "西班牙", 24, ShowcaseClearScene),
)

private fun onShowcaseCityClick(
    city: ShowcaseCity,
    onEvent: (HomeUiEvent) -> Unit,
) {
    if (city.cityId.isBlank()) {
        onEvent(HomeUiEvent.SearchOpened)
        onEvent(HomeUiEvent.SearchQueryChanged(city.name))
        return
    }
    onEvent(HomeUiEvent.CitySelected(city.cityId))
}

private fun showcaseWeatherForCity(name: String): ShowcaseWeather {
    return when (name) {
        "上海" -> ShowcaseWeather(26, ShowcaseClearScene)
        "北京" -> ShowcaseWeather(18, ShowcaseCloudyScene)
        "广州" -> ShowcaseWeather(29, ShowcaseClearScene)
        "深圳" -> ShowcaseWeather(28, ShowcaseCloudyScene)
        "香港" -> ShowcaseWeather(27, ShowcaseCloudyScene)
        "台北" -> ShowcaseWeather(25, ShowcaseRainScene)
        "东京" -> ShowcaseWeather(17, ShowcaseRainScene)
        "首尔" -> ShowcaseWeather(16, ShowcaseCloudyScene)
        "新加坡" -> ShowcaseWeather(27, ShowcaseCloudyScene)
        "曼谷" -> ShowcaseWeather(30, ShowcaseClearScene)
        "纽约" -> ShowcaseWeather(23, ShowcaseClearScene)
        "洛杉矶" -> ShowcaseWeather(24, ShowcaseClearScene)
        "伦敦" -> ShowcaseWeather(15, ShowcaseCloudyScene)
        "巴黎" -> ShowcaseWeather(21, ShowcaseClearScene)
        "柏林" -> ShowcaseWeather(17, ShowcaseCloudyScene)
        "罗马" -> ShowcaseWeather(23, ShowcaseClearScene)
        else -> ShowcaseWeather(18, ShowcaseCloudyScene)
    }
}

private fun showcaseCountryForCity(name: String): String {
    return when (name) {
        "北京", "上海", "广州", "深圳", "香港", "台北" -> "中国"
        "东京" -> "日本"
        "首尔" -> "韩国"
        "新加坡" -> "新加坡"
        "曼谷" -> "泰国"
        "纽约", "洛杉矶", "旧金山" -> "美国"
        "多伦多", "温哥华" -> "加拿大"
        "伦敦" -> "英国"
        "巴黎" -> "法国"
        "柏林" -> "德国"
        "罗马" -> "意大利"
        "马德里" -> "西班牙"
        "悉尼", "墨尔本" -> "澳大利亚"
        "迪拜" -> "阿联酋"
        "伊斯坦布尔" -> "土耳其"
        "莫斯科" -> "俄罗斯"
        "开罗" -> "埃及"
        "约翰内斯堡" -> "南非"
        "里约热内卢", "圣保罗" -> "巴西"
        "墨西哥城" -> "墨西哥"
        else -> ""
    }
}

private fun continentForCountry(country: String): String {
    return when (country) {
        "中国", "日本", "韩国", "新加坡", "泰国", "阿联酋" -> "亚洲"
        "英国", "法国", "德国", "意大利", "西班牙", "土耳其", "俄罗斯" -> "欧洲"
        "美国", "加拿大", "墨西哥" -> "北美洲"
        "巴西" -> "南美洲"
        "澳大利亚" -> "大洋洲"
        "埃及", "南非" -> "非洲"
        else -> "亚洲"
    }
}

private fun String.countryLabel(): String {
    return split("·")
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
}

@Composable
private fun SettingsTab(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    debugToolsEnabled: Boolean,
    weatherUiTestLauncherEnabled: Boolean,
    onWeatherUiTestLauncherEnabledChanged: (Boolean) -> Unit,
) {
    var showRefreshIntervalDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .lightScreenBackground(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LocationScreenTopBar(
                    title = "设置",
                    navigationIcon = Icons.Default.Close,
                    navigationDescription = "返回首页",
                    onNavigationClick = { onEvent(HomeUiEvent.TabSelected(HomeTab.Home)) },
                )
            }
            item {
                SectionLabel(
                    text = "显示与单位",
                    dark = true,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                )
            }
            item {
                SettingsGroup {
                    Column {
                        SettingsRow(
                            title = "主题",
                            value = state.settings.theme,
                            icon = Icons.Default.DarkMode,
                            onClick = { onEvent(HomeUiEvent.ThemeSettingClicked) },
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "温度单位",
                            value = state.settings.temperatureUnit,
                            icon = Icons.Default.Thermostat,
                            onClick = { onEvent(HomeUiEvent.TemperatureUnitClicked) },
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "风速单位",
                            value = state.settings.windSpeedUnit,
                            icon = Icons.Default.Air,
                            onClick = { onEvent(HomeUiEvent.WindSpeedUnitClicked) },
                        )
                    }
                }
            }
            item {
                SectionLabel(text = "通用", dark = true, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }
            item {
                SettingsGroup {
                    Column {
                        NotificationSettingsRow(
                            checked = state.settings.notificationsEnabled,
                            onCheckedChange = { onEvent(HomeUiEvent.NotificationsChanged(it)) },
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "自动刷新",
                            value = state.settings.weatherRefreshInterval.displayText(),
                            supportingText = "前台和后台同步更新",
                            icon = Icons.Default.Schedule,
                            onClick = { showRefreshIntervalDialog = true },
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "关于",
                            value = "Version 2.00",
                            icon = Icons.Default.Settings,
                        )
                    }
                }
            }
            if (debugToolsEnabled) {
                item {
                    SectionLabel(
                        text = "开发与测试",
                        dark = true,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                }
                item {
                    SettingsGroup {
                        WeatherUiTestSettingsRow(
                            checked = weatherUiTestLauncherEnabled,
                            onCheckedChange = onWeatherUiTestLauncherEnabledChanged,
                        )
                    }
                }
            }
        }
        if (showRefreshIntervalDialog) {
            WeatherRefreshIntervalDialog(
                selectedInterval = state.settings.weatherRefreshInterval,
                onDismiss = { showRefreshIntervalDialog = false },
                onIntervalSelected = { interval ->
                    showRefreshIntervalDialog = false
                    onEvent(HomeUiEvent.WeatherRefreshIntervalSelected(interval))
                },
            )
        }
    }
}

private fun WeatherRefreshInterval.displayText(): String {
    return when (this) {
        WeatherRefreshInterval.FifteenMinutes -> "15 分钟"
        WeatherRefreshInterval.ThirtyMinutes -> "30 分钟"
        WeatherRefreshInterval.OneHour -> "1 小时"
        WeatherRefreshInterval.ThreeHours -> "3 小时"
    }
}

@Composable
private fun WeatherRefreshIntervalDialog(
    selectedInterval: WeatherRefreshInterval,
    onDismiss: () -> Unit,
    onIntervalSelected: (WeatherRefreshInterval) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "自动刷新间隔",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                WeatherRefreshInterval.entries.forEach { interval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntervalSelected(interval) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = interval == selectedInterval,
                            onClick = { onIntervalSelected(interval) },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = interval.displayText(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = Color.White,
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightScreenBorder),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    icon: ImageVector,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 58.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF64748B),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    color = LightScreenMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LightScreenMuted,
        )
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LightScreenMuted,
            )
        }
    }
}

@Composable
private fun NotificationSettingsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = LightScreenMuted,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "通知",
            modifier = Modifier.weight(1f),
            color = LightScreenInk,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun WeatherUiTestSettingsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .heightIn(min = 68.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = LightScreenMuted,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "显示天气测试入口",
                color = LightScreenInk,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "在天气页悬浮显示，仅 Debug 包可用",
                color = LightScreenMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 50.dp),
        color = LightScreenBorder,
    )
}

@Composable
private fun WeatherLoading(style: WeatherVisualStyle) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = style.content)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "天气加载中",
                color = style.content.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EmptyWeather(
    message: String,
    style: WeatherVisualStyle,
    onSearchClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = style.heroIcon,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = message,
                color = style.content,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSearchClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择城市")
            }
        }
    }
}

@Composable
private fun WeatherPanel(
    style: WeatherVisualStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = style.panelColor.copy(alpha = style.panelColor.alpha * 0.84f),
        contentColor = style.panelContent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        content = content,
    )
}

private fun detailIcon(title: String): ImageVector {
    return when (title) {
        "体感温度" -> Icons.Default.Thermostat
        "风速" -> Icons.Default.Air
        "湿度" -> Icons.Default.WaterDrop
        "气压" -> Icons.Default.Speed
        "能见度" -> Icons.Default.Visibility
        else -> Icons.Default.WbSunny
    }
}

private fun detailIconTint(
    title: String,
    style: WeatherVisualStyle,
): Color {
    return when (title) {
        "湿度" -> style.coolAccent
        "紫外线指数" -> style.warmAccent
        else -> style.panelContent.copy(alpha = 0.68f)
    }
}

private fun String.toPlainPollutantLabel(): String {
    return replace("₂", "2").replace("₃", "3")
}

internal fun airQualityColor(aqi: Int): Color {
    val position = (aqi.coerceIn(0, 300) / 300f) * (AirQualityScaleColors.size - 1)
    val startIndex = position.toInt().coerceAtMost(AirQualityScaleColors.lastIndex)
    val endIndex = (startIndex + 1).coerceAtMost(AirQualityScaleColors.lastIndex)
    return androidx.compose.ui.graphics.lerp(
        start = AirQualityScaleColors[startIndex],
        stop = AirQualityScaleColors[endIndex],
        fraction = position - startIndex,
    )
}

private fun String.toTemperatureText(): String {
    return if (isBlank()) "--°" else "$this°"
}

private fun Int?.toDegreeRangeText(): String {
    return this?.let { "$it°" } ?: "--"
}
