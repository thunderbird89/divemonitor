package tech.provingground.divemonitor.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import tech.provingground.divemonitor.R;

/**
 * Created by zmeggyesi on 2017. 04. 02..
 */

public class DatabasePurgeGate extends DialogFragment {
	private static final String TAG = "DatabasePurgeGate";
	private CallbackListener listener;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (CallbackListener) getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
		alertBuilder.setTitle(getString(R.string.content_purge_dialog_title));
		alertBuilder.setMessage(getString(R.string.content_purge_dialog_content));
		alertBuilder.setPositiveButton(getString(R.string.content_strong_yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DBs deleting");
				listener.initiatePurge(DatabasePurgeGate.this);
			}
		});
		alertBuilder.setNegativeButton(getString(R.string.content_strong_abort), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return alertBuilder.create();
	}

	public interface CallbackListener {
		void initiatePurge(DialogFragment dialog);
	}
}
