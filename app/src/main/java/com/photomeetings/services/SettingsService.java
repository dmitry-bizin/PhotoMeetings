package com.photomeetings.services;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.CheckBox;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.model.Point;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static android.text.format.DateUtils.YEAR_IN_MILLIS;

public abstract class SettingsService {

    private static final String SETTINGS = "settings";
    private static final String RADIUS = "radius";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String DEFAULT_RADIUS = "100";
    private static final String ADDRESS = "address";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final float DEFAULT_LAT = 0.0f;
    private static final float DEFAULT_LNG = 0.0f;
    private static final String DEFAULT_ADDRESS = "";
    private static final String SEARCH_FOR_CURRENT_POSITION = "searchForCurrentPosition";
    private static final boolean DEFAULT_SEARCH_FOR_CURRENT_POSITION = true;
    private static final float EPS_M = 50.0f;//метры

    public static Point getFullAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        String address = sharedPreferences.getString(ADDRESS, DEFAULT_ADDRESS);
        float lat = sharedPreferences.getFloat(LAT, DEFAULT_LAT);
        float lng = sharedPreferences.getFloat(LNG, DEFAULT_LNG);
        return new Point(lat, lng, address);
    }

    public static String getAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ADDRESS, DEFAULT_ADDRESS);
    }

    public static String getRadius(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RADIUS, DEFAULT_RADIUS);
    }

    public static void saveRadius(Context context, String radius) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(RADIUS, radius).apply();
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

    public static void saveCurrentLocation(@Nullable Location location, Context context) {
        if (location != null) {
            String provider = location.getProvider().equals(LocationManager.GPS_PROVIDER) ? context.getString(R.string.gps) : context.getString(R.string.wifi_network);
            Point savedAddress = getFullAddress(context);
            float[] results = new float[1];
            Location.distanceBetween(savedAddress.getLat(), savedAddress.getLng(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] >= EPS_M) {
                Toast.makeText(context, context.getString(R.string.changed_location_1) + provider + context.getString(R.string.changed_location_2), Toast.LENGTH_LONG).show();
                SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
                sharedPreferences.edit().putFloat(LAT, (float) location.getLatitude()).apply();
                sharedPreferences.edit().putFloat(LNG, (float) location.getLongitude()).apply();
            }
        }
    }

    public static void saveFullAddress(@Nullable Point fullAddress, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        if (fullAddress == null) {
            fullAddress = new Point(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ADDRESS);
        }
        sharedPreferences.edit().putString(ADDRESS, fullAddress.getAddress()).apply();
        sharedPreferences.edit().putFloat(LAT, fullAddress.getLat()).apply();
        sharedPreferences.edit().putFloat(LNG, fullAddress.getLng()).apply();
    }

    public static void saveSearchForCurrentPosition(boolean searchForCurrentPosition, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SEARCH_FOR_CURRENT_POSITION, searchForCurrentPosition).apply();
    }

    public static void requestLastKnownLocation(Context context, LocationManager locationManager,
                                                LocationListener locationListener,
                                                @Nullable CheckBox searchForCurrentPosition) {
        boolean permissionGrantedFineLocation = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permissionGrantedCoarseLocation = checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGrantedFineLocation) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(context)), locationListener);
        }
        if (permissionGrantedCoarseLocation) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(context)), locationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), context);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), context);
        } else {
            requestToEnableGeoLocationService(context, locationManager, searchForCurrentPosition);
        }
    }

    public static void requestToEnableGeoLocationService(final Context context, LocationManager locationManager,
                                                         @Nullable final CheckBox searchForCurrentPosition) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(R.string.enable_geolocation);
            alertDialog.setMessage(R.string.enable_geolocation_message);
            alertDialog.setPositiveButton(R.string.settings_geolocation, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    saveSearchForCurrentPosition(false, context);
                    if (searchForCurrentPosition != null) {
                        searchForCurrentPosition.setChecked(false);
                    }
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }

    public static boolean isSearchForCurrentPosition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SEARCH_FOR_CURRENT_POSITION, DEFAULT_SEARCH_FOR_CURRENT_POSITION);
    }

    public static long getStartTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(START_TIME, System.currentTimeMillis() - YEAR_IN_MILLIS);
    }

    public static long getEndTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(END_TIME, System.currentTimeMillis() + YEAR_IN_MILLIS);
    }

    public static void saveStartTime(long startTime, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(START_TIME, startTime).apply();
    }

    public static void saveEndTime(long endTime, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(END_TIME, endTime).apply();
    }

}
