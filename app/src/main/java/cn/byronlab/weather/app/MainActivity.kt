package cn.byronlab.weather.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import cn.byronlab.weather.presentation.home.HomeScreen
import cn.byronlab.weather.presentation.home.HomeViewModel
import cn.byronlab.weather.presentation.theme.MinimalistWeatherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinimalistWeatherTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
