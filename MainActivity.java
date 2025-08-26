package com.my.myapp;

import android.widget.Toast;
import com.my.myapp.AddNewAccountActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, AddNewAccountDialogFragment.OnAccountAddedListener {

	BottomNavigationView bottomNavigationView;
	DrawerLayout drawerLayout;
	NavigationView navigationView;
	ActionBarDrawerToggle toggle;
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("مركزي");
		toolbar.setTitleMarginStart(0);
		toolbar.setTitleMarginEnd(0);

		drawerLayout = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		bottomNavigationView = findViewById(R.id.bottom_navigation);
		bottomNavigationView.setOnItemSelectedListener(item -> {
			switch (item.getItemId()) {
			case R.id.nav_new_acc_page:
				AddNewAccountDialogFragment dialog = AddNewAccountDialogFragment.newInstance(true); // شاشة كاملة
				dialog.show(getSupportFragmentManager(), "add_account_dialog");
				return true;
			case R.id.nav_reports:
				return true;
			case R.id.nav_settings:
				startActivity(new Intent(getBaseContext(), SettingsActivity.class));
				return true;
			default:
				return false;
			}
		});

		loadFragment(new AccountMainListFragment());
	}

	//this is onther way to call the fragment
	public void loadFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, fragment).commit();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
		case R.id.nav_home:
			break;
		case R.id.nav_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.nav_accounts_list:
			startActivity(new Intent(this, AllAccountsActivity.class));
			break;
		}
		drawerLayout.closeDrawers();
		return true;
	}

	@Override
	public void onAccountAdded(String name, String phone, String date) {
		Toast.makeText(this, "تمت إضافة الحساب: " + name, Toast.LENGTH_SHORT).show();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
		if (fragment instanceof AccountMainListFragment) {
			((AccountMainListFragment) fragment).loadAccount();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadFragment(new AccountMainListFragment());
	}
}