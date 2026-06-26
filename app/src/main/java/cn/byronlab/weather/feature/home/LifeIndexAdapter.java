package cn.byronlab.weather.feature.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.byronlab.weather.base.BaseRecyclerViewAdapter;
import cn.byronlab.weather.R;
import cn.byronlab.weather.data.db.entities.minimalist.LifeIndex;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static cn.byronlab.weather.R.drawable.ic_index_sunscreen;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/12/13
 */
public class LifeIndexAdapter extends BaseRecyclerViewAdapter<LifeIndexAdapter.ViewHolder> {

    private Context context;
    private List<LifeIndex> indexList;

    public LifeIndexAdapter(Context context, List<LifeIndex> indexList) {
        this.context = context;
        this.indexList = indexList;
    }

    @Override
    public LifeIndexAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_life_index, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(LifeIndexAdapter.ViewHolder holder, int position) {
        LifeIndex index = indexList.get(position);
        holder.indexIconImageView.setImageDrawable(getIndexDrawable(context, index.getName()));
        holder.indexLevelTextView.setText(index.getIndex());
        holder.indexNameTextView.setText(index.getName());
    }

    @Override
    public int getItemCount() {
        return indexList == null ? 0 : indexList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.index_icon_image_view)
        ImageView indexIconImageView;
        @BindView(R.id.index_level_text_view)
        TextView indexLevelTextView;
        @BindView(R.id.index_name_text_view)
        TextView indexNameTextView;

        ViewHolder(View itemView, LifeIndexAdapter adapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> adapter.onItemHolderClick(LifeIndexAdapter.ViewHolder.this));
        }
    }

    private Drawable getIndexDrawable(Context context, String indexName) {


        int colorResourceId = ic_index_sunscreen;
        if (indexName.contains("防晒")) {
            colorResourceId = ic_index_sunscreen;
        } else if (indexName.contains("穿衣")) {
            colorResourceId = R.drawable.ic_index_dress;
        } else if (indexName.contains("运动")) {
            colorResourceId = R.drawable.ic_index_sport;
        } else if (indexName.contains("逛街")) {
            colorResourceId = R.drawable.ic_index_shopping;
        } else if (indexName.contains("晾晒")) {
            colorResourceId = R.drawable.ic_index_sun_cure;
        } else if (indexName.contains("洗车")) {
            colorResourceId = R.drawable.ic_index_car_wash;
        } else if (indexName.contains("感冒")) {
            colorResourceId = R.drawable.ic_index_clod;
        } else if (indexName.contains("广场舞")) {
            colorResourceId = R.drawable.ic_index_dance;
        }
        return context.getResources().getDrawable(colorResourceId);
    }
}
