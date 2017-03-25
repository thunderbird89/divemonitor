package com.example.divemonitor_commons.model;

import com.google.gson.Gson;

/**
 * Created by zmeggyesi on 2017. 03. 25..
 */

public class DiveInitData {
	private long key;
	private float surfacePressure;

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public long getKey() {
		return key;
	}

	public DiveInitData() {
	}

	public void setKey(long key) {
		this.key = key;
	}

	public float getSurfacePressure() {
		return surfacePressure;
	}

	public void setSurfacePressure(float surfacePressure) {
		this.surfacePressure = surfacePressure;
	}
}
