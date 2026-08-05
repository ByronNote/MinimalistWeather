package cn.byronlab.weather.presentation.weatherui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherCloudCover
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.tokens.WeatherVisualStyle

internal fun weatherConditionIcon(scene: WeatherSceneSpec): ImageVector {
    return when {
        scene.precipitation == WeatherPrecipitation.Snow ||
            scene.precipitation == WeatherPrecipitation.SnowGrains -> Icons.Default.AcUnit
        scene.precipitation != WeatherPrecipitation.None ||
            scene.lightningIntensity != WeatherLightningIntensity.None -> Icons.Default.WaterDrop
        scene.atmosphere == WeatherAtmosphere.Fog || scene.cloudCover != WeatherCloudCover.Clear -> Icons.Default.Cloud
        scene.skyPhase == WeatherSkyPhase.Night -> Icons.Default.DarkMode
        else -> Icons.Default.WbSunny
    }
}

internal fun weatherConditionIconTint(
    scene: WeatherSceneSpec,
    style: WeatherVisualStyle,
): Color {
    return when {
        scene.precipitation != WeatherPrecipitation.None ||
            scene.lightningIntensity != WeatherLightningIntensity.None -> style.coolAccent
        scene.atmosphere == WeatherAtmosphere.Fog || scene.cloudCover != WeatherCloudCover.Clear -> Color(0xFFB7C4D7)
        scene.skyPhase == WeatherSkyPhase.Night -> style.coolAccent
        else -> style.warmAccent
    }
}
