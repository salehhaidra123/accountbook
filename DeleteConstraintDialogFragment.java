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
import java.util.List;
import java.util.ArrayList;

public class DeleteConstraintDialogFragment extends DialogFragment {
	
	// الواجهة لتعكس أنها قد تحذف عدة قيود
	public interface OnConstraintsDeletedListener {
		void onConstraintsDeleted(List<Constraint> constraints);
	}
	
	private static final String ARG_CONSTRAINTS = "constraints_list";
	
	private List<Constraint> constraints;
	private OnConstraintsDeletedListener listener;
	
	// الدالة لإنشاء الـ Dialog، تستقبل قائمة من القيود
	public static DeleteConstraintDialogFragment newInstance(List<Constraint> constraints) {
		DeleteConstraintDialogFragment fragment = new DeleteConstraintDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_CONSTRAINTS, new ArrayList<>(constraints));
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setOnConstraintsDeletedListener(OnConstraintsDeletedListener listener) {
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		if (getArguments() != null) {
			constraints = (List<Constraint>) getArguments().getSerializable(ARG_CONSTRAINTS);
		}
		
		if (constraints == null || constraints.isEmpty()) {
			// لا يوجد عناصر للحذف، أغلق الـ Dialog
			dismiss();
			return new AlertDialog.Builder(getContext()).create();
		}
		
		String message;
		if (constraints.size() == 1) {
			// إذا كان هناك قيد واحد فقط، اعرض الرسالة مع تفاصيل القيد
			Constraint constraint = constraints.get(0);
			String details = constraint.getDetails();
			
			// إذا كان تحويلاً، أضف معلومات إضافية
			if (constraint.getTransferId() != null) {
				details = "تحويل: " + details;
			}
			
			message = "هل تريد حذف القيد:\n" + details + "\n؟";
			} else {
			// إذا كان هناك عدة قيود، اعرض رسالة عامة مع العدد
			int transferCount = 0;
			int regularCount = 0;
			
			// عد عدد التحويلات والقيود العادية
			for (Constraint constraint : constraints) {
				if (constraint.getTransferId() != null) {
					transferCount++;
					} else {
					regularCount++;
				}
			}
			
			if (transferCount > 0 && regularCount > 0) {
				message = "هل أنت متأكد من حذف " + constraints.size() + " من القيود المحددة؟\n" +
				"(يشمل " + transferCount + " تحويل و " + regularCount + " قيد عادي)";
				} else if (transferCount > 0) {
				message = "هل أنت متأكد من حذف " + transferCount + " تحويل؟";
				} else {
				message = "هل أنت متأكد من حذف " + regularCount + " قيد؟";
			}
		}
		
		return new AlertDialog.Builder(getContext())
		.setTitle("تأكيد الحذف")
		.setMessage(message)
		.setPositiveButton("نعم", (dialog, which) -> {
			Log.d("DELETE_DEBUG", "Trying to delete " + constraints.size() + " constraints.");
			
			// استخدام DatabaseHelper من النمط Singleton
			DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());
			
			// استدعاء دالة الحذف المحدثة
			boolean success = dbHelper.deleteMixedConstraints(constraints);
			
			Log.d("DELETE_DEBUG", "Delete success: " + success);
			
			if (success) {
				Toast.makeText(getContext(), "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
				if (listener != null) {
					listener.onConstraintsDeleted(constraints);
				}
				} else {
				Toast.makeText(getContext(), "فشل في الحذف", Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss())
		.create();
	}
}