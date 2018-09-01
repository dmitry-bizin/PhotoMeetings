package com.photomeetings.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.photomeetings.model.Point;
import com.photomeetings.services.GeoService;
import com.photomeetings.services.SettingsService;

import java.util.List;

public class AsyncGeocodingTask extends AsyncTask<Void, Void, List<Point>> {

    private String address;
    private Context context;

    public AsyncGeocodingTask(String address, Context context) {
        this.address = address;
        this.context = context;
    }

    @Override
    protected void onPostExecute(List<Point> points) {
        SettingsService.saveFullAddress(points.isEmpty() ? null : points.get(0), context);
    }

    @Override
    protected List<Point> doInBackground(Void... params) {
        return GeoService.geocoding(address);
    }

}
