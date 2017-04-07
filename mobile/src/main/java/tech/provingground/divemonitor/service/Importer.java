package tech.provingground.divemonitor.service;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import tech.provingground.divemonitor.EnvironmentReading;
import tech.provingground.divemonitor.model.GlobalContext;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zmeggyesi on 2017. 03. 26..
 */

public class Importer extends AsyncTask<Asset, Void, Boolean> {
	private static final String TAG = "BackgroundImporter";
	private final GlobalContext context;
	private final String nodeId;

	public Importer(GlobalContext ctx, String callbackNodeId) {
		super();
		context = ctx;
		nodeId = callbackNodeId;
	}

	@Override
	protected Boolean doInBackground(Asset... assets) {
		Log.d(TAG, "Beginning import");
		String pathname = context.getDatabasePath(context.getDivesHelper().getDatabaseName()).getParent();
		File remoteDB = new File(pathname, "remoteReadings.db");
		InputStream is = Wearable.DataApi.getFdForAsset(context.getClient(), assets[0]).await().getInputStream();
		OutputStream os = null;
		try {
			os = new FileOutputStream(remoteDB);
			IOUtils.copy(is, os);
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		SQLiteDatabase readings = context.getEnvironmentReadingsDatabase(true);
		readings.execSQL("ATTACH \"" + remoteDB.getPath() + "\" AS remote;");
		readings.execSQL("BEGIN TRANSACTION;");
		readings.execSQL("INSERT OR IGNORE INTO " + EnvironmentReading.Record.TABLE_NAME + " (timestamp, dive, lightLevel, pressure, temperature, orientationAzimuth, orientationPitch, orientationRoll) SELECT ALL timestamp, dive, lightLevel, pressure, temperature, orientationAzimuth, orientationPitch, orientationRoll FROM remote.readings;");
		readings.execSQL("COMMIT;");
		readings.execSQL("DETACH remote;");
		String[] columns = {"*"};
		Cursor c = readings.query(EnvironmentReading.Record.TABLE_NAME, columns, null, null, null, null, null);
		Log.d(TAG, Integer.toString(c.getCount(), 10));
		remoteDB.delete();
		Log.d(TAG, "Import finished, releasing data buffer");
		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Intent i = new Intent(context, LogTransferService.class);
			i.setAction(LogTransferService.ACTION_SEND_DATABASE_CLEAR_CALLBACK);
			i.putExtra("nodeId", nodeId);
			context.startService(i);
		}
	}
}
