package com.my.myapp;

import android.content.Context;
import android.os.Bundle;
import android.app.Dialog;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;


public class EditAccountDialogFragment extends DialogFragment {
	
	private static final String ARG_ACCOUNT = "account_arg";
	private Account account;
	private String selectedAccType;
	DatabaseHelper dbHelper;
	private OnAccountEditedListener listener;
	
	public interface OnAccountEditedListener {
		void onAccountEdited();
	}
	
	public static EditAccountDialogFragment newInstance(Account account) {
		EditAccountDialogFragment fragment = new EditAccountDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_ACCOUNT, account);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnAccountEditedListener) {
			listener = (OnAccountEditedListener) context;
			} else {
			throw new ClassCastException(context.toString() + " must implement OnAccountEditedListener");
		}
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		account = (Account) getArguments().getSerializable(ARG_ACCOUNT);
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_edit_account_dialog, null);
		
		AutoCompleteTextView spAccType = view.findViewById(R.id.sp_account_type);
		TextInputEditText etName = view.findViewById(R.id.et_account_name);
		TextInputEditText etPhone = view.findViewById(R.id.et_phone);
		Button btnEdit = view.findViewById(R.id.btn_edit_account);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		etName.setText(account.getAccountName());
		etPhone.setText(account.getAccountPhone());
		spAccType.setText(account.getAccountType(), false);
		
		String[] accountTypeArray = {"مدين", "دائن", "صندوق"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, accountTypeArray);
		spAccType.setAdapter(adapter);
		
		spAccType.setOnItemClickListener((parent, view1, position, id) -> {
			selectedAccType = parent.getItemAtPosition(position).toString();
		});
		
		AlertDialog dialog = new AlertDialog.Builder(requireContext())
		.setView(view)
		.setCancelable(false)
		.setTitle("تعديل قيد")
		.create();
		
		btnCancel.setOnClickListener(v -> dialog.dismiss());
		
		btnEdit.setOnClickListener(v -> {
			String newType = selectedAccType != null ? selectedAccType : spAccType.getText().toString();
			String newName = etName.getText().toString().trim();
			String newPhone = etPhone.getText().toString().trim();
			
			if (!newType.isEmpty() && !newName.isEmpty() && !newPhone.isEmpty()) {
				 dbHelper = new DatabaseHelper(getContext());
				dbHelper.updateAccount(account.getAccountId(), newType, newName, newPhone);
				
				if (listener != null) {
					listener.onAccountEdited(); // ✅ نُعلِم النشاط بالتحديث
				}
				
				dialog.dismiss();
				} else {
				Toast.makeText(getContext(), "يرجى ملئ كافة البيانات", Toast.LENGTH_SHORT).show();
			}
		});
		
		return dialog;
	}
}