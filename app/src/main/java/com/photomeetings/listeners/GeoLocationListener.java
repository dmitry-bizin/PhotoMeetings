package com.photomeetings.listeners;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.photomeetings.services.SettingsService;

public class GeoLocationListener implements LocationListener {

    private Context context;
    private LocationManager locationManager;

    public GeoLocationListener(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
    }

    @Override
    public void onLocationChanged(Location location) {
        SettingsService.saveCurrentLocation(location, context);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE: {
                SettingsService.saveCurrentLocation(locationManager.getLastKnownLocation(provider), context);
            }
            case LocationProvider.OUT_OF_SERVICE: {
                //Toast.makeText(context, "Служба определения местоположения (" + p + ") недоступна!", Toast.LENGTH_LONG).show();
            }
            case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                //Toast.makeText(context, "Служба определения местоположения (" + p + ") временно недоступна!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        SettingsService.saveCurrentLocation(locationManager.getLastKnownLocation(provider), context);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
