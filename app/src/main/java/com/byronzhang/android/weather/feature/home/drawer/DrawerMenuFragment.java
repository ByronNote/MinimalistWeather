package com.byronzhang.android.weather.feature.home.drawer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.byronzhang.android.weather.base.BaseFragment;
import com.byronzhang.android.weather.R;
import com.byronzhang.android.weather.feature.selectcity.SelectCityActivity;
import com.byronzhang.android.weather.data.db.entities.minimalist.Weather;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DrawerMenuFragment extends BaseFragment implements DrawerContract.View {


    private static final String ARG_COLUMN_COUNT = "column-count";


    @BindView(R.id.add_city_btn)
    Button addCityButton;
    @BindView(R.id.rv_city_manager)
    RecyclerView cityManagerRecyclerView;

    private Unbinder unbinder;

    private int columnCount = 3;
    private List<Weather> weatherList;
    private CityManagerAdapter cityManagerAdapter;

    private DrawerContract.Presenter presenter;

    private OnSelectCity onSelectCity;

    public DrawerMenuFragment() {
    }

    public static DrawerMenuFragment newInstance(int columnCount) {
        DrawerMenuFragment fragment = new DrawerMenuFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectCity) {
            onSelectCity = (OnSelectCity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer_menu, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Context context = rootView.getContext();
        if (columnCount <= 1) {
            cityManagerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            cityManagerRecyclerView.setLayoutManager(new GridLayoutManager(context, columnCount));
        }
        cityManagerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        weatherList = new ArrayList<>();
        cityManagerAdapter = new CityManagerAdapter(weatherList);
        cityManagerAdapter.setOnItemClickListener(new CityManagerAdapter.OnCityManagerItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    presenter.saveCurrentCityToPreference(weatherList.get(position).getCityId());
                    onSelectCity.onSelect(weatherList.get(position).getCityId());
                } catch (InvalidClassException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDeleteClick(String cityId) {
                presenter.deleteCity(cityId);
            }
        });
        cityManagerRecyclerView.setAdapter(cityManagerAdapter);

        presenter.subscribe();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.unSubscribe();
    }

    @Override
    public void displaySavedCities(List<Weather> weatherList) {
        this.weatherList.clear();
        this.weatherList.addAll(weatherList);
        cityManagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(DrawerMenuPresenter presenter) {

        this.presenter = presenter;
    }


    @OnClick(R.id.add_city_btn)
    void onAddCityClick() {
        Intent intent = new Intent(getActivity(), SelectCityActivity.class);
        startActivity(intent);
    }


    public interface OnSelectCity {

        void onSelect(String cityId);
    }
}
