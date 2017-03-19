package com.example.zmeggyesi.divemonitor.mobile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.R;

public class Monitoring extends Activity implements SensorEventListener {

    public static final int SENSOR_REFRESH_RATE = 5000000;
    private SensorManager manager;
    private Sensor accelerometer;
    private ArrayAdapter<String> arrayAdapter;
    private Sensor magnetometer;
    private float[] acceleration = new float[3];
    private float[] magnetics = new float[3];
    private Sensor barometer;

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SENSOR_REFRESH_RATE);
        manager.registerListener(this, magnetometer, SENSOR_REFRESH_RATE);
        manager.registerListener(this, barometer, SENSOR_REFRESH_RATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_monitoring, R.id.orientation);
    }

    public void stopMonitor(View view) {
        Intent i = new Intent(this, Home.class);
        startActivity(i);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER || event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            final float[] rotationMatrix = new float[9];
            final float[] orientationMatrix = new float[3];
            final double[] orientationDegrees = new double[3];
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acceleration = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetics = event.values;
            }
            SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetics);
            SensorManager.getOrientation(rotationMatrix, orientationMatrix);
            for (int i = 0; i < 3; i++) {
                double orientationVal = Double.parseDouble(Float.toString(orientationMatrix[i]));
                orientationDegrees[i] = Math.toDegrees(orientationVal);
            }
            String format = "Azimuth: %1$f\nPitch: %2$f\nRoll: %3$f";
            TextView orientationReadout = (TextView) findViewById(R.id.orientation);
            orientationReadout.setText(String.format(format, orientationDegrees[0], orientationDegrees[1], orientationDegrees[2]));
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            String format = "Pressure: %1$f\nComputed altitude: %2$f m";
            TextView pressureReadout = (TextView) findViewById(R.id.pressure);
            pressureReadout.setText(String.format(format, event.values[0],
                    SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0])));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
