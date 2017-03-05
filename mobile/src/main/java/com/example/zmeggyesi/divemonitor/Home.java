package com.example.zmeggyesi.divemonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Home extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void startMonitor(View view) {
        Intent i = new Intent(this, Monitoring.class);
        startActivity(i);
    }

    public void connectWatch(View view) {
        Intent intent = new Intent(this, Connection.class);
        startActivity(intent);
    }
}