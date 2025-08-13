package com.my.myapp;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteAccountDialogFragment extends DialogFragment {
	
	private static final String ARG_ACCOUNT_ID = "account_id";
	private int accountId;
	
	// Listener interface
	public interface OnDeleteConfirmedListener {
		void onAccountDeleteConfirmed(int accountId);
	}
	
	private OnDeleteConfirmedListener listener;
	
	public static DeleteAccountDialogFragment newInstance(int accountId) {
		DeleteAccountDialogFragment fragment = new DeleteAccountDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_ACCOUNT_ID, accountId);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnDeleteConfirmedListener) {
			listener = (OnDeleteConfirmedListener) context;
			} else {
			throw new RuntimeException(context.toString() + " must implement OnDeleteConfirmedListener");
		}
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		if (getArguments() != null) {
			accountId = getArguments().getInt(ARG_ACCOUNT_ID);
		}
		
		return new AlertDialog.Builder(requireActivity())
		.setTitle("تأكيد الحذف")
		.setMessage("هل تريد حذف هذا الحساب؟")
		.setPositiveButton("نعم", (dialog, which) -> {
			if (listener != null) {
				listener.onAccountDeleteConfirmed(accountId);
			}
		})
		.setNegativeButton("إلغاء", null)
		.create();
	}
}