package cn.byronlab.weather.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cn.byronlab.weather.BuildConfig
import cn.byronlab.weather.presentation.home.HomeScreen
import cn.byronlab.weather.presentation.home.HomeUiEvent
import cn.byronlab.weather.presentation.home.HomeViewModel
import cn.byronlab.weather.presentation.theme.MinimalistWeatherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private val transparent = android.graphics.Color.TRANSPARENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateSystemBars(useDarkStatusBarIcons = false)
        setContent {
            var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission()) }
            val debugPreferences = remember {
                getSharedPreferences(DEBUG_TOOLS_PREFERENCES, MODE_PRIVATE)
            }
            var weatherUiTestLauncherEnabled by remember {
                mutableStateOf(
                    BuildConfig.DEBUG && debugPreferences.getBoolean(
                        WEATHER_UI_TEST_LAUNCHER_ENABLED,
                        false,
                    ),
                )
            }
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissions ->
                locationPermissionGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                viewModel.onEvent(
                    if (locationPermissionGranted) {
                        HomeUiEvent.CurrentLocationRequested
                    } else {
                        HomeUiEvent.CurrentLocationPermissionDenied
                    },
                )
            }
            MinimalistWeatherTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onDarkStatusBarIconsChanged = ::updateSystemBars,
                    locationPermissionGranted = locationPermissionGranted,
                    onLocationPermissionRequest = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ),
                        )
                    },
                    debugToolsEnabled = BuildConfig.DEBUG,
                    weatherUiTestLauncherEnabled = weatherUiTestLauncherEnabled,
                    onWeatherUiTestLauncherEnabledChanged = { enabled ->
                        if (BuildConfig.DEBUG) {
                            weatherUiTestLauncherEnabled = enabled
                            debugPreferences.edit()
                                .putBoolean(WEATHER_UI_TEST_LAUNCHER_ENABLED, enabled)
                                .apply()
                        }
                    },
                )
            }
        }
    }

    private fun updateSystemBars(useDarkStatusBarIcons: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = if (useDarkStatusBarIcons) {
                SystemBarStyle.light(transparent, transparent)
            } else {
                SystemBarStyle.dark(transparent)
            },
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(31, 59, 91)),
        )
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val DEBUG_TOOLS_PREFERENCES = "debug_ui_tools"
        const val WEATHER_UI_TEST_LAUNCHER_ENABLED = "weather_ui_test_launcher_enabled"
    }
}
