package cn.byronlab.weather.feature.selectcity;

import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cn.byronlab.weather.base.BaseActivity;
import cn.byronlab.weather.library.util.ActivityUtils;
import cn.byronlab.weather.R;
import cn.byronlab.weather.WeatherApplication;
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com ==>> byronzhang.com)
 */
public class SelectCityActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    SelectCityFragment selectCityFragment;

    @Inject
    SelectCityPresenter selectCityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        selectCityFragment = (SelectCityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (selectCityFragment == null) {
            selectCityFragment = SelectCityFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), selectCityFragment, R.id.fragment_container);
        }

        DaggerSelectCityComponent.builder()
                .applicationComponent(WeatherApplication.getInstance().getApplicationComponent())
                .selectCityModule(new SelectCityModule(selectCityFragment))
                .build().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_city, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            RxSearchView.queryTextChanges(searchView)
                    .map(charSequence -> charSequence == null ? null : charSequence.toString().trim())
                    .throttleLast(100, TimeUnit.MILLISECONDS)
                    .debounce(100, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(searchText -> selectCityFragment.cityListAdapter.getFilter().filter(searchText));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
