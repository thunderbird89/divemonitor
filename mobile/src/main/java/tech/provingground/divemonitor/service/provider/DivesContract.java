package tech.provingground.divemonitor.service.provider;

import android.net.Uri;

import tech.provingground.divemonitor.model.Dive;

/**
 * Created by zmeggyesi on 2017. 03. 28..
 */

public final class DivesContract {
	/**
	 * Authority for the Dives provider
	 */
	public static final String AUTHORITY = "tech.provingground.divemonitor.provider.dives";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * <pre>content://</pre>-style URI for the provider
	 */
	public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "dives");

	/**
	 * Content Type for entire provider
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tech.provingground.divemonitor.dive";

	/**
	 * Content Type for single item
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tech.provingground.divemonitor.dive";

	/**
	 * Default projections for UI
	 */
	public static final String[] DEFAULT_PROJECTION_UI = {
			Dive.Record._ID,
			Dive.Record.COLUMN_NAME_LOCATION,
			Dive.Record.COLUMN_NAME_TIMESTAMP,
			Dive.Record.COLUMN_NAME_DISPLAY_NAME
	};
}
