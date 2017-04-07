package tech.provingground.divemonitor.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import tech.provingground.divemonitor.R;
import tech.provingground.divemonitor.model.GlobalContext;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;


public class LogTransferService extends IntentService implements DataApi.DataListener {
	public static final String ACTION_GET_LOG_DATABASE = "tech.provingground.divemonitor.GET_LOG_DATABASE";
	public static final String ACTION_SEND_DATABASE_CLEAR_CALLBACK = "tech.provingground.divemonitor.SEND_CALLBACK";
	private static final String TAG = "LogRetrievalService";
	private GoogleApiClient client;
	private GlobalContext gc;
	private String selectedNodeId;

	public LogTransferService() {
		super("LogTransferService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		Wearable.DataApi.addListener(client, this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_GET_LOG_DATABASE.equals(action)) {
				selectedNodeId = intent.getStringExtra("nodeId");
				handleActionGetLogs(selectedNodeId);
			} else if (ACTION_SEND_DATABASE_CLEAR_CALLBACK.equals(action)) {
				String selectedNodeId = intent.getStringExtra("nodeId");
				sendCallback(selectedNodeId);
			}
		}
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEventBuffer) {
		Log.d(TAG, "Receiving data event");
		for (DataEvent event : dataEventBuffer) {
			if (event.getType() == DataEvent.TYPE_CHANGED & event.getDataItem().getUri().getPath().equals("/logDB")) {
				event.freeze();
				DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
				Asset asset = item.getDataMap().getAsset("data");
				Importer importer = new Importer(gc, selectedNodeId);
				importer.execute(asset);
			}
		}
	}

	private void handleActionGetLogs(String nodeId) {
		Log.d(TAG, "Retrieving logs from " + nodeId);
		PendingResult res = Wearable.MessageApi.sendMessage(client, nodeId, "/getLogs", null);
		res.setResultCallback(new ResultCallback() {
			@Override
			public void onResult(@NonNull Result result) {
				Log.d(TAG, result.getStatus().getStatusMessage());
			}
		});
	}

	private void sendCallback(String nodeId) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(gc, getText(R.string.toast_database_retrieved), Toast.LENGTH_LONG).show();
			}
		});

		Log.d(TAG, "Sending callback to remote device...");
		PendingResult res = Wearable.MessageApi.sendMessage(client, nodeId,
				"/logRetrievalComplete", null);
		res.setResultCallback(new ResultCallback() {
			@Override
			public void onResult(@NonNull Result result) {
				Log.d(TAG, result.getStatus().getStatusMessage());
				Wearable.DataApi.removeListener(client, LogTransferService.this);
			}
		});
	}
}
