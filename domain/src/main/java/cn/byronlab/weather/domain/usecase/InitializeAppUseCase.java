package cn.byronlab.weather.domain.usecase;

import cn.byronlab.weather.domain.model.Unit;
import cn.byronlab.weather.domain.repository.AppStartupRepository;
import cn.byronlab.weather.domain.result.DomainResult;

public final class InitializeAppUseCase {

    private final AppStartupRepository repository;

    public InitializeAppUseCase(AppStartupRepository repository) {
        this.repository = repository;
    }

    public DomainResult<Unit> execute() {
        return repository.initialize();
    }
}
