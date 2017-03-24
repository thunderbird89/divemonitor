package com.example.zmeggyesi.divemonitor.mobile.model;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.mobile.service.DiveDatabaseHelper;
import com.example.zmeggyesi.divemonitor.mobile.service.EnvironmentReadingDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zmeggyesi on 2017. 03. 07..
 */

public class GlobalContext extends Application implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private final String TAG = "Global Context";
	private GoogleApiClient apiClient;
	private DiveDatabaseHelper divesHelper;
	private EnvironmentReadingDatabaseHelper environmentReadingsHelper;

	@Override
	public void onTerminate() {
		divesHelper.close();
		environmentReadingsHelper.close();
		apiClient.disconnect();
		super.onTerminate();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		apiClient.connect();
		setupDBs();
	}

	public GoogleApiClient getClient() {
		if (apiClient == null) {
			return apiClient = new GoogleApiClient.Builder(this)
					.addApi(Wearable.API)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		} else {
			if (apiClient.isConnected()) {
				return apiClient;
			} else {
				apiClient.connect();
				return apiClient;
			}
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.d(TAG, "API Connected");
		Intent bi = new Intent("apiConnected");
		Wearable.DataApi.addListener(apiClient, this);
		sendBroadcast(bi);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.wtf(TAG, "PreDive Failed");
		throw new RuntimeException("Could not connect to Google API");
	}

	private void setupDBs() {
		divesHelper = new DiveDatabaseHelper(this);
		environmentReadingsHelper = new EnvironmentReadingDatabaseHelper(this);
	}

	public SQLiteDatabase getEnvironmentReadingsDatabase(boolean rw) {
		if (rw) {
			SQLiteDatabase writableDatabase = environmentReadingsHelper.getWritableDatabase();
			writableDatabase.setForeignKeyConstraintsEnabled(true);
			return writableDatabase;
		} else {
			SQLiteDatabase readableDatabase = environmentReadingsHelper.getReadableDatabase();
			return readableDatabase;
		}
	}

	public SQLiteDatabase getDivesDatabase(boolean rw) {
		if (rw) {
			SQLiteDatabase writableDatabase = divesHelper.getWritableDatabase();
			writableDatabase.setForeignKeyConstraintsEnabled(true);
			return writableDatabase;
		} else {
			SQLiteDatabase readableDatabase = divesHelper.getReadableDatabase();
			return readableDatabase;
		}
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		Log.d(TAG, "receiving data event");
		for (DataEvent event : dataEventBuffer) {
			if (event.getType() == DataEvent.TYPE_CHANGED & event.getDataItem().getUri().getPath().equals("/logDB")) {
				DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
				Asset asset = item.getDataMap().getAsset("data");
				new Backgroundtask().execute(asset);
			}
		}
	}

	public class Backgroundtask extends AsyncTask<Asset, Void, Void> {
		@Override
		protected Void doInBackground(Asset... assets) {
			String pathname = getDatabasePath(divesHelper.getDatabaseName()).getParent();
			File remoteDB = new File(pathname, "remoteReadings.db");
			InputStream is = Wearable.DataApi.getFdForAsset(apiClient, assets[0]).await().getInputStream();
			OutputStream os = null;
			try {
				os = new FileOutputStream(remoteDB);
				IOUtils.copy(is, os);
				Log.d(TAG, "Import finished");
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return null;
		}
	}
}
