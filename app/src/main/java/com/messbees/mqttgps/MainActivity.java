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

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView gpsText, resultText;
    private LocationManager locationManager;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String CLIENT_ID;
    private static final String TAG = "MainActivity";
    private String lastPayload = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsText = (TextView) findViewById(R.id.gps_text);
        resultText = (TextView) findViewById(R.id.result_text);
        ((Button) findViewById(R.id.gps_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.post_button)).setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        CLIENT_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.BROKER_URL, CLIENT_ID);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String payload = new String(mqttMessage.getPayload());
                if (topic.equals(Constants.TOPIC) && payload.equals(lastPayload)) {
                    Log.d(TAG, "Posted successfully!");
                    resultText.setText(R.string.post_success);

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        brokerConnect();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission is granted!");
                    getGps();

                } else {
                    Log.d(TAG, "Permission was not granted!");
                    Toast.makeText(MainActivity.this, R.string.err_no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gps_button:
                Log.d(TAG, "Checking for permission...");
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission is granted");

                    getGps();

                } else {
                    Log.d(TAG, "Permission was not granted yet, requesting...");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }
                break;
            case R.id.post_button:
                try {
                    String payload = getGps();
                    lastPayload = payload;
                    Log.d(TAG, "Posting...");
                    resultText.setText(R.string.post_waiting);
                    pahoMqttClient.publishMessage(mqttAndroidClient, payload, 2, Constants.TOPIC);
                } catch (MqttException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    resultText.setText(R.string.post_fail);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    resultText.setText(R.string.post_fail);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    resultText.setText(R.string.post_fail);
                }
                break;
        }
    }

    private String getGps() {
        String result = "";
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            String lat = Double.toString(location.getLatitude());
            String lon = Double.toString(location.getLongitude());
            String accuracy = Float.toString(location.getAccuracy());
            String coords = lat + " " + lon;

            gpsText.setText(coords);
            Gson gson = new Gson();
            Message message = new Message(CLIENT_ID, lat, lon, accuracy);
            String json = gson.toJson(message);
            Log.d(TAG, json);

            result = json;
        }
        catch (SecurityException e) {
            Toast.makeText(MainActivity.this, R.string.err_no_permission, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getLocalizedMessage());
        }
        return result;
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
                    Log.d(TAG, "Connect failure: " + arg1.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, R.string.err_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (MqttException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
