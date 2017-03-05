package com.example.zmeggyesi.divemonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Connection extends Activity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    private GoogleApiClient client;
    private Node selectedNode;
    private TextView outputArea;
    private Map<String, Node> nodeMap;
    private float surfacePressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        outputArea = (TextView) findViewById(R.id.output);
        client = getGoogleAPIClient();
        watchCapabilities();
        initiateConnection();
        initialScan();
        initializeSensors();
        Spinner remoteMonitor = (Spinner) findViewById(R.id.remoteMonitor);
        remoteMonitor.setOnItemSelectedListener(this);
    }

    private void initializeSensors() {
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        manager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @NonNull
    private GoogleApiClient getGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("API", "Connection Established");
                        outputArea.setText("Successfully connected");
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

    private void initialScan() {
        Log.d("API", "Performing initial capability scan");
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

    public void connectToMonitor(View view) {
        if (selectedNode == null) {
            PendingResult<CapabilityApi.GetCapabilityResult> result =
                    Wearable.CapabilityApi.getCapability(
                            client, "dive_monitor",
                            CapabilityApi.FILTER_REACHABLE);
            result.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                @Override
                public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                    updateMonitorList(getCapabilityResult.getCapability());
                    sendMonitoringStartMessage();
                }
            });
        } else {
            sendMonitoringStartMessage();
        }
    }

    private void sendMonitoringStartMessage() {
        Log.d("Remote", "Sending message to " + selectedNode.getDisplayName());

        Wearable.MessageApi.sendMessage(client, selectedNode.getId(),
                "/startMonitoring",
                ByteBuffer.allocate(64).putFloat(surfacePressure).array())
                .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        Log.d("Remote", sendMessageResult.getStatus().getStatusMessage());
                        outputArea.setText("Monitor started");
                    }
                });
    }

    public void closeConnection(View view) {
        client.disconnect();
        Log.d("API Connection", "Connection closed");
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
        Log.d("API", "Listening for compatible device");
    }

    private void updateMonitorList(CapabilityInfo capabilityInfo) {
        Log.d("API", "Connected capabilities changed");
        Set<Node> nodes = capabilityInfo.getNodes();
        populateNodeList(nodes);
    }

    private void populateNodeList(Set<Node> nodes) {
        Log.d("API", "Selecting dive monitor device");
        nodeMap = new HashMap<String, Node>();
        Node bestNode = null;
        for (Node node : nodes) {
            nodeMap.put(node.getDisplayName(), node);
        }
        List<CharSequence> nodeNames = new ArrayList<>();
        for (String n : nodeMap.keySet()) {
            Node node = nodeMap.get(n);
            if (node.isNearby()) {
                Log.d("API", "Found device in direct connection: " + node.getDisplayName());
                Log.d("API", "Dive monitor device selected: " + node.getId());
                nodeNames.add(0, n);
            } else {
                nodeNames.add(n);
            }
        }
        Spinner remoteMonitor = (Spinner) findViewById(R.id.remoteMonitor);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item,
                nodeNames);
                Log.d("API", "Dive monitor device selected: " + bestNode);
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
}
