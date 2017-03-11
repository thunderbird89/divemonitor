package com.example.zmeggyesi.divemonitor.sensorium;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.DatabaseService;

public class RecorderService extends Service {
	public static final String TAG = "Recorder";
	private final LocalBinder localBinder = new LocalBinder();

	public RecorderService() {
	}

	private boolean recordOpen = false;
	private DatabaseService dsb = new DatabaseService(this);
	private SQLiteDatabase db;
	private Record currentRecord;

	@Override
	public void onCreate() {
		super.onCreate();
		db = dsb.getWritableDatabase();
	}

	public void recordReading(Intent intent) { // TODO: change param type!
		if (!recordOpen) {
			currentRecord = new Record();
			currentRecord.timestamp = System.currentTimeMillis();
			recordOpen = true;
		}

		String dataType = intent.getStringExtra("dataType");
		switch (dataType) {
			case "pressure" : addPressure(intent.getFloatExtra("pressure", 0));
				break;
			case "orientation" : addOrientation(intent.getFloatArrayExtra("orientation"));
				break;
			case "lightLevel" : addLight(intent.getFloatExtra("lightLevel", 0));
				break;
			case "temperature" : addTemperature(intent.getFloatExtra("temperature", 0));
				break;
		}

		if (!currentRecord.needsLightLevel &
				!currentRecord.needsOrientation &
				!currentRecord.needsPressure &
				!currentRecord.needsTemperature) {
			// TODO: write to db
			recordOpen = false;
		}
	}

	private void addTemperature(float temperature) {
		if (currentRecord.needsTemperature) {
			currentRecord.tempreature = temperature;
			currentRecord.needsTemperature = false;
		}

	}

	private void addLight(float lightLevel) {
		if (currentRecord.needsLightLevel) {
			currentRecord.lightLevel = lightLevel;
			currentRecord.needsLightLevel = false;
		}
	}

	private void addOrientation(float[] orientations) {
		if (currentRecord.needsOrientation) {
			currentRecord.orientation = orientations;
			currentRecord.needsOrientation = false;
		}

	}

	private void addPressure(float pressure) {
		if (currentRecord.needsPressure) {
			currentRecord.pressure = pressure;
			currentRecord.needsPressure = false;
		}
	}

	private class Record {
		private boolean needsPressure = true;
		private boolean needsTemperature = true;
		private boolean needsOrientation = true;
		private boolean needsLightLevel = true;

		private float pressure;
		private float tempreature;
		private float[] orientation;
		private float lightLevel;
		private long timestamp;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service is binding to " + intent.getStringExtra("binder"));
		return localBinder;
	}

	private class LocalBinder extends Binder {
		RecorderService getRecorder() {
			return RecorderService.this;
		}
	}
}
