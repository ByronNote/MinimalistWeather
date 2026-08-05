# MinimalistWeather v2 UI Design Specification

> A beautiful minimalist weather application built with Jetpack Compose.

Version: 2.0  
Platform: Android  
UI Framework: Jetpack Compose  
Design Language: Material 3 + Minimalism

---

# 1. Design Philosophy

MinimalistWeather 不追求成为功能堆叠型天气工具，而是打造一个：

> 简洁、优雅、现代的 Android 天气展示应用。

核心设计目标：

- 极简的信息层级
- 天气状态驱动 UI
- 动态天气视觉反馈
- Material 3 设计规范
- 展示 Jetpack Compose 能力

避免：

- 大量卡片堆叠
- 复杂生活指数
- 商业天气 App 的信息密度

---

# 2. Data Source

## Open-Meteo API

MinimalistWeather 使用 Open-Meteo 作为主要天气数据源。

数据流程：

```
User Location

    ↓

Latitude / Longitude

    ↓

Open-Meteo API

    ↓

Weather Model

    ↓

Compose UI
```

---

# 3. Overall UI Structure

应用采用三 Tab 结构：

```
Main Screen

├── Home
│
├── Cities
│
└── Settings
```

---

# 4. Home Screen

首页是核心体验。

整体结构：

```
┌──────────────────────┐
│ Location        ⚙️   │
│                      │
│                      │
│          ☀️          │
│                      │
│          28°         │
│                      │
│        Sunny         │
│                      │
│    Feels like 31°    │
│                      │
│──────────────────────│
│                      │
│ Today's Summary      │
│                      │
│ Hourly Forecast      │
│                      │
│ Weekly Forecast      │
│                      │
└──────────────────────┘
```

---

# 5. Weather Hero Section

## Purpose

展示当前天气状态。

对应 Open-Meteo:

```
current.temperature_2m

current.weather_code

current.wind_speed_10m

current.apparent_temperature
```

---

## UI

```
Shanghai
China


        ☀️


        28°


       Sunny


    Feels like 31°
```

---

## Typography

Temperature:

```
96sp
Bold
```

Weather description:

```
24sp
Medium
```

Location:

```
16sp
Regular
```

---

# 6. Dynamic Weather Background

根据 weather_code 动态改变背景。

---

## Clear Weather

Weather Code:

```
0
```

Background:

```
Sky Blue
+
Warm Gradient
```

Animation:

- Sun glow
- Slow light movement

---

## Cloudy Weather

Weather Code:

```
1 - 3
```

Background:

```
Soft Gray Blue
```

Animation:

- Moving clouds

---

## Rain Weather

Weather Code:

```
51 - 67
80 - 82
```

Background:

```
Dark Blue Gray
```

Animation:

- Rain particles

---

## Snow Weather

Weather Code:

```
71 - 77
85 - 86
```

Background:

```
Light Cold Gray
```

Animation:

- Snow particles

---

# 7. Today's Summary

## Data Source

Open-Meteo Daily API:

```
temperature_2m_max

temperature_2m_min

sunrise

sunset

precipitation_probability_max

uv_index_max
```

---

## UI

```
Today


☀ High       32°

☾ Low        25°


Sunrise      05:32

Sunset       18:45


Rain         20%

UV           6
```

---

# 8. Hourly Forecast

## Purpose

展示未来小时天气变化。

---

## Data Source

```
hourly.temperature_2m

hourly.weather_code

hourly.precipitation_probability

hourly.wind_speed
```

---

## UI

Horizontal LazyRow:

```
09:00

☀️

28°


12:00

☀️

31°


15:00

🌤

32°


18:00

🌙

29°
```

---

# 9. Temperature Chart

使用 Compose Canvas 绘制温度曲线。

效果类似：

```
32°

        ●

    ●       ●

●             ●


26°
```

技术：

```
Canvas

+

Path

+

Animation
```

---

# 10. Weekly Forecast

## Data Source

Open-Meteo Daily API:

```
daily.time

daily.weather_code

daily.temperature_2m_max

daily.temperature_2m_min
```

---

## UI

列表形式：

```
Today

☀ Sunny

32° ━━━━━ 25°



Monday

🌤 Cloudy

30° ━━━━ 24°



Tuesday

🌧 Rain

27° ━━━ 23°
```

---

## Temperature Range Bar

设计：

```
minimum temperature

        ━━━━━━━━━

maximum temperature
```

颜色根据温度动态变化。

---

# 11. Weather Details

默认折叠。

标题：

```
More Details
```

展开：

```
Wind

↗ 12 km/h


Humidity

65%


Pressure

1012 hPa


Visibility

10 km
```

---

## Data Source

```
relative_humidity_2m

surface_pressure

visibility

wind_speed_10m

wind_direction_10m
```

---

# 12. Cities Screen

用于管理收藏城市。

---

## UI

```
Cities


Shanghai

28°
Sunny


Tokyo

24°
Cloudy


New York

21°
Rain
```

---

## Data Model

```kotlin
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)
```

---

# 13. Settings Screen

功能保持简单。

```
Settings


Theme

System / Light / Dark


Temperature Unit

°C / °F


Wind Speed

km/h / mph


About

Version
```

---

# 14. Color System

## Light Theme

```
Background

#F8FAFC


Primary

#2563EB


Text

#0F172A
```

---

## Dark Theme

```
Background

#020617


Text

#F8FAFC
```

---

# 15. Material Design

使用：

- Material 3
- Dynamic Color
- Adaptive Layout
- Edge To Edge

---

# 16. Android Implementation

## Recommended Stack

```
Kotlin

Jetpack Compose

Material 3

MVVM

Clean Architecture

Coroutines

Flow

Hilt

Retrofit

Kotlin Serialization

Room

DataStore

WorkManager
```

---

# 17. Architecture Mapping

```
presentation

    ↓

domain

    ↓

data

    ↓

Open-Meteo API
```

---

# 18. UI Components

Components:

```
WeatherHero

TemperatureDisplay

HourlyForecast

TemperatureChart

DailyForecast

WeatherDetailCard

LocationHeader
```

---

# 19. Design Principles

## Do

✅ Large typography  
✅ Weather driven animation  
✅ Clean hierarchy  
✅ Native Android experience  
✅ Smooth Compose animation


## Don't

❌ Information overload  
❌ Too many cards  
❌ Advertisement style layout  
❌ Complex weather encyclopedia

---

# 20. Product Positioning

MinimalistWeather is not:

> Another weather application.

It is:

> A showcase of modern Android development practices through a beautiful minimalist weather experience.

```
Jetpack Compose
+
Clean Architecture
+
Beautiful UI
+
Open Source
```