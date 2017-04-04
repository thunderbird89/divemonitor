package com.example.divemonitor_commons.model;

import android.provider.BaseColumns;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

@com.fasterxml.jackson.annotation.JsonPropertyOrder({"timestamp",
		"pressure",
		"temperature",
		"orientationAzimuth",
		"orientationRoll",
		"orientationPitch"})
public class EnvironmentReading {
	private long timestamp;
	private float pressure;
	private float temperature;
	private float orientationAzimuth;
	private float orientationRoll;
	private float orientationPitch;

	public EnvironmentReading(long timestamp, float pressure, float temperature, float orientationAzimuth, float orientationRoll, float orientationPitch) {
		this.timestamp = timestamp;
		this.pressure = pressure;
		this.temperature = temperature;
		this.orientationAzimuth = orientationAzimuth;
		this.orientationRoll = orientationRoll;
		this.orientationPitch = orientationPitch;
	}

	public float getOrientationAzimuth() {
		return orientationAzimuth;
	}

	public void setOrientationAzimuth(float orientationAzimuth) {
		this.orientationAzimuth = orientationAzimuth;
	}

	public float getOrientationPitch() {
		return orientationPitch;
	}

	public void setOrientationPitch(float orientationPitch) {
		this.orientationPitch = orientationPitch;
	}

	public float getOrientationRoll() {
		return orientationRoll;
	}

	public void setOrientationRoll(float orientationRoll) {
		this.orientationRoll = orientationRoll;
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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public final class Record implements BaseColumns {
		public static final String TABLE_NAME = "readings";
		public static final String COLUMN_NAME_PRESSURE = "pressure";
		public static final String COLUMN_NAME_TEMPERATURE = "temperature";
		public static final String COLUMN_NAME_ORIENTATION_AZIMUTH = "orientationAzimuth";
		public static final String COLUMN_NAME_ORIENTATION_PITCH = "orientationPitch";
		public static final String COLUMN_NAME_ORIENTATION_ROLL = "orientationRoll";
		public static final String COLUMN_NAME_LIGHTLEVEL = "lightLevel";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_DIVE_KEY = "dive";
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
}
