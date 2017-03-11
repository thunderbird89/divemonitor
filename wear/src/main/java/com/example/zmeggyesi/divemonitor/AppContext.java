package com.example.zmeggyesi.divemonitor;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorManager;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public class AppContext extends Application {
	private SensorManager manager;

	public SensorManager getManager() {
		return manager;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	}
}
