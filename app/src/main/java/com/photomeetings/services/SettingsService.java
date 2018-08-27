package com.photomeetings.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.photomeetings.model.Point;

public abstract class SettingsService {

    public static final String SETTINGS = "settings";
    public static final String ADDRESS = "address";
    public static final String RADIUS = "radius";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String DEFAULT_RADIUS = "100";
    public static final float DEFAULT_LAT = 0;
    public static final float DEFAULT_LNG = 0;
    public static final String DEFAULT_ADDRESS = "";
    public static final String SEARCH_FOR_CURRENT_POSITION = "searchForCurrentPosition";
    public static final boolean DEFAULT_SEARCH_FOR_CURRENT_POSITION = false;

    public static Point getAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        String address = sharedPreferences.getString(ADDRESS, DEFAULT_ADDRESS);
        float lat = sharedPreferences.getFloat(LAT, DEFAULT_LAT);
        float lng = sharedPreferences.getFloat(LNG, DEFAULT_LNG);
        return new Point(lat, lng, address);
    }

    public static String getRadius(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RADIUS, DEFAULT_RADIUS);
    }

    public static void setDefaultSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(RADIUS)) {
            sharedPreferences.edit().putString(RADIUS, DEFAULT_RADIUS).apply();
        }
        if (!sharedPreferences.contains(ADDRESS)) {
            sharedPreferences.edit().putString(ADDRESS, DEFAULT_ADDRESS).apply();
        }
        if (!sharedPreferences.contains(LAT)) {
            sharedPreferences.edit().putFloat(LAT, DEFAULT_LAT).apply();
        }
        if (!sharedPreferences.contains(LNG)) {
            sharedPreferences.edit().putFloat(LNG, DEFAULT_LNG).apply();
        }
    }

}
