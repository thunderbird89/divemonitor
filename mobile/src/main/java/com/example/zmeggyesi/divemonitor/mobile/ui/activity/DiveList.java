package com.example.zmeggyesi.divemonitor.mobile.ui.activity;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.example.zmeggyesi.divemonitor.mobile.service.CSVExporter;
import com.example.zmeggyesi.divemonitor.mobile.service.provider.DivesContract;
import com.example.zmeggyesi.divemonitor.mobile.ui.dialog.DiveExportGate;

/**
 * Created by zmeggyesi on 2017. 04. 01..
 */

public class DiveList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, DiveExportGate.CSVExportCallbackListener {

	private SimpleCursorAdapter adapter;

	@Override
	public ListAdapter getListAdapter() {
		return super.getListAdapter();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		progressBar.setIndeterminate(true);
		getListView().setEmptyView(progressBar);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		int[] toViews = {android.R.id.text1, android.R.id.text2};
		adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, null, DivesContract.DEFAULT_PROJECTION_UI, toViews, 0);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);
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
		DialogFragment fragment = new DiveExportGate();
		fragment.show(getFragmentManager(), "export");
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
