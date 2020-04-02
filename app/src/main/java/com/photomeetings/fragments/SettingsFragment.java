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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import com.photomeetings.R;
import com.photomeetings.adapters.AddressAutoCompleteAdapter;
import com.photomeetings.listeners.GeoLocationListener;
import com.photomeetings.model.Point;
import com.photomeetings.services.SettingsService;
import com.photomeetings.tasks.AsyncGeocodingTask;
import com.photomeetings.views.DelayAutoCompleteTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class SettingsFragment extends Fragment {

    private final Map<Integer, Integer> positionToRadius = new HashMap<>();
    private final Map<Integer, Integer> radiusToPosition = new HashMap<>();

    {
        int[] radiuses = {100, 500, 1_000, 2_000, 5_000, 10_000, 20_000, 50_000};
        for (int i = 1; i <= radiuses.length; i++) {
            positionToRadius.put(i, radiuses[i - 1]);
            radiusToPosition.put(radiuses[i - 1], i);
        }
    }

    private DelayAutoCompleteTextView autoCompleteTextViewAddress;
    private ProgressBar progressBar;
    private boolean settingsWasChanged;
    private CheckBox searchForCurrentPosition;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private NumberPicker numberPicker;
    private Context context;
    private EditText startTimeEditText;
    private EditText endTimeEditText;
    private EditText searchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        context = getContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GeoLocationListener(context, locationManager);
        prepareAutoCompleteViewAddress(view);
        prepareSearchForCurrentPosition(view);
        prepareRadius(view);
        prepareEditTexts(view);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchForCurrentPosition.isChecked()) {
                    new AsyncGeocodingTask(String.valueOf(autoCompleteTextViewAddress.getText()), context).execute();
                }
                SettingsService.saveRadius(context, positionToRadius.get(numberPicker.getValue()));
                SettingsService.saveSearchForCurrentPosition(searchForCurrentPosition.isChecked(), context);
                SettingsService.saveStartTime(getDateFromEditText(startTimeEditText), context);
                SettingsService.saveEndTime(getDateFromEditText(endTimeEditText), context);
                SettingsService.saveSearch(searchEditText.getText().toString(), context);
                setSettingsWasChanged(true);
            }
        });
        return view;
    }

    private long getDateFromEditText(EditText editText) {
        try {
            return SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse(editText.getText().toString()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;//stub
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
                Point fullAddress = (Point) adapterView.getItemAtPosition(position);
                //fixme: происходит сохранение без нажатия кнопки сохранить.
                //fixme: когда кнопка сохранить будет убрана, не забыть сохранять любой контрол настроек при его изменении
                //fixme: и самое главное - менять settingsWasChanged!!!
                SettingsService.saveFullAddress(fullAddress, context);
                autoCompleteTextViewAddress.setText(fullAddress.getAddress());
            }
        });
    }

    private void prepareSearchForCurrentPosition(View view) {
        searchForCurrentPosition = view.findViewById(R.id.searchForCurrentPosition);
        searchForCurrentPosition.setChecked(SettingsService.isSearchForCurrentPosition(context));
        autoCompleteTextViewAddress.setEnabled(!searchForCurrentPosition.isChecked());
        searchForCurrentPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkLocationPermission();
                } else {
                    locationManager.removeUpdates(locationListener);
                }
                autoCompleteTextViewAddress.setEnabled(!isChecked);
            }

        });
    }

    private void checkLocationPermission() {
        boolean permissionGrantedFineLocation = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permissionGrantedCoarseLocation = checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGrantedFineLocation && !permissionGrantedCoarseLocation) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            SettingsService.requestLastKnownLocation(context, locationManager, locationListener, searchForCurrentPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (searchForCurrentPosition.isChecked()) {
            checkLocationPermission();
        }
    }

    private void prepareRadius(View view) {
        numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(radiusToPosition.size());
        numberPicker.setValue(radiusToPosition.get(SettingsService.getRadius(context)));
        List<Integer> radiuses = new ArrayList<>(positionToRadius.values());
        String[] displayedValues = new String[radiuses.size()];
        for (int i = 0; i < displayedValues.length; i++) {
            displayedValues[i] = String.valueOf(radiuses.get(i));
        }
        numberPicker.setDisplayedValues(displayedValues);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults.length == 2) {
            boolean permissionGrantedFineLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permissionGrantedCoarseLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (permissionGrantedFineLocation) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, SettingsService.getRadius(context), locationListener);
            }
            if (permissionGrantedCoarseLocation) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000, SettingsService.getRadius(context), locationListener);
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

    private void prepareEditTexts(View view) {
        startTimeEditText = view.findViewById(R.id.startDateEditText);
        endTimeEditText = view.findViewById(R.id.endDateEditText);
        searchEditText = view.findViewById(R.id.searchEditText);
        startTimeEditText.setText(SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(new Date(SettingsService.getStartTime(context))));
        endTimeEditText.setText(SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(new Date(SettingsService.getEndTime(context))));
        searchEditText.setText(SettingsService.getSearch(context));
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
