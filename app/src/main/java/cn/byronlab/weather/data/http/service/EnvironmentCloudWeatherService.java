package cn.byronlab.weather.data.http.service;

import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudCityAirLive;
import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudForecast;
import cn.byronlab.weather.data.http.entity.envicloud.EnvironmentCloudWeatherLive;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/2/16
 */
public interface EnvironmentCloudWeatherService {

    /**
     * 获取指定城市的实时天气
     * <p>
     * API地址：http://service.envicloud.cn:8082/v2/weatherlive/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/101020100
     *
     * @param cityId 城市id
     * @return Observable
     */
    @GET("/v2/weatherlive/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/{cityId}")
    Observable<EnvironmentCloudWeatherLive> getWeatherLive(@Path("cityId") String cityId);

    /**
     * 获取指定城市7日天气预报
     * <p>
     * API地址：http://service.envicloud.cn:8082/v2/weatherforecast/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/101020100
     *
     * @param cityId 城市id
     * @return Observable
     */
    @GET("/v2/weatherforecast/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/{cityId}")
    Observable<EnvironmentCloudForecast> getWeatherForecast(@Path("cityId") String cityId);

    /**
     * 获取指定城市的实时空气质量
     * <p>
     * API地址：http://service.envicloud.cn:8082/v2/cityairlive/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/101020100
     *
     * @param cityId 城市id
     * @return Observable
     */
    @GET("/v2/cityairlive/MTG2MJE1OTI0MZIXNZCWMTYZMDGZOTYW/{cityId}")
    Observable<EnvironmentCloudCityAirLive> getAirLive(@Path("cityId") String cityId);
}
