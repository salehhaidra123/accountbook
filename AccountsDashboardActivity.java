package com.my.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AccountsDashboardActivity extends AppCompatActivity {
	Toolbar toolbar;
	LinearLayout layoutAccounts, layoutAccountType, layoutAccountGroup, layoutConstraintType;
	TextView tvAccounts;
	//	ImageView ivAccounts;
	//	Button btnShowAllAccounts , btnCategories ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accounts_dashboard);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("لوحة تحكم بالجسابات");

		layoutAccounts = findViewById(R.id.layout_show_all_accounts);
		layoutAccountType = findViewById(R.id.layout_account_type);
		layoutAccountGroup = findViewById(R.id.layout_account_group);
		layoutConstraintType = findViewById(R.id.layout_constraint_type);

		tvAccounts = findViewById(R.id.tv_accounts_home);
		layoutAccounts.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getBaseContext(), AllAccountsActivity.class);
				startActivity(intent);
			}

		});

		layoutAccountType.setOnClickListener(v -> {
			Intent intent = new Intent(getBaseContext(), AccountTypeActivity.class);
			startActivity(intent);
		});
		layoutAccountGroup.setOnClickListener(v -> {
			Intent intent = new Intent(getBaseContext(), AccountGroupActivity.class);
			startActivity(intent);
		});
		layoutConstraintType.setOnClickListener(v -> {
			Intent intent = new Intent(getBaseContext(), ConstraintTypeActivity.class);
			startActivity(intent);
		});
	}
}