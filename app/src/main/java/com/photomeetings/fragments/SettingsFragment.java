package com.photomeetings.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.photomeetings.R;
import com.photomeetings.adapters.AddressAutoCompleteAdapter;
import com.photomeetings.listeners.GeoLocationListener;
import com.photomeetings.model.Point;
import com.photomeetings.services.SettingsService;
import com.photomeetings.tasks.AsyncGeocodingTask;
import com.photomeetings.views.DelayAutoCompleteTextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class SettingsFragment extends Fragment {

    private DelayAutoCompleteTextView autoCompleteTextViewAddress;
    private ProgressBar progressBar;
    private boolean settingsWasChanged;
    private CheckBox searchForCurrentPosition;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DiscreteSeekBar discreteSeekBar;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        context = getContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GeoLocationListener(context, locationManager);
        discreteSeekBar = view.findViewById(R.id.discreteSeekBar);
        prepareAutoCompleteViewAddress(view);
        prepareSearchForCurrentPosition(view);
        prepareRadius();
        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchForCurrentPosition.isChecked()) {
                    new AsyncGeocodingTask(String.valueOf(autoCompleteTextViewAddress.getText()), context).execute();
                }
                SettingsService.saveRadius(context, String.valueOf(discreteSeekBar.getProgress()));
                SettingsService.saveSearchForCurrentPosition(searchForCurrentPosition.isChecked(), context);
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
        autoCompleteTextViewAddress.setText(SettingsService.getAddress(context));
        autoCompleteTextViewAddress.setThreshold(4);
        autoCompleteTextViewAddress.setAdapter(new AddressAutoCompleteAdapter(context));
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
        searchForCurrentPosition.setChecked(SettingsService.isSearchForCurrentPosition(context));
        autoCompleteTextViewAddress.setEnabled(!searchForCurrentPosition.isChecked());
        if (searchForCurrentPosition.isChecked()) {
            boolean permissionGrantedFineLocation = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean permissionGrantedCoarseLocation = checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }
        searchForCurrentPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    boolean permissionGrantedFineLocation = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    boolean permissionGrantedCoarseLocation = checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                    } else {
                        SettingsService.requestLastKnownLocation(context, locationManager, locationListener, searchForCurrentPosition);
                    }
                } else {
                    locationManager.removeUpdates(locationListener);
                }
                autoCompleteTextViewAddress.setEnabled(!isChecked);
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void prepareRadius() {
        discreteSeekBar.setProgress(Integer.parseInt(SettingsService.getRadius(context)));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults.length == 2) {
            boolean permissionGrantedFineLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permissionGrantedCoarseLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (permissionGrantedFineLocation) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(context)), locationListener);
            }
            if (permissionGrantedCoarseLocation) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000, Integer.parseInt(SettingsService.getRadius(context)), locationListener);
            }
            if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
                searchForCurrentPosition.setChecked(false);
                return;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                SettingsService.saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), context);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                SettingsService.saveCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER), context);
            } else {
                SettingsService.requestToEnableGeoLocationService(context, locationManager, searchForCurrentPosition);
            }
        } else {
            searchForCurrentPosition.setChecked(false);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
