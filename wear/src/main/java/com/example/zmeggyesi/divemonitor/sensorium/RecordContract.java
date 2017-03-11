package com.example.zmeggyesi.divemonitor.sensorium;

import android.provider.BaseColumns;

/**
 * Created by zmeggyesi on 2017. 03. 11..
 */

public final class RecordContract {
	private RecordContract() {
	}

	public class Record implements BaseColumns {
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
	}
}
