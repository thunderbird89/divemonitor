package com.example.zmeggyesi.divemonitor.sensorium;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;

import com.example.zmeggyesi.divemonitor.services.RecorderService;

/**
 * Created by zmeggyesi on 2017. 03. 12..
 */

abstract class SensorHandler {

	void bindRecorder(Context context, ServiceConnection connection, String className) {
		Intent serviceStartIntent = new Intent(context, RecorderService.class);
		serviceStartIntent.putExtra("binder", className);
		context.bindService(serviceStartIntent, connection, Context.BIND_AUTO_CREATE);
	}

	protected abstract void announcePresence();
}
