package com.example.zmeggyesi.divemonitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Monitor extends WearableActivity implements SensorEventListener {
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private long lastReading;
    private BoxInsetLayout mContainerView;
    private TextView pressure;
    private TextView mClockView;
    private TextView orientation;
    private SensorManager manager;
    private Sensor barometer;
    private GoogleApiClient client;

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        readPressure(event.values[0], event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        setAmbientEnabled();

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE, true);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        pressure = (TextView) findViewById(R.id.pressure);
        orientation = (TextView) findViewById(R.id.orientation);
        mClockView = (TextView) findViewById(R.id.clock);
        connectToDataLayer();
//        Wearable.MessageApi.addListener(client, MonitorTriggerListener.class);
    }

    private void connectToDataLayer() {
        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("API Connection", "Connection Established");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("API Connection", "Connection Suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.wtf("API Connection", "Connection Failed");
                    }
                })
                .build();
        client.connect();
    }

    public void toggleConnection(View view) {
        if (client.isConnected()) {
            client.disconnect();
            Log.d("API Connection", Boolean.toString(client.isConnected()));
        } else {
            client.connect();
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void readPressure(float reading, long timestamp) {
        if (timestamp - lastReading > 10000000000l) {
            pressure.setText(String.valueOf(reading));
            lastReading = timestamp;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            pressure.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            pressure.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
