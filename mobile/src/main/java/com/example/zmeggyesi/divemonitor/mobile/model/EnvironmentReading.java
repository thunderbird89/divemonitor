package com.example.zmeggyesi.divemonitor.mobile.model;

import android.provider.BaseColumns;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class EnvironmentReading {
	private long timestamp;
	private float pressure;
	private float temperature;
	private float[] orientation;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public float getPressure() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float[] getOrientation() {
		return orientation;
	}

	public void setOrientation(float[] orientation) {
		this.orientation = orientation;
	}

	public final class Record implements BaseColumns {
		private boolean needsPressure = true;
		private boolean needsTemperature = true;
		private boolean needsOrientation = true;
		private boolean needsLightLevel = true;

		private float pressure;
		private float tempreature;
		private float[] orientation;
		private float lightLevel;
		private long timestamp;

		public static final String TABLE_NAME = "readings";
		public static final String COLUMN_NAME_PRESSURE = "pressure";
		public static final String COLUMN_NAME_TEMPERATURE = "temperature";
		public static final String COLUMN_NAME_ORIENTATION_AZIMUTH = "orientationAzimuth";
		public static final String COLUMN_NAME_ORIENTATION_PITCH = "orientationPitch";
		public static final String COLUMN_NAME_ORIENTATION_ROLL = "orientationRoll";
		public static final String COLUMN_NAME_LIGHTLEVEL = "lightLevel";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_DIVE_KEY = "dive";
	}
}
