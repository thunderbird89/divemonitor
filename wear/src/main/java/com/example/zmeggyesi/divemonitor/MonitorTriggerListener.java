package com.example.zmeggyesi.divemonitor;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;

public class MonitorTriggerListener extends WearableListenerService {
    public MonitorTriggerListener() {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d("Remote", "Data Changed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d("Remote", "Begin monitoring!");
        if (messageEvent.getPath().equals("/startMonitoring")) {
            Intent monitoringStart = new Intent(this, Monitor.class);
            float surfacePressure = ByteBuffer.wrap(messageEvent.getData()).getFloat();
            Log.d("Remote", "Received surface pressure from remote device: " + surfacePressure);
            monitoringStart.putExtra("surfacePressure", surfacePressure != 0 ? surfacePressure : SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
            monitoringStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(monitoringStart);
        }
    }
}
