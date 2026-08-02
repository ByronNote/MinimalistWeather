package cn.byronlab.weather.domain.repository;

import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.result.DomainResult;

public interface AppStartupRepository {

    DomainResult<Unit> initialize();
}
