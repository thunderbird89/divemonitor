package com.example.zmeggyesi.divemonitor.wear.sensorium;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.wear.services.RecorderService;


/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class PressureHandler extends SensorHandler implements SensorEventListener {
	public static final String TAG = "Sensorium-pressure";
	private final LocalBroadcastManager lbm;
	private boolean serviceBound = false;
	private RecorderService rec;
	private final ServiceConnection CONN = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
			rec = binder.getRecorder();
			Log.d(TAG, "Recorder service connected");
			serviceBound = true;
			announcePresence();
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


	public PressureHandler(LocalBroadcastManager localBroadcastManager, Context context) {
		this.lbm = localBroadcastManager;
		this.context = context;
		bindRecorder(context, CONN, this.getClass().getName());
	}

	@Override
	protected void announcePresence() {
		Log.d(TAG, "Announcing presence on the device");
		Intent ready = new Intent();
		ready.setAction(context.getResources().getString(R.string.listener_ready_action));
		ready.putExtra("listener",TAG);
		lbm.sendBroadcast(ready);
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
		recording.putExtra("rawPressure", event.values[0]);
		recording.setAction(context.getString(R.string.broadcast_reading_pressure));
		rec.recordReading(recording);
		lbm.sendBroadcast(recording);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
