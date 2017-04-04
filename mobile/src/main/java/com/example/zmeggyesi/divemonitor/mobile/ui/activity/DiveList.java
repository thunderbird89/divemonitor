package com.example.zmeggyesi.divemonitor.mobile.ui.activity;

import android.Manifest;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.zmeggyesi.divemonitor.R;
import com.example.zmeggyesi.divemonitor.mobile.service.CSVExporter;
import com.example.zmeggyesi.divemonitor.mobile.service.provider.DivesContract;
import com.example.zmeggyesi.divemonitor.mobile.ui.dialog.DiveExportGate;

/**
 * Created by zmeggyesi on 2017. 04. 01..
 */

public class DiveList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, DiveExportGate.CSVExportCallbackListener {

	private SimpleCursorAdapter adapter;
	private static boolean canWritetoExternalStorage;

	@Override
	public ListAdapter getListAdapter() {
		return super.getListAdapter();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case 0 :
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					canWritetoExternalStorage = true;
				} else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
					canWritetoExternalStorage = false;
				}
		}
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dive_list);

		int[] toViews = {android.R.id.text1, android.R.id.text2};
		adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, null, DivesContract.DEFAULT_PROJECTION_UI, toViews, 0);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);

		if (PackageManager.PERMISSION_DENIED == getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
				PackageManager.PERMISSION_DENIED == getBaseContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
			canWritetoExternalStorage = false;
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
		} else {
			canWritetoExternalStorage = true;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, DivesContract.CONTENT_URI, DivesContract.DEFAULT_PROJECTION_UI, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (canWritetoExternalStorage) {
			DialogFragment fragment = new DiveExportGate();
			fragment.show(getFragmentManager(), "export");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void startExport(DialogFragment dialog) {
		Log.d("DiveList", "Export started");
		Intent intent = new Intent(this, CSVExporter.class);
		intent.setAction(CSVExporter.ACTION_START_CSV_EXPORT);
		intent.putExtra("diveKey", "1");
		startService(intent);
	}
}
