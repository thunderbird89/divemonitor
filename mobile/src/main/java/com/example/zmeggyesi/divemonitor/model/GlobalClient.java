package com.example.zmeggyesi.divemonitor.model;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by zmeggyesi on 2017. 03. 07..
 */

public class GlobalClient extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private GoogleApiClient apiClient;

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
		Log.wtf("App", "Connection Failed");
		throw new RuntimeException("Could not connect to Google API");
	}
}
