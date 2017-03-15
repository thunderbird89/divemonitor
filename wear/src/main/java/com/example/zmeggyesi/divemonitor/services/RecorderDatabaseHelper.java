package com.example.zmeggyesi.divemonitor.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zmeggyesi.divemonitor.dao.contracts.RecordContract.Record;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class RecorderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "diveEnvironmentData.db";
    private static final String TABLE_NAME = "diveEnvironmentData";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_INIT_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            Record._ID + " INTEGER PRIMARY KEY," +
            Record.COLUMN_NAME_TIMESTAMP + " INTEGER," +
            Record.COLUMN_NAME_LIGHTLEVEL + " INTEGER," +
            Record.COLUMN_NAME_PRESSURE + " INTEGER," +
            Record.COLUMN_NAME_TEMPERATURE + "INTEGER)";

    public RecorderDatabaseHelper(Context context) {
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

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
