package cn.byronlab.weather.data.db.dao;

import android.content.Context;

import cn.byronlab.weather.data.db.entities.City;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import cn.byronlab.weather.data.db.CityDatabaseHelper;
import cn.byronlab.weather.data.db.entities.HotCity;

/**
 * City表操作类
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/3/13
 */
public class CityDao {

    private Dao<City, Integer> cityDaoOperation;
    private Dao<HotCity, Integer> hotCityDaoOperation;

    @Inject
    public CityDao(Context context) {

        this.cityDaoOperation = CityDatabaseHelper.getInstance(context).getCityDao(City.class);
        this.hotCityDaoOperation = CityDatabaseHelper.getInstance(context).getCityDao(HotCity.class);
    }

    /**
     * 查询表中的所有城市
     *
     * @return 城市列表数据
     */
    public List<City> queryCityList() {

        try {
            return cityDaoOperation.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据城市查询城市信息
     *
     * @param cityId 城市ID
     * @return city
     * @throws SQLException
     */
    public City queryCityById(String cityId) throws SQLException {

        QueryBuilder<City, Integer> queryBuilder = cityDaoOperation.queryBuilder();
        queryBuilder.where().eq(City.CITY_ID_FIELD_NAME, cityId);

        return queryBuilder.queryForFirst();
    }

    /**
     * 查询所有热门城市
     *
     * @return 热门城市列表
     */
    public List<HotCity> queryAllHotCity() {
        try {
            return hotCityDaoOperation.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
