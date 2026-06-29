package cn.byronlab.weather.feature.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import cn.byronlab.weather.base.BaseFragment;
import cn.byronlab.weather.R;
import cn.byronlab.weather.data.db.entities.minimalist.AirQualityLive;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;
import cn.byronlab.weather.data.db.entities.minimalist.LifeIndex;
import cn.byronlab.weather.data.db.entities.minimalist.Weather;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherLive;
import cn.byronlab.weather.data.WeatherDetail;
import cn.byronlab.weather.widget.IndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomePageFragment extends BaseFragment implements HomePageContract.View {

    //AQI
    @BindView(R.id.tv_aqi)
    TextView aqiTextView;
    @BindView(R.id.tv_quality)
    TextView qualityTextView;
    @BindView(R.id.indicator_view_aqi)
    IndicatorView aqiIndicatorView;
    @BindView(R.id.tv_advice)
    TextView adviceTextView;
    @BindView(R.id.tv_city_rank)
    TextView cityRankTextView;

    //详细天气信息
    @BindView(R.id.detail_recycler_view)
    RecyclerView detailRecyclerView;

    //预报
    @BindView(R.id.forecast_recycler_view)
    RecyclerView forecastRecyclerView;

    //生活指数
    @BindView(R.id.life_index_recycler_view)
    RecyclerView lifeIndexRecyclerView;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private Unbinder unbinder;

    private Weather weather;

    private List<WeatherDetail> weatherDetails;
    private List<WeatherForecast> weatherForecasts;
    private List<LifeIndex> lifeIndices;

    private DetailAdapter detailAdapter;
    private ForecastAdapter forecastAdapter;
    private LifeIndexAdapter lifeIndexAdapter;

    private HomePageContract.Presenter presenter;

    public HomePageFragment() {

    }

    public static HomePageFragment newInstance() {

        return new HomePageFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        //天气详情
        detailRecyclerView.setNestedScrollingEnabled(false);
        detailRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        weatherDetails = new ArrayList<>();
        detailAdapter = new DetailAdapter(weatherDetails);
        detailAdapter.setOnItemClickListener((adapterView, view, i, l) -> {
        });
        forecastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        detailRecyclerView.setAdapter(detailAdapter);

        //天气预报
        forecastRecyclerView.setNestedScrollingEnabled(false);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        weatherForecasts = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(weatherForecasts);
        forecastAdapter.setOnItemClickListener((adapterView, view, i, l) -> {
        });
        forecastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        forecastRecyclerView.setAdapter(forecastAdapter);

        //生活指数
        lifeIndexRecyclerView.setNestedScrollingEnabled(false);
        lifeIndexRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        lifeIndices = new ArrayList<>();
        lifeIndexAdapter = new LifeIndexAdapter(getActivity(), lifeIndices);
        lifeIndexAdapter.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(HomePageFragment.this.getContext(), lifeIndices.get(i).getDetails(), Toast.LENGTH_LONG).show());
        lifeIndexRecyclerView.setItemAnimator(new DefaultItemAnimator());
        lifeIndexRecyclerView.setAdapter(lifeIndexAdapter);

        aqiIndicatorView.setIndicatorValueChangeListener((currentIndicatorValue, stateDescription, indicatorTextColor) -> {
            aqiTextView.setText(String.valueOf(currentIndicatorValue));
            AirQualityLive airQualityLive = weather == null ? null : weather.getAirQualityLive();
            if (airQualityLive == null || TextUtils.isEmpty(airQualityLive.getQuality())) {
                qualityTextView.setText(stateDescription);
            } else {
                qualityTextView.setText(airQualityLive.getQuality());
            }
            aqiTextView.setTextColor(indicatorTextColor);
            qualityTextView.setTextColor(indicatorTextColor);
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        assert presenter != null;
        presenter.subscribe();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void displayWeatherInformation(Weather weather) {

        this.weather = weather;
        onFragmentInteractionListener.updatePageTitle(weather);

        AirQualityLive airQualityLive = weather.getAirQualityLive();
        aqiIndicatorView.setIndicatorValue(airQualityLive == null ? 0 : airQualityLive.getAqi());
        adviceTextView.setText(airQualityLive == null ? "" : airQualityLive.getAdvice());
        String rank = airQualityLive == null ? "" : airQualityLive.getCityRank();
        cityRankTextView.setText(TextUtils.isEmpty(rank) ? "首要污染物: " + (airQualityLive == null ? "" : airQualityLive.getPrimary()) : rank);

        weatherDetails.clear();
        weatherDetails.addAll(createDetails(weather));
        detailAdapter.notifyDataSetChanged();

        weatherForecasts.clear();
        if (weather.getWeatherForecasts() != null) {
            weatherForecasts.addAll(weather.getWeatherForecasts());
        }
        forecastAdapter.notifyDataSetChanged();

        lifeIndices.clear();
        if (weather.getLifeIndexes() != null) {
            lifeIndices.addAll(weather.getLifeIndexes());
        }
        lifeIndexAdapter.notifyDataSetChanged();

        onFragmentInteractionListener.addOrUpdateCityListInDrawerMenu(weather);
    }

    private List<WeatherDetail> createDetails(Weather weather) {

        List<WeatherDetail> details = new ArrayList<>();
        WeatherLive weatherLive = weather.getWeatherLive();
        WeatherForecast forecast = firstForecast(weather);
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "体感温度", safeString(weatherLive == null ? null : weatherLive.getFeelsTemperature()) + "°C"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "湿度", safeString(weatherLive == null ? null : weatherLive.getHumidity()) + "%"));
//        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "气压", (int) Double.parseDouble(weather.getWeatherLive().getAirPressure()) + "hPa"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "紫外线指数", safeString(forecast == null ? null : forecast.getUv())));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "降水量", safeString(weatherLive == null ? null : weatherLive.getRain()) + "mm"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "降水概率", safeString(forecast == null ? null : forecast.getPop()) + "%"));
        details.add(new WeatherDetail(R.drawable.ic_index_sunscreen, "能见度", safeString(forecast == null ? null : forecast.getVisibility()) + "km"));
        return details;
    }

    private WeatherForecast firstForecast(Weather weather) {
        if (weather == null || weather.getWeatherForecasts() == null || weather.getWeatherForecasts().isEmpty()) {
            return null;
        }
        return weather.getWeatherForecasts().get(0);
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void setPresenter(HomePageContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoadError(String message) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onWeatherLoadFailed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unSubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnFragmentInteractionListener {
        void updatePageTitle(Weather weather);

        /**
         * 更新完天气数据同时需要刷新侧边栏的已添加的城市列表
         *
         * @param weather 天气数据
         */
        void addOrUpdateCityListInDrawerMenu(Weather weather);

        void onWeatherLoadFailed();
    }
}
