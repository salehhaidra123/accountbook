package com.my.myapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Locale;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class AccountMainListAdapter extends BaseAdapter {
	Context context;
	ArrayList<Account> accounts;

	public AccountMainListAdapter(Context context, ArrayList<Account> accounts) {
		this.context = context;
		this.accounts = accounts;
	}

	@Override
	public int getCount() {
		return accounts.size();
	}

	@Override
	public Object getItem(int position) {
		return accounts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return accounts.get(position).getAccountId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_account_main_list, parent, false);
		}
		
		TextView name = convertView.findViewById(R.id.tv_list_name);
		TextView numOfConst = convertView.findViewById(R.id.tv_num_of_const);
		TextView balance = convertView.findViewById(R.id.tv_balance);
		TextView indicator = convertView.findViewById(R.id.tv_indicator);
		
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		Account newAccount = accounts.get(position);
		
		name.setText(newAccount.getAccountName());
		int constNumber = dbHelper.getConstraintsCountByAccountId(newAccount.getAccountId());
		numOfConst.setText(String.valueOf(constNumber));
		
		double newbalance = dbHelper.getAccountBalanceByType(newAccount.getAccountId(), newAccount.getAccountType());		
		// Format balance with comma separators and optional decimal
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);
		
		String formattedBalance = formatter.format(newbalance);
		balance.setText(formattedBalance);
		
		double indicatorBalance = dbHelper.getAccountBalanceByType(newAccount.getAccountId() , newAccount.getAccountType());
		if (indicatorBalance > 0 ) {
			indicator.setText("▲");
			indicator.setTextColor(Color.parseColor("#FF4CAF50"));
		}else if( indicatorBalance < 0 ) {
			indicator.setText("▼");
			indicator.setTextColor(Color.parseColor("#F44336"));
		}
		return convertView;
	}
}