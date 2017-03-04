package com.example.zmeggyesi.divemonitor.com.example.zmeggyesi.divemonitor.sensorium;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.R;

/**
 * Created by zmeggyesi on 2017. 03. 03..
 */

public class PressureMonitor extends Activity implements SensorEventListener {

    private SensorManager manager;
    private Sensor pressureSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        setContentView(R.layout.activity_pressure_monitor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, pressureSensor, 1000000);
    }

    @Override
    public final void onSensorChanged(SensorEvent e) {
        float pressure = e.values[0];
    }

    @Override
    public final void onAccuracyChanged(Sensor s, int accuracy) {
        System.out.print("Sensor accuracy has changed, is now: " + String.valueOf(accuracy));
    }
}
