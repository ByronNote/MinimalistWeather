package cn.byronlab.weather.feature.selectcity;

import cn.byronlab.weather.di.component.ApplicationComponent;
import cn.byronlab.weather.di.scope.ActivityScoped;

import dagger.Component;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/11/30
 */
@ActivityScoped
@Component(modules = SelectCityModule.class, dependencies = ApplicationComponent.class)
public interface SelectCityComponent {

    void inject(SelectCityActivity selectCityActivity);
}
