package com.photomeetings.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.photomeetings.fragments.DetailsFragment;
import com.photomeetings.model.vk.VKPhoto;
import com.photomeetings.services.SearchPhotosService;

import java.io.Serializable;
import java.util.List;

public class PhotoPagerAdapter extends FragmentStatePagerAdapter implements Serializable {

    private List<VKPhoto> vkPhotos;
    private SearchPhotosService searchPhotosService;

    public PhotoPagerAdapter(FragmentManager fragmentManager, List<VKPhoto> vkPhotos,
                             SearchPhotosService searchPhotosService) {
        super(fragmentManager);
        this.vkPhotos = vkPhotos;
        this.searchPhotosService = searchPhotosService;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == getCount() - 1) {
            searchPhotosService.vkPhotos(this, null);
        }
        DetailsFragment detailsFragment = new DetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("vkPhoto", vkPhotos.get(position));
        detailsFragment.setArguments(bundle);
        return detailsFragment;
    }

    @Override
    public int getCount() {
        return vkPhotos.size();
    }

}
