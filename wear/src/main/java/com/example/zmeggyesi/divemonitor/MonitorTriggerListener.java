package com.example.zmeggyesi.divemonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

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
            Intent i = new Intent(this, Monitor.class);
            i.putExtra("surfacePressure", new Float(500));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            Intent monitoring = new Intent(this, MonitorService.class);
            startService(monitoring);
        }
    }
}
