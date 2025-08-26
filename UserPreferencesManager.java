package com.my.myapp.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.my.myapp.models.UserData;
import java.io.ByteArrayOutputStream;
public class UserPreferencesManager {
	private static final String PREF_NAME = "user_preferences";
	private static final String KEY_USER_DATA = "user_data";
	
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	
	// Singleton pattern
	private static UserPreferencesManager instance;
	
	// إضافة متغير لتخزين البيانات الحالية مؤقتًا
	private UserData cachedUserData = null;
	
	private UserPreferencesManager(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}
	
	public static synchronized UserPreferencesManager getInstance(Context context) {
		if (instance == null) {
			instance = new UserPreferencesManager(context.getApplicationContext());
		}
		return instance;
	}
	
	// Save complete UserData object - مع تعديل لاستخدام commit()
	public void saveUserData(UserData userData) {
		// Convert UserData to JSON string - تم تصحيح الخطأ هنا
		String userDataJson = userData.toJson(); // استدعاء الدالة على كائن userData بدلاً من تمريره كوسيط
		editor.putString(KEY_USER_DATA, userDataJson);
		editor.commit(); // استخدام commit() بدلاً من apply()
		
		// تحديث التخزين المؤقت
		cachedUserData = userData;
	}
	
	// Get complete UserData object - مع تعديل لاستخدام التخزين المؤقت
	public UserData getUserData() {
		if (cachedUserData != null) {
			return cachedUserData;
		}
		
		String userDataJson = sharedPreferences.getString(KEY_USER_DATA, null);
		if (userDataJson != null) {
			cachedUserData = UserData.fromJson(userDataJson);
			return cachedUserData;
		}
		cachedUserData = new UserData(); // Return empty UserData if none exists
		return cachedUserData;
	}
	
	// Update individual fields - مع تعديل لاستخدام التخزين المؤقت
	public void updateField(String fieldName, String value) {
		UserData userData = getUserData(); // سيستخدم التخزين المؤقت الآن
		
		switch (fieldName) {
			case "first_name":
			userData.setFirstName(value);
			break;
			case "address":
			userData.setAddress(value);
			break;
			case "phone":
			userData.setPhone(value);
			break;
			case "company":
			userData.setCompany(value);
			break;
		}
		
		saveUserData(userData);
	}
	
	// إضافة دالة جديدة لتحديث الحقول النصية فقط
	public void updateTextField(String fieldName, String value) {
		// الحصول على البيانات الحالية
		UserData userData = getUserData();
		
		// حفظ الصورة مؤقتًا
		Bitmap tempLogo = userData.getLogoBitmap();
		
		// تحديث الحقل المطلوب
		switch (fieldName) {
			case "first_name":
			userData.setFirstName(value);
			break;
			case "address":
			userData.setAddress(value);
			break;
			case "phone":
			userData.setPhone(value);
			break;
			case "company":
			userData.setCompany(value);
			break;
		}
		
		// استعادة الصورة
		userData.setLogoBitmap(tempLogo);
		
		// حفظ البيانات
		saveUserData(userData);
	}
	
	// Update logo - مع تعديل لاستخدام التخزين المؤقت
	public void updateLogo(Bitmap bitmap) {
		UserData userData = getUserData(); // سيستخدم التخزين المؤقت الآن
		userData.setLogoBitmap(bitmap);
		saveUserData(userData);
	}
	
	// Get individual fields
	public String getFirstName() {
		return getUserData().getFirstName();
	}
	
	public String getAddress() {
		return getUserData().getAddress();
	}
	
	public String getPhone() {
		return getUserData().getPhone();
	}
	
	public String getCompany() {
		return getUserData().getCompany();
	}
	
	public Bitmap getLogo() {
		return getUserData().getLogoBitmap();
	}
	
	// Check if data exists
	public boolean hasData() {
		return sharedPreferences.contains(KEY_USER_DATA);
	}
	
	public boolean hasFirstName() {
		return !getUserData().getFirstName().isEmpty();
	}
	
	public boolean hasAddress() {
		return !getUserData().getAddress().isEmpty();
	}
	
	public boolean hasPhone() {
		return !getUserData().getPhone().isEmpty();
	}
	
	public boolean hasCompany() {
		return !getUserData().getCompany().isEmpty();
	}
	
	public boolean hasLogo() {
		return getUserData().getLogoBitmap() != null;
	}
	
	// Clear data - مع تعديل لمسح التخزين المؤقت
	public void clearData() {
		editor.remove(KEY_USER_DATA);
		editor.commit(); // استخدام commit() بدلاً من apply()
		cachedUserData = null; // مسح التخزين المؤقت
	}
}