package com.example.zmeggyesi.divemonitor.sensorium;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.services.RecorderService;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class OrientationHandler extends SensorHandler implements SensorEventListener {
	private static final String TAG = "Sensorium-orientation";
	private static final List<Integer> HANDLED_SENSORS = Lists.newArrayList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD);

	private float[] magneticField;
	private float[] acceleration;
	private boolean gotAcceleration = false;
	private boolean gotMagneticField = false;
	private float[] orientation;
	private SensorManager manager;
	private boolean serviceBound = false;
	private RecorderService rec;
	private final ServiceConnection CONN = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
			rec = binder.getRecorder();
			Log.d(TAG, "Recorder service connected");
			serviceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "Recorder service disconnected");
			serviceBound = false;
		}
	};
	private Context context;

	public OrientationHandler(SensorManager manager, Context ctx) {
		this.manager = manager;
		this.context = ctx;
		bindRecorder(context, CONN, this.getClass().getName());
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
				Intent recording = new Intent(context, RecorderService.class);
				recording.putExtra("dataType", RecorderService.DataTypes.ORIENTATION);
				recording.putExtra("data", orientation);
				rec.recordReading(recording);
				gotAcceleration = false;
				gotMagneticField = false;
			} else {
				return; // I want to make it explicit that no work is done, thus, `return`
			}
		} else {
			Log.d(TAG, "Parallel invocation of sensor listeners with sensor " + event.sensor.getStringType());
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
