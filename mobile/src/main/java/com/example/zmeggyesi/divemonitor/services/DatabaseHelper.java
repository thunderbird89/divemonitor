package com.example.zmeggyesi.divemonitor.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zmeggyesi on 2017. 03. 15..
 */

public class DatabaseHelper extends SQLiteOpenHelper {
	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
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

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
