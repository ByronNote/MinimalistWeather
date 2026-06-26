package cn.byronlab.weather.feature.home;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.byronlab.weather.base.BaseRecyclerViewAdapter;
import cn.byronlab.weather.R;
import cn.byronlab.weather.data.db.entities.minimalist.WeatherForecast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/6/23
 */
public class ForecastAdapter extends BaseRecyclerViewAdapter<ForecastAdapter.ViewHolder> {

    private final List<WeatherForecast> weatherForecasts;

    public ForecastAdapter(List<WeatherForecast> weatherForecasts) {
        this.weatherForecasts = weatherForecasts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(itemView, this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ForecastAdapter.ViewHolder holder, int position) {
        WeatherForecast weatherForecast = weatherForecasts.get(position);
        holder.weekTextView.setText(weatherForecast.getWeek());
        holder.dateTextView.setText(weatherForecast.getDate());
        holder.weatherIconImageView.setImageResource(R.mipmap.ic_launcher);
        holder.weatherTextView.setText(TextUtils.isEmpty(weatherForecast.getWeather()) ?
                (weatherForecast.getWeatherDay().equals(weatherForecast.getWeatherNight()) ?
                        weatherForecast.getWeatherDay() : weatherForecast.getWeatherDay() + "转" + weatherForecast.getWeatherNight())
                : weatherForecast.getWeather());
        holder.tempMaxTextView.setText(weatherForecast.getTempMax() + "°");
        holder.tempMinTextView.setText(weatherForecast.getTempMin() + "°");
    }

    @Override
    public int getItemCount() {
        return weatherForecasts == null ? 0 : weatherForecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.week_text_view)
        TextView weekTextView;
        @BindView(R.id.date_text_view)
        TextView dateTextView;
        @BindView(R.id.weather_icon_image_view)
        ImageView weatherIconImageView;
        @BindView(R.id.weather_text_view)
        TextView weatherTextView;
        @BindView(R.id.temp_max_text_view)
        TextView tempMaxTextView;
        @BindView(R.id.temp_min_text_view)
        TextView tempMinTextView;

        ViewHolder(View itemView, ForecastAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> adapter.onItemHolderClick(ViewHolder.this));
        }
    }
}
