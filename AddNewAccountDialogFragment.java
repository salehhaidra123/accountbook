package com.my.myapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddNewAccountDialogFragment extends DialogFragment {
	
	private TextInputEditText etAccountName, etAccountPhone, etAccountDate;
	private AutoCompleteTextView spAccountType, spAccountGroup;
	private MaterialButton btnAddAccount;
	
	private OnAccountAddedListener accountAddedListener;
	private DatabaseHelper dbHelper;
	List<AccountType> accountTypeList;
	ArrayAdapter<String> accountTypeAdapter;
	Map<String, Integer> typeNameToIdMap = new HashMap<>();
	List<AccountGroup> accountGroupList;
	ArrayAdapter<String> accountGroupAdapter;
	Map<String, Integer> groupNameToIdMap = new HashMap<>();
	
	public interface OnAccountAddedListener {
		void onAccountAdded(String name, String phone, String date);
	}
	
	// إنشاء نسخة جديدة من الـ Fragment مع تمرير خيار الشاشة الكاملة
	public static AddNewAccountDialogFragment newInstance(boolean fullScreen) {
		AddNewAccountDialogFragment fragment = new AddNewAccountDialogFragment();
		Bundle args = new Bundle();
		args.putBoolean("full_screen", fullScreen);
		fragment.setArguments(args);
		return fragment;
	}
	
	
//		etName.setTypeface(robotoMedium);
	
	// ربط الـ Listener وتهيئة قاعدة البيانات
	//conect listener and intilize database
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnAccountAddedListener) {
			accountAddedListener = (OnAccountAddedListener) context;
		}
		dbHelper = new DatabaseHelper(context);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		accountAddedListener = null;
	}
	
	// ضبط حجم النافذة إذا كان fullScreen مفعل
	@Override
	public void onStart() {
		super.onStart();
		boolean fullScreen = getArguments() != null && getArguments().getBoolean("full_screen", false);
		if (fullScreen && getDialog() != null) {
			Dialog dialog = getDialog();
			if (dialog.getWindow() != null) {
				dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
			}
		}
	}
	
	@Override
	public int getTheme() {
		boolean fullScreen = getArguments() != null && getArguments().getBoolean("full_screen", false);
		return fullScreen ? android.R.style.Theme_Material_Light_NoActionBar_Fullscreen : super.getTheme();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_new_account, container, false);
		Typeface robotoMedium = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Rakkas-Regular.ttf");
		
		etAccountName = view.findViewById(R.id.et_account_name);
		etAccountName.setTypeface(robotoMedium);
		etAccountPhone = view.findViewById(R.id.et_account_phone);
		etAccountDate = view.findViewById(R.id.et_account_date);
		
		spAccountType = view.findViewById(R.id.sp_account_type);
		spAccountGroup = view.findViewById(R.id.sp_account_group);
		
		btnAddAccount = view.findViewById(R.id.btn_add_new_acc);
		
		setupAccountTypeAutoComplete();
		setupAccountGroupAutoComplete();
		setupDatePicker();
		
		// تعيين التاريخ الحالي افتراضيًا
		etAccountDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
		
		btnAddAccount.setOnClickListener(v -> {
			String name = etAccountName.getText().toString().trim();
			String phone = etAccountPhone.getText().toString().trim();
			String date = etAccountDate.getText().toString().trim();
			String type = spAccountType.getText().toString().trim();
			String group = spAccountGroup.getText().toString().trim();
			
			if (name.isEmpty() || date.isEmpty() || type.isEmpty()) {
				Toast.makeText(getContext(), "يرجى إدخال جميع الحقول المطلوبة", Toast.LENGTH_SHORT).show();
				return;
			}
			
			// إنشاء حساب جديد
			Account account = new Account();
			account.setAccountName(name);
			account.setAccountPhone(phone);
			account.setCreatedDate(date);
			account.setAccountType(type);
			account.setAccountGroup(group);
			
			boolean inserted = dbHelper.insertAccount(account);
			if (inserted) {
				Toast.makeText(getContext(), "تمت إضافة الحساب", Toast.LENGTH_SHORT).show();
				if (accountAddedListener != null) {
					accountAddedListener.onAccountAdded(name, phone, date);
				}
				dismiss();
				} else {
				Toast.makeText(getContext(), "فشل في الإضافة", Toast.LENGTH_SHORT).show();
			}
		});
		
		return view;
	}
	
	private void setupAccountTypeAutoComplete() {
		accountTypeList = dbHelper.getAllAccountTypes();
		List<String> typeNames = new ArrayList<>();
		typeNameToIdMap = new HashMap<>();
		
		for (AccountType type : accountTypeList) {
			typeNames.add(type.getAccountType());
			typeNameToIdMap.put(type.getAccountType(), type.getAccountTypeId());
		}
		
		accountTypeAdapter = new ArrayAdapter<>(
		getContext(),
		android.R.layout.simple_dropdown_item_1line,
		typeNames
		);
		
		spAccountType.setAdapter(accountTypeAdapter);
		spAccountType.setOnClickListener(v -> spAccountType.showDropDown());
	}
	
	private void setupAccountGroupAutoComplete() {
		accountGroupList = dbHelper.getAllAccountGroup();
		List<String> groupNames = new ArrayList<>();
		groupNameToIdMap = new HashMap<>();
		
		for (AccountGroup group : accountGroupList) {
			groupNames.add(group.getAccountGroupName());
			groupNameToIdMap.put(group.getAccountGroupName(), group.getAccountGroupId());
		}
		
		accountGroupAdapter = new ArrayAdapter<>(
		getContext(),
		android.R.layout.simple_dropdown_item_1line,
		groupNames
		);
		
		spAccountGroup.setAdapter(accountGroupAdapter);
		spAccountGroup.setOnClickListener(v -> spAccountGroup.showDropDown());
	}
	
	private void setupDatePicker() {
		etAccountDate.setOnClickListener(v -> {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			
			DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view, y, m, d) -> {
				String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
				etAccountDate.setText(selectedDate);
			}, year, month, day);
			
			datePicker.show();
		});
	}
}