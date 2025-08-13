package com.my.myapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteConstraintDialogFragment2 extends DialogFragment {
	
	private static final String ARG_CONSTRAINT_ID = "constraint_id";
	private int constraintId = -1;
	
	// واجهة للتواصل مع الـ Activity
	public interface OnDeleteConfirmedListener {
		void onConstraintDeleteConfirmed(int constraintId);
	}
	
	private OnDeleteConfirmedListener listener;
	
	public static DeleteConstraintDialogFragment2 newInstance(int id) {
		DeleteConstraintDialogFragment2 fragment = new DeleteConstraintDialogFragment2();
		Bundle args = new Bundle();
		args.putInt(ARG_CONSTRAINT_ID, id);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnDeleteConfirmedListener) {
			listener = (OnDeleteConfirmedListener) context;
			} else {
			throw new RuntimeException(context.toString()
			+ " must implement OnDeleteConfirmedListener");
		}
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		if (getArguments() != null) {
			constraintId = getArguments().getInt(ARG_CONSTRAINT_ID, -1);
		}
		
		if (constraintId == -1) {
			return new AlertDialog.Builder(requireContext())
			.setTitle("خطأ")
			.setMessage("لم يتم العثور على رقم القيد.")
			.setPositiveButton("موافق", null)
			.create();
		}
		
		return new AlertDialog.Builder(requireContext())
		.setTitle("تأكيد الحذف")
		.setMessage("هل تريد حذف هذا القيد نهائيًا؟")
		.setPositiveButton("نعم", (dialog, which) -> {
			Log.d("DELETE_DEBUG", "User confirmed delete for ID=" + constraintId);
			
			if (listener != null) {
				listener.onConstraintDeleteConfirmed(constraintId);
			}
		})
		.setNegativeButton("إلغاء", null)
		.create();
	}
}