package com.messbees.mqttgps;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

class MyLocationListener implements LocationListener {

    private static final String TAG = "LocationListener";

    @Override
    public void onLocationChanged(Location loc) {
        String longitude = Double.toString(loc.getLongitude());
        Log.v(TAG, longitude);
        String latitude = Double.toString(loc.getLatitude());
        Log.v(TAG, latitude);


    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
