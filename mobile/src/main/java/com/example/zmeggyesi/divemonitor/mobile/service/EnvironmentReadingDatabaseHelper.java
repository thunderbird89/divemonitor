package com.example.zmeggyesi.divemonitor.mobile.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.mobile.model.Dive;
import com.example.divemonitor_commons.model.EnvironmentReading;

/**
 * Created by zmeggyesi on 2017. 03. 17..
 */

public class EnvironmentReadingDatabaseHelper extends SQLiteOpenHelper {
	public EnvironmentReadingDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private static final String DATABASE_NAME = "dives.db";
	private static final int DATABASE_VERSION = 2;
	public static final String TABLE_CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + EnvironmentReading.Record.TABLE_NAME + " (" +
			EnvironmentReading.Record._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			EnvironmentReading.Record.COLUMN_NAME_DIVE_KEY + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_LIGHTLEVEL + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_AZIMUTH + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_PITCH + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_ROLL + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_PRESSURE + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_TEMPERATURE + " INTEGER," +
			EnvironmentReading.Record.COLUMN_NAME_TIMESTAMP + " INTEGER," +
			"FOREIGN KEY (" + EnvironmentReading.Record.COLUMN_NAME_DIVE_KEY + ") REFERENCES " + Dive.Record.TABLE_NAME + "(" + Dive.Record._ID + "));";

	@Override
	public SQLiteDatabase getWritableDatabase() {
		return super.getWritableDatabase();
	}

	@Override
	public SQLiteDatabase getReadableDatabase() {
		return super.getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DB", "Upgrading database: " + oldVersion + " -> " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + EnvironmentReading.Record.TABLE_NAME);
		db.execSQL(TABLE_CREATE_QUERY);

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
