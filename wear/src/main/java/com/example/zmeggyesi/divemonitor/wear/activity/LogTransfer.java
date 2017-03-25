package com.example.zmeggyesi.divemonitor.wear.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.wear.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.wear.services.RemoteEnvironmentDatabaseHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by zmeggyesi on 2017. 03. 24..
 */

public class LogTransfer extends Activity {

	private GoogleApiClient client;
	private RemoteEnvironmentDatabaseHelper redbh;
	private GlobalContext gc;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		redbh = gc.getRemoteEnvironmentDatabaseHelper();

		Intent i = getIntent();
		if (i.getBooleanExtra("retrievalComplete", false)) {
			deleteLogs();
		} else {
			sendLogs();
		}
	}

	private void sendLogs() {
		Log.d("API", "Transmitting logs...");
		try {
			File dbFile = getApplicationContext().getDatabasePath(redbh.getDatabaseName());
			Asset asset = Asset.createFromBytes(IOUtils.toByteArray(new FileInputStream(dbFile)));
			PutDataMapRequest pdmr = PutDataMapRequest.create("/logDB");
			pdmr.getDataMap().putAsset("data", asset);
			pdmr.getDataMap().putLong("timestamp", System.currentTimeMillis());
			final PutDataRequest request = pdmr.asPutDataRequest();
			request.setUrgent();
			PendingResult res = Wearable.DataApi.putDataItem(client, request);
			res.setResultCallback(new ResultCallback() {
				@Override
				public void onResult(@NonNull Result result) {
					Log.d("API", "Logs sent...");
					finish();
				}
			}, 10, TimeUnit.SECONDS);
		} catch (IOException e) {
			Log.e("Database", "Could not send logs to phone!", e);
		}
	}

	private void deleteLogs() {
		Log.d("Database", "Dropping readings table as per instruction");
		gc.getEnvironmentReadingsDatabase().close();
		gc.deleteDatabase(redbh.getDatabaseName());
		gc.setupDBs();
	}
}
