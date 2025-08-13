package com.my.myapp;



import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {
	Toolbar toolbar;
	TextView tvAnotherSetting, tvAccountList;
	ImageView ivAnothersetting, ivAccountList;
	LinearLayout layoutAccountDashboard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle(getString(R.string.settings));
		
		tvAccountList = findViewById(R.id.tv_account_list);
	//	ivAccountList = findViewById(R.id.iv_account_list);
		tvAnotherSetting = findViewById(R.id.tv_another_account);
	//	ivAnothersetting = findViewById(R.id.iv_another_setting);
		layoutAccountDashboard = findViewById(R.id.layout_account_dashboard);
		
		layoutAccountDashboard.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getBaseContext(), AccountsDashboardActivity.class);
				startActivity(intent);
			}
			
		});
		
	}
}