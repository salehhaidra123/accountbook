package com.my.myapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class AllAccountsAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Account> accountList;
	private ListView listView; // مرجع للـ ListView
	
	public AllAccountsAdapter(Context context, ArrayList<Account> accountList, ListView listView) {
		this.context = context;
		this.accountList = accountList;
		this.listView = listView;
	}
	
	@Override
	public int getCount() {
		return accountList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return accountList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return accountList.get(position).getAccountId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context)
			.inflate(R.layout.list_item_all_accounts, parent, false);
		}
		
		TextView id = convertView.findViewById(R.id.tv_id);
		TextView name = convertView.findViewById(R.id.tv_name);
		TextView balance = convertView.findViewById(R.id.tv_balance);
		
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		Account newAccount = accountList.get(position);
		
		id.setText(String.valueOf(newAccount.getAccountId()));
		name.setText(newAccount.getAccountName());
		
		double newBalance = dbHelper.getAccountBalanceByType(
		newAccount.getAccountId(),
		newAccount.getAccountType()
		);
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);
		balance.setText(formatter.format(newBalance));
		
		// ✅ التلوين حسب التحديد
		if (listView.isItemChecked(position)) {
			convertView.setBackgroundColor(Color.LTGRAY); // محدد
			} else {
			convertView.setBackgroundColor(Color.TRANSPARENT); // غير محدد
		}
		
		return convertView;
	}
}