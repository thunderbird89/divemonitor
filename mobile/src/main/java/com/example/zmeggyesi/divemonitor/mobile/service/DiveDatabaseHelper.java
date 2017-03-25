package com.example.zmeggyesi.divemonitor.mobile.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zmeggyesi.divemonitor.mobile.model.Dive.Record;

/**
 * Created by zmeggyesi on 2017. 03. 15..
 */

public class DiveDatabaseHelper extends SQLiteOpenHelper {
	public DiveDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private static final String DATABASE_NAME = "dives.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + Record.TABLE_NAME + " (" +
			Record._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			Record.COLUMN_NAME_DISPLAY_NAME + " TEXT," +
			Record.COLUMN_NAME_LOCATION + " TEXT," +
			Record.COLUMN_NAME_TIMESTAMP + " INTEGER," +
			Record.COLUMN_NAME_END_TIMESTAMP + " INTEGER);";

	public String getDatabaseName() {
		return super.getDatabaseName();
	}

	@Override
	public SQLiteDatabase getWritableDatabase() {
		return super.getWritableDatabase();
	}

	@Override
	public SQLiteDatabase getReadableDatabase() {
		return super.getReadableDatabase();
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
