package tech.provingground.divemonitor.model;

import android.location.Location;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by zmeggyesi on 2017. 03. 06..
 */

public class SerializableLocation implements Serializable {
	public static final String FORMAT = "Lat: %1$,.3f; Long: %2$,.3f; Alt: %3$,.3f; Acc: %4$,.3f";
	private double latitude;
	private double longitude;
	private double altitude;
	private float accuracy;

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

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, FORMAT,
				this.latitude,
				this.longitude,
				this.altitude,
				this.accuracy);
	}
}
