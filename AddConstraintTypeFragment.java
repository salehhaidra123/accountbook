package com.my.myapp;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

public class AddConstraintTypeFragment extends DialogFragment {

	private TextInputEditText editTextConstraintTypeName;
	private DatabaseHelper dbHelper;

	private OnConstraintTypeAddedListener listener;

	public interface OnConstraintTypeAddedListener {
		void onConstraintTypeAdded();
	}

	public void setOnConstraintTypeAddedListener(OnConstraintTypeAddedListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_constraint_type, container, false);

		editTextConstraintTypeName = view.findViewById(R.id.edit_text_constraint_type_name);
		Button btnSave = view.findViewById(R.id.btn_save);
		Button btnCancel = view.findViewById(R.id.btn_cancel);

		dbHelper = new DatabaseHelper(getContext());

		btnSave.setOnClickListener(v -> {
			String typeName = editTextConstraintTypeName.getText().toString().trim();

			if (typeName.isEmpty()) {
				editTextConstraintTypeName.setError("أدخل اسم النوع");
				return;
			}

			ConstraintType constraintType = new ConstraintType(typeName);
			boolean success = dbHelper.insertConstraintType(constraintType);
			if (listener != null) {
				listener.onConstraintTypeAdded();
			}
			if (success) {
				Toast.makeText(getContext(), "✅ تم إضافة نوع القيد", Toast.LENGTH_SHORT).show();
				dismiss();
			} else {
				Toast.makeText(getContext(), "❌ فشل في الإضافة، قد يكون الاسم مكرر", Toast.LENGTH_SHORT).show();
			}
		});

		btnCancel.setOnClickListener(v -> dismiss());

		return view;
	}
}