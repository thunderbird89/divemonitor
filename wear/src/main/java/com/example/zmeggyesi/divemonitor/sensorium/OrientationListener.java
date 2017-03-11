package com.example.zmeggyesi.divemonitor.sensorium;

import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class OrientationListener implements SensorEventListener {
	private static final String TAG = "Sensorium-orientation";
	private static final List<Integer> HANDLED_SENSORS = Lists.newArrayList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD);

	private float[] magneticField;
	private float[] acceleration;
	private boolean gotAcceleration = false;
	private boolean gotMagneticField = false;
	private float[] orientation;
	private SensorManager manager;

	public OrientationListener(SensorManager manager) {
		this.manager = manager;
	}

	public float[] getOrientation() {
		return orientation;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (HANDLED_SENSORS.contains(event.sensor.getType())) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				acceleration = event.values;
				gotAcceleration = true;
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticField = event.values;
				gotMagneticField = true;
			}

			if (gotAcceleration & gotMagneticField) {
				float[] rotationMatrix = new float[9];
				manager.getRotationMatrix(rotationMatrix, null, acceleration, magneticField);
				manager.getOrientation(rotationMatrix, orientation);
				gotAcceleration = false;
				gotMagneticField = false;
			} else {
				return;
			}
		} else {
			Log.d(TAG, "Parallel invocation of sensor listeners with sensor " + event.sensor.getStringType());
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
