package com.my.myapp;

import android.widget.Toast;
import android.widget.Button;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class AddAccountTypeFragment extends DialogFragment{
	private TextInputEditText editTextAccountTypeName;
	private DatabaseHelper dbHelper;
	private OnAccountTypeAddedListener listener;

	
	public interface OnAccountTypeAddedListener {
		void onAccountTypeAdded();
	}

	public void setOnAccountTypeAddedListener(OnAccountTypeAddedListener listener) {
		this.listener = listener;
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_account_type, container, false);
		
		editTextAccountTypeName= view.findViewById(R.id.edit_text_account_type_name);
		Button btnSave = view.findViewById(R.id.btn_save);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		dbHelper = new DatabaseHelper(getContext());
		
		btnSave.setOnClickListener(v -> {
			String typeName = editTextAccountTypeName.getText().toString().trim();
			
			if (typeName.isEmpty()) {
				editTextAccountTypeName.setError("أدخل اسم النوع");
				return;
			}
			
			AccountType accountType = new AccountType(typeName);
			boolean success = dbHelper.insertAccountType(accountType);
			if (listener != null) {
				listener.onAccountTypeAdded();
			}
			if (success) {
				Toast.makeText(getContext(), "✅تم اضافة نوع الحساب", Toast.LENGTH_SHORT).show();
				dismiss();
				} else {
				Toast.makeText(getContext(), "❌ فشل في الاضافة", Toast.LENGTH_SHORT).show();
			}
		});
		
		btnCancel.setOnClickListener(v -> dismiss());
		
		return view;
	}

	
}