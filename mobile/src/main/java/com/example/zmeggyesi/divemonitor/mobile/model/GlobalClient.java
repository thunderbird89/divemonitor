package com.example.zmeggyesi.divemonitor.mobile.model;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.mobile.service.DiveDatabaseHelper;
import com.example.zmeggyesi.divemonitor.mobile.service.EnvironmentReadingDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by zmeggyesi on 2017. 03. 07..
 */

public class GlobalClient extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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
}
