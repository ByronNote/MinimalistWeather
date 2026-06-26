package cn.byronlab.weather.widget;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 */
public interface IndicatorValueChangeListener {

    void onChange(int currentIndicatorValue, String stateDescription, int indicatorTextColor);
}
