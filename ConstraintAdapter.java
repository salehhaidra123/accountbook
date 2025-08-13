package com.my.myapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Set;
import java.util.HashSet;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstraintAdapter extends RecyclerView.Adapter<ConstraintAdapter.ConstraintViewHolder> {

	private Context context;
	private List<Constraint> constraintList;
	private String accountType;
	private OnItemClickListener clickListener;
	private OnItemLongClickListener longClickListener;
	private Map<Integer, String> typeIdToNameMap = new HashMap<>();

	// لتخزين العناصر المحددة
	private Set<Integer> selectedPositions = new HashSet<>();

	public interface OnItemClickListener {
		void onItemClick(int position);
	}
	
	public interface OnItemLongClickListener {
		boolean onItemLongClick(int position); // تغيير من void إلى boolean
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.clickListener = listener;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.longClickListener = listener;
	}
	public ConstraintAdapter(Context context, List<Constraint> constraintList, String accountType) {
		this.context = context;
		this.constraintList = constraintList;
		this.accountType = accountType;
	}

	public void setTypeIdToNameMap(Map<Integer, String> map) {
		this.typeIdToNameMap = map;
	}

	@NonNull
	@Override
	public ConstraintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_constraint, parent, false);
		return new ConstraintViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ConstraintViewHolder holder, int position) {
		Constraint constraint = constraintList.get(position);
		String customerAccType = accountType;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(',');
		DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);

		holder.tvDate.setText(constraint.getDate());
		holder.tvDetails.setText(constraint.getDetails());
		holder.tvDebit.setText(formatter.format(constraint.getDebit()));
		holder.tvCredit.setText(formatter.format(constraint.getCredit()));

		int typeId = constraint.getConstraintTypeId();
		String typeName = typeIdToNameMap.get(typeId);
		holder.tvConstType.setText(typeName != null ? typeName : "نوع غير معروف");

		double runningBalance = 0;
		for (int i = 0; i <= position; i++) {
			Constraint c = constraintList.get(i);
			if ("صندوق".equals(customerAccType) || "مدين".equals(customerAccType)) {
				runningBalance += c.getDebit() - c.getCredit();
			} else if ("دائن".equals(customerAccType)) {
				runningBalance += c.getCredit() - c.getDebit();
			}
		}
		holder.tvBalance.setText(formatter.format(runningBalance));

		// تغيير اللون إذا كان العنصر محدد
		if (selectedPositions.contains(position)) {
			holder.itemView.setBackgroundColor(Color.LTGRAY);
		} else {
			holder.itemView.setBackgroundColor(Color.WHITE);
		}

		// المؤشر
		if (position == 0) {
			holder.tvIndicator.setText("➖");
			holder.tvIndicator.setTextColor(Color.GRAY);
		} else {
			double previousBalance = 0;
			for (int i = 0; i < position; i++) {
				Constraint c = constraintList.get(i);
				if ("صندوق".equals(customerAccType) || "مدين".equals(customerAccType)) {
					previousBalance += c.getDebit() - c.getCredit();
				} else if ("دائن".equals(customerAccType)) {
					previousBalance += c.getCredit() - c.getDebit();
				}
			}

			if (runningBalance > previousBalance) {
				holder.tvIndicator.setText("▲");
				holder.tvIndicator.setTextColor(Color.parseColor("#FF4CAF50"));
			} else if (runningBalance < previousBalance) {
				holder.tvIndicator.setText("▼");
				holder.tvIndicator.setTextColor(Color.parseColor("#F44336"));
			} else {
				holder.tvIndicator.setText("➖");
				holder.tvIndicator.setTextColor(Color.GRAY);
			}
		}
	}

	@Override
	public int getItemCount() {
		return constraintList.size();
	}

	public class ConstraintViewHolder extends RecyclerView.ViewHolder {
		TextView tvDate, tvDetails, tvDebit, tvCredit, tvBalance, tvIndicator, tvConstType;

		public ConstraintViewHolder(@NonNull View itemView) {
			super(itemView);
			tvDate = itemView.findViewById(R.id.tv_const_date);
			tvDetails = itemView.findViewById(R.id.tv_const_detail);
			tvDebit = itemView.findViewById(R.id.tv_const_debit);
			tvCredit = itemView.findViewById(R.id.tv_const_credit);
			tvBalance = itemView.findViewById(R.id.tv_const_balance);
			tvIndicator = itemView.findViewById(R.id.tv_balance_indicator);
			tvConstType = itemView.findViewById(R.id.tv_const_type);

			itemView.setOnClickListener(v -> {
				if (clickListener != null) {
					clickListener.onItemClick(getAdapterPosition());
				}
			});

			itemView.setOnLongClickListener(v -> {
				if (longClickListener != null) {
					longClickListener.onItemLongClick(getAdapterPosition());
				}
				return true;
			});
		}
	}

	public void updateData(List<Constraint> newList) {
		constraintList = newList;
		notifyDataSetChanged();
	}

	// ✅ دوال Multi Selection
	public void toggleSelection(int position) {
		if (selectedPositions.contains(position)) {
			selectedPositions.remove(position);
		} else {
			selectedPositions.add(position);
		}
		notifyItemChanged(position);
	}

	public void clearSelection() {
		selectedPositions.clear();
		notifyDataSetChanged();
	}

	public Set<Integer> getSelectedItems() {
		return selectedPositions;
	}

	public boolean isSelected(int position) {
		return selectedPositions.contains(position);
	}

	public List<Constraint> getSelectedConstraints() {
		List<Constraint> selectedList = new ArrayList<>();
		for (Integer pos : selectedPositions) {
			if (pos >= 0 && pos < constraintList.size()) {
				selectedList.add(constraintList.get(pos));
			}
		}
		return selectedList;
	}
}