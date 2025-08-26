package com.my.myapp;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.my.myapp.models.UserData;
import com.my.myapp.utils.UserPreferencesManager;
import java.io.IOException;
public class PersonalInfoActivity extends AppCompatActivity implements EditFieldDialogFragment.OnFieldUpdatedListener {
	
	private static final int REQUEST_IMAGE_PICK = 1001;
	private static final int REQUEST_PERMISSION = 1002;
	
	private UserPreferencesManager preferencesManager;
	private UserData currentUserData;
	
	// TextViews لعرض القيم الحالية
	private TextView tvName, tvAddress, tvPhone, tvCompany, tvLogo;
	
	// Layouts للنقر
	private LinearLayout layoutName, layoutAddress, layoutPhone, layoutCompany, layoutLogo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_info);
		
		// تهيئة SharedPreferences Manager
		preferencesManager = UserPreferencesManager.getInstance(this);
		
		// الحصول على بيانات المستخدم الحالية
		currentUserData = preferencesManager.getUserData();
		
		// تهيئة الـ TextViews
		tvName = findViewById(R.id.tv_name);
		tvAddress = findViewById(R.id.tv_adress);
		tvPhone = findViewById(R.id.tv_phone);
		tvCompany = findViewById(R.id.tv_compny_name);
		tvLogo = findViewById(R.id.tv_logo);
		
		// تهيئة الـ Layouts للنقر
		layoutName = findViewById(R.id.layout_name);
		layoutAddress = findViewById(R.id.layout_adress);
		layoutPhone = findViewById(R.id.layout_phone);
		layoutCompany = findViewById(R.id.layout_compny_name);
		layoutLogo = findViewById(R.id.layout_logo);
		
		// تحديث الواجهة بالبيانات المحفوظة
		updateUIWithUserData();
		
		// إعداد مستمعي النقر
		setupClickListeners();
	}
	
	private void updateUIWithUserData() {
		// تحديث النصوص بناءً على البيانات المتوفرة
		tvName.setText(currentUserData.getFirstName().isEmpty() ? "الاسم" : currentUserData.getFirstName());
		tvAddress.setText(currentUserData.getAddress().isEmpty() ? "العنوان" : currentUserData.getAddress());
		tvPhone.setText(currentUserData.getPhone().isEmpty() ? "رقم الهاتف" : currentUserData.getPhone());
		tvCompany.setText(currentUserData.getCompany().isEmpty() ? "اسم الشركة" : currentUserData.getCompany());
		
		// تحديث نص الشعار
		if (currentUserData.getLogoBitmap() != null) {
			tvLogo.setText("تم اختيار شعار");
			} else {
			tvLogo.setText("تغيير الشعار");
		}
	}
	
	private void setupClickListeners() {
		// النقر على layout الاسم
		layoutName.setOnClickListener(v -> {
			String currentValue = currentUserData.getFirstName();
			EditFieldDialogFragment dialog = EditFieldDialogFragment.newInstance(
			"تعديل الاسم", "first_name", currentValue, "أدخل الاسم");
			dialog.show(getSupportFragmentManager(), "EditFirstNameDialog");
		});
		
		// النقر على layout العنوان
		layoutAddress.setOnClickListener(v -> {
			String currentValue = currentUserData.getAddress();
			EditFieldDialogFragment dialog = EditFieldDialogFragment.newInstance(
			"تعديل العنوان", "address", currentValue, "أدخل العنوان");
			dialog.show(getSupportFragmentManager(), "EditAddressDialog");
		});
		
		// النقر على layout رقم الهاتف
		layoutPhone.setOnClickListener(v -> {
			String currentValue = currentUserData.getPhone();
			EditFieldDialogFragment dialog = EditFieldDialogFragment.newInstance(
			"تعديل رقم الهاتف", "phone", currentValue, "أدخل رقم الهاتف");
			dialog.show(getSupportFragmentManager(), "EditPhoneDialog");
		});
		
		// النقر على layout اسم الشركة
		layoutCompany.setOnClickListener(v -> {
			String currentValue = currentUserData.getCompany();
			EditFieldDialogFragment dialog = EditFieldDialogFragment.newInstance(
			"تعديل اسم الشركة", "company", currentValue, "أدخل اسم الشركة");
			dialog.show(getSupportFragmentManager(), "EditCompanyDialog");
		});
		
		// النقر على layout الشعار
		layoutLogo.setOnClickListener(v -> {
			if (checkPermissions()) {
				openImagePicker();
				} else {
				requestPermissions();
			}
		});
	}
	
	@Override
	public void onFieldUpdated(String fieldName, String newValue) {
		// استخدام الدالة الجديدة لتحديث الحقول النصية فقط
		preferencesManager.updateTextField(fieldName, newValue);
		
		// الحصول على البيانات المحدثة
		currentUserData = preferencesManager.getUserData();
		
		// تحديث الواجهة
		updateUIWithUserData();
		
		// إظهار رسالة النجاح
		String successMessage = "";
		switch (fieldName) {
			case "first_name":
			successMessage = "تم تحديث الاسم بنجاح";
			break;
			case "address":
			successMessage = "تم تحديث العنوان بنجاح";
			break;
			case "phone":
			successMessage = "تم تحديث رقم الهاتف بنجاح";
			break;
			case "company":
			successMessage = "تم تحديث اسم الشركة بنجاح";
			break;
		}
		
		Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
		
		// عرض ملخص البيانات الحالية
		Log.d("UserData", "البيانات الحالية:\n" + currentUserData.getDataSummary());
	}
	
	// Methods for image picking
	private boolean checkPermissions() {
		return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}
	
	private void requestPermissions() {
		ActivityCompat.requestPermissions(this,
		new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
		REQUEST_PERMISSION);
	}
	
	private void openImagePicker() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_IMAGE_PICK);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				openImagePicker();
				} else {
				Toast.makeText(this, "الصلاحية مطلوبة لاختيار الصورة", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
			Uri selectedImageUri = data.getData();
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
				
				// Update the logo in UserData
				currentUserData.setLogoBitmap(bitmap);
				preferencesManager.updateLogo(bitmap);
				
				// Update UI
				tvLogo.setText("تم اختيار شعار");
				Toast.makeText(this, "تم حفظ الشعار بنجاح", Toast.LENGTH_SHORT).show();
				
				// Show preview dialog
				showImagePreviewDialog(bitmap);
				
				} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(this, "فشل في تحميل الصورة", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void showImagePreviewDialog(Bitmap bitmap) {
		androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
		builder.setTitle("معاينة الشعار");
		
		ImageView imageView = new ImageView(this);
		imageView.setImageBitmap(bitmap);
		
		builder.setView(imageView);
		builder.setPositiveButton("موافق", (dialog, which) -> dialog.dismiss());
		
		builder.create().show();
	}
}