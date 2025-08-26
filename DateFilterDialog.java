package com.my.myapp;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.app.Dialog;
import android.view.View;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import java.util.Calendar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.widget.EditText;
import android.widget.Button;

public class DateFilterDialog extends DialogFragment {
	
	private EditText fromDateEdit, toDateEdit;
	private OnFilterListener listener;
	private Button goButton, cancelButton;
	
	public interface OnFilterListener {
		void onFilter(String fromDate, String toDate);
	}
	
	public void setOnFilterListener(OnFilterListener listener){
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// استخدم الـ Style المخصص هنا
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dalog_date_filter, null);
		
		fromDateEdit = view.findViewById(R.id.from_date);
		toDateEdit = view.findViewById(R.id.to_date);
		goButton = view.findViewById(R.id.goButton);
		cancelButton = view.findViewById(R.id.cancelButton);
		
		fromDateEdit.setOnClickListener(v -> showDatePicker(fromDateEdit));
		toDateEdit.setOnClickListener(v -> showDatePicker(toDateEdit));
		
		// إعداد الأزرار المخصصة
		goButton.setOnClickListener(v -> {
			if(listener != null){
				listener.onFilter(fromDateEdit.getText().toString(),
				toDateEdit.getText().toString());
				dismiss();
			}
		});
		
		cancelButton.setOnClickListener(v -> dismiss());
		
		builder.setView(view);
	//	.setTitle("فلترة حسب التاريخ");
		
		return builder.create();
	}
	
	private void showDatePicker(EditText editText){
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog dialog = new DatePickerDialog(getContext(),
		(view, year, month, dayOfMonth) -> {
			// إضافة صفر أمام اليوم والشهر إذا كان رقم واحد
			String day = String.format("%02d", dayOfMonth);
			String mon = String.format("%02d", month + 1);
			String date = day + "/" + mon + "/" + year; // dd/MM/yyyy
			editText.setText(date);
		},
		calendar.get(Calendar.YEAR),
		calendar.get(Calendar.MONTH),
		calendar.get(Calendar.DAY_OF_MONTH));
		dialog.show();
	}
}