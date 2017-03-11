package com.example.zmeggyesi.divemonitor.sensorium;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class PressureHandler implements SensorEventListener {

	private float pressure;

	public float getPressure() {
		return pressure;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
