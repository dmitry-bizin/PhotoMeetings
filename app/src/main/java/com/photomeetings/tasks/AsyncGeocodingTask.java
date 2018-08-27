package com.photomeetings.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.photomeetings.model.Point;
import com.photomeetings.services.GeoService;

import java.util.List;

import static com.photomeetings.services.SettingsService.ADDRESS;
import static com.photomeetings.services.SettingsService.DEFAULT_ADDRESS;
import static com.photomeetings.services.SettingsService.DEFAULT_LAT;
import static com.photomeetings.services.SettingsService.DEFAULT_LNG;
import static com.photomeetings.services.SettingsService.LAT;
import static com.photomeetings.services.SettingsService.LNG;

public class AsyncGeocodingTask extends AsyncTask<Void, Void, List<Point>> {

    private String address;
    private SharedPreferences sharedPreferences;

    public AsyncGeocodingTask(String address, SharedPreferences sharedPreferences) {
        this.address = address;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected void onPostExecute(List<Point> points) {
        Point point;
        if (!points.isEmpty()) {
            point = points.get(0);
        } else {
            point = new Point(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ADDRESS);
        }
        sharedPreferences.edit().putString(ADDRESS, point.getAddress()).apply();
        sharedPreferences.edit().putFloat(LAT, point.getLat()).apply();
        sharedPreferences.edit().putFloat(LNG, point.getLng()).apply();
    }

    @Override
    protected List<Point> doInBackground(Void... params) {
        return GeoService.geocoding(address);
    }

}
