package com.example.zmeggyesi.divemonitor.model.contracts;

import android.provider.BaseColumns;

/**
 * Created by zmeggyesi on 2017. 03. 15..
 */

public final class DiveContract {
	private DiveContract() {
	}
	public class Record implements BaseColumns {
		public static final String TABLE_NAME = "dives";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_DISPLAY_NAME = "displayName";
	}
}
