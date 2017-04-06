package com.example.zmeggyesi.divemonitor.mobile.ui.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.divemonitor_commons.model.EnvironmentReading;
import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.mobile.model.Dive;
import com.example.zmeggyesi.divemonitor.mobile.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.mobile.service.DiveDatabaseHelper;
import com.example.zmeggyesi.divemonitor.mobile.service.EnvironmentReadingDatabaseHelper;
import com.example.zmeggyesi.divemonitor.mobile.service.LogTransferService;
import com.example.zmeggyesi.divemonitor.mobile.ui.dialog.DatabasePurgeGate;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zmeggyesi on 2017. 03. 25..
 */

public class DatabaseManipulation extends Activity implements AdapterView.OnItemSelectedListener, DatabasePurgeGate.CallbackListener {
	private GlobalContext gc;
	private static final String TAG = "Database";
	private GoogleApiClient client;
	private Map<String, Node> nodeMap;
	private Node selectedNode;
	private TextView outputArea;

	@Override
	public void initiatePurge(DialogFragment dialog) {
		executePurge();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String nodeDisplayName = (String) parent.getItemAtPosition(position);
		selectedNode = nodeMap.get(nodeDisplayName);
		gc.setSelectedNode(selectedNode);
		outputArea.setText(String.format(getString(R.string.content_connected_to_node), selectedNode.getDisplayName()));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		String nodeDisplayName = (String) parent.getItemAtPosition(0);
		selectedNode = nodeMap.get(nodeDisplayName);
		gc.setSelectedNode(selectedNode);
		outputArea.setText(String.format(getString(R.string.content_connected_to_node), selectedNode.getDisplayName()));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database_manipulation);
		outputArea = (TextView) findViewById(R.id.output);
		gc = (GlobalContext) getApplicationContext();
		client = gc.getClient();
		Spinner remoteMonitor = (Spinner) findViewById(R.id.remoteMonitor);
		remoteMonitor.setOnItemSelectedListener(this);
		watchCapabilities();
		initialScan();
		Intent i = getIntent();
		if (i.getBooleanExtra("retrievalComplete", false)) {
			signalRetrievalComplete();
		}
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

	private void initiateConnection() {
		client.connect();
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
				selectedNode = node;
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

	public void retrieveLogs(View view) {
		Intent intent = new Intent(this, LogTransferService.class);
		intent.setAction(LogTransferService.ACTION_GET_LOG_DATABASE);
		intent.putExtra("nodeId", selectedNode.getId());
		startService(intent);
	}

	public void signalRetrievalComplete() {
		selectedNode = gc.getSelectedNode();
		Log.d(TAG, "Retrieving logs from " + selectedNode.getDisplayName());
		Wearable.MessageApi.sendMessage(client, selectedNode.getId(),
				"/logRetrievalComplete", null);
	}



	public void purgeDatabases(View view) {
		DialogFragment frag = new DatabasePurgeGate();
		frag.show(getFragmentManager(), "purge");
	}

	private void executePurge() {
		Log.d(TAG, "DB Purge starting");
		signalRetrievalComplete();
		SQLiteDatabase dives = gc.getDivesDatabase(true);
		SQLiteDatabase readings = gc.getEnvironmentReadingsDatabase(true);
		readings.execSQL("DROP TABLE IF EXISTS " + EnvironmentReading.Record.TABLE_NAME);
		readings.execSQL(EnvironmentReadingDatabaseHelper.TABLE_CREATE_QUERY);
		dives.execSQL("DROP TABLE IF EXISTS " + Dive.Record.TABLE_NAME);
		dives.execSQL(DiveDatabaseHelper.TABLE_CREATE_QUERY);
		Toast.makeText(this, getString(R.string.content_database_purge_complete), Toast.LENGTH_LONG).show();
	}
}
