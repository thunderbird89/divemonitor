package tech.provingground.divemonitor.service.provider;

import android.net.Uri;

/**
 * Created by zmeggyesi on 2017. 04. 02..
 */

public final class EnvironmentReadingsContract {
	/**
	 * Authority for the Environment Readings provider
	 */
	public static final String AUTHORITY = "tech.provingground.divemonitor.provider.environmentreadings";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * <pre>content://</pre>-style URI for the provider
	 */
	public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "environmentreadings");

	/**
	 * Content Type for entire provider
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tech.provingground.divemonitor.environmentreadings";

	/**
	 * Content Type for single item
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tech.provingground.divemonitor.environmentreadings";

	public static final String[] DEFAULT_PROJECTION = {
			"*"
	};
}
