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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditConstraintDialogFragment extends DialogFragment {
	private EditText editTextDate, editTextDetails, editTextDebit, editTextCredit;
	private AutoCompleteTextView autoConstraintType;
	private Constraint constraint;
	private DatabaseHelper dbHelper;
	private OnConstraintEditedListener listener;
	private List<ConstraintType> constraintTypeList;
	private ArrayAdapter<String> constraintTypeAdapter;
	private Map<String, Integer> typeNameToIdMap = new HashMap<>();
	
	public interface OnConstraintEditedListener {
		void onConstraintEdited();
	}
	
	public static EditConstraintDialogFragment newInstance(Constraint constraint, String titleText) {
		EditConstraintDialogFragment fragment = new EditConstraintDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable("constraint", constraint);
		args.putString("title", titleText);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void setOnConstraintEditedListener(OnConstraintEditedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnConstraintEditedListener) {
			listener = (OnConstraintEditedListener) context;
			} else {
			throw new RuntimeException(context.toString() + " must implement OnConstraintEditedListener");
		}
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_constraint_dialog, container, false);
		
		TextView title = view.findViewById(R.id.dialog_title);
		editTextDate = view.findViewById(R.id.edit_text_date);
		autoConstraintType = view.findViewById(R.id.auto_complete_constraint_type);
		editTextDetails = view.findViewById(R.id.edit_text_details);
		editTextDebit = view.findViewById(R.id.edit_text_debit);
		editTextCredit = view.findViewById(R.id.edit_text_credit);
		Button btnSave = view.findViewById(R.id.btn_edit_constraint);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		
		String titleText = getArguments() != null ? getArguments().getString("title", "تعديل القيد") : "تعديل القيد";
		title.setText(titleText);
		
		dbHelper = new DatabaseHelper(getContext());
		
		// تحميل أنواع القيود وربطها بالـ AutoCompleteTextView
		constraintTypeList = dbHelper.getAllConstraintTypes();
		List<String> typeNames = new ArrayList<>();
		typeNameToIdMap = new HashMap<>();
		
		for (ConstraintType type : constraintTypeList) {
			typeNames.add(type.getConstraintTypeName());
			typeNameToIdMap.put(type.getConstraintTypeName(), type.getConstraintTypeId());
		}
		
		constraintTypeAdapter = new ArrayAdapter<>(
		getContext(),
		android.R.layout.simple_dropdown_item_1line,
		typeNames
		);
		autoConstraintType.setAdapter(constraintTypeAdapter);
		autoConstraintType.setOnClickListener(v -> autoConstraintType.showDropDown());
		
		if (getArguments() != null) {
			constraint = (Constraint) getArguments().getSerializable("constraint");
			if (constraint != null) {
				editTextDate.setText(constraint.getDate());
				editTextDetails.setText(constraint.getDetails());
				editTextDebit.setText(String.valueOf(constraint.getDebit()));
				editTextCredit.setText(String.valueOf(constraint.getCredit()));
				
				// تعيين نوع القيد الحالي
				String currentTypeName = "";
				int currentTypeId = constraint.getConstraintTypeId();
				for (Map.Entry<String, Integer> entry : typeNameToIdMap.entrySet()) {
					if (entry.getValue() == currentTypeId) {
						currentTypeName = entry.getKey();
						break;
					}
				}
				autoConstraintType.setText(currentTypeName, false);
			}
		}
		
		final Calendar calendar = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		
		editTextDate.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
				calendar.set(year, month, dayOfMonth);
				String selectedDate = sdf.format(calendar.getTime());
				editTextDate.setText(selectedDate);
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		});
		
		btnCancel.setOnClickListener(v -> dismiss());
		
		btnSave.setOnClickListener(v -> {
			String date = editTextDate.getText().toString().trim();
			String details = editTextDetails.getText().toString().trim();
			String debitStr = editTextDebit.getText().toString().trim();
			String creditStr = editTextCredit.getText().toString().trim();
			
			// الحصول على نوع القيد المختار
			String selectedTypeName = autoConstraintType.getText().toString().trim();
			int selectedTypeId = typeNameToIdMap.containsKey(selectedTypeName) ?
			typeNameToIdMap.get(selectedTypeName) : -1;
			
			Log.d("نوع_القيد", "المختار: " + selectedTypeName + ", id: " + selectedTypeId);
			
			double debit = debitStr.isEmpty() ? 0 : Double.parseDouble(debitStr);
			double credit = creditStr.isEmpty() ? 0 : Double.parseDouble(creditStr);
			
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
			
			if (constraint != null) {
				constraint.setDate(date);
				constraint.setDetails(details);
				constraint.setDebit(debit);
				constraint.setCredit(credit);
				constraint.setConstraintTypeId(selectedTypeId);
				
				boolean success = dbHelper.updateConstraint(constraint.getId(), constraint.getDate(),
				constraint.getDetails(), constraint.getDebit(), constraint.getCredit(),
				constraint.getConstraintTypeId());
				
				if (success) {
					Toast.makeText(getContext(), "✅ تم تعديل القيد بنجاح", Toast.LENGTH_SHORT).show();
					if (listener != null) {
						listener.onConstraintEdited();
					}
					dismiss();
					} else {
					Toast.makeText(getContext(), "❌ فشل في تعديل القيد", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		return view;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
}