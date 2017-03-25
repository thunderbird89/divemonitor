package com.example.zmeggyesi.divemonitor.mobile.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.divemonitor_commons.model.DiveInitData;
import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.mobile.model.Dive;
import com.example.zmeggyesi.divemonitor.mobile.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.mobile.model.SerializableLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreDive extends Activity implements SensorEventListener, AdapterView.OnItemSelectedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

	private final String TAG = "API";
	private GoogleApiClient client;
	private Node selectedNode;
	private TextView outputArea;
	private Map<String, Node> nodeMap;
	private float surfacePressure;
	private Location location;
	private SQLiteDatabase divesDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection);
		outputArea = (TextView) findViewById(R.id.output);
		GlobalContext gc = (GlobalContext) getApplicationContext();
		divesDB = gc.getDivesDatabase(true);
		client = gc.getClient();
		initializeLocation();
		watchCapabilities();
		initiateConnection();
		initialScan();
		initializeSensors();
		Spinner remoteMonitor = (Spinner) findViewById(R.id.remoteMonitor);
		remoteMonitor.setOnItemSelectedListener(this);
	}

	private void initializeLocation() {
		LocationRequest req = new LocationRequest();
		req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		req.setInterval(5l);
		checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
		LocationServices.FusedLocationApi.requestLocationUpdates(client, req, new com.google.android.gms.location.LocationListener() {
			@Override
			public void onLocationChanged(Location momentaryLocation) {
				location = momentaryLocation;
			}
		});
	}

	private void initializeSensors() {
		SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		manager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void initiateConnection() {
		client.connect();
	}

	public void scanDevices(View view) {
		PendingResult<CapabilityApi.GetCapabilityResult> result =
				Wearable.CapabilityApi.getCapability(
						client, "dive_monitor",
						CapabilityApi.FILTER_REACHABLE);
		result.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
			@Override
			public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
				updateMonitorList(getCapabilityResult.getCapability());
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void initialScan() {
		Log.d(TAG, "Performing initial capability scan");
		PendingResult<CapabilityApi.GetCapabilityResult> result =
				Wearable.CapabilityApi.getCapability(
						client, "dive_monitor",
						CapabilityApi.FILTER_REACHABLE);
		result.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
			@Override
			public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
				updateMonitorList(getCapabilityResult.getCapability());
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void beginDive(View view) {
		Dive dive = new Dive();
		dive.setStartDate(new Date());
		dive.setLocation(location);
		dive.setSerializableLocation(new SerializableLocation(location));
		dive.setSurfacePressure(surfacePressure);

		ContentValues diveCV = new ContentValues();
		diveCV.put(Dive.Record.COLUMN_NAME_LOCATION, dive.getSerializableLocation().toString());
		diveCV.put(Dive.Record.COLUMN_NAME_TIMESTAMP, dive.getStartDate().getTime());
		long diveKey = divesDB.insert(Dive.Record.TABLE_NAME, null, diveCV);
		Log.d(TAG, "New dive saved with key " + diveKey);

		final DiveInitData initData = new DiveInitData();
		initData.setKey(diveKey);
		initData.setSurfacePressure(surfacePressure);

		if (selectedNode == null) {
			Log.d(TAG, "Locating remote monitor node...");
			PendingResult<CapabilityApi.GetCapabilityResult> result =
					Wearable.CapabilityApi.getCapability(
							client, "dive_monitor",
							CapabilityApi.FILTER_REACHABLE);
			result.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
				@Override
				public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
					updateMonitorList(getCapabilityResult.getCapability());
					sendMonitoringStartMessage(initData);
				}
			});
		} else {
			sendMonitoringStartMessage(initData);
		}

		Intent i = new Intent(this, DiveInProgress.class);
		i.putExtra("dive", dive);
		i.putExtra("remoteMonitorId", selectedNode.getId());
		i.putExtra("diveKey", diveKey);
		startActivity(i);
	}

	private void sendMonitoringStartMessage(DiveInitData initData) {
		Log.d(TAG, "Sending message to " + selectedNode.getDisplayName());

		Wearable.MessageApi.sendMessage(client, selectedNode.getId(),
				"/startMonitoring",
				initData.toString().getBytes(StandardCharsets.UTF_8))
				.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
					@Override
					public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
						Log.d("Remote", sendMessageResult.getStatus().toString());
					}
				});
	}

	public void retrieveLogs(View view) {
		Log.d(TAG, "Retrieving logs from " + selectedNode.getDisplayName());
		Wearable.MessageApi.sendMessage(client, selectedNode.getId(),
				"/getLogs", null);
	}

	public void closeConnection(View view) {
		Log.d(TAG, "PreDive closed");
		Intent i = new Intent(this, Home.class);
		startActivity(i);
	}

	private void watchCapabilities() {
		CapabilityApi.CapabilityListener listener = new CapabilityApi.CapabilityListener() {
			@Override
			public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
				updateMonitorList(capabilityInfo);
			}
		};
		Wearable.CapabilityApi.addCapabilityListener(client, listener, "dive_monitor");
		Log.d(TAG, "Listening for compatible device");
	}

	private void updateMonitorList(CapabilityInfo capabilityInfo) {
		Log.d(TAG, "Connected capabilities changed");
		Set<Node> nodes = capabilityInfo.getNodes();
		populateNodeList(nodes);
	}

	private void populateNodeList(Set<Node> nodes) {
		Log.d(TAG, "Selecting dive monitor device");
		nodeMap = new HashMap<String, Node>();
		Node bestNode = null;
		for (Node node : nodes) {
			nodeMap.put(node.getDisplayName(), node);
		}
		List<CharSequence> nodeNames = new ArrayList<>();
		for (String n : nodeMap.keySet()) {
			Node node = nodeMap.get(n);
			if (node.isNearby()) {
				Log.d(TAG, "Found device in direct connection: " + node.getDisplayName());
				Log.d(TAG, "Dive monitor device selected: " + node.getId());
				nodeNames.add(0, n);
			} else {
				nodeNames.add(n);
			}
		}
		Spinner remoteMonitor = (Spinner) findViewById(R.id.remoteMonitor);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item,
				nodeNames);
		Log.d(TAG, "Dive monitor device selected: " + bestNode);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		remoteMonitor.setAdapter(adapter);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			surfacePressure = event.values[0];
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String nodeDisplayName = (String) parent.getItemAtPosition(position);
		selectedNode = nodeMap.get(nodeDisplayName);
		outputArea.setText("Connected to " + selectedNode.getDisplayName());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		String nodeDisplayName = (String) parent.getItemAtPosition(0);
		selectedNode = nodeMap.get(nodeDisplayName);
		outputArea.setText("Connected to " + selectedNode.getDisplayName());

	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.d("PreDive", "Client has connected");
	}

	@Override
	public void onConnectionSuspended(int i) {

	}
}
