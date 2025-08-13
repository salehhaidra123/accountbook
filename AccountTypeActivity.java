package com.my.myapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class AccountTypeActivity extends AppCompatActivity
implements AddAccountTypeFragment.OnAccountTypeAddedListener {
	
	Toolbar toolbar;
	ListView listView;
	Button btnExport;
	FloatingActionButton fabAddNewType;
	DatabaseHelper dbHelper;
	AccountTypeAdapter adapter;
	List<AccountType> accountTypeList;
	private ActionMode actionMode;
	private int selectedItemPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_type);
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("انواع الحسابات");
		listView = findViewById(R.id.list_view_account_type);
		btnExport = findViewById(R.id.btn_export_pdf);
		fabAddNewType = findViewById(R.id.fab_add_new_type);
		
		dbHelper = new DatabaseHelper(this);
		
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if (actionMode == null) {
				selectedItemPosition = position;
				actionMode = startSupportActionMode(actionModeCallback);
			}
			return true;
		});
		
		loadAccountTypes();
		
		fabAddNewType.setOnClickListener(v -> {
			AddAccountTypeFragment fragment = new AddAccountTypeFragment();
			fragment.setOnAccountTypeAddedListener(this);
			fragment.show(getSupportFragmentManager(), "AddAccountType");
		});
	}
	
	private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
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
				// TODO: تنفيذ تعديل النوع هنا
				mode.finish();
				return true;
				
				case R.id.menu_delete:
				AccountType accToDelete = accountTypeList.get(selectedItemPosition);
				confirmDeleteDialog(accToDelete.getAccountTypeId());
				mode.finish();
				return true;
				
				default:
				return false;
			}
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			selectedItemPosition = -1;
		}
	};
	
	private void confirmDeleteDialog(int accountTypeId) {
		ConfirmDeleteDialogFragment confirmDialog = ConfirmDeleteDialogFragment.newInstance(
		"حذف نوع القيد",
		"هل تريد بالتأكيد حذف نوع القيد هذا؟",
		accountTypeId
		);
		
		confirmDialog.setOnConfirmDeleteListener(new ConfirmDeleteDialogFragment.OnConfirmDeleteListener() {
			@Override
			public void onDeleteConfirmed(int id) {
				boolean deleted = dbHelper.deleteAccountType(id);
				if (deleted) {
					Toast.makeText(AccountTypeActivity.this, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
					loadAccountTypes(); // تحديث القائمة
					} else {
					Toast.makeText(AccountTypeActivity.this, "فشل الحذف", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onDeleteCancelled(int id) {
				Toast.makeText(AccountTypeActivity.this, "تم إلغاء الحذف", Toast.LENGTH_SHORT).show();
			}
		});
		
		confirmDialog.show(getSupportFragmentManager(), "confirm_delete");
	}
	
	private void loadAccountTypes() {
		accountTypeList = dbHelper.getAllAccountTypes();
		adapter = new AccountTypeAdapter(this, accountTypeList);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void onAccountTypeAdded() {
		loadAccountTypes();
	}
}