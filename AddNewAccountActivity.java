package com.my.myapp;

import com.my.myapp.Account;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.my.myapp.DatabaseHelper;
import com.my.myapp.R;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.Calendar;

public class AddNewAccountActivity extends AppCompatActivity {
	TextInputEditText etDate, etName, etPhone ;
	TextInputLayout etDateLayout;
	AutoCompleteTextView spAccType, spAccGroup;
	String selectedAccType, selectedAccGroup;
	Button btnAddAccount;
	DatabaseHelper dbHelper;
	Toolbar toolbar;
	Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_account);

		dbHelper = new DatabaseHelper(this);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		etName = findViewById(R.id.et_account_name);
		etName.requestFocus();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		etDate = findViewById(R.id.et_account_date);
		String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
		etDate.setText(currentDate);
		etDateLayout = findViewById(R.id.input_layout_account_date);

		spAccType = findViewById(R.id.sp_account_type);
		//spAccType.requestFocus();
		String[] accountTypeArray = {  "مدين", "دائن", "صندوق" };
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
				accountTypeArray);
		spAccType.setAdapter(adapter);
		spAccType.setText(accountTypeArray[0] ,false );
	//	selectedAccType = accountTypeArray [0];
		etPhone = findViewById(R.id.et_account_phone);
		spAccGroup = findViewById(R.id.sp_account_group);
		spAccGroup.setText("عام" , false);
		btnAddAccount = findViewById(R.id.btn_add_new_acc);

		/*		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.account_types,
				android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter); */

		spAccType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				selectedAccType = parent.getItemAtPosition(position).toString();

				if (!selectedAccType.isEmpty()) {
					Toast.makeText(getApplicationContext(), "النوع المختار: " + selectedAccType, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		etDate.setOnClickListener(v -> {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) -> {
				String selectedDate = y + "-" + (m + 1) + "-" + d;
				etDate.setText(selectedDate);
			}, year, month, day);

			datePicker.show();
		});

		btnAddAccount.setOnClickListener(view -> {
			String date = etDate.getText().toString();
			String accountType = selectedAccType != null ? selectedAccType.trim() : "";
			String name = etName.getText().toString().trim();
			String phone = etPhone.getText().toString().trim();
			String accountGroup = spAccGroup.getText().toString().trim();

			if (name.isEmpty() || date.isEmpty() || accountType.isEmpty()) {
				Toast.makeText(AddNewAccountActivity.this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
				return;
			}
			Account account = new  Account();
			account.setCreatedDate(date);
			account.setAccountType(accountType);
			account.setAccountName(name);
			account.setAccountPhone(phone);
			account.setAccountGroup(accountGroup);
			
			boolean inserted = dbHelper.insertAccount(account);
	//	boolean  inserted = dbHelper.insertAccount(account, typeId, groupId);
			
			
			
		//	boolean inserted = dbHelper.insertAccount(date, accountType, name, phone, accountGroup);
			if (inserted) {
				Toast.makeText(this, "تمت إضافة الحساب بنجاح", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "فشل في إضافة الحساب", Toast.LENGTH_SHORT).show();
			}

			setResult(RESULT_OK);
			finish();
		});
	
		
		
		
		
	}
}