package tech.provingground.divemonitor.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import tech.provingground.divemonitor.model.Dive;
import tech.provingground.divemonitor.model.GlobalContext;

/**
 * Created by zmeggyesi on 2017. 03. 27..
 */

public class DiveProvider extends ContentProvider {
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private GlobalContext gc;
	private SQLiteDatabase divesDB;

	static {
		URI_MATCHER.addURI(DivesContract.AUTHORITY, "dives", 1);
		URI_MATCHER.addURI(DivesContract.AUTHORITY, "dives/#", 2);
	}

	public DiveProvider() {}

	@Override
	public boolean onCreate() {
		gc = (GlobalContext) getContext();
		try {
			divesDB = gc.getDivesDatabase(false);
		} catch (NullPointerException npe) {
			divesDB = gc.getDivesDatabase(false);
		}
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri,
	                    @Nullable String[] projection,
	                    @Nullable String selection,
	                    @Nullable String[] selectionArgs,
	                    @Nullable String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
			case 1 :
				// Multi-row query
				if (TextUtils.isEmpty(sortOrder)) {
					sortOrder = "_ID ASC";
				}
				break;
			case 2 :
				// Single-row query
				selection = selection + "_ID = " + uri.getLastPathSegment();
				break;
			default :
				throw new IllegalArgumentException("URI not recognized: " + uri);
		}

		if (projection == null) {
			projection = DivesContract.DEFAULT_PROJECTION_UI;
		}

		return divesDB.query(Dive.Record.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case 1 : return DivesContract.CONTENT_TYPE;
			case 2 : return DivesContract.CONTENT_ITEM_TYPE;
			default : throw new UnsupportedOperationException("Requested URI does not match: " + uri);
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		throw new UnsupportedOperationException("Provider is read-only!");
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
		throw new UnsupportedOperationException("Provider is read-only!");
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
		throw new UnsupportedOperationException("Provider is read-only!");
	}
}
