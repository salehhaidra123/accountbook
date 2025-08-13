package com.my.myapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my.myapp.R;
import com.my.myapp.AccountType;

import java.util.List;

public class AccountTypeAdapter extends BaseAdapter {
	
	private Context context;
	private List<AccountType> accountTypeList;
	private LayoutInflater inflater;
	
	public AccountTypeAdapter(Context context, List<AccountType> accountTypeList) {
		this.context = context;
		this.accountTypeList = accountTypeList;
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return accountTypeList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return accountTypeList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return accountTypeList.get(position).getAccountTypeId(); // or just return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_simple, parent, false);
			holder = new ViewHolder();
			holder.tvId = convertView.findViewById(R.id.tv_id);
			holder.tvName = convertView.findViewById(R.id.tv_name);
			holder.tvAccountNumber = convertView.findViewById(R.id.tv_account_number);
			convertView.setTag(holder);
			} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		AccountType accountType = accountTypeList.get(position);
		
		holder.tvId.setText(String.valueOf(accountType.getAccountTypeId()));
		holder.tvName.setText(accountType.getAccountType());
		holder.tvAccountNumber.setText(""); // لا يوجد حقل رقم حساب هنا، نتركه فاضي أو نخفيه
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView tvId;
		TextView tvName;
		TextView tvAccountNumber;
	}
}