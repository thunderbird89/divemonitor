package com.example.zmeggyesi.divemonitor.model;

import android.location.Location;
import android.provider.BaseColumns;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class Dive implements Serializable {
	private Date startDate;
	private Date endDate;

	transient private Location location;
	private SerializableLocation serializableLocation;

	public SerializableLocation getSerializableLocation() {
		return serializableLocation;
	}

	public void setSerializableLocation(SerializableLocation serializableLocation) {
		this.serializableLocation = serializableLocation;
	}

	private List<EnvironmentReading> readings;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
		this.serializableLocation = new SerializableLocation(location);
	}

	public List<EnvironmentReading> getReadings() {
		return readings;
	}

	public void setReadings(List<EnvironmentReading> readings) {
		this.readings = readings;
	}

	public float getSurfacePressure() {
		return surfacePressure;
	}

	public void setSurfacePressure(float surfacePressure) {
		this.surfacePressure = surfacePressure;
	}

	private float surfacePressure;

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public class Record implements BaseColumns {
		public static final String TABLE_NAME = "dives";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_DISPLAY_NAME = "displayName";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_END_TIMESTAMP = "endTimestamp";
	}
}
