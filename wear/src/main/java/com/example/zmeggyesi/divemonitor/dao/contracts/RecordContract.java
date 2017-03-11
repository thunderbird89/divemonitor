package com.example.zmeggyesi.divemonitor.dao.contracts;

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
		public static final String COLUMN_NAME_TEMPERATURE = "temperature";
		public static final String COLUMN_NAME_ORIENTATION = "orientation";
		public static final String COLUMN_NAME_LIGHTLEVEL = "lightLevel";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
	}
}
