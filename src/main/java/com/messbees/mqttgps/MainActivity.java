package com.messbees.mqttgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView gpsText;
    private MyLocationListener locationListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsText = (TextView) findViewById(R.id.gps_text);
        ((Button) findViewById(R.id.gps_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.post_button)).setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGps();

                } else {
                    Toast.makeText(MainActivity.this, R.string.err_no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gps_button:
                Log.d(TAG, "Checking for permission");
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    getGps();
                } else {
                    Log.d(TAG, "Permission was not granted yet");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }
                break;
            case R.id.post_button:
                //TODO: post mqtt message
                break;
        }
    }

    private void getGps() {
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String coords = Double.toString(location.getLatitude()) + " " + Double.toString(location.getLatitude());
            String accuracy = Float.toString(location.getAccuracy());
            Log.d(TAG, coords);
            Log.d(TAG, accuracy);
            gpsText.setText(coords);
        }
        catch (SecurityException e) {
            Toast.makeText(MainActivity.this, R.string.err_no_permission, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

}
