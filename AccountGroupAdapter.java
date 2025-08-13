package com.my.myapp.adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my.myapp.R;
import com.my.myapp.AccountGroup;

import java.util.List;

public class AccountGroupAdapter extends BaseAdapter {
	
	private Context context;
	private List<AccountGroup> accountGroupList;
	private LayoutInflater inflater;
	
	public AccountGroupAdapter(Context context, List<AccountGroup> accountGroupList) {
		this.context = context;
		this.accountGroupList = accountGroupList;
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return accountGroupList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return accountGroupList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return accountGroupList.get(position).getAccountGroupId();
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
		
		AccountGroup group = accountGroupList.get(position);
		
		holder.tvId.setText(String.valueOf(group.getAccountGroupId()));
		holder.tvName.setText(group.getAccountGroupName());
		holder.tvAccountNumber.setText(""); // لاحقًا تُملأ بعدد الحسابات المرتبطة بالمجموعة
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView tvId;
		TextView tvName;
		TextView tvAccountNumber;
	}
}