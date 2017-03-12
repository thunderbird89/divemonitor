package com.example.zmeggyesi.divemonitor.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.dao.contracts.RecordContract.Record;

public class RecorderService extends Service {
	private static final String TAG = "Recorder";
	private final RecorderBinder localBinder = new RecorderBinder();
	private final RecorderDatabaseHelper dsb = new RecorderDatabaseHelper(this);
	private boolean recordOpen = false;
	private SQLiteDatabase db;
	private RecordBean currentRecord;

	public RecorderService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		db = dsb.getWritableDatabase();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service is binding to " + intent.getStringExtra("binder"));
		return localBinder;
	}

	public void recordReading(Intent intent) {
		if (!recordOpen) {
			currentRecord = new RecordBean();
			currentRecord.timestamp = System.currentTimeMillis();
			recordOpen = true;
		}

		String dataType = intent.getStringExtra("dataType");
		switch (dataType) {
			case DataTypes.PRESSURE: addPressure(intent.getFloatExtra("data", 0));
				break;
			case DataTypes.ORIENTATION: addOrientation(intent.getFloatArrayExtra("data"));
				break;
			case DataTypes.LIGHT_LEVEL: addLight(intent.getFloatExtra("data", 0));
				break;
			case DataTypes.TEMPERATURE: addTemperature(intent.getFloatExtra("data", 0));
				break;
		}

		if (!currentRecord.needsLightLevel &
				!currentRecord.needsOrientation &
				!currentRecord.needsPressure &
				!currentRecord.needsTemperature) {
			writeToDB();

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

	private void addTemperature(float temperature) {
		if (currentRecord.needsTemperature) {
			currentRecord.temperature = temperature;
			currentRecord.needsTemperature = false;
		}

	}

	private void writeToDB() {
		ContentValues dbRecord = new ContentValues();
		dbRecord.put(Record.COLUMN_NAME_LIGHTLEVEL, currentRecord.lightLevel);
		dbRecord.put(Record.COLUMN_NAME_ORIENTATION_AZIMUTH,currentRecord.orientation[0]);
		dbRecord.put(Record.COLUMN_NAME_ORIENTATION_PITCH,currentRecord.orientation[1]);
		dbRecord.put(Record.COLUMN_NAME_ORIENTATION_ROLL,currentRecord.orientation[2]);
		dbRecord.put(Record.COLUMN_NAME_PRESSURE, currentRecord.pressure);
		dbRecord.put(Record.COLUMN_NAME_TEMPERATURE, currentRecord.temperature);
		dbRecord.put(Record.COLUMN_NAME_TIMESTAMP, currentRecord.timestamp);
		db.insert(Record.TABLE_NAME, null, dbRecord);
		recordOpen = false;
	}

	/**
	 * Created by zmeggyesi on 2017. 03. 12..
	 */

	public static class DataTypes {
		public static final String PRESSURE = "pressure";
		public static final String ORIENTATION = "orientation";
		public static final String LIGHT_LEVEL = "lightLevel";
		public static final String TEMPERATURE = "temperature";
	}

	private class RecordBean {
		private boolean needsPressure = true;
		private boolean needsTemperature = true;
		private boolean needsOrientation = true;
		private boolean needsLightLevel = true;

		private float pressure;
		private float temperature;
		private float[] orientation;
		private float lightLevel;
		private long timestamp;
	}

	public class RecorderBinder extends Binder {
		public RecorderService getRecorder() {
			return RecorderService.this;
		}
	}
}
