package com.photomeetings.tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.model.vk.VKPhoto;
import com.photomeetings.services.GeoService;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

public class AsyncReverseGeocodingTask extends AsyncTask<Void, Void, String> {

    private TextView addressTextView;
    private VKPhoto vkPhoto;

    public AsyncReverseGeocodingTask(TextView addressTextView, VKPhoto vkPhoto) {
        this.addressTextView = addressTextView;
        this.vkPhoto = vkPhoto;
    }

    @Override
    protected void onPostExecute(@Nullable String address) {
        if (address == null) {
            Toast.makeText(addressTextView.getContext(), R.string.geo_network_error, Toast.LENGTH_LONG).show();
        } else {
            vkPhoto.setAddress(address);
            addressTextView.append(address);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", vkPhoto.getLat(), vkPhoto.getLng());
            Intent test = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (addressTextView.getContext().getPackageManager().resolveActivity(test, 0) != null) {
                Pattern pattern = Pattern.compile(".*", Pattern.DOTALL);
                Linkify.addLinks(addressTextView, pattern,
                        String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f", vkPhoto.getLat(), vkPhoto.getLng()));
            }
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result;
        try {
            result = GeoService.reverseGeocoding(vkPhoto.getLat(), vkPhoto.getLng());
        } catch (IOException e) {
            result = null;
        }
        return result;
    }

}
