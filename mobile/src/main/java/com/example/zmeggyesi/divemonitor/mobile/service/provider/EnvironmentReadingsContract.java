package com.example.zmeggyesi.divemonitor.mobile.service.provider;

import android.net.Uri;

/**
 * Created by zmeggyesi on 2017. 04. 02..
 */

public final class EnvironmentReadingsContract {
	/**
	 * Authority for the Environment Readings provider
	 */
	public static final String AUTHORITY = "com.example.zmeggyesi.divemonitor.provider.environmentreadings";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * <pre>content://</pre>-style URI for the provider
	 */
	public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "environmentreadings");

	/**
	 * Content Type for entire provider
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.zmeggyesi.divemonitor.environmentreadings";

	/**
	 * Content Type for single item
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.zmeggyesi.divemonitor.environmentreadings";

	public static final String[] DEFAULT_PROJECTION = {
			"*"
	};
}
