package com.messbees.mqttgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView gpsText;
    private LocationManager locationManager;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String CLIENT_ID;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsText = (TextView) findViewById(R.id.gps_text);
        ((Button) findViewById(R.id.gps_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.post_button)).setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        CLIENT_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.BROKER_URL, CLIENT_ID);

        brokerConnect();

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
                try {
                    //TODO: form payload
                    String payload = "";
                    publishMessage(payload);
                } catch (MqttException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
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

    private void brokerConnect() {
        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken mqttToken) {
                    try {
                        Log.d(TAG, "Connected to broker");
                        pahoMqttClient.subscribe(mqttAndroidClient, Constants.TOPIC, 2);
                        Log.d(TAG, "Subscribed");
                    } catch (MqttException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    Log.d(TAG, "Connect failure");
                }
            });
        }
        catch (MqttException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void publishMessage(String payload) throws MqttException, UnsupportedEncodingException {
        pahoMqttClient.publishMessage(mqttAndroidClient, payload, 2, Constants.TOPIC);
    }
}
