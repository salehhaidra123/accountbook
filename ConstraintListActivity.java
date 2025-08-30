package com.my.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.my.myapp.reports.ConstraintPdfGenerator;
import com.my.myapp.utils.PreferencesManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstraintListActivity extends AppCompatActivity implements
		EditConstraintDialogFragment.OnConstraintEditedListener, AddConstraintDialogFragment.OnDateSelectedListener,
		DateFilterDialog.OnFilterListener, DeleteConstraintDialogFragment.OnConstraintsDeletedListener {

	// Constants
	private static final String TAG = "ConstraintListActivity";
	private static final int REQUEST_PERMISSION = 123;

	// UI Components
	private MaterialToolbar toolbar;
	private RecyclerView recyclerView;
	private TextView tvDebit, tvCredit, tvBalance, tvRight, tvLeft;
	private ImageButton btnFilter;
	private LinearLayout aboveBottomBar;

	// Data Components
	private ConstraintAdapter adapter;
	private DatabaseHelper dbHelper;
	private List<Constraint> constraintList;
	private Map<Integer, String> typeIdToNameMap = new HashMap<>();

	// State Variables
	private String accountType;
	private int accountId;
	private String lastSelectedDate = null;
	private String fromDateFilter = null;
	private String toDateFilter = null;
	private boolean isFiltered = false;
	private boolean isAboveBottomBarVisible = true;
	private boolean isProcessingLongClick = false;

	// Action Mode
	private ActionMode actionMode;

	// Preferences Manager
	private PreferencesManager preferencesManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_constraint_list);

		// Initialize components
		initViews();
		initData();
		setupRecyclerView();
		loadInitialData(); // This creates the adapter
		setupClickListeners();
		// Load initial data

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		lastSelectedDate = null;
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	@Override
	public void onBackPressed() {
		if (actionMode != null) {
			actionMode.finish();
		} else {
			super.onBackPressed();
		}
	}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.top_app_bar, menu);
	
	// Search setup
	MenuItem searchItem = menu.findItem(R.id.action_search);
	SearchView searchView = (SearchView) searchItem.getActionView();
	setupSearchView(searchView);
	
	// PDF setup
	MenuItem pdfItem = menu.findItem(R.id.action_pdf);
	pdfItem.setOnMenuItemClickListener(item -> {
		if (checkPermissions()) createPdfAndOpen();
		return true;
	});
	
	// إظهار الأيقونات في قائمة الفائض
	if (menu instanceof MenuBuilder) {
		MenuBuilder menuBuilder = (MenuBuilder) menu;
		menuBuilder.setOptionalIconsVisible(true);
	}
	MenuItem settingsItem = menu.findItem(R.id.action_settings);
    if (settingsItem != null) {
        settingsItem.setVisible(false);
    }
	return true;
}


@Override
public boolean onOptionsItemSelected(MenuItem item) {
	int id = item.getItemId();
	
	if (id == R.id.action_share) {
		// showShareDialog();
		Toast.makeText(this, "مشاركة", Toast.LENGTH_SHORT).show();
		return true;
		} else if (id == R.id.action_export_excel) {
		// exportExcel();
		Toast.makeText(this, "تصدير Excel", Toast.LENGTH_SHORT).show();
		return true;
		} else if (id == R.id.action_settings) {
		// openSettings();
		Toast.makeText(this, "الإعدادات", Toast.LENGTH_SHORT).show();
		return true;
	}
	
	return super.onOptionsItemSelected(item);
}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				createPdfAndOpen();
			} else {
				Toast.makeText(this, "Permissions required to save file", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Interface Implementations
	@Override
	public void onConstraintEdited() {
		loadConstraints();
	}

	@Override
	public void onConstraintsDeleted() {
		loadConstraints();
	}

	@Override
	public void onDateSelected(String selectedDate) {
		lastSelectedDate = selectedDate;
	}

	@Override
	public void onFilter(String fromDate, String toDate) {
		fromDateFilter = fromDate;
		toDateFilter = toDate;
		filterConstraints(fromDate, toDate, accountId);
		btnFilter.setImageResource(R.drawable.ic_redo);
		isFiltered = true;
		recalculateTotals();
	}

	// Initialization Methods
	private void initViews() {
		toolbar = findViewById(R.id.topAppBar);
		recyclerView = findViewById(R.id.recycler_view);
		tvDebit = findViewById(R.id.tv_debit);
		tvCredit = findViewById(R.id.tv_credit);
		tvBalance = findViewById(R.id.tv_balance);
		tvRight = findViewById(R.id.tv_right);
		tvLeft = findViewById(R.id.tv_left);
		btnFilter = findViewById(R.id.btn_filter);
		aboveBottomBar = findViewById(R.id.aboveBottomBar);

		setSupportActionBar(toolbar);
		if (toolbar.getOverflowIcon() != null) {
			toolbar.getOverflowIcon().setTint(ContextCompat.getColor(this, android.R.color.black));
		}
	}

	private void initData() {
		Intent intent = getIntent();
		accountId = intent.getIntExtra("account_id", -1);
		String accountName = intent.getStringExtra("account_name");
		accountType = intent.getStringExtra("account_type");

		if (accountId == -1) {
			Toast.makeText(this, "Error: No account selected", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		toolbar.setTitle(accountName);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());

		dbHelper = new DatabaseHelper(this);
		preferencesManager = PreferencesManager.getInstance(this);
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		// إضافة مستمع التمرير لإخفاء/إظهار الشريط السفلي
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy > 0 && isAboveBottomBarVisible) {
					hideAboveBottomBar();
				} else if (dy < 0 && !isAboveBottomBarVisible) {
					showAboveBottomBar();
				}
			}
		});

		// مزامنة التمرير الأفقي بين الرأس والـ RecyclerView
		HorizontalScrollView headerScroll = findViewById(R.id.horizontal_scrollbar_header);
		CustomHorizontalScrollView containerScroll = findViewById(R.id.scroll_view_horizontal);

		// عند تمرير الرأس، قم بتمرير الحاوية
		headerScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				containerScroll.scrollTo(scrollX, 0);
			}
		});

		// عند تمرير الحاوية، قم بتمرير الرأس
		containerScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				headerScroll.scrollTo(scrollX, 0);
			}
		});
	}

	private void setupClickListeners() {
		// Set item click listener
		adapter.setOnItemClickListener(position -> {
			if (actionMode != null) {
				adapter.toggleSelection(position);
				actionMode.invalidate();
				if (adapter.getSelectedItems().isEmpty()) {
					actionMode.finish();
				}
			}
		});

		// Set item long click listener
		adapter.setOnItemLongClickListener(position -> {
			if (actionMode == null) {
				// Activate ActionMode first
				actionMode = startSupportActionMode(actionModeCallback);
				if (actionMode != null) {
					// Then select the item
					adapter.toggleSelection(position);
					actionMode.invalidate();
				}
			} else {
				// If ActionMode is already active, just toggle selection
				adapter.toggleSelection(position);
				actionMode.invalidate();
			}
			return true;
		});

		// Set button listeners
		findViewById(R.id.btn_add_constraint).setOnClickListener(v -> showAddConstraintDialog());
		btnFilter.setOnClickListener(v -> openFilterDialog());
	}

	// Data Loading Methods
	private void loadInitialData() {
		// Load constraint types
		List<ConstraintType> types = dbHelper.getAllConstraintTypes();
		for (ConstraintType type : types) {
			typeIdToNameMap.put(type.getConstraintTypeId(), type.getConstraintTypeName());
		}

		// Load constraints
		loadConstraints();
	}

	private void loadConstraints() {
		if (dbHelper == null)
			return;

		List<Constraint> newConstraints = dbHelper.getAllConstraintsByAccountId(accountId);
		constraintList = newConstraints != null ? newConstraints : new ArrayList<>();

		if (adapter == null) {
			adapter = new ConstraintAdapter(this, constraintList, accountType);
			adapter.setTypeIdToNameMap(typeIdToNameMap);
			recyclerView.setAdapter(adapter);
			setupClickListeners();
		} else {
			adapter.updateData(constraintList);
		}

		recalculateTotals();
	}

	private void filterConstraints(String fromDate, String toDate, int accountId) {
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
				"SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?"
						+ " AND " + DBConstants.COL_CONST_DATE + " >= ?" + " AND " + DBConstants.COL_CONST_DATE
						+ " <= ?" + " ORDER BY " + DBConstants.COL_CONST_DATE + " ASC",
				new String[] { String.valueOf(accountId), fromDate, toDate });

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

		constraintList = filteredList;
		if (adapter != null) {
			adapter.updateData(constraintList);
		}
		recalculateTotals();
	}

	// UI Update Methods
	private void recalculateTotals() {
		boolean calculateFromDisplay = preferencesManager.getCalculateFromDisplay();
		if (calculateFromDisplay) {
			calculateTotalsFromDisplayedData();
		} else {
			updateBalanceSummary();
		}
	}

	private void calculateTotalsFromDisplayedData() {
		if (constraintList == null || constraintList.isEmpty()) {
			updateBalanceSummaryDisplay(0.0, 0.0, 0.0);
			return;
		}

		double totalDebit = 0.0;
		double totalCredit = 0.0;
		for (Constraint constraint : constraintList) {
			totalDebit += constraint.getDebit();
			totalCredit += constraint.getCredit();
		}

		double totalBalance;
		if ("صندوق".equals(accountType) || "مدين".equals(accountType)) {
			totalBalance = totalDebit - totalCredit;
		} else if ("دائن".equals(accountType)) {
			totalBalance = totalCredit - totalDebit;
		} else {
			totalBalance = 0.0;
		}

		updateBalanceSummaryDisplay(totalDebit, totalCredit, totalBalance);
	}

	private void updateBalanceSummary() {
		if (dbHelper == null)
			return;

		double debit = dbHelper.getTotalDebitByDate(accountId, fromDateFilter, toDateFilter);
		double credit = dbHelper.getTotalCreditByDate(accountId, fromDateFilter, toDateFilter);
		double balance = dbHelper.getAccountBalanceByDate(accountId, accountType, fromDateFilter, toDateFilter);

		updateBalanceSummaryDisplay(debit, credit, balance);
	}

	private void updateBalanceSummaryDisplay(double debit, double credit, double balance) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);

		tvDebit.setText("مدين:  " + formatter.format(debit));
		tvCredit.setText("دائن:  " + formatter.format(credit));
		tvBalance.setText("الرصيد:  " + formatter.format(balance));
		tvRight.setText("مدين : " + formatter.format(debit));
		tvLeft.setText("دائن:  " + formatter.format(credit));

		if (balance < 0) {
			tvBalance.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
		} else {
			tvBalance.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
		}
	}

	private void hideAboveBottomBar() {
		if (aboveBottomBar != null && isAboveBottomBarVisible) {
			aboveBottomBar.animate().translationY(aboveBottomBar.getHeight()).setDuration(200)
					.withEndAction(() -> isAboveBottomBarVisible = false).start();
		}
	}

	private void showAboveBottomBar() {
		if (aboveBottomBar != null && !isAboveBottomBarVisible) {
			aboveBottomBar.animate().translationY(0).setDuration(200)
					.withEndAction(() -> isAboveBottomBarVisible = true).start();
		}
	}

	// Search Methods
	private void setupSearchView(SearchView searchView) {
		searchView.setQueryHint("Search in details...");
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchInDisplayedConstraints(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				searchInDisplayedConstraints(newText);
				return true;
			}
		});

		searchView.setOnCloseListener(() -> {
			if (fromDateFilter != null && toDateFilter != null) {
				filterConstraints(fromDateFilter, toDateFilter, accountId);
			} else {
				loadConstraints();
			}
			return false;
		});
	}

	private void searchInDisplayedConstraints(String searchText) {
		List<Constraint> originalList;
		if (fromDateFilter != null && toDateFilter != null) {
			originalList = getFilteredConstraintsFromDB(fromDateFilter, toDateFilter, accountId);
		} else {
			originalList = dbHelper.getAllConstraintsByAccountId(accountId);
		}

		List<Constraint> filteredList = new ArrayList<>();
		if (searchText.isEmpty()) {
			filteredList = originalList;
		} else {
			String normalizedSearchText = normalizeArabic(searchText);
			for (Constraint constraint : originalList) {
				String normalizedDetails = normalizeArabic(constraint.getDetails());
				if (normalizedDetails.contains(normalizedSearchText)) {
					filteredList.add(constraint);
				}
			}
		}

		constraintList = filteredList;
		if (adapter != null) {
			adapter.updateData(constraintList);
		}
		recalculateTotals();
	}

	private List<Constraint> getFilteredConstraintsFromDB(String fromDate, String toDate, int accountId) {
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
				"SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?"
						+ " AND " + DBConstants.COL_CONST_DATE + " >= ?" + " AND " + DBConstants.COL_CONST_DATE
						+ " <= ?" + " ORDER BY " + DBConstants.COL_CONST_DATE + " ASC",
				new String[] { String.valueOf(accountId), fromDate, toDate });

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
		return filteredList;
	}

	private String normalizeArabic(String text) {
		if (text == null)
			return "";
		text = text.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا");
		text = text.replace("ة", "ه");
		text = text.replace("َ", "").replace("ُ", "").replace("ِ", "").replace("ً", "").replace("ٌ", "")
				.replace("ٍ", "").replace("ّ", "").replace("ْ", "");
		return text;
	}

	// Filter Methods
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

	private void clearFilter(int accountId) {
		fromDateFilter = null;
		toDateFilter = null;
		loadConstraints();
	}

	// Dialog Methods
	private void showAddConstraintDialog() {
		AddConstraintDialogFragment addDialog = AddConstraintDialogFragment.newInstance(accountId);
		if (lastSelectedDate != null) {
			addDialog.setInitialDate(lastSelectedDate);
		}
		addDialog.setOnConstraintAddedListener(() -> {
			lastSelectedDate = addDialog.getCurrentSelectedDate();
			loadConstraints();
		});
		addDialog.show(getSupportFragmentManager(), "AddConstraintDialog");
	}

	// PDF Export Methods
	private void createPdfAndOpen() {
		if (checkPermissions()) {
			String accountName = getIntent().getStringExtra("account_name");
			if (accountName == null)
				accountName = "Unknown Account";

			ConstraintPdfGenerator.createAndOpenPdf(this, accountName, constraintList, typeIdToNameMap, accountId,
					accountType, fromDateFilter, toDateFilter);
		}
	}

	private boolean checkPermissions() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(
						Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
					return true;
				} else {
					requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
					return false;
				}
			} else {
				return true;
			}
		}
		return true;
	}

	// Action Mode Callback
	private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.contextual_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			int selectedCount = adapter.getSelectedItems().size();

			MenuItem editItem = menu.findItem(R.id.menu_edit);
			MenuItem deleteItem = menu.findItem(R.id.menu_delete);

			// Show edit option only for single selection
			if (editItem != null) {
				editItem.setVisible(selectedCount == 1);
			}

			// Show delete option for any selection
			if (deleteItem != null) {
				deleteItem.setVisible(selectedCount > 0);
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			List<Constraint> selectedConstraints = adapter.getSelectedConstraints();

			if (selectedConstraints.isEmpty()) {
				mode.finish();
				return true;
			}

			if (item.getItemId() == R.id.menu_edit) {
				if (selectedConstraints.size() == 1) {
					Constraint constraintToEdit = selectedConstraints.get(0);
					EditConstraintDialogFragment editDialog = EditConstraintDialogFragment.newInstance(constraintToEdit,
							"Edit Constraint");
					editDialog.setOnConstraintEditedListener(ConstraintListActivity.this);
					editDialog.show(getSupportFragmentManager(), "EditDialog");
				}
				mode.finish();
				return true;
			} else if (item.getItemId() == R.id.menu_delete) {
				DeleteConstraintDialogFragment deleteDialog = DeleteConstraintDialogFragment
						.newInstance(selectedConstraints);
				deleteDialog.setOnConstraintsDeletedListener(ConstraintListActivity.this);
				deleteDialog.show(getSupportFragmentManager(), "DeleteDialog");
				mode.finish();
				return true;
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			if (adapter != null) {
				adapter.clearSelection();
			}
		}
	};

	// Getter
	public String getLastSelectedDate() {
		return lastSelectedDate;
	}
}