package com.example.zmeggyesi.divemonitor.model;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.services.RemoteEnvironmentDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by zmeggyesi on 2017. 03. 07..
 */

public class GlobalContext extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private GoogleApiClient apiClient;
	private RemoteEnvironmentDatabaseHelper remoteEnvironmentDatabaseHelper;

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
		Log.d("App", "API Connected");
		Intent bi = new Intent("apiConnected");
		sendBroadcast(bi);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.wtf("App", "PreDive Failed");
		throw new RuntimeException("Could not connect to Google API");
	}

	private void setupDBs() {
		remoteEnvironmentDatabaseHelper = new RemoteEnvironmentDatabaseHelper(this);
	}

	public SQLiteDatabase getEnvironmentReadingsDatabase(boolean rw) {
		if (rw) {
			SQLiteDatabase writableDatabase = remoteEnvironmentDatabaseHelper.getWritableDatabase();
			writableDatabase.setForeignKeyConstraintsEnabled(true);
			return writableDatabase;
		} else {
			SQLiteDatabase readableDatabase = remoteEnvironmentDatabaseHelper.getReadableDatabase();
			return readableDatabase;
		}
	}
}
