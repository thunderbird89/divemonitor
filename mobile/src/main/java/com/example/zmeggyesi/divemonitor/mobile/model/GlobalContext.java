package com.example.zmeggyesi.divemonitor.mobile.model;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.divemonitor_commons.model.EnvironmentReading;
import com.example.zmeggyesi.divemonitor.mobile.activity.DatabaseManipulation;
import com.example.zmeggyesi.divemonitor.mobile.service.DiveDatabaseHelper;
import com.example.zmeggyesi.divemonitor.mobile.service.EnvironmentReadingDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
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
	private Node selectedNode;

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

	public Node getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.d(TAG, "API Connected");
		Intent bi = new Intent("apiConnected");
		sendBroadcast(bi);
	}

	@Override
	public void onConnectionSuspended(int i) {
		switch (i) {
			case GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST:
				Log.w(TAG, "Network disrupted");
				break;
			case GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED:
				Log.w(TAG, "Network service disconnected or killed!");
				break;
			default:
				Log.wtf(TAG, "API Client killed for an unknown reason, not in constants!");
				break;
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.wtf(TAG, "Connection Failed");
		Log.d(TAG, "Failure cause: " + connectionResult.getErrorMessage());
		throw new RuntimeException("Could not connect to Google API");
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

	@Override
	public void onTerminate() {
		divesHelper.close();
		environmentReadingsHelper.close();
		apiClient.disconnect();
		super.onTerminate();
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		Log.d(TAG, "Receiving data event");
		for (DataEvent event : dataEventBuffer) {
			if (event.getType() == DataEvent.TYPE_CHANGED & event.getDataItem().getUri().getPath().equals("/logDB")) {
				event.freeze();
				DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
				Asset asset = item.getDataMap().getAsset("data");
				new Backgroundtask().execute(asset);
				Wearable.DataApi.deleteDataItems(apiClient, event.getDataItem().getUri());
			}
		}
		dataEventBuffer.release();
	}

	private void setupDBs() {
		divesHelper = new DiveDatabaseHelper(this);
		environmentReadingsHelper = new EnvironmentReadingDatabaseHelper(this);
	}

	public class Backgroundtask extends AsyncTask<Asset, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Asset... assets) {
			Log.d(TAG, "Beginning import");
			String pathname = getDatabasePath(divesHelper.getDatabaseName()).getParent();
			File remoteDB = new File(pathname, "remoteReadings.db");
			InputStream is = Wearable.DataApi.getFdForAsset(apiClient, assets[0]).await().getInputStream();
			OutputStream os = null;
			try {
				os = new FileOutputStream(remoteDB);
				IOUtils.copy(is, os);
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			SQLiteDatabase readings = environmentReadingsHelper.getWritableDatabase();
			readings.execSQL("ATTACH \"" + remoteDB.getPath() + "\" AS remote;");
			readings.execSQL("BEGIN TRANSACTION;");
			readings.execSQL("INSERT OR IGNORE INTO " + EnvironmentReading.Record.TABLE_NAME + " (timestamp, dive, lightLevel, pressure, temperature, orientationAzimuth, orientationPitch, orientationRoll) SELECT ALL timestamp, dive, lightLevel, pressure, temperature, orientationAzimuth, orientationPitch, orientationRoll FROM remote.readings;");
			readings.execSQL("COMMIT;");
			readings.execSQL("DETACH remote;");
			remoteDB.delete();
			Log.d(TAG, "Import finished");

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Wearable.DataApi.removeListener(apiClient, GlobalContext.this);
				Log.d(TAG, "Sending callback to clear watch memory");
				Intent i = new Intent(getApplicationContext(), DatabaseManipulation.class);
				i.putExtra("retrievalComplete", true);
				startActivity(i);
			}
		}
	}
}
