package com.my.myapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import com.my.myapp.R;
import com.my.myapp.ConstraintType;

import java.util.List;

public class ConstraintTypeAdapter extends BaseAdapter {
	
	private Context context;
	private List<ConstraintType> constraintTypeList;
	private LayoutInflater inflater;
	
	public ConstraintTypeAdapter(Context context, List<ConstraintType> constraintTypeList) {
		this.context = context;
		this.constraintTypeList = constraintTypeList;
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return constraintTypeList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return constraintTypeList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return constraintTypeList.get(position).getConstraintTypeId();
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
		
		ConstraintType constraintType = constraintTypeList.get(position);
		
		holder.tvId.setText(String.valueOf(constraintType.getConstraintTypeId()));
		holder.tvName.setText(constraintType.getConstraintTypeName());
		holder.tvAccountNumber.setText(""); // يُملأ لاحقًا بعدد القيود المرتبطة
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView tvId;
		TextView tvName;
		TextView tvAccountNumber;
	}
}