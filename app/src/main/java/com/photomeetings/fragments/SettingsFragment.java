package com.photomeetings.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.adapters.AddressAutoCompleteAdapter;
import com.photomeetings.model.Point;
import com.photomeetings.services.SettingsService;
import com.photomeetings.tasks.AsyncGeocodingTask;
import com.photomeetings.views.DelayAutoCompleteTextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static com.photomeetings.services.SettingsService.ADDRESS;
import static com.photomeetings.services.SettingsService.DEFAULT_ADDRESS;
import static com.photomeetings.services.SettingsService.DEFAULT_RADIUS;
import static com.photomeetings.services.SettingsService.DEFAULT_SEARCH_FOR_CURRENT_POSITION;
import static com.photomeetings.services.SettingsService.LAT;
import static com.photomeetings.services.SettingsService.LNG;
import static com.photomeetings.services.SettingsService.RADIUS;
import static com.photomeetings.services.SettingsService.SEARCH_FOR_CURRENT_POSITION;
import static com.photomeetings.services.SettingsService.SETTINGS;

public class SettingsFragment extends Fragment {

    private DelayAutoCompleteTextView autoCompleteTextViewAddress;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean settingsWasChanged;
    private CheckBox searchForCurrentPosition;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DiscreteSeekBar discreteSeekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        sharedPreferences = getActivity().getSharedPreferences(SETTINGS, MODE_PRIVATE);
        discreteSeekBar = view.findViewById(R.id.discreteSeekBar);
        prepareAutoCompleteViewAddress(view);
        prepareSearchForCurrentPosition(view);
        prepareRadius();
        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchForCurrentPosition.isChecked()) {
                    new AsyncGeocodingTask(String.valueOf(autoCompleteTextViewAddress.getText()), sharedPreferences).execute();
                }
                sharedPreferences.edit().putString(RADIUS, String.valueOf(discreteSeekBar.getProgress())).apply();
                sharedPreferences.edit().putBoolean(SEARCH_FOR_CURRENT_POSITION, searchForCurrentPosition.isChecked()).apply();
                setSettingsWasChanged(true);
            }
        });
        return view;
    }

    public boolean isSettingsWasChanged() {
        return settingsWasChanged;
    }

    public void setSettingsWasChanged(boolean settingsWasChanged) {
        this.settingsWasChanged = settingsWasChanged;
    }

    private void prepareAutoCompleteViewAddress(View view) {
        autoCompleteTextViewAddress = view.findViewById(R.id.autoCompleteTextViewAddress);
        autoCompleteTextViewAddress.setText(sharedPreferences.getString(ADDRESS, DEFAULT_ADDRESS));
        autoCompleteTextViewAddress.setThreshold(4);
        autoCompleteTextViewAddress.setAdapter(new AddressAutoCompleteAdapter(getContext()));
        autoCompleteTextViewAddress.setProgressBar(progressBar);
        autoCompleteTextViewAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Point point = (Point) adapterView.getItemAtPosition(position);
                autoCompleteTextViewAddress.setText(point.getAddress());
            }
        });
    }

    private void prepareSearchForCurrentPosition(View view) {
        searchForCurrentPosition = view.findViewById(R.id.searchForCurrentPosition);
        searchForCurrentPosition.setChecked(sharedPreferences.getBoolean(SEARCH_FOR_CURRENT_POSITION, DEFAULT_SEARCH_FOR_CURRENT_POSITION));
        autoCompleteTextViewAddress.setEnabled(!searchForCurrentPosition.isChecked());
        if (searchForCurrentPosition.isChecked()) {
            requestPermissionsIfNeeded();
        }
        searchForCurrentPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestPermissionsIfNeeded();
                }
                autoCompleteTextViewAddress.setEnabled(!isChecked);
            }

        });
    }

    private void prepareRadius() {
        discreteSeekBar.setProgress(Integer.parseInt(sharedPreferences.getString(RADIUS, DEFAULT_RADIUS)));
    }

    private void requestPermissionsIfNeeded() {
        boolean permissionGrantedFineLocation = checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permissionGrantedCoarseLocation = checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            if (permissionGrantedFineLocation) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(getContext())), locationListener);
            }
            if (permissionGrantedCoarseLocation) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(getContext())), locationListener);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), sharedPreferences);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), sharedPreferences);
            } else {
                requestToEnableGeoLocationService();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults.length == 2) {
            boolean permissionGrantedFineLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permissionGrantedCoarseLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (permissionGrantedFineLocation) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(getContext())), locationListener);
            }
            if (permissionGrantedCoarseLocation) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(getContext())), locationListener);
            }
            if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
                searchForCurrentPosition.setChecked(false);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), sharedPreferences);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), sharedPreferences);
            } else {
                requestToEnableGeoLocationService();
            }
        } else {
            searchForCurrentPosition.setChecked(false);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveCurrentLocation(@Nullable Location location, SharedPreferences sharedPreferences) {
        if (location != null) {
            sharedPreferences.edit().putFloat(LAT, (float) location.getLatitude()).apply();
            sharedPreferences.edit().putFloat(LNG, (float) location.getLongitude()).apply();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        final Context context = getContext();
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                String provider = location.getProvider().equals(LocationManager.GPS_PROVIDER) ? "GPS" : "WiFi и сеть";
                saveCurrentLocation(location, sharedPreferences);
                Toast.makeText(context, "Местоположение изменено (" + provider + ")!\nОбновите экран поиска фотографий!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                String p = provider.equals(LocationManager.GPS_PROVIDER) ? "GPS" : "WiFi и сеть";
                switch (status) {
                    case LocationProvider.AVAILABLE: {
                        saveCurrentLocation(locationManager.getLastKnownLocation(provider), sharedPreferences);
                    }
                    case LocationProvider.OUT_OF_SERVICE: {
                        //Toast.makeText(context, "Служба определения местоположения (" + p + ") недоступна!", Toast.LENGTH_LONG).show();
                    }
                    case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                        //Toast.makeText(context, "Служба определения местоположения (" + p + ") временно недоступна!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                saveCurrentLocation(locationManager.getLastKnownLocation(provider), sharedPreferences);
            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };
    }

    private void requestToEnableGeoLocationService() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setTitle("Включение службы геолокации");
            alertDialog.setMessage("Для поиска фотографий по текущему местоположению включите службу определения геолокации в настройках (для более точного поиска включите GPS)");
            alertDialog.setPositiveButton("Настройки местоположения", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    searchForCurrentPosition.setChecked(false);
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }

}
