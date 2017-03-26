package com.example.zmeggyesi.divemonitor.wear.model;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.wear.services.RemoteEnvironmentDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by zmeggyesi on 2017. 03. 07..
 */

public class GlobalContext extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private final String TAG = "Global Context";
	private GoogleApiClient apiClient;
	private long currentDiveKey;

	public long getCurrentDiveKey() {
		return currentDiveKey;
	}

	public void setCurrentDiveKey(long currentDiveKey) {
		this.currentDiveKey = currentDiveKey;
	}

	public RemoteEnvironmentDatabaseHelper getRemoteEnvironmentDatabaseHelper() {
		return remoteEnvironmentDatabaseHelper;
	}

	private RemoteEnvironmentDatabaseHelper remoteEnvironmentDatabaseHelper;

	public SensorManager getSensorManager() {
		return (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
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
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		} else {
			return apiClient;
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

	public void setupDBs() {
		remoteEnvironmentDatabaseHelper = new RemoteEnvironmentDatabaseHelper(this);
		remoteEnvironmentDatabaseHelper.getWritableDatabase();
	}

	public SQLiteDatabase getEnvironmentReadingsDatabase() {
		SQLiteDatabase writableDatabase = remoteEnvironmentDatabaseHelper.getWritableDatabase();
		writableDatabase.setForeignKeyConstraintsEnabled(true);
		return writableDatabase;
	}
}
