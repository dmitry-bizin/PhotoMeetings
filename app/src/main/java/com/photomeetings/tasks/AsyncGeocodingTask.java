package com.photomeetings.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.model.Point;
import com.photomeetings.services.GeoService;
import com.photomeetings.services.SettingsService;

import java.io.IOException;
import java.util.List;

public class AsyncGeocodingTask extends AsyncTask<Void, Void, List<Point>> {

    private String address;
    private Context context;

    public AsyncGeocodingTask(String address, Context context) {
        this.address = address;
        this.context = context;
    }

    @Override
    protected void onPostExecute(@Nullable List<Point> points) {
        if (points == null) {
            Toast.makeText(context, R.string.geo_network_error, Toast.LENGTH_LONG).show();
        } else {
            SettingsService.saveFullAddress(points.isEmpty() ? null : points.get(0), context);
        }
    }

    @Override
    protected List<Point> doInBackground(Void... params) {
        List<Point> points;
        try {
            points = GeoService.geocoding(address);
        } catch (IOException e) {
            points = null;
        }
        return points;
    }

}
