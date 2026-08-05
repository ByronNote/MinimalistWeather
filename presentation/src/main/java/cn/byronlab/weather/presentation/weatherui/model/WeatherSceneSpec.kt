package cn.byronlab.weather.presentation.weatherui.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeatherSceneSpec(
    val skyPhase: WeatherSkyPhase = WeatherSkyPhase.Day,
    val cloudCover: WeatherCloudCover = WeatherCloudCover.Clear,
    val precipitation: WeatherPrecipitation = WeatherPrecipitation.None,
    val precipitationIntensity: WeatherIntensity = WeatherIntensity.None,
    val precipitationPattern: WeatherPrecipitationPattern = WeatherPrecipitationPattern.Steady,
    val freezing: Boolean = false,
    val atmosphere: WeatherAtmosphere = WeatherAtmosphere.Clear,
    val lightningIntensity: WeatherLightningIntensity = WeatherLightningIntensity.None,
    val windLevel: WeatherWindLevel = WeatherWindLevel.Calm,
)

enum class WeatherSkyPhase {
    Day,
    Night,
}

enum class WeatherCloudCover {
    Clear,
    MostlyClear,
    PartlyCloudy,
    Cloudy,
    Overcast,
}

enum class WeatherPrecipitation {
    None,
    Drizzle,
    Rain,
    Snow,
    SnowGrains,
    Sleet,
    Hail,
}

enum class WeatherIntensity {
    None,
    Light,
    Moderate,
    Heavy,
}

enum class WeatherPrecipitationPattern {
    Steady,
    Showers,
    Flurries,
}

enum class WeatherLightningIntensity {
    None,
    Occasional,
    Frequent,
}

enum class WeatherWindLevel {
    Calm,
    Breezy,
    Windy,
}

enum class WeatherAtmosphere {
    Clear,
    Fog,
}

internal enum class WeatherVisualType {
    Clear,
    PartlyCloudy,
    Cloudy,
    Overcast,
    Rain,
    Snow,
}

internal val WeatherSceneSpec.visualType: WeatherVisualType
    get() = when (precipitation) {
        WeatherPrecipitation.Snow,
        WeatherPrecipitation.SnowGrains -> WeatherVisualType.Snow
        WeatherPrecipitation.Drizzle,
        WeatherPrecipitation.Rain,
        WeatherPrecipitation.Sleet,
        WeatherPrecipitation.Hail -> WeatherVisualType.Rain
        WeatherPrecipitation.None -> when (cloudCover) {
            WeatherCloudCover.Clear -> WeatherVisualType.Clear
            WeatherCloudCover.MostlyClear,
            WeatherCloudCover.PartlyCloudy -> WeatherVisualType.PartlyCloudy
            WeatherCloudCover.Cloudy -> WeatherVisualType.Cloudy
            WeatherCloudCover.Overcast -> WeatherVisualType.Overcast
        }
    }
