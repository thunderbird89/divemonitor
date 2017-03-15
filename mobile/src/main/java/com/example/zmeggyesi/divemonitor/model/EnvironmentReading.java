package com.example.zmeggyesi.divemonitor.model;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

class EnvironmentReading {
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
}
