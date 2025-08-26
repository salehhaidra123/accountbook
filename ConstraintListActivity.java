package com.my.myapp;

import android.content.Intent;
import android.database.Cursor;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.my.myapp.reports.ConstraintPdfGenerator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstraintListActivity extends AppCompatActivity
implements EditConstraintDialogFragment.OnConstraintEditedListener,
AddConstraintDialogFragment.OnDateSelectedListener, DateFilterDialog.OnFilterListener {
	
	TextView tvDebit, tvCredit, tvBalance, tvTitle;
	Toolbar toolbar;
	ImageButton btnFilter;
	private String fromDateFilter = null;
	private String toDateFilter = null;
	boolean isFiltered = false;
	private MaterialToolbar topAppBar;
	private RecyclerView recyclerView;
	private ConstraintAdapter adapter;
	private DatabaseHelper dbHelper;
	private List<Constraint> constraintList; // قد يكون null في البداية
	private String accountType = "مدين";
	private ActionMode actionMode;
	private Constraint selectedConstraint;
	private int selectedPosition = -1;
	private int accountId;
	private String lastSelectedDate = null;
	Map<Integer, String> typeIdToNameMap = new HashMap<>();
	private static final int REQUEST_PERMISSION = 123;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_constraint_list);
		
		Intent intent = getIntent();
		accountId = intent.getIntExtra("account_id", -1);
		String accountName = intent.getStringExtra("account_name");
		accountType = intent.getStringExtra("account_type");
		
		// تحقق مهم من صحة ID الحساب
		if (accountId == -1) {
			Toast.makeText(this, "خطأ: لم يتم تحديد حساب", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		topAppBar = findViewById(R.id.topAppBar);
		setSupportActionBar(topAppBar);
		topAppBar.setTitle(accountName);
		topAppBar.setNavigationOnClickListener(v -> onBackPressed());
		
		topAppBar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.action_pdf) {
				if (checkPermissions()) {
					createPdfAndOpen();
				}
				return true;
			}
			return false;
		});
		
		recyclerView = findViewById(R.id.recycler_view);
		ImageButton btnAdd = findViewById(R.id.btn_add_constraint);
		dbHelper = new DatabaseHelper(this); // تهيئة dbHelper
		
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
		recyclerView.addItemDecoration(divider);
		
		// تحميل أنواع القيود أولاً
		List<ConstraintType> types = dbHelper.getAllConstraintTypes();
		for (ConstraintType type : types) {
			typeIdToNameMap.put(type.getConstraintTypeId(), type.getConstraintTypeName());
		}
		
		// تحميل القيود وتهيئة الـ Adapter
		loadConstraints(); // سيقوم بتهيئة constraintList و adapter
		
		// إعداد مستمعي النقر
		adapter.setOnItemClickListener(position -> {
			if (actionMode != null) {
				toggleSelection(position);
			}
		});
		
		adapter.setOnItemLongClickListener(position -> {
			if (actionMode == null) {
				selectedConstraint = constraintList.get(position);
				selectedPosition = position;
				actionMode = startActionMode(actionModeCallback);
				adapter.notifyItemChanged(position);
			}
			return true;
		});
		
		btnAdd.setOnClickListener(v -> {
			AddConstraintDialogFragment addDialog = AddConstraintDialogFragment.newInstance(accountId);
			if (lastSelectedDate != null) {
				addDialog.setInitialDate(lastSelectedDate);
			}
			addDialog.setOnConstraintAddedListener(() -> {
				lastSelectedDate = addDialog.getCurrentSelectedDate();
				loadConstraints(); // إعادة تحميل البيانات بعد الإضافة
			});
			addDialog.show(getSupportFragmentManager(), "AddConstraintDialog");
		});
		
		btnFilter = findViewById(R.id.btn_filter);
		btnFilter.setOnClickListener(v -> openFilterDialog());
	}
	
	private void filterConstraints(String fromDate, String toDate, int accountId) {
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
		"SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS +
		" WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?" +
		" AND " + DBConstants.COL_CONST_DATE + " >= ?" +
		" AND " + DBConstants.COL_CONST_DATE + " <= ?" +
		" ORDER BY " + DBConstants.COL_CONST_DATE + " ASC",
		new String[]{String.valueOf(accountId), fromDate, toDate});
		
		ArrayList<Constraint> filteredList = new ArrayList<>();
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_ID));
				int accId = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_ACCOUNT_ID));
				int typeId = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_TYPE));
				String date = cursor.getString(cursor.getColumnIndex(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndex(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndex(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndex(DBConstants.COL_CONST_CREDIT));
				filteredList.add(new Constraint(id, accId, typeId, date, details, debit, credit));
			} while (cursor.moveToNext());
		}
		cursor.close();
		// تحديث القائمة المرجعية والـ adapter
		constraintList = filteredList;
		if (adapter != null) {
			adapter.updateData(constraintList);
		}
	}
	
	public void openFilterDialog() {
		if (!isFiltered) {
			DateFilterDialog dialog = new DateFilterDialog();
			dialog.setOnFilterListener(this);
			dialog.show(getSupportFragmentManager(), "FilterDialog");
			} else {
			clearFilter(accountId);
			btnFilter.setImageResource(R.drawable.ic_filter);
			isFiltered = false;
		}
	}
	
	@Override
	public void onFilter(String fromDate, String toDate) {
		fromDateFilter = fromDate;
		toDateFilter = toDate;
		filterConstraints(fromDate, toDate, accountId);
		btnFilter.setImageResource(R.drawable.ic_redo);
		isFiltered = true;
		updateBalanceSummary();
	}
	
	private void clearFilter(int accountId) {
		fromDateFilter = null;
		toDateFilter = null;
		loadConstraints(); // إعادة تحميل كل القيود
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.top_app_bar, menu);
		return true;
	}
	
	private void toggleSelection(int position) {
		if (selectedPosition == position) {
			selectedConstraint = null;
			selectedPosition = -1;
			if (actionMode != null) {
				actionMode.finish();
			}
			} else {
			selectedConstraint = constraintList.get(position);
			selectedPosition = position;
			adapter.notifyDataSetChanged();
		}
	}
	
	private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		// ... (كود actionModeCallback يبقى كما هو بدون تغيير)
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.contextual_menu, menu);
			return true;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.menu_edit) {
				if (selectedConstraint != null) {
					EditConstraintDialogFragment editDialog = EditConstraintDialogFragment
					.newInstance(selectedConstraint, "تعديل القيد");
					editDialog.setOnConstraintEditedListener(ConstraintListActivity.this);
					editDialog.show(getSupportFragmentManager(), "EditDialog");
				}
				mode.finish();
				return true;
				} else if (item.getItemId() == R.id.menu_delete) {
				if (selectedConstraint != null) {
					DeleteConstraintDialogFragment deleteDialog = DeleteConstraintDialogFragment
					.newInstance(selectedConstraint);
					deleteDialog.setOnConstraintDeletedListener(() -> loadConstraints());
					deleteDialog.show(getSupportFragmentManager(), "DeleteDialog");
				}
				mode.finish();
				return true;
			}
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			selectedConstraint = null;
			selectedPosition = -1;
			adapter.notifyDataSetChanged();
		}
	};
	
	private void loadConstraints() {
		if (dbHelper == null) {
			dbHelper = new DatabaseHelper(this);
		}
		List<Constraint> newConstraints = dbHelper.getAllConstraintsByAccountId(accountId);
		if (newConstraints != null) {
			constraintList = newConstraints;
			// إذا كان الـ adapter لم يتم إنشاؤه بعد، قم بإنشائه
			if (adapter == null) {
				adapter = new ConstraintAdapter(this, constraintList, accountType);
				adapter.setTypeIdToNameMap(typeIdToNameMap);
				recyclerView.setAdapter(adapter);
				} else {
				// إذا كان موجوداً، فقط قم بتحديث بياناته
				adapter.updateData(constraintList);
			}
		}
		updateBalanceSummary();
	}
	
	@Override
	public void onConstraintEdited() {
		loadConstraints();
	}
	
	private void updateBalanceSummary() {
		tvDebit = findViewById(R.id.tv_debit);
		tvCredit = findViewById(R.id.tv_credit);
		tvBalance = findViewById(R.id.tv_balance);
		
		if (dbHelper == null) return; // لا تفعل شيئاً إذا dbHelper لم يُهيأ
		
		double debit = dbHelper.getTotalDebitByDate(accountId, fromDateFilter, toDateFilter);
		double credit = dbHelper.getTotalCreditByDate(accountId, fromDateFilter, toDateFilter);
		double balance = dbHelper.getAccountBalanceByDate(accountId, accountType, fromDateFilter, toDateFilter);
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);
		
		tvDebit.setText("مدين:  " + formatter.format(debit));
		tvCredit.setText("دائن:  " + formatter.format(credit));
		tvBalance.setText("الرصيد:  " + formatter.format(balance));
		
		if (balance < 0) {
			tvBalance.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
			} else {
			tvBalance.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
		}
	}
	
	@Override
	public void onDateSelected(String selectedDate) {
		lastSelectedDate = selectedDate;
	}
	
	public String getLastSelectedDate() {
		return lastSelectedDate;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		lastSelectedDate = null;
		if (dbHelper != null) {
			dbHelper.close(); // إغلاق الاتصال بقاعدة البيانات عند تدمير النشاط
		}
	}
	
	private void createPdfAndOpen() {
		if (checkPermissions()) {
			String accountName = getIntent().getStringExtra("account_name");
			if (accountName == null) accountName = "حساب غير معروف";
			
			ConstraintPdfGenerator.createAndOpenPdf(
			this,
			accountName,
			constraintList,
			typeIdToNameMap,
			accountId,
			accountType,
			fromDateFilter,
			toDateFilter
			);
		}
	}
	
	private boolean checkPermissions() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
					return true;
					} else {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
					return false;
				}
				} else {
				return true;
			}
		}
		return true;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				createPdfAndOpen();
				} else {
				Toast.makeText(this, "الصلاحيات مطلوبة لحفظ الملف", Toast.LENGTH_SHORT).show();
			}
		}
	}
}