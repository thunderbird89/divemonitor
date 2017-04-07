package tech.provingground.divemonitor.sensorium;

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

import tech.provingground.divemonitor.R;
import tech.provingground.divemonitor.services.RecorderService;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class LightLevelHandler extends SensorHandler implements SensorEventListener {
	public static final String TAG = "Sensorium-light";
	private final LocalBroadcastManager lbm;
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

	public LightLevelHandler(LocalBroadcastManager localBroadcastManager, Context context) {
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
		Intent recording = new Intent(context, RecorderService.class);
		recording.putExtra("dataType", RecorderService.DataTypes.LIGHT_LEVEL);
		recording.putExtra("data", event.values[0]);
		recording.setAction(context.getString(R.string.broadcast_reading_light));
		rec.recordReading(recording);
		lbm.sendBroadcast(recording);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
