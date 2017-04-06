package com.example.zmeggyesi.divemonitor.wear.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("mm:ss.SS", Locale.US);
	private static final String TAG = "DataLayer";

	private long lastReading;
	private BoxInsetLayout mContainerView;
	private TextView pressureDisplay;
	private TextView clockDisplay;
	private TextView depthDisplay;
	private TextView durationDisplay;

	private final TextView[] CORE_ELEMENTS = {
			clockDisplay,
			pressureDisplay,
			depthDisplay
	};

	private TextView temperatureDisplay;
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
	private float pressureReading;
	private float depth;
	private LocalBroadcastManager localBroadcastManager;
	private long timestamp;

	private BroadcastReceiver readingReadyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getString(R.string.broadcast_reading_pressure).equals(intent.getAction())) {
				pressureReading = intent.getFloatExtra("data", 0);
				timestamp = System.currentTimeMillis();
				if (surfacePressure == 0) {
					Log.d(TAG, "Setting reference pressure from first reading");
					surfacePressure = pressureReading;
				}
				computeDepth(pressureReading, timestamp);
			}
		}
	};
	private final BroadcastReceiver terminationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Terminating monitoring");
			setAutoResumeEnabled(false);
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
	private AlarmManager am;
	private PendingIntent pendingIntent;

	private void displayPressure(String format) {
		pressureDisplay.setText(format);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalContext gc = (GlobalContext) getApplicationContext();
		manager = gc.getSensorManager();
		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		setContentView(R.layout.activity_monitor);
		setAmbientEnabled();
		setAutoResumeEnabled(true);
		registerReceiver(terminationReceiver, new IntentFilter("terminateMonitoring"));

		IntentFilter readingReadyFilter = new IntentFilter();
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_pressure));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_temperature));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_orientation));
		readingReadyFilter.addAction(getString(R.string.broadcast_reading_light));

		localBroadcastManager.registerReceiver(readingReadyReceiver, readingReadyFilter);

		mContainerView = (BoxInsetLayout) findViewById(R.id.container);

		setupDisplayElements();

		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent ambientStateIntent = new Intent(gc, Monitor.class);
		Intent[] intents = {ambientStateIntent};
		pendingIntent = PendingIntent.getActivities(gc, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
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

		IntentFilter filter = new IntentFilter();
		filter.addAction(getResources().getString(R.string.listener_ready_action));
		if (getIntent().getBooleanExtra("launchedFromPhone", false)) {
			localBroadcastManager.registerReceiver(listenerReadyReceiver, filter);
		}
	}

	private void setupDisplayElements() {
		pressureDisplay = (TextView) findViewById(R.id.pressure);
		pressureDisplay.setVisibility(View.VISIBLE);
		temperatureDisplay = (TextView) findViewById(R.id.temperature);
		temperatureDisplay.setVisibility(View.GONE);
		clockDisplay = (TextView) findViewById(R.id.clock);
		clockDisplay.setVisibility(View.VISIBLE);
		durationDisplay = (TextView) findViewById(R.id.duration);
		durationDisplay.setVisibility(View.GONE);
		depthDisplay = (TextView) findViewById(R.id.depth);
		depthDisplay.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(terminationReceiver);
		super.onStop();
	}

	@Override
	protected void onStart() {
		registerReceiver(terminationReceiver, new IntentFilter("terminateMonitoring"));
		super.onStart();
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
		refreshAndReschedule();
	}

	@Override
	public void onUpdateAmbient() {
		super.onUpdateAmbient();
		updateDisplay();
		refreshAndReschedule();
	}

	@Override
	public void onExitAmbient() {
		updateDisplay();
		super.onExitAmbient();
		am.cancel(pendingIntent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (!"com.example.zmeggyesi.divemonitor.START_MONITORING".equals(intent.getAction())) {
			refreshAndReschedule();
		}
	}

	private void refreshAndReschedule() {

		updateDisplay();

		long time = System.currentTimeMillis();
		if (isAmbient()) {
			long delay = TimeUnit.SECONDS.toMillis(1L) - (time % TimeUnit.SECONDS.toMillis(1L));
			long triggerTime = time + delay;
			am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
		}
	}

	private void registerListener(String listener) {
		switch (listener) {
			case OrientationHandler.TAG:
				manager.registerListener(ol, magneto, SensorManager.SENSOR_DELAY_NORMAL);
				manager.registerListener(ol, accelero, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case TemperatureHandler.TAG:
				manager.registerListener(th, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case PressureHandler.TAG:
				manager.registerListener(ph, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case LightLevelHandler.TAG:
				manager.registerListener(lh, light, SensorManager.SENSOR_DELAY_NORMAL);
				break;
		}
	}

	private void computeDepth(float reading, long timestamp) {
		if (timestamp - lastReading > TimeUnit.SECONDS.toMillis(1L)) {
			depth = SensorManager.getAltitude(surfacePressure, reading) * -1;
			lastReading = timestamp;
		}
	}

	private void updateDisplay() {
		Log.d(TAG, "Updating display in mode " + Boolean.toString(isAmbient()));
		if (isAmbient()) {
			mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
			pressureDisplay.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
			depthDisplay.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
			durationDisplay.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
			clockDisplay.setTextColor(getResources().getColor(R.color.white, null));

			clockDisplay.setVisibility(View.VISIBLE);
			pressureDisplay.setVisibility(View.VISIBLE);
			depthDisplay.setVisibility(View.VISIBLE);

			clockDisplay.setText(AMBIENT_DATE_FORMAT.format(new Date()));

			depthDisplay.setText(String.format(getString(R.string.depth_format), depth));
			pressureDisplay.setText(String.format(getString(R.string.pressure_format), pressureReading));
			durationDisplay.setText(DURATION_FORMAT.format(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
		} else {
			mContainerView.setBackground(null);

			clockDisplay.setVisibility(View.VISIBLE);
			pressureDisplay.setVisibility(View.VISIBLE);
			depthDisplay.setVisibility(View.VISIBLE);

			clockDisplay.setTextColor(getResources().getColor(R.color.black, getTheme()));
			clockDisplay.setText(AMBIENT_DATE_FORMAT.format(new Date()));

			depthDisplay.setTextColor(getResources().getColor(R.color.black, getTheme()));
			depthDisplay.setText(String.format(getString(R.string.depth_format), depth));
			pressureDisplay.setTextColor(getResources().getColor(R.color.black, getTheme()));
			pressureDisplay.setText(String.format(getString(R.string.pressure_format), pressureReading));
		}
	}
}
