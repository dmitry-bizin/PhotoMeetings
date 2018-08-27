package com.photomeetings.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.photomeetings.R;
import com.photomeetings.adapters.GridViewAdapter;
import com.photomeetings.adapters.PhotoPagerAdapter;
import com.photomeetings.model.vk.VKPhoto;
import com.photomeetings.services.SearchPhotosService;
import com.photomeetings.services.SettingsService;

import java.util.ArrayList;
import java.util.List;

public class GridViewFragment extends Fragment {

    private GridViewAdapter gridViewAdapter;
    private PhotoPagerAdapter photoPagerAdapter;
    private SearchPhotosService searchPhotosService;
    private SettingsFragment settingsFragment;
    private List<VKPhoto> vkPhotos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsFragment = new SettingsFragment();
        vkPhotos = new ArrayList<>();
        gridViewAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, vkPhotos);
        photoPagerAdapter = new PhotoPagerAdapter(getFragmentManager(), vkPhotos, searchPhotosService);
        searchPhotosService = new SearchPhotosService(SettingsService.getAddress(getContext()), SettingsService.getRadius(getContext()), gridViewAdapter);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View gridViewFragment = inflater.inflate(R.layout.grid_view_fragment, container, false);
        ProgressBar progressBarGridView = gridViewFragment.findViewById(R.id.progressBarGridView);
        TextView nothingFoundTextView = gridViewFragment.findViewById(R.id.nothingFoundTextView);
        TextView errorTextView = gridViewFragment.findViewById(R.id.errorTextView);
        searchPhotosService.setViews(progressBarGridView, nothingFoundTextView, errorTextView);
        prepareGridView(gridViewFragment);
        return gridViewFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareGridView(View gridViewFragment) {
        final GridView gridView = gridViewFragment.findViewById(R.id.gridView);
        gridView.setAdapter(gridViewAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = gridViewFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (!gridViewAdapter.isLoading()) {
                    searchPhotosService.update(SettingsService.getAddress(getContext()), SettingsService.getRadius(getContext()));
                    searchPhotosService.vkPhotos(null, swipeRefreshLayout, true);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

        });
        gridView.setOnScrollListener(new GridView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount
                        && !gridViewAdapter.isLoading()
                        && !gridViewAdapter.isAllDownloaded()) {
                    searchPhotosService.vkPhotos(null, null, false);
                }
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startViewPagerFragment(position);
            }
        });
        if (settingsFragment.isSettingsWasChanged()) {
            settingsFragment.setSettingsWasChanged(false);
            searchPhotosService.update(SettingsService.getAddress(getContext()), SettingsService.getRadius(getContext()));
            searchPhotosService.vkPhotos(null, null, true);
        }
    }

    private void startViewPagerFragment(int position) {
        photoPagerAdapter = new PhotoPagerAdapter(getFragmentManager(), vkPhotos, searchPhotosService);
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("photoPagerAdapter", photoPagerAdapter);
        viewPagerFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment, viewPagerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void startSettingsFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment, settingsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
