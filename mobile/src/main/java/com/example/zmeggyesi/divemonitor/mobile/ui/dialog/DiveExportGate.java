package com.example.zmeggyesi.divemonitor.mobile.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.zmeggyesi.divemonitor.R;

/**
 * Created by zmeggyesi on 2017. 04. 03..
 */

public class DiveExportGate extends DialogFragment {

	public interface CSVExportCallbackListener {
		void startExport(DialogFragment dialog);
	}

	private CSVExportCallbackListener listener;

	public static DiveExportGate newInstanceWithArgs(int diveKey) {
		DiveExportGate deg = new DiveExportGate();
		Bundle args = new Bundle();
		args.putInt("diveKey", diveKey);
		deg.setArguments(args);
		
		return deg;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (CSVExportCallbackListener) getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(getString(R.string.content_export_dialog_title));
		builder.setMessage(getString(R.string.content_export_dialog_content));
		builder.setPositiveButton(R.string.content_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.startExport(DiveExportGate.this);
			}
		});
		builder.setNegativeButton(R.string.content_no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		return builder.create();
	}
}
