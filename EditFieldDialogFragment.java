package com.my.myapp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EditFieldDialogFragment extends DialogFragment {
	
	private static final String ARG_TITLE = "title";
	private static final String ARG_FIELD_NAME = "field_name";
	private static final String ARG_CURRENT_VALUE = "current_value";
	private static final String ARG_HINT = "hint";
	
	private String fieldName;
	private OnFieldUpdatedListener listener;
	private EditText editText;
	private Button btnSave;
	
	public interface OnFieldUpdatedListener {
		void onFieldUpdated(String fieldName, String newValue);
	}
	
	public static EditFieldDialogFragment newInstance(String title, String fieldName, String currentValue, String hint) {
		EditFieldDialogFragment fragment = new EditFieldDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_FIELD_NAME, fieldName);
		args.putString(ARG_CURRENT_VALUE, currentValue);
		args.putString(ARG_HINT, hint);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			fieldName = getArguments().getString(ARG_FIELD_NAME);
		}
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_edit_field, container, false);
		
		// تهيئة العناصر
		TextView tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
		editText = view.findViewById(R.id.et_field_value);
		btnSave = view.findViewById(R.id.btn_save);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		if (getArguments() != null) {
			String title = getArguments().getString(ARG_TITLE);
			String currentValue = getArguments().getString(ARG_CURRENT_VALUE);
			String hint = getArguments().getString(ARG_HINT);
			
			// تعيين العنوان
			tvDialogTitle.setText(title);
			
			// تعيين القيمة الحالية والنص التلميحي
			editText.setText(currentValue);
			editText.setHint(hint);
			
			// تحديد النص بالكامل ووضع المؤشر في النهاية
			if (currentValue != null && !currentValue.isEmpty()) {
				editText.selectAll();
				} else {
				editText.requestFocus();
			}
			
			// تفعيل/تعطيل زر الحفظ بناءً على وجود نص
			editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				
				@Override
				public void afterTextChanged(Editable s) {
					btnSave.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
				}
			});
			
			// تفعيل زر الحفظ إذا كان هناك نص بالفعل
			btnSave.setEnabled(!TextUtils.isEmpty(currentValue));
		}
		
		btnSave.setOnClickListener(v -> {
			String newValue = editText.getText().toString().trim();
			if (listener != null) {
				listener.onFieldUpdated(fieldName, newValue);
			}
			dismiss();
		});
		
		btnCancel.setOnClickListener(v -> dismiss());
		
		return view;
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// فتح لوحة المفاتيح تلقائيًا عند عرض الـ Dialog
		editText.postDelayed(() -> {
			editText.requestFocus();
			InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 100);
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnFieldUpdatedListener) {
			listener = (OnFieldUpdatedListener) context;
			} else {
			throw new RuntimeException(context.toString() + " must implement OnFieldUpdatedListener");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
}