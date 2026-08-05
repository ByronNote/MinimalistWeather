package cn.byronlab.weather.domain.repository

import cn.byronlab.weather.domain.result.DomainResult

interface AppStartupRepository {

    suspend fun initialize(): DomainResult<Unit>
}
