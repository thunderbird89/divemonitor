package com.example.zmeggyesi.divemonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

public class DatabaseService extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "diveEnvironmentData";
    private static final int DATABASE_VERSION = 1;

    public DatabaseService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
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
