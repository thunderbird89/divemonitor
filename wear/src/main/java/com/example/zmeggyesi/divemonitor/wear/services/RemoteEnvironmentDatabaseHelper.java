package com.example.zmeggyesi.divemonitor.wear.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.divemonitor_commons.model.EnvironmentReading;


/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class RemoteEnvironmentDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "environmentReadings.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_INIT_STATEMENT = "CREATE TABLE IF NOT EXISTS " + EnvironmentReading.Record.TABLE_NAME + " (" +
            EnvironmentReading.Record._ID + " INTEGER PRIMARY KEY," +
            EnvironmentReading.Record.COLUMN_NAME_TIMESTAMP + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_LIGHTLEVEL + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_AZIMUTH + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_PITCH + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_ROLL + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_PRESSURE + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_DIVE_KEY + " INTEGER," +
            EnvironmentReading.Record.COLUMN_NAME_TEMPERATURE + " INTEGER)";

    public RemoteEnvironmentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_INIT_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DB", "Upgrading database: " + oldVersion + " -> " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + EnvironmentReading.Record.TABLE_NAME);
		db.execSQL(TABLE_INIT_STATEMENT);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
