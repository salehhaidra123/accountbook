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
import com.my.myapp.adapter.AccountGroupAdapter;

public class AccountGroupActivity extends AppCompatActivity implements AddAccountGroupFragment.OnAccountGroupAddedListener {
	
	Toolbar toolbar;
	ListView listView;
	Button btnExport;
	FloatingActionButton fabAddNewGroup;
	DatabaseHelper dbHelper;
	AccountGroupAdapter adapter;
	List<AccountGroup> accountGroupList;
	private ActionMode actionMode;
	private int selectedItemPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_group);
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("تصنيف الحسابات");
		listView = findViewById(R.id.list_view_account_group);
		btnExport = findViewById(R.id.btn_export_pdf);
		fabAddNewGroup = findViewById(R.id.fab_add_new_group);
		dbHelper = new DatabaseHelper(this);
		
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if (actionMode == null) {
				selectedItemPosition = position;
				actionMode = startSupportActionMode(actionModeCallback);
			}
			return true;
		});
		
		loadAccountGroups();
		
		fabAddNewGroup.setOnClickListener(v -> {
			AddAccountGroupFragment fragment = new AddAccountGroupFragment();
			fragment.setOnAccountGroupAddedListener(this);
			fragment.show(getSupportFragmentManager(), "AddAccountGroup");
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
				// TODO: تنفيذ تعديل العنصر إذا أردت
				mode.finish();
				return true;
				
				case R.id.menu_delete:
				AccountGroup groupToDelete = accountGroupList.get(selectedItemPosition);
				confirmDeleteDialog(groupToDelete.getAccountGroupId());
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
	
	private void confirmDeleteDialog(int accountGroupId) {
		ConfirmDeleteDialogFragment confirmDialog = ConfirmDeleteDialogFragment.newInstance(
		"حذف تصنيف الحساب",
		"هل تريد بالتأكيد حذف تصنيف الحساب هذا؟",
		accountGroupId
		);
		
		confirmDialog.setOnConfirmDeleteListener(new ConfirmDeleteDialogFragment.OnConfirmDeleteListener() {
			@Override
			public void onDeleteConfirmed(int id) {
				boolean deleted = dbHelper.deleteAccountGroup(id);
				if (deleted) {
					Toast.makeText(AccountGroupActivity.this, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
					loadAccountGroups();
					} else {
					Toast.makeText(AccountGroupActivity.this, "فشل الحذف", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onDeleteCancelled(int id) {
				Toast.makeText(AccountGroupActivity.this, "تم إلغاء الحذف", Toast.LENGTH_SHORT).show();
			}
		});
		
		confirmDialog.show(getSupportFragmentManager(), "confirm_delete");
	}
	
	private void loadAccountGroups() {
		accountGroupList = dbHelper.getAllAccountGroup();
		adapter = new AccountGroupAdapter(this, accountGroupList);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void onAccountGroupAdded() {
		loadAccountGroups();
	}
}