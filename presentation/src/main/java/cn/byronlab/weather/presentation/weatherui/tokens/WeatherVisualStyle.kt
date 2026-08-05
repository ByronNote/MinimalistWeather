package cn.byronlab.weather.presentation.weatherui.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cn.byronlab.weather.presentation.weatherui.model.WeatherAtmosphere
import cn.byronlab.weather.presentation.weatherui.model.WeatherIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherLightningIntensity
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitation
import cn.byronlab.weather.presentation.weatherui.model.WeatherPrecipitationPattern
import cn.byronlab.weather.presentation.weatherui.model.WeatherSceneSpec
import cn.byronlab.weather.presentation.weatherui.model.WeatherSkyPhase
import cn.byronlab.weather.presentation.weatherui.model.WeatherVisualType
import cn.byronlab.weather.presentation.weatherui.model.visualType

@Immutable
internal data class WeatherVisualStyle(
    val backgroundColors: List<Color>,
    val content: Color,
    val panelColor: Color,
    val panelContent: Color,
    val glassChipColor: Color,
    val accent: Color,
    val heroIcon: Color,
    val warmAccent: Color,
    val coolAccent: Color,
    val photoOverlayTop: Color,
    val photoOverlayBottom: Color,
    val photoOverlaySide: Color,
    val sunCore: Color,
    val sunGlow: Color,
)

internal fun weatherVisualStyle(scene: WeatherSceneSpec): WeatherVisualStyle {
    val type = scene.visualType
    val base = baseWeatherVisualStyle(
        type = type,
        isDaytime = scene.skyPhase == WeatherSkyPhase.Day,
    )
    return when {
        scene.atmosphere == WeatherAtmosphere.Fog -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF788B9B), Color(0xFFB4C0C9), Color(0xFFE5E9EC))
            } else {
                listOf(Color(0xFF283744), Color(0xFF526675), Color(0xFF8998A3))
            },
            panelColor = Color(0xFF4D6374).copy(alpha = 0.58f),
            glassChipColor = Color.White.copy(alpha = 0.17f),
            accent = Color(0xFFB7CAD7),
            heroIcon = Color(0xFFF0F4F6),
            photoOverlayTop = Color(0x596A7C89),
            photoOverlayBottom = Color(0xD54A6477),
            photoOverlaySide = Color(0x52657683),
        )
        scene.lightningIntensity != WeatherLightningIntensity.None -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF0B1424), Color(0xFF22344A), Color(0xFF718397))
            } else {
                listOf(Color(0xFF050A13), Color(0xFF111D2D), Color(0xFF394C60))
            },
            panelColor = Color(0xFF152C43).copy(alpha = 0.68f),
            glassChipColor = Color.White.copy(alpha = 0.15f),
            accent = Color(0xFF85C8F2),
            heroIcon = Color(0xFFFFED9A),
            photoOverlayTop = Color(0xA01B2A3D),
            photoOverlayBottom = Color(0xF00B2035),
            photoOverlaySide = Color(0x99203043),
        )
        scene.freezing || scene.precipitation == WeatherPrecipitation.Sleet -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF40566B), Color(0xFF7890A5), Color(0xFFD8E5EE))
            } else {
                listOf(Color(0xFF101D2B), Color(0xFF2B4256), Color(0xFF71899B))
            },
            panelColor = Color(0xFF29455D).copy(alpha = 0.62f),
            accent = Color(0xFF9ED9F7),
            heroIcon = Color(0xFFE9F7FF),
            photoOverlayTop = Color(0x80516477),
            photoOverlayBottom = Color(0xE3294962),
            photoOverlaySide = Color(0x76516477),
        )
        type == WeatherVisualType.Rain &&
            scene.precipitationPattern == WeatherPrecipitationPattern.Showers -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF294C68), Color(0xFF658DA6), Color(0xFFC7D6DE))
            } else {
                listOf(Color(0xFF0A1728), Color(0xFF20384B), Color(0xFF587286))
            },
            panelColor = Color(0xFF244A64).copy(alpha = 0.61f),
            photoOverlayTop = Color(0x7A38566C),
            photoOverlayBottom = Color(0xE51E425A),
            photoOverlaySide = Color(0x7038566C),
        )
        type == WeatherVisualType.Rain && scene.precipitationIntensity == WeatherIntensity.Light -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF456681), Color(0xFF7895AA), Color(0xFFC9D6DF))
            } else {
                listOf(Color(0xFF0E1C2B), Color(0xFF2E485C), Color(0xFF6C8292))
            },
            panelColor = Color(0xFF31556F).copy(alpha = 0.58f),
            photoOverlayTop = Color(0x70536B80),
            photoOverlayBottom = Color(0xDE315A75),
            photoOverlaySide = Color(0x66536B80),
        )
        type == WeatherVisualType.Rain && scene.precipitationIntensity == WeatherIntensity.Heavy -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF101A2C), Color(0xFF273D52), Color(0xFF8799A9))
            } else {
                listOf(Color(0xFF050B15), Color(0xFF142536), Color(0xFF485E70))
            },
            panelColor = Color(0xFF17334B).copy(alpha = 0.66f),
            photoOverlayTop = Color(0x94304255),
            photoOverlayBottom = Color(0xEF102A40),
            photoOverlaySide = Color(0x87304255),
        )
        type == WeatherVisualType.Snow && scene.precipitationIntensity == WeatherIntensity.Heavy -> base.copy(
            backgroundColors = if (scene.skyPhase == WeatherSkyPhase.Day) {
                listOf(Color(0xFF9FBBD5), Color(0xFFD7E8F5), Color(0xFFF6FAFC))
            } else {
                listOf(Color(0xFF253B51), Color(0xFF5F7C96), Color(0xFFB1C4D4))
            },
            panelColor = Color(0xFF496B88).copy(alpha = 0.58f),
            photoOverlayTop = Color(0x78829EB7),
            photoOverlayBottom = Color(0xDD3F6484),
            photoOverlaySide = Color(0x70829EB7),
        )
        else -> base
    }
}

private fun baseWeatherVisualStyle(
    type: WeatherVisualType,
    isDaytime: Boolean,
): WeatherVisualStyle {
    val daytime = when (type) {
        WeatherVisualType.Clear -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFF3EA2FF), Color(0xFF7CCBFF), Color(0xFFF8FAFC)),
            content = Color.White,
            panelColor = Color(0xFF356F9E).copy(alpha = 0.56f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.18f),
            accent = Color(0xFF83BFFF),
            heroIcon = Color(0xFFFFF1A8),
            warmAccent = Color(0xFFFF9F43),
            coolAccent = Color(0xFF4EA5FF),
            photoOverlayTop = Color(0x55325F8D),
            photoOverlayBottom = Color(0xE01B5684),
            photoOverlaySide = Color(0x5520527C),
            sunCore = Color(0xFFFFF2B8),
            sunGlow = Color(0xFFFFE19A),
        )
        WeatherVisualType.PartlyCloudy -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFF4F9DE0), Color(0xFF8BC7ED), Color(0xFFF8FAFC)),
            content = Color.White,
            panelColor = Color(0xFF356B96).copy(alpha = 0.57f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.18f),
            accent = Color(0xFF9BCBF2),
            heroIcon = Color(0xFFFFF0A8),
            warmAccent = Color(0xFFFFA145),
            coolAccent = Color(0xFF5EA9F3),
            photoOverlayTop = Color(0x55355779),
            photoOverlayBottom = Color(0xDF235D87),
            photoOverlaySide = Color(0x55355779),
            sunCore = Color(0xFFFFF2B8),
            sunGlow = Color(0xFFFFE19A),
        )
        WeatherVisualType.Cloudy -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFF8EA4BF), Color(0xFFDDE7F2), Color(0xFFF8FAFC)),
            content = Color.White,
            panelColor = Color(0xFF385A78).copy(alpha = 0.58f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.18f),
            accent = Color(0xFFA8C1DC),
            heroIcon = Color(0xFFF1F5F9),
            warmAccent = Color(0xFFFFA94D),
            coolAccent = Color(0xFF60A5FA),
            photoOverlayTop = Color(0x66354F68),
            photoOverlayBottom = Color(0xE02A4C68),
            photoOverlaySide = Color(0x66354F68),
            sunCore = Color(0xFFE6EEF7),
            sunGlow = Color(0xFFFFFFFF),
        )
        WeatherVisualType.Overcast -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFF66788B), Color(0xFFAAB8C5), Color(0xFFE5EBF0)),
            content = Color.White,
            panelColor = Color(0xFF344C62).copy(alpha = 0.64f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.14f),
            accent = Color(0xFF9FB1C2),
            heroIcon = Color(0xFFE5ECF2),
            warmAccent = Color(0xFFFFA65A),
            coolAccent = Color(0xFF79AEE0),
            photoOverlayTop = Color(0x803A4A59),
            photoOverlayBottom = Color(0xE62A4358),
            photoOverlaySide = Color(0x803A4A59),
            sunCore = Color(0xFFDDE6EE),
            sunGlow = Color(0xFFEAF2F8),
        )
        WeatherVisualType.Rain -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFF14213D), Color(0xFF334155), Color(0xFFCBD5E1)),
            content = Color(0xFFF8FAFC),
            panelColor = Color(0xFF1E3A55).copy(alpha = 0.62f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.18f),
            accent = Color(0xFF38BDF8),
            heroIcon = Color(0xFFBAE6FD),
            warmAccent = Color(0xFFFFB86B),
            coolAccent = Color(0xFF38BDF8),
            photoOverlayTop = Color(0x80334358),
            photoOverlayBottom = Color(0xEA152D44),
            photoOverlaySide = Color(0x80334358),
            sunCore = Color(0xFFBFE8FF),
            sunGlow = Color(0xFF7DD3FC),
        )
        WeatherVisualType.Snow -> WeatherVisualStyle(
            backgroundColors = listOf(Color(0xFFBCD6F2), Color(0xFFE8F4FF), Color(0xFFF8FAFC)),
            content = Color.White,
            panelColor = Color(0xFF52718F).copy(alpha = 0.52f),
            panelContent = Color.White,
            glassChipColor = Color.White.copy(alpha = 0.18f),
            accent = Color(0xFF38BDF8),
            heroIcon = Color.White,
            warmAccent = Color(0xFFFFB86B),
            coolAccent = Color(0xFF38BDF8),
            photoOverlayTop = Color(0x6689A6C2),
            photoOverlayBottom = Color(0xD84B6F91),
            photoOverlaySide = Color(0x6689A6C2),
            sunCore = Color(0xFFFFFFFF),
            sunGlow = Color(0xFFDFF6FF),
        )
    }
    if (isDaytime) {
        return daytime
    }
    return daytime.copy(
        backgroundColors = when (type) {
            WeatherVisualType.Clear -> listOf(Color(0xFF0B1935), Color(0xFF244E79), Color(0xFF6B94B7))
            WeatherVisualType.PartlyCloudy -> listOf(Color(0xFF101E38), Color(0xFF315879), Color(0xFF7797AE))
            WeatherVisualType.Cloudy -> listOf(Color(0xFF182536), Color(0xFF42576B), Color(0xFF8795A2))
            WeatherVisualType.Overcast -> listOf(Color(0xFF111A25), Color(0xFF344351), Color(0xFF687783))
            WeatherVisualType.Rain -> listOf(Color(0xFF07111F), Color(0xFF192B3C), Color(0xFF536979))
            WeatherVisualType.Snow -> listOf(Color(0xFF263B52), Color(0xFF58738D), Color(0xFFA5B7C7))
        },
        panelColor = when (type) {
            WeatherVisualType.Clear,
            WeatherVisualType.PartlyCloudy -> Color(0xFF173B60).copy(alpha = 0.64f)
            WeatherVisualType.Cloudy -> Color(0xFF253F56).copy(alpha = 0.66f)
            WeatherVisualType.Overcast -> Color(0xFF202F3D).copy(alpha = 0.70f)
            WeatherVisualType.Rain -> Color(0xFF102A3F).copy(alpha = 0.72f)
            WeatherVisualType.Snow -> Color(0xFF3B5872).copy(alpha = 0.64f)
        },
        glassChipColor = Color.White.copy(alpha = 0.13f),
        accent = when (type) {
            WeatherVisualType.Clear,
            WeatherVisualType.PartlyCloudy -> Color(0xFF96C8FA)
            WeatherVisualType.Cloudy,
            WeatherVisualType.Overcast -> Color(0xFFA7BACB)
            WeatherVisualType.Rain -> Color(0xFF6DCBF4)
            WeatherVisualType.Snow -> Color(0xFFCDEBFA)
        },
        heroIcon = if (type == WeatherVisualType.Rain) Color(0xFFA9DDF5) else Color(0xFFEAF3FF),
        photoOverlayTop = Color(0x7A1E3347),
        photoOverlayBottom = Color(0xEA102B41),
        photoOverlaySide = Color(0x76203950),
        sunCore = Color(0xFFF7F2E4),
        sunGlow = Color(0xFFDCEBFF),
    )
}
