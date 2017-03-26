package com.example.zmeggyesi.divemonitor.wear.sensorium;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.wear.activity.Monitor;
import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.wear.services.RecorderService;


/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class TemperatureHandler extends SensorHandler implements SensorEventListener {
	public static final String TAG = "Sensorium-temperature";
	private final LocalBroadcastManager lbm;
	private float temperature;
	private RecorderService rec;
	private Context context;
	private boolean serviceBound = false;
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
	public TemperatureHandler(LocalBroadcastManager localBroadcastManager, Context context) {
		this.lbm = localBroadcastManager;
		this.context = context;
		bindRecorder(context, CONN, this.getClass().getName());
	}

	@Override
	protected void announcePresence() {
		Intent ready = new Intent(context, Monitor.class);
		ready.setAction(context.getString(R.string.listener_ready_action));
		ready.putExtra("listener", TAG);
		lbm.sendBroadcast(ready);
	}

	public float getTemperature() {
		return temperature;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Intent recording = new Intent(context, RecorderService.class);
		recording.putExtra("dataType", RecorderService.DataTypes.TEMPERATURE);
		recording.putExtra("data", event.values[0]);
		recording.setAction(context.getString(R.string.broadcast_reading_temperature));
		rec.recordReading(recording);
		lbm.sendBroadcast(recording);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

}
