package com.my.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

public class ConstraintListActivity extends AppCompatActivity implements
EditConstraintDialogFragment.OnConstraintEditedListener, AddConstraintDialogFragment.OnDateSelectedListener {
	
	TextView tvDebit, tvCredit, tvBalance;
	Toolbar toolbar;
	private RecyclerView recyclerView;
	private ConstraintAdapter adapter;
	private DatabaseHelper dbHelper;
	private List<Constraint> constraintList;
	private String accountType = "مدين"; // يمكنك تغييره بناءً على السياق
	private ActionMode actionMode;
	private Constraint selectedConstraint;
	private int selectedPosition = -1;
	private int accountId;
	private String lastSelectedDate = null;
	Map<Integer, String> typeIdToNameMap = new HashMap<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_constraint_list);
		
		Intent intent = getIntent();
		accountId = intent.getIntExtra("account_id", -1);
		String accountName = intent.getStringExtra("account_name");
		String accountType = intent.getStringExtra("account_type");
		
		if (accountId == -1) {
			Toast.makeText(this, "Error: Account ID not found", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("حساب:" + accountName);
		
		recyclerView = findViewById(R.id.recycler_view);
		ImageButton btnAdd = findViewById(R.id.btn_add_constraint);
		
		
		dbHelper = new DatabaseHelper(this);
		
		//setup RecyclerView
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
		LinearLayoutManager.VERTICAL);
		recyclerView.addItemDecoration(divider);
		
		constraintList = dbHelper.getAllConstraintsByAccountId(accountId);
		adapter = new ConstraintAdapter(this, constraintList, accountType);
		recyclerView.setAdapter(adapter);
		
		//bring const Type
		List<ConstraintType> types = dbHelper.getAllConstraintTypes();
		for (ConstraintType type : types) {
			typeIdToNameMap.put(type.getConstraintTypeId(), type.getConstraintTypeName());
		}
		// show name of type not id
		adapter = new ConstraintAdapter(this, constraintList, accountType);
		adapter.setTypeIdToNameMap(typeIdToNameMap); // ✅ فقط هذه واحدة نحتاجها
		
		recyclerView.setAdapter(adapter);
		
		updateBalanceSummary();
		
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
			return true; // عشان يوقف الحدث بعد الضغط الطويل
		});
		
		btnAdd.setOnClickListener(v -> {
			AddConstraintDialogFragment addDialog = AddConstraintDialogFragment.newInstance(accountId);
			if (lastSelectedDate != null) {
				addDialog.setInitialDate(lastSelectedDate);
				
			}
			addDialog.setOnConstraintAddedListener(() -> {
				lastSelectedDate = addDialog.getCurrentSelectedDate(); // افترض أنك تحفظه في الفراجمنت
				loadConstraints();
			});
			
			//addDialog.setOnConstraintAddedListener(this::loadConstraints);
			addDialog.show(getSupportFragmentManager(), "AddConstraintDialog");
		});
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
			switch (item.getItemId()) {
				case R.id.menu_edit:
				if (selectedConstraint != null) {
					EditConstraintDialogFragment editDialog = EditConstraintDialogFragment
					.newInstance(selectedConstraint, "تعديل القيد");
					editDialog.setOnConstraintEditedListener(ConstraintListActivity.this);
					editDialog.show(getSupportFragmentManager(), "EditDialog");
				}
				mode.finish();
				return true;
				
				case R.id.menu_delete:
				if (selectedConstraint != null) {
					DeleteConstraintDialogFragment deleteDialog = DeleteConstraintDialogFragment
					.newInstance(selectedConstraint);
					deleteDialog.setOnConstraintDeletedListener(() -> {
						loadConstraints();
					});
					deleteDialog.show(getSupportFragmentManager(), "DeleteDialog");
				}
				mode.finish();
				return true;
				
				default:
				return false;
			}
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
		constraintList = dbHelper.getAllConstraintsByAccountId(accountId);
		adapter.updateData(constraintList);
		updateBalanceSummary();
	}
	
	@Override
	public void onConstraintEdited() {
		loadConstraints();
		updateBalanceSummary();
	}
	
	private void updateBalanceSummary() {
		tvDebit = findViewById(R.id.tv_debit);
		tvCredit = findViewById(R.id.tv_credit);
		tvBalance = findViewById(R.id.tv_balance);
		
		double debit = dbHelper.getTotalDebitByAccountId(accountId);
		double credit = dbHelper.getTotalCreditByAccountId(accountId);
		double balance = dbHelper.getAccountBalanceByType(accountId, accountType);
		
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
	
	//	Constraint c = new Constraint();
	@Override
	public void onDateSelected(String selectedDate) {
		// يمكنك التعامل مع التاريخ المختار هنا، مثل عرضه في Toast أو تخزينه
		lastSelectedDate = selectedDate;
		//	Toast.makeText(this, "التاريخ المختار: " + selectedDate, Toast.LENGTH_SHORT).show();
	}
	
	public String getLastSelectedDate() {
		return lastSelectedDate;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		lastSelectedDate = null;
	}
	
}