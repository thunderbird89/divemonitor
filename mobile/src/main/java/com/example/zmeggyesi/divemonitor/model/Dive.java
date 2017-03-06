package com.example.zmeggyesi.divemonitor.model;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class Dive implements Serializable {
	private Date startDate;
	private Date endDate;
	private Location location;
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
}
