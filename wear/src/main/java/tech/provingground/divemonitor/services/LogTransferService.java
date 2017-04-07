package tech.provingground.divemonitor.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import tech.provingground.divemonitor.model.GlobalContext;
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

public class LogTransferService extends IntentService {
	private static final String ACTION_TRANSFER_LOGS = "tech.provingground.divemonitor.wear.TRANSFER_LOGS";
	private static final String ACTION_CLEAR_LOCAL_DB = "tech.provingground.divemonitor.wear.CLEAR_LOCAL_DB";
	private final String TAG = "LogTransferService";

	private GlobalContext gc;
	private GoogleApiClient client;
	private RemoteEnvironmentDatabaseHelper redbh;

	public LogTransferService() {
		super("LogTransferService");
	}

	public static void startLogTransfer(Context context) {
		Intent intent = new Intent(context, LogTransferService.class);
		intent.setAction(ACTION_TRANSFER_LOGS);
		context.startService(intent);
	}

	public static void clearLocalDB(Context context) {
		Intent intent = new Intent(context, LogTransferService.class);
		intent.setAction(ACTION_CLEAR_LOCAL_DB);
		context.startService(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		redbh = gc.getRemoteEnvironmentDatabaseHelper();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_TRANSFER_LOGS.equals(action)) {
				handleLogTransfer();
			} else if (ACTION_CLEAR_LOCAL_DB.equals(action)) {
				clearLocalDB();
			}
		}
	}

	private void clearLocalDB() {
		Log.d(TAG, "Dropping readings table as per instruction");
		gc.getEnvironmentReadingsDatabase().close();
		gc.deleteDatabase(redbh.getDatabaseName());
		gc.setupDBs();
		Log.d(TAG, "Local DB cleared");
	}

	private void handleLogTransfer() {
		Log.d(TAG, "Transmitting logs...");
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
					Log.d(TAG, "Logs sent...");
				}
			}, 10, TimeUnit.SECONDS);
		} catch (IOException e) {
			Log.e(TAG, "Could not send logs to phone!", e);
		}
	}
}
