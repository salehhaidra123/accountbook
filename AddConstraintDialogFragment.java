package com.my.myapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

public class AddConstraintDialogFragment extends DialogFragment {

	private EditText editTextDate, editTextDetails, editTextDebit, editTextCredit;
	private DatabaseHelper dbHelper;
	private int accountId;
	private String todayDate = null;
	private OnDateSelectedListener dateSelectedListener;
	private OnConstraintAddedListener listener;
	private String initialDate = null;
	AutoCompleteTextView autoConstraintType;
	List<ConstraintType> constraintTypeList;
	ArrayAdapter<String> constraintTypeAdapter;
	Map<String, Integer> typeNameToIdMap = new HashMap<>();

	public static AddConstraintDialogFragment newInstance(int accountId) {
		AddConstraintDialogFragment fragment = new AddConstraintDialogFragment();
		Bundle args = new Bundle();
		args.putInt("account_id", accountId);
		fragment.setArguments(args);
		return fragment;
	}

	public interface OnConstraintAddedListener {
		void onConstraintAdded();
	}

	public interface OnDateSelectedListener {
		void onDateSelected(String selectedDate);
	}

	public void setInitialDate(String date) {
		this.initialDate = date;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnDateSelectedListener) {
			dateSelectedListener = (OnDateSelectedListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnDateSelectedListener");
		}
	}

	public void setOnConstraintAddedListener(OnConstraintAddedListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_edit_constraint_dialog, container, false);

		editTextDate = view.findViewById(R.id.edit_text_date);
		editTextDetails = view.findViewById(R.id.edit_text_details);
		editTextDebit = view.findViewById(R.id.edit_text_debit);
		editTextCredit = view.findViewById(R.id.edit_text_credit);
		autoConstraintType = view.findViewById(R.id.auto_complete_constraint_type);
		Button btnSave = view.findViewById(R.id.btn_edit_constraint);
		Button btnCancel = view.findViewById(R.id.btn_cancel);

		dbHelper = new DatabaseHelper(getContext());

		// تحميل أنواع القيود وربطها بالـ AutoCompleteTextView
		constraintTypeList = dbHelper.getAllConstraintTypes();
		List<String> typeNames = new ArrayList<>();
		typeNameToIdMap = new HashMap<>();

		for (ConstraintType type : constraintTypeList) {
			typeNames.add(type.getConstraintTypeName());
			typeNameToIdMap.put(type.getConstraintTypeName(), type.getConstraintTypeId());
		}

		constraintTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
				typeNames);
		autoConstraintType.setAdapter(constraintTypeAdapter);
		autoConstraintType.setText(typeNames.get(0), false);
		autoConstraintType.setOnClickListener(v -> autoConstraintType.showDropDown());

		// اختيار النوع الأول افتراضياً
		/*	if (!typeNames.isEmpty()) {
				autoConstraintType.setText(typeNames.get(0), false);
			}*/

		// استلام معرف الحساب من الـ arguments
		if (getArguments() != null) {
			accountId = getArguments().getInt("account_id", -1);
		}

		// إعداد التاريخ الحالي
		final Calendar calendar = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

		if (initialDate != null) {
			editTextDate.setText(initialDate);
		} else {
			todayDate = sdf.format(calendar.getTime());
			editTextDate.setText(todayDate);
		}

		editTextDate.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
				calendar.set(year, month, dayOfMonth);
				String selectedDate = sdf.format(calendar.getTime());
				editTextDate.setText(selectedDate);
				if (dateSelectedListener != null) {
					dateSelectedListener.onDateSelected(selectedDate);
				}
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		});

		// زر الإلغاء
		btnCancel.setOnClickListener(v -> dismiss());

		// زر الحفظ
		btnSave.setOnClickListener(v -> {
			String date = editTextDate.getText().toString().trim();
			String details = editTextDetails.getText().toString().trim();
			String debitStr = editTextDebit.getText().toString().trim();
			String creditStr = editTextCredit.getText().toString().trim();

			// الحصول على نوع القيد المختار
			String selectedTypeName = autoConstraintType.getText().toString().trim();
			int selectedTypeId = typeNameToIdMap.containsKey(selectedTypeName) ? typeNameToIdMap.get(selectedTypeName)
					: -1;
			Log.d("نوع_القيد", "المختار: " + selectedTypeName + ", id: " + selectedTypeId);
			double debit = debitStr.isEmpty() ? 0 : Double.parseDouble(debitStr);
			double credit = creditStr.isEmpty() ? 0 : Double.parseDouble(creditStr);

			// التحقق من البيانات
			if (details.isEmpty()) {
				editTextDetails.setError("البيان مطلوب");
				return;
			}

			if ((debit > 0 && credit > 0) || (debit == 0 && credit == 0)) {
				Toast.makeText(getContext(), "أدخل مبلغ في المدين أو الدائن فقط", Toast.LENGTH_SHORT).show();
				return;
			}

			if (selectedTypeId == -1) {
				Toast.makeText(getContext(), "اختر نوع القيد", Toast.LENGTH_SHORT).show();
				return;
			}
		/*	Constraint constraint = new Constraint();
			constraint.setAccountId(accountId);
			constraint.setDate(date);
			constraint.setDetails(details);
			constraint.setDebit(debit);
			constraint.setCredit(credit);
			constraint.setConstraintTypeId(selectedTypeId);
			Log.d("نوع_القيد", "المختار: " + selectedTypeName + ", id: " + selectedTypeId);*/
			// الإدخال في قاعدة البيانات
			// الإدخال في قاعدة البيانات
			boolean success = dbHelper.insertConstraint(accountId, date, details, debit, credit, selectedTypeId // إضافة معرّف نوع القيد
			);
			if (success) {
				Toast.makeText(getContext(), "✅ تم إضافة القيد بنجاح", Toast.LENGTH_SHORT).show();
				if (listener != null) {
					listener.onConstraintAdded();
				}
				dismiss();
			} else {
				Toast.makeText(getContext(), "❌ فشل في إضافة القيد", Toast.LENGTH_SHORT).show();
			}
		});

		return view;
	}

	public String getCurrentSelectedDate() {
		return editTextDate != null ? editTextDate.getText().toString() : null;
	}

	/*	@Override
		public void onDestroyView() {
			super.onDestroyView();
			//todayDate = null;
			if (todayDate != null && dListener != null) {
				dListener.onDateSelected(todayDate);
			}
		}*/
}