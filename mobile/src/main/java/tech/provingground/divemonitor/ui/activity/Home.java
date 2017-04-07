package tech.provingground.divemonitor.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import tech.provingground.divemonitor.R;

public class Home extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    public void startMonitor(View view) {
        Intent i = new Intent(this, Monitoring.class);
        startActivity(i);
    }

    public void listDives(View view) {
        Intent i = new Intent(this, DiveList.class);
        startActivity(i);
    }

    public void connectWatch(View view) {
        Intent intent = new Intent(this, PreDive.class);
        startActivity(intent);
    }

    public void startDBManipulation(View view) {
        Intent intent = new Intent(this, DatabaseManipulation.class);
        startActivity(intent);
    }
}
