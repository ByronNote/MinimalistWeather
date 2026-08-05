package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.model.WeatherRefreshInterval

interface WeatherRefreshScheduler {

    fun schedule(interval: WeatherRefreshInterval)
}
