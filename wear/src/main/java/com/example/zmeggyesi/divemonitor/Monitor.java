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

import com.example.zmeggyesi.divemonitor.sensorium.OrientationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Monitor extends WearableActivity {
	private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
	private static final String TAG = "DataLayer";

	private long lastReading;
    private BoxInsetLayout mContainerView;
    private TextView pressure;
    private TextView mClockView;
    private TextView temperature;

	private SensorManager manager;
    private GoogleApiClient client;
    private float surfacePressure;
	private OrientationListener ol;
	private Sensor magneto;
	private Sensor accelero;

	@Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(ol);
    }

    @Override
    protected void onResume() {
        super.onResume();
    	manager.registerListener(ol, magneto, SensorManager.SENSOR_DELAY_NORMAL);
    	manager.registerListener(ol, accelero, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        setAmbientEnabled();

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		ol = new OrientationListener(manager);
		magneto = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelero = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        pressure = (TextView) findViewById(R.id.pressure);
        temperature = (TextView) findViewById(R.id.temperature);
        mClockView = (TextView) findViewById(R.id.clock);
        connectToDataLayer();
        surfacePressure = getIntent().getFloatExtra("surfacePressure", 1000);
    }

    private void connectToDataLayer() {
        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connection Established");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Connection Suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.wtf(TAG, "Connection Failed");
                    }
                })
                .build();
        client.connect();
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
            float depth = SensorManager.getAltitude(surfacePressure, reading) * -1;
            pressure.setText(String.format(getResources().getString(R.string.pressure_format), reading, depth));
            lastReading = timestamp;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            pressure.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setTextColor(getResources().getColor(R.color.white, null));
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            pressure.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
