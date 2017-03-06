package com.example.zmeggyesi.divemonitor.model;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by zmeggyesi on 2017. 03. 06..
 */

public class SerializableLocation implements Serializable {
	private double latitude;
	private double longitude;
	private double altitude;
	private float accuracy;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public SerializableLocation(double latitude, double longitude, double altitude, float accuracy) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.accuracy = accuracy;
	}

	public SerializableLocation(Location loc) {
		this.latitude = loc.getLatitude();
		this.longitude = loc.getLongitude();
		this.altitude = loc.getAltitude();
		this.accuracy = loc.getAccuracy();
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
}
