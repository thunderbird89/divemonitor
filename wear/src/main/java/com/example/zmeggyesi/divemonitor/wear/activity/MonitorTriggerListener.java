package com.example.zmeggyesi.divemonitor.wear.activity;

import android.content.Intent;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.divemonitor_commons.model.DiveInitData;
import com.example.zmeggyesi.divemonitor.wear.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.wear.services.LogTransferService;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MonitorTriggerListener extends WearableListenerService {

	private final String TAG = "Remote";
	private GlobalContext gc;

	@Override
	public void onCreate() {
		super.onCreate();
		gc = (GlobalContext) getApplicationContext();
	}

	public MonitorTriggerListener() {
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		super.onDataChanged(dataEventBuffer);
		Log.d(TAG, "Data Changed");
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		super.onMessageReceived(messageEvent);
		Log.d(TAG, messageEvent.getPath());
		if (messageEvent.getPath().equals("/startMonitoring")) {
			Intent i = new Intent(this, Monitor.class);
			DiveInitData initData = new Gson().fromJson(new String(messageEvent.getData(), StandardCharsets.UTF_8), DiveInitData.class);

			float surfacePressure = initData.getSurfacePressure();
			Log.d(TAG, "Received surface pressure from remote device: " + surfacePressure);
			gc.setCurrentDiveKey(initData.getKey());
			Log.d(TAG, "Dive key is " + gc.getCurrentDiveKey());

			i.putExtra("surfacePressure", surfacePressure != 0 ? surfacePressure : SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Log.d(TAG, "Begin monitoring!");
			startActivity(i);
		} else if (messageEvent.getPath().equals("/endMonitoring")) {
	        sendBroadcast(new Intent("terminateMonitoring"));
		} else if (messageEvent.getPath().equals("/getLogs")) {
			Intent intent = new Intent(this, LogTransferService.class);
			intent.setAction("com.example.zmeggyesi.divemonitor.wear.TRANSFER_LOGS");
			startService(intent);
		} else if (messageEvent.getPath().equals("/logRetrievalComplete")) {
			Intent i = new Intent(this, LogTransferService.class);
			i.setAction("com.example.zmeggyesi.divemonitor.wear.CLEAR_LOCAL_DB");
			startActivity(i);
		}
	}
}
