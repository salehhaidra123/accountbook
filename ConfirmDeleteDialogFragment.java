package com.my.myapp;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConfirmDeleteDialogFragment extends DialogFragment {
	
	private OnConfirmDeleteListener listener;
	private String title = "تأكيد الحذف";
	private String message = "هل أنت متأكد من حذف العنصر؟";
	private int itemId = -1;
	
	public interface OnConfirmDeleteListener {
		void onDeleteConfirmed(int id);
		void onDeleteCancelled(int id);
	}
	
	// إنشاء الفراجمنت مع تمرير العنوان و id العنصر للحذف
	public static ConfirmDeleteDialogFragment newInstance(String title, String message, int id) {
		ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		args.putInt("id", id);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setOnConfirmDeleteListener(OnConfirmDeleteListener listener) {
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		// قراءة القيم من الباندل
		if (getArguments() != null) {
			title = getArguments().getString("title", title);
			message = getArguments().getString("message", message);
			itemId = getArguments().getInt("id", -1);
		}
		
		return new MaterialAlertDialogBuilder(requireContext())
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("نعم", (dialog, which) -> {
			if (listener != null) {
				listener.onDeleteConfirmed(itemId);
			}
			dismiss();
		})
		.setNegativeButton("لا", (dialog, which) -> {
			if (listener != null) {
				listener.onDeleteCancelled(itemId);
			}
			dismiss();
		})
		.create();
	}
}