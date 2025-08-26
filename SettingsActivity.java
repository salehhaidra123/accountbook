package com.my.myapp;



import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
	Toolbar toolbar;
	TextView tvAnotherSetting, tvAccountList;
	ImageView ivAnothersetting, ivAccountList;
	LinearLayout layoutAccountDashboard , layoutPrintOptions , layoutPersonalInfo;
	ImageButton btnBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle(getString(R.string.settings));
		
		
		layoutAccountDashboard = findViewById(R.id.layout_account_dashboard);
		layoutPrintOptions = findViewById(R.id.layout_print_options);
		layoutPersonalInfo = findViewById(R.id.layout_personal_info);
		
		layoutAccountDashboard.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getBaseContext(), AccountsDashboardActivity.class);
				startActivity(intent);
			}
			
		});
		
		layoutPrintOptions.setOnClickListener( v -> {
			Intent intent = new Intent(getBaseContext() , PrintOptionsActivity.class);
			startActivity(intent);
		});
		
		layoutPersonalInfo.setOnClickListener( v -> {
			Intent intent = new  Intent(getBaseContext() , PersonalInfoActivity.class);
			startActivity(intent);
		});
		
		btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> {
			onBackPressed();
		});
		
	}
}