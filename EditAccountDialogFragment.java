package com.my.myapp;

import android.content.Context;
import android.content.DialogInterface;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAccountDialogFragment extends DialogFragment {
	
	private static final String ARG_ACCOUNT = "account_arg";
	private Account account;
	private String selectedAccType;
	private String selectedAccGroup;
	DatabaseHelper dbHelper;
	private OnAccountEditedListener listener;
	
	// متغيرات للتعامل مع أنواع الحسابات ومجموعاتها
	private AutoCompleteTextView spAccType, spAccGroup;
	private List<AccountType> accountTypeList;
	private List<AccountGroup> accountGroupList;
	private ArrayAdapter<String> accountTypeAdapter;
	private ArrayAdapter<String> accountGroupAdapter;
	private Map<String, Integer> typeNameToIdMap = new HashMap<>();
	private Map<String, Integer> groupNameToIdMap = new HashMap<>();
	
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
		
		// تهيئة العناصر من واجهة المستخدم
		spAccType = view.findViewById(R.id.sp_account_type);
		spAccGroup = view.findViewById(R.id.sp_account_group); // إضافة حقل مجموعة الحساب
		TextInputEditText etName = view.findViewById(R.id.et_account_name);
		TextInputEditText etPhone = view.findViewById(R.id.et_phone);
		Button btnEdit = view.findViewById(R.id.btn_edit_account);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		// تعيين القيم الحالية
		etName.setText(account.getAccountName());
		etPhone.setText(account.getAccountPhone());
		
		// تهيئة قاعدة البيانات
		dbHelper = new DatabaseHelper(getContext());
		
		// جلب أنواع الحسابات ومجموعاتها من قاعدة البيانات
		setupAccountTypeAutoComplete();
		setupAccountGroupAutoComplete();
		
		// تعيين القيم المحددة مسبقًا
		spAccType.setText(account.getAccountType(), false);
		spAccGroup.setText(account.getAccountGroup(), false);
		
		AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(view).setCancelable(false)
		.setTitle("تعديل حساب").create();
		
		btnCancel.setOnClickListener(v -> dialog.dismiss());
		
		btnEdit.setOnClickListener(v -> {
			String newType = spAccType.getText().toString().trim();
			String newGroup = spAccGroup.getText().toString().trim();
			String newName = etName.getText().toString().trim();
			String newPhone = etPhone.getText().toString().trim();
			
			if (!newType.isEmpty() && !newGroup.isEmpty() && !newName.isEmpty() && !newPhone.isEmpty()) {
				// الحصول على معرفات نوع الحساب ومجموعة الحساب
				int typeId = typeNameToIdMap.getOrDefault(newType, -1);
				int groupId = groupNameToIdMap.getOrDefault(newGroup, -1);
				
				if (typeId != -1 && groupId != -1) {
					// تحديث الحساب مع المعرفات الجديدة
					dbHelper.updateAccountV2(account.getAccountId(), typeId, groupId, newName, newPhone);
					
					if (listener != null) {
						listener.onAccountEdited();
					}
					
					dialog.dismiss();
					} else {
					Toast.makeText(getContext(), "نوع الحساب أو مجموعة الحساب غير صالح", Toast.LENGTH_SHORT).show();
				}
				} else {
				Toast.makeText(getContext(), "يرجى ملء كافة البيانات", Toast.LENGTH_SHORT).show();
			}
		});
		
		return dialog;
	}
	
	private void setupAccountTypeAutoComplete() {
		accountTypeList = dbHelper.getAllAccountTypes();
		List<String> typeNames = new ArrayList<>();
		typeNameToIdMap = new HashMap<>();
		
		for (AccountType type : accountTypeList) {
			typeNames.add(type.getAccountType());
			typeNameToIdMap.put(type.getAccountType(), type.getAccountTypeId());
		}
		
		accountTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, typeNames);
		
		spAccType.setAdapter(accountTypeAdapter);
		spAccType.setOnItemClickListener((parent, view1, position, id) -> {
			selectedAccType = parent.getItemAtPosition(position).toString();
		});
		
		// إظهار القائمة عند النقر
		spAccType.setOnClickListener(v -> spAccType.showDropDown());
	}
	
	private void setupAccountGroupAutoComplete() {
		accountGroupList = dbHelper.getAllAccountGroup();
		List<String> groupNames = new ArrayList<>();
		groupNameToIdMap = new HashMap<>();
		
		for (AccountGroup group : accountGroupList) {
			groupNames.add(group.getAccountGroupName());
			groupNameToIdMap.put(group.getAccountGroupName(), group.getAccountGroupId());
		}
		
		accountGroupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, groupNames);
		
		spAccGroup.setAdapter(accountGroupAdapter);
		spAccGroup.setOnItemClickListener((parent, view1, position, id) -> {
			selectedAccGroup = parent.getItemAtPosition(position).toString();
		});
		
		// إظهار القائمة عند النقر
		spAccGroup.setOnClickListener(v -> spAccGroup.showDropDown());
	}
	
	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		if (listener != null) {
			listener.onAccountEdited(); // هنا نخبر الـ Activity
		}
	}
}