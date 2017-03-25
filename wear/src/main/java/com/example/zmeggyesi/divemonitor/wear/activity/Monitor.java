package com.example.zmeggyesi.divemonitor.wear.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.wear.model.GlobalContext;
import com.example.zmeggyesi.divemonitor.wear.sensorium.LightLevelHandler;
import com.example.zmeggyesi.divemonitor.wear.sensorium.OrientationHandler;
import com.example.zmeggyesi.divemonitor.wear.sensorium.PressureHandler;
import com.example.zmeggyesi.divemonitor.wear.sensorium.TemperatureHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Monitor extends WearableActivity {
	private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
	private static final String TAG = "DataLayer";

	private long lastReading;
    private BoxInsetLayout mContainerView;
    private TextView pressure;
    private TextView mClockView;
    private TextView temperature;

	private SensorManager manager;
    private float surfacePressure;
	private OrientationHandler ol;
	private LightLevelHandler lh;
	private Sensor magneto;
	private Sensor accelero;
	private Sensor temperatureSensor;
	private Sensor light;
	private PressureHandler ph;
	private Sensor pressureSensor;
	private TemperatureHandler th;
	private final BroadcastReceiver listenerReadyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra("listener")) {
				registerListener(intent.getStringExtra("listener"));
				Log.d(TAG, "Registering listener " + intent.getStringExtra("listener"));
		}
		}
	};
	private LocalBroadcastManager localBroadcastManager;
	private BroadcastReceiver readingReadyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Readings", intent.getAction());
		}
	};

	private final BroadcastReceiver terminationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			manager.unregisterListener(ol);
			manager.unregisterListener(th);
			manager.unregisterListener(ph);
			manager.unregisterListener(lh);
			unregisterReceiver(terminationReceiver);
			localBroadcastManager.unregisterReceiver(listenerReadyReceiver);
			localBroadcastManager.unregisterReceiver(readingReadyReceiver);
			finish();
		}
	};

	private void displayPressure(String format) {
		pressure.setText(format);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalContext gc = (GlobalContext) getApplicationContext();
		manager = gc.getSensorManager();
		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		setContentView(R.layout.activity_monitor);
		setAmbientEnabled();
		registerReceiver(terminationReceiver, new IntentFilter("terminateMonitoring"));
		IntentFilter filter = new IntentFilter();
		filter.addAction(getResources().getString(R.string.listener_ready_action));
		IntentFilter readingReadyFilter = new IntentFilter();
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_pressure));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_temperature));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_orientation));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_light));
		localBroadcastManager.registerReceiver(listenerReadyReceiver, filter);
		localBroadcastManager.registerReceiver(readingReadyReceiver, readingReadyFilter);

		mContainerView = (BoxInsetLayout) findViewById(R.id.container);
		pressure = (TextView) findViewById(R.id.pressure);
		temperature = (TextView) findViewById(R.id.temperature);
		mClockView = (TextView) findViewById(R.id.clock);
		// TODO: return this value from the handler for more precise initialization?
		surfacePressure = getIntent().getFloatExtra("surfacePressure", 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
	    ol = new OrientationHandler(localBroadcastManager, manager, getApplicationContext());
	    lh = new LightLevelHandler(localBroadcastManager, getApplicationContext());
	    ph = new PressureHandler(localBroadcastManager, getApplicationContext());
	    th = new TemperatureHandler(localBroadcastManager, getApplicationContext());
	    magneto = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD, true);
	    accelero = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true);
	    light = manager.getDefaultSensor(Sensor.TYPE_LIGHT, true);
	    temperatureSensor = manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE, true);
	    pressureSensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE, true);
    }

	private void registerListener(String listener) {
		switch (listener) {
			case OrientationHandler.TAG :
				manager.registerListener(ol, magneto, SensorManager.SENSOR_DELAY_NORMAL);
				manager.registerListener(ol, accelero, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case TemperatureHandler.TAG :
				manager.registerListener(th, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case PressureHandler.TAG :
				manager.registerListener(ph, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case LightLevelHandler.TAG :
				manager.registerListener(lh, light, SensorManager.SENSOR_DELAY_NORMAL);
				break;
		}
	}

	@Override
    protected void onPause() {
		super.onPause();
		manager.unregisterListener(ol);
		manager.unregisterListener(th);
		manager.unregisterListener(ph);
		manager.unregisterListener(lh);
	}

	@Override
	public void onEnterAmbient(Bundle ambientDetails) {
		super.onEnterAmbient(ambientDetails);
		updateDisplay();
	}

	@Override
	public void onUpdateAmbient() {
		super.onUpdateAmbient();
		updateDisplay();
	}

	@Override
	public void onExitAmbient() {
		updateDisplay();
		super.onExitAmbient();
	}

    private void readPressure(float reading, long timestamp) {
        if (timestamp - lastReading > TimeUnit.SECONDS.toMicros(1L)) {
            float depth = SensorManager.getAltitude(surfacePressure, reading) * -1;
            pressure.setText(String.format(getResources().getString(R.string.pressure_format), reading, depth));
            lastReading = timestamp;
        }
        updateDisplay();
    }

	private void updateDisplay() {
		if (isAmbient()) {
			mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
			pressure.setTextColor(getResources().getColor(android.R.color.white));
			mClockView.setVisibility(View.VISIBLE);
			mClockView.setTextColor(getResources().getColor(R.color.white, null));
			mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
		} else {
			mContainerView.setBackground(null);
			pressure.setTextColor(getResources().getColor(android.R.color.black));
			mClockView.setVisibility(View.VISIBLE);
		}
	}
}
