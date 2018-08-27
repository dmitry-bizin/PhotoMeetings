package com.photomeetings.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.photomeetings.R;
import com.photomeetings.adapters.PhotoPagerAdapter;

public class ViewPagerFragment extends Fragment {

    private int position;
    private PhotoPagerAdapter photoPagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        position = args.getInt("position");
        photoPagerAdapter = (PhotoPagerAdapter) args.getSerializable("photoPagerAdapter");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_layout, container, false);
        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(photoPagerAdapter);
        viewPager.setCurrentItem(position);
        //viewPager.setPageTransformer(false, new FadePageTransformer());
        viewPager.setPageMargin(30);
        return view;
    }

}
