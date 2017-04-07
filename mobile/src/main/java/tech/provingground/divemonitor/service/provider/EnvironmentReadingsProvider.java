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

import tech.provingground.divemonitor.EnvironmentReading;
import tech.provingground.divemonitor.model.GlobalContext;

/**
 * Created by zmeggyesi on 2017. 04. 02..
 */

public class EnvironmentReadingsProvider extends ContentProvider {
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private GlobalContext gc;
	private SQLiteDatabase readingsDB;

	static {
		URI_MATCHER.addURI(EnvironmentReadingsContract.AUTHORITY, "readings", 1);
		URI_MATCHER.addURI(EnvironmentReadingsContract.AUTHORITY, "readings/#", 2);
		URI_MATCHER.addURI(EnvironmentReadingsContract.AUTHORITY, "readings/dive/#", 3);
	}

	@Override
	public boolean onCreate() {
		gc = (GlobalContext) getContext();
		readingsDB = gc.getEnvironmentReadingsDatabase(false);
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
				// Query all readings
				if (TextUtils.isEmpty(sortOrder)) {
					sortOrder = "_ID ASC";
				}
				break;
			case 2 :
				// Get a single readings cluster
				selection = "_ID = " + uri.getLastPathSegment();
				break;
			case 3 :
				// Query readings for a single dive
				selection = EnvironmentReading.Record.COLUMN_NAME_DIVE_KEY + " = " + uri.getLastPathSegment();
				break;
			default :
				throw new IllegalArgumentException("URI not recognized: " + uri);
		}

		if (projection == null) {
			projection = EnvironmentReadingsContract.DEFAULT_PROJECTION;
		}

		return readingsDB.query(EnvironmentReading.Record.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case 1 : return EnvironmentReadingsContract.CONTENT_TYPE;
			case 2 : return EnvironmentReadingsContract.CONTENT_ITEM_TYPE;
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
