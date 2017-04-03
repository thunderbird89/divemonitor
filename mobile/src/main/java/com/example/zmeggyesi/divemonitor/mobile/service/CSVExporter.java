package com.example.zmeggyesi.divemonitor.mobile.service;

import android.app.IntentService;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.mobile.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.mobile.service.provider.EnvironmentReadingsContract;
import com.example.zmeggyesi.divemonitor.mobile.service.provider.EnvironmentReadingsProvider;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * Created by zmeggyesi on 2017. 04. 03..
 */

public class CSVExporter extends IntentService implements CursorLoader.OnLoadCompleteListener<Cursor> {

	private EnvironmentReadingsProvider provider;
	public static final String ACTION_START_CSV_EXPORT = "com.example.zmeggyesi.divemonitor.START_CSV_EXPORT";

	public CSVExporter(String name) {
		super(name);
	}

	public CSVExporter() {
		super("CSVExporter");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		provider = new EnvironmentReadingsProvider();
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (intent != null) {
			if (ACTION_START_CSV_EXPORT.equals(intent.getAction())) {
				String diveKey = intent.getStringExtra("diveKey");
				Uri contentUri = new Uri.Builder().scheme("content").authority(EnvironmentReadingsContract.AUTHORITY).path("readings/dive/" + diveKey).build();
				CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, null, null, null, null);
				loader.registerListener(0, this);
				loader.startLoading();
			}
		}
	}

	@Override
	public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
		if (data != null) {
			Log.d("Exporter", Integer.toString(data.getCount()));
		}
	}
}
