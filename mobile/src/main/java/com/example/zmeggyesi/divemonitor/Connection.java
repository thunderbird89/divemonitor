package com.example.zmeggyesi.divemonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Connection extends Activity {

    private GoogleApiClient client;
    private String selectedNodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("API Connection", "Connection Established");
                        TextView tw = (TextView) findViewById(R.id.output);
                        tw.setText("Successfully connected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("API Connection", "Connection Suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.wtf("API Connection", "Connection Failed");
                    }
                })
                .build();
        watchCapabilities();
        initiateConnection();
    }

    private void initiateConnection() {
        client.connect();
    }

    public void scanDevices(View view) {
        PendingResult<CapabilityApi.GetAllCapabilitiesResult> result =
                Wearable.CapabilityApi.getAllCapabilities(
                        client,
                        CapabilityApi.FILTER_REACHABLE);
        result.setResultCallback(new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                TextView tw = (TextView) findViewById(R.id.output);
                Map<String, CapabilityInfo> caps = getAllCapabilitiesResult.getAllCapabilities();
                StringBuilder text = new StringBuilder();
                for (String key : caps.keySet()) {
                    CapabilityInfo ci = caps.get(key);
                    text.append(ci.getName());
                    text.append(":");
                    Set<Node> nodes = ci.getNodes();
                    for (Node node : nodes) {
                        text.append(node.getDisplayName());
                        text.append(",");
                    }
                    text.append("\n");
                }
                tw.setText(text.toString());
            }
        });
    }

    public void connectToMonitor(View view) {
        if (selectedNodeId == null) {
            PendingResult<CapabilityApi.GetCapabilityResult> result =
                    Wearable.CapabilityApi.getCapability(
                            client, "dive_monitor",
                            CapabilityApi.FILTER_REACHABLE);
            result.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                @Override
                public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                    updateMonitor(getCapabilityResult.getCapability());
                    sendMonitoringStartMessage();
                }
            });
        } else {
            sendMonitoringStartMessage();
        }
    }

    private void sendMonitoringStartMessage() {
        Log.d("Remote", "Sending message to " + selectedNodeId);
        Wearable.MessageApi.sendMessage(client, selectedNodeId, "/startMonitoring", null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                TextView tw = (TextView) findViewById(R.id.output);
                Log.d("Remote", sendMessageResult.getStatus().getStatusMessage());
                tw.setText("Monitor started");
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
                updateMonitor(capabilityInfo);
            }
        };
        Wearable.CapabilityApi.addCapabilityListener(client, listener, "dive_monitor");
    }

    private void updateMonitor(CapabilityInfo capabilityInfo) {
        Set<Node> nodes = capabilityInfo.getNodes();
        selectedNodeId = selectNode(nodes);
    }

    private String selectNode(Set<Node> nodes) {
        String bestNodeId = null;
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
