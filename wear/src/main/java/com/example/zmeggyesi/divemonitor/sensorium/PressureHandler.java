package com.example.zmeggyesi.divemonitor.sensorium;

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


/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class PressureHandler extends SensorHandler implements SensorEventListener {
	private static final String TAG = "Sensorium-pressure";
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
	private float referencePressure;
	private boolean referenceSet = false;


	public PressureHandler(Context context) {
		this.context = context;
		bindRecorder(context, CONN, this.getClass().getName());
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!referenceSet) {
			referencePressure = event.values[0];
			referenceSet = true;
		}
		Intent recording = new Intent(context, RecorderService.class);
		recording.putExtra("dataType", RecorderService.DataTypes.PRESSURE);
		recording.putExtra("data", SensorManager.getAltitude(referencePressure, event.values[0]) * -1);
		rec.recordReading(recording);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
