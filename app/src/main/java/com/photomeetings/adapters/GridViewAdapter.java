package com.photomeetings.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.photomeetings.R;
import com.photomeetings.model.vk.VKPhoto;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GridViewAdapter extends ArrayAdapter<VKPhoto> implements Serializable {

    private Context context;
    private int layoutResourceId;
    private boolean isLoading = false;
    private boolean isAllDownloaded = false;

    public GridViewAdapter(Context context, int layoutResourceId, List<VKPhoto> vkPhotos) {
        super(context, layoutResourceId, vkPhotos);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View resultView = convertView;
        MiniatureView miniatureView;
        if (resultView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            resultView = inflater.inflate(layoutResourceId, parent, false);
            miniatureView = new MiniatureView();
            miniatureView.titleTextView = resultView.findViewById(R.id.gridItemTitle);
            miniatureView.imageView = resultView.findViewById(R.id.gridItemImage);
            resultView.setTag(miniatureView);
        } else {
            miniatureView = (MiniatureView) resultView.getTag();
        }
        VKPhoto vkPhoto = getItem(position);
        if (vkPhoto != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMMM yyyy, HH:mm:ss", Locale.getDefault());
            miniatureView.titleTextView.setText(simpleDateFormat.format(new Date(vkPhoto.getDate() * 1_000)));
            Picasso.get().load(vkPhoto.getPhotoForMiniature()).into(miniatureView.imageView);
        }
        return resultView;
    }

    public boolean isAllDownloaded() {
        return isAllDownloaded;
    }

    public void setAllDownloaded(boolean allDownloaded) {
        isAllDownloaded = allDownloaded;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    static class MiniatureView {
        TextView titleTextView;
        ImageView imageView;
    }

}
