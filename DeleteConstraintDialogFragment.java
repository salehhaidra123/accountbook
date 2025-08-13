package com.my.myapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteConstraintDialogFragment extends DialogFragment {
	
	public interface OnConstraintDeletedListener {
		void onConstraintDeleted();
	}
	
	private static final String ARG_CONSTRAINT_ID = "constraint_id";
	private static final String ARG_CONSTRAINT_DETAILS = "constraint_details";
	
	private int constraintId;
	private String constraintDetails;
	private OnConstraintDeletedListener listener;
	
	public static DeleteConstraintDialogFragment newInstance(Constraint constraint) {
		DeleteConstraintDialogFragment fragment = new DeleteConstraintDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_CONSTRAINT_ID, constraint.getId());
		args.putString(ARG_CONSTRAINT_DETAILS, constraint.getDetails());
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setOnConstraintDeletedListener(OnConstraintDeletedListener listener) {
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		if (getArguments() != null) {
			constraintId = getArguments().getInt(ARG_CONSTRAINT_ID, -1);
			constraintDetails = getArguments().getString(ARG_CONSTRAINT_DETAILS, "");
		}
		
		return new AlertDialog.Builder(getContext())
		.setTitle("تأكيد الحذف")
		.setMessage("هل تريد حذف القيد:\n" + constraintDetails + "\n؟")
		.setPositiveButton("نعم", (dialog, which) -> {
			Log.d("DELETE_DEBUG", "Trying to delete constraint ID: " + constraintId);
			
			if (constraintId <= 0) {
				Log.e("DELETE_DEBUG", "Invalid constraint ID: " + constraintId);
				Toast.makeText(getContext(), "معرّف القيد غير صالح", Toast.LENGTH_SHORT).show();
				return;
			}
			
			DatabaseHelper dbHelper = new DatabaseHelper(getContext());
			boolean success = dbHelper.deleteConstraint(constraintId);
			dbHelper.close();
			
			Log.d("DELETE_DEBUG", "Delete success: " + success);
			
			if (success) {
				Toast.makeText(getContext(), "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
				if (listener != null) {
					listener.onConstraintDeleted();
				}
				} else {
				Toast.makeText(getContext(), "فشل في الحذف", Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss())
		.create();
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
	}
}