package com.my.myapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class AccountMainListFragment extends Fragment {

	ListView listView;
	AccountMainListAdapter adapter;
	ArrayList<Account> accountList;
	DatabaseHelper dbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//	return super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_account_main_list, container, false);

		listView = view.findViewById(R.id.list_view_accounts);
		dbHelper = new DatabaseHelper(getContext());
		accountList = dbHelper.getAllAccounts();
		adapter = new AccountMainListAdapter(getContext(), accountList);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Account selectedAccount = accountList.get(position);

				Intent intent = new Intent(getContext(), ConstraintListActivity.class);
				intent.putExtra("account_id", selectedAccount.getAccountId());
				intent.putExtra("account_name", selectedAccount.getAccountName());
				intent.putExtra("account_type", selectedAccount.getAccountType());
				startActivity(intent);
			}
		});
		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.menu_account_options, menu); // نفس القائمة التي شرحناها سابقًا
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Account selectedAccount = accountList.get(info.position);

		switch (item.getItemId()) {
		case R.id.action_edit:
			// تنفيذ التعديل
			//	editAccount(selectedAccount);
			return true;
		case R.id.action_delete:
			// تنفيذ الحذف
			//	deleteAccount(selectedAccount.getAccountId());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void loadAccount() {
		accountList.clear();
		accountList.addAll(dbHelper.getAllAccounts());
		adapter.notifyDataSetChanged();
	}

	public void refreshAccounts() {
		if (getContext() != null) {
			DatabaseHelper db = new DatabaseHelper(getContext());
			accountList = db.getAllAccounts();
			adapter = new AccountMainListAdapter(getContext(), accountList);
			listView.setAdapter(adapter);
		}
	}

}