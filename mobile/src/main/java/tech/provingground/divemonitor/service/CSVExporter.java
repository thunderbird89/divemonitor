package tech.provingground.divemonitor.service;

import android.app.IntentService;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import tech.provingground.divemonitor.EnvironmentReading;
import tech.provingground.divemonitor.model.GlobalContext;
import tech.provingground.divemonitor.service.provider.EnvironmentReadingsContract;
import tech.provingground.divemonitor.service.provider.EnvironmentReadingsProvider;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zmeggyesi on 2017. 04. 03..
 */

public class CSVExporter extends IntentService implements CursorLoader.OnLoadCompleteListener<Cursor> {

	public static final String TAG = "Exporter";
	private EnvironmentReadingsProvider provider;
	public static final String ACTION_START_CSV_EXPORT = "tech.provingground.divemonitor.START_CSV_EXPORT";
	private GlobalContext gc;

	public CSVExporter(String name) {
		super(name);
	}

	public CSVExporter() {
		super("CSVExporter");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gc = (GlobalContext) getApplicationContext();
		provider = new EnvironmentReadingsProvider();
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (intent != null) {
			if (ACTION_START_CSV_EXPORT.equals(intent.getAction())) {
				int diveKey = intent.getIntExtra("diveKey", 0);
				Uri contentUri = new Uri.Builder().scheme("content").authority(EnvironmentReadingsContract.AUTHORITY).path("readings/dive/" + diveKey).build();
				CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, null, null, null, null);
				loader.registerListener(0, this);
				loader.startLoading();
			}
		}
	}

	@Override
	public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
		if (data != null) {
			Log.d(TAG, Integer.toString(data.getCount()));
			List<EnvironmentReading> exportedReadings = new ArrayList<>();
			data.moveToFirst();
			while (!data.isAfterLast()) {
				Log.d(TAG, Integer.toString(data.getPosition()));

				EnvironmentReading reading = new EnvironmentReading(data.getLong(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_TIMESTAMP)),
						data.getFloat(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_PRESSURE)),
						data.getFloat(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_TEMPERATURE)),
						data.getFloat(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_AZIMUTH)),
						data.getFloat(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_ROLL)),
						data.getFloat(data.getColumnIndex(EnvironmentReading.Record.COLUMN_NAME_ORIENTATION_PITCH)));

				exportedReadings.add(reading);

				data.moveToNext();
			}

			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = mapper.schemaFor(EnvironmentReading.class).withHeader().withColumnSeparator(',');
			ObjectWriter writer = mapper.writer(schema);

			try {
				File exportPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
				exportPath.mkdirs();
				File exportFile = new File(exportPath, "export-" + System.currentTimeMillis() + ".csv");
				Log.d(TAG, exportFile.getAbsolutePath());
				FileOutputStream fos = new FileOutputStream(exportFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
				OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
				writer.writeValue(osw, exportedReadings);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Export file not found!", e);
			} catch (JsonGenerationException e) {
				Log.e(TAG, "Could not generate JSON!", e);
			} catch (JsonMappingException e) {
				Log.e(TAG, "JSON mapping failed!", e);
			} catch (IOException e) {
				Log.wtf(TAG, "Unknown IOException!!", e);
			}

		}
	}
}
