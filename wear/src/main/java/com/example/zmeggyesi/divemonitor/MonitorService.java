package com.example.zmeggyesi.divemonitor;

import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by zmeggyesi on 2017. 03. 05..
 */

class MonitorService extends WearableListenerService {
    DatabaseService dbs = new DatabaseService(this);
}
