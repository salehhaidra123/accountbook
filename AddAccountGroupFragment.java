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

public class AddAccountGroupFragment extends DialogFragment{
	private TextInputEditText editTextAccountGroupName;
	private DatabaseHelper dbHelper;
	private OnAccountGroupAddedListener listener;
	
	
	public interface OnAccountGroupAddedListener {
		void onAccountGroupAdded();
	}
	
	public void setOnAccountGroupAddedListener(OnAccountGroupAddedListener listener) {
		this.listener = listener;
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_account_group, container, false);
		
		editTextAccountGroupName= view.findViewById(R.id.edit_text_account_group_name);
		Button btnSave = view.findViewById(R.id.btn_save);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		dbHelper = new DatabaseHelper(getContext());
		
		btnSave.setOnClickListener(v -> {
			String groupName = editTextAccountGroupName.getText().toString().trim();
			
			if (groupName.isEmpty()) {
				editTextAccountGroupName.setError("أدخل اسم النوع");
				return;
			}
			
			AccountGroup accountGroup = new AccountGroup(groupName);
			boolean success = dbHelper.insertAccountGroup(accountGroup);
			if (listener != null) {
				listener.onAccountGroupAdded();
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