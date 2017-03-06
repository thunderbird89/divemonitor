package com.example.zmeggyesi.divemonitor;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.model.Dive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class DiveInProgress extends Activity {

	private NotificationManager notificationManager;
	private TextView output;
	private Dive dive;
	private String remoteId;
	private GoogleApiClient client = getGoogleAPIClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		output.setText(dive.getStartDate().toString());
	}

	@NonNull
	private GoogleApiClient getGoogleAPIClient() {
		return new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(@Nullable Bundle bundle) {
						Log.d("API", "Connection Established");
					}

					@Override
					public void onConnectionSuspended(int i) {
						Log.d("API", "Connection Suspended");
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
						Log.wtf("API", "Connection Failed");
					}
				})
				.build();
	}

	public void closeDive(View view) {
		notificationManager.cancel(0);
		Wearable.MessageApi.sendMessage(client, remoteId,
				"/endMonitoring", null)
				.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
					@Override
					public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
						Log.d("Remote", sendMessageResult.getStatus().getStatusMessage());
					}
				});
		dive.setEndDate(new Date());
	}
}
