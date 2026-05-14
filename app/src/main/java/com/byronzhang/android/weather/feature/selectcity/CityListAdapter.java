package com.byronzhang.android.weather.feature.selectcity;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.byronzhang.android.weather.base.BaseRecyclerViewAdapter;
import com.byronzhang.android.weather.R;
import com.byronzhang.android.weather.data.db.entities.City;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/3/16
 */
public class CityListAdapter extends BaseRecyclerViewAdapter<CityListAdapter.ViewHolder> implements Filterable {

    private List<City> cities;
    public List<City> mFilterData;//过滤后的数据

    private RecyclerViewFilter filter;

    public CityListAdapter(List<City> cities) {
        this.cities = cities;
        mFilterData = cities;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        City city = mFilterData.get(position);
        String cityName = city.getCityName();
        String parentName = city.getParent();
        if (!cityName.equals(parentName)) {
            cityName = parentName + "." + cityName;
        }
        holder.cityNameTextView.setText(cityName);
    }

    @Override
    public int getItemCount() {
        return mFilterData == null ? 0 : mFilterData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.city_name_text_view)
        TextView cityNameTextView;

        ViewHolder(View itemView, CityListAdapter cityListAdapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> cityListAdapter.onItemHolderClick(this));
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new RecyclerViewFilter();
        }
        return filter;
    }

    private class RecyclerViewFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            //返回的results即为过滤后的ArrayList<City>
            FilterResults results = new FilterResults();

            //无约束字符串则返回null
            if (charSequence == null || charSequence.length() == 0) {
                results.values = null;
                results.count = 0;
            } else {
                String prefixString = charSequence.toString().toLowerCase();
                //新建Values存放过滤后的数据
                ArrayList<City> newValues = new ArrayList<>();
                Stream.of(cities)
                        .filter(city -> (city.getCityName().contains(prefixString)
                                || city.getCityNameEn().contains(prefixString) || city.getParent().contains(prefixString)
                                || city.getRoot().contains(prefixString)))
                        .forEach(newValues::add);
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mFilterData = (List<City>) filterResults.values;
            if (filterResults.count > 0) {
                notifyDataSetChanged();//重绘当前可见区域
            } else {
                mFilterData = cities;
                notifyDataSetChanged();//会重绘控件（还原到初始状态）
            }
        }
    }
}
