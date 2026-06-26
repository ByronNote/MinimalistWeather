package cn.byronlab.weather.base;

/**
 * presenter interface,所有Presenter必须实现此接口
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 */
public interface BasePresenter {

    void subscribe();

    void unSubscribe();
}
