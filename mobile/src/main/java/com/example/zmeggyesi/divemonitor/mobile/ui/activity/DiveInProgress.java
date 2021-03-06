package com.example.zmeggyesi.divemonitor.mobile.ui.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.mobile.model.Dive;
import com.example.zmeggyesi.divemonitor.mobile.model.GlobalContext;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DiveInProgress extends Activity {

	private final String TAG = "Remote";
	private NotificationManager notificationManager;
	private TextView output;
	private Dive dive;
	private String remoteId;
	private GoogleApiClient client;
	private SQLiteDatabase divesDB;
	private Long diveKey;
	private GlobalContext gc;

	public void closeDive(View view) {
		Log.d(TAG, "Sending message to " + remoteId);
		PendingResult res = Wearable.MessageApi.sendMessage(client, remoteId, "/endMonitoring", null);
		res.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
			@Override
			public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
				Log.d(TAG, sendMessageResult.getStatus().toString());
			}
		}, 10, TimeUnit.SECONDS);
		dive.setEndDate(new Date());
		notificationManager.cancel(0);
		ContentValues diveCV = new ContentValues();
		diveCV.put(Dive.Record.COLUMN_NAME_END_TIMESTAMP, dive.getEndDate().getTime());
		String[] args = {diveKey.toString()};
		int result = divesDB.update(Dive.Record.TABLE_NAME, diveCV, Dive.Record._ID + " = ?", args);
		Intent intent = new Intent(this, Home.class);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		divesDB = gc.getDivesDatabase(true);

		setContentView(R.layout.activity_dive_in_progress);
		Notification notification = new Notification.Builder(this)
				.setOngoing(true)
				.setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
				.setContentTitle("Dive in progress...")
				.setContentText("Owner is now diving. Please wait warmly until surfaced...")
				.build();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
		output = (TextView) findViewById(R.id.output);
		dive = (Dive) getIntent().getSerializableExtra("dive");
		remoteId = getIntent().getStringExtra("remoteMonitorId");
		diveKey = getIntent().getLongExtra("diveKey", -1L);
		output.setText(dive.toString());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		client = gc.getClient();
	}

	@Override
	protected void onResume() {
		super.onResume();
		client = gc.getClient();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
