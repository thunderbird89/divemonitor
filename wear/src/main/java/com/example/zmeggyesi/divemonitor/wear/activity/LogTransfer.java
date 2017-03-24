package com.example.zmeggyesi.divemonitor.wear.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zmeggyesi.divemonitor.wear.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.wear.services.RemoteEnvironmentDatabaseHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zmeggyesi on 2017. 03. 24..
 */

public class LogTransfer extends Activity {

	private GoogleApiClient client;
	private RemoteEnvironmentDatabaseHelper redbh;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalContext gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		client.connect();
		redbh = gc.getRemoteEnvironmentDatabaseHelper();
		sendLogs();
	}

	private void sendLogs() {
		Log.d("API", "Transmitting logs...");
		try {
			File dbFile = getApplicationContext().getDatabasePath(redbh.getDatabaseName());
			Asset asset = Asset.createFromBytes(IOUtils.toByteArray(new FileInputStream(dbFile)));
			PutDataMapRequest pdmr = PutDataMapRequest.create("/logDB");
			pdmr.getDataMap().putAsset("data", asset);
			PutDataRequest request = pdmr.asPutDataRequest();
			request.setUrgent();
			Wearable.DataApi.putDataItem(client, request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
