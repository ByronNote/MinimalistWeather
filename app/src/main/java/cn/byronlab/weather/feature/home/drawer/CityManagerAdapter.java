package cn.byronlab.weather.feature.home.drawer;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import cn.byronlab.weather.base.BaseRecyclerViewAdapter;
import cn.byronlab.weather.library.util.DateConvertUtils;
import cn.byronlab.weather.R;
import cn.byronlab.weather.data.db.entities.minimalist.Weather;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 城市管理页面Adapter
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/3/16
 */
public class CityManagerAdapter extends BaseRecyclerViewAdapter<CityManagerAdapter.ViewHolder> {

    private final List<Weather> weatherList;

    public CityManagerAdapter(List<Weather> weatherList) {
        this.weatherList = weatherList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_manager, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Weather weather = weatherList.get(position);
        holder.city.setText(weather.getCityName());
        holder.weather.setText(weather.getWeatherLive().getWeather());
        holder.temp.setText(new StringBuilder().append(weather.getWeatherForecasts().get(0).getTempMin()).append("~").append(weather.getWeatherForecasts().get(0).getTempMax()).append("℃").toString());
        holder.publishTime.setText("发布于 " + DateConvertUtils.timeStampToDate(weather.getWeatherLive().getTime(), DateConvertUtils.DATA_FORMAT_PATTEN_YYYY_MM_DD_HH_MM));
        holder.deleteButton.setOnClickListener(v -> {
            Weather removeWeather = weatherList.get(holder.getAdapterPosition());
            weatherList.remove(removeWeather);
            notifyItemRemoved(holder.getAdapterPosition());

            if (onItemClickListener != null && onItemClickListener instanceof OnCityManagerItemClickListener) {
                ((OnCityManagerItemClickListener) onItemClickListener).onDeleteClick(removeWeather.getCityId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return weatherList == null ? 0 : weatherList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_delete)
        ImageButton deleteButton;
        @BindView(R.id.item_tv_city)
        TextView city;
        @BindView(R.id.item_tv_publish_time)
        TextView publishTime;
        @BindView(R.id.item_tv_weather)
        TextView weather;
        @BindView(R.id.item_tv_temp)
        TextView temp;

        ViewHolder(View itemView, CityManagerAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> adapter.onItemHolderClick(ViewHolder.this));
        }
    }

    public interface OnCityManagerItemClickListener extends AdapterView.OnItemClickListener {

        void onDeleteClick(String cityId);
    }

}
