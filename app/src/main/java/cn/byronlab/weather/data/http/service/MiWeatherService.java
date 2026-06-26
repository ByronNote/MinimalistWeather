package cn.byronlab.weather.data.http.service;

import cn.byronlab.weather.data.http.entity.mi.MiWeather;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public interface MiWeatherService {

    /**
     * http://weatherapi.market.xiaomi.com/wtr-v2/weather?cityId=101010100
     *
     * @param cityId 城市ID
     * @return 天气数据
     */
    @GET("weather")
    Observable<MiWeather> getMiWeather(@Query("cityId") String cityId);

    /**
     * https://weatherapi.market.xiaomi.com/wtr-v3/weather/all?latitude=0&longitude=0&locationKey=weathercn%3A101010100&days=15&appKey=weather20151024&sign=zUFJoAR2ZVrDy1vF3D07&isGlobal=false&locale=zh_cn
     *
     * @param cityId 城市ID
     * @return 天气数据
     */
    @GET("weather/all?latitude=0&longitude=0&locationKey=weathercn:{cityId}&days={days}&appKey=weather20151024&sign=zUFJoAR2ZVrDy1vF3D07&isGlobal=false&locale=zh_cn")
    Observable<MiWeather> getWeather(@Path("cityId") String cityId, @Path("days") int days);

}
