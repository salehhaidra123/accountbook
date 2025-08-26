package com.my.myapp.models;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.ByteArrayOutputStream;
public class UserData {
	private String firstName = "";
	private String address = "";
	private String phone = "";
	private String company = "";
	private Bitmap logoBitmap = null;
	
	// Empty constructor
	public UserData() {}
	
	// Full constructor
	public UserData(String firstName, String address, String phone, String company, Bitmap logoBitmap) {
		this.firstName = firstName;
		this.address = address;
		this.phone = phone;
		this.company = company;
		this.logoBitmap = logoBitmap;
	}
	
	// Constructor without logo
	public UserData(String firstName, String address, String phone, String company) {
		this.firstName = firstName;
		this.address = address;
		this.phone = phone;
		this.company = company;
	}
	
	// Getters and Setters
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getCompany() {
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public Bitmap getLogoBitmap() {
		return logoBitmap;
	}
	
	public void setLogoBitmap(Bitmap logoBitmap) {
		this.logoBitmap = logoBitmap;
	}
	
	// Convert UserData to JSON string - مع تحسين لمعالجة الصور
	public String toJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("firstName", firstName);
			jsonObject.put("address", address);
			jsonObject.put("phone", phone);
			jsonObject.put("company", company);
			
			// Convert bitmap to Base64 string if exists - مع تحسين الحجم
			if (logoBitmap != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// تقليل جودة الصورة وتصغيرها قبل حفظها
				Bitmap resizedBitmap = resizeBitmap(logoBitmap, 300, 300); // تصغير الصورة
				resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // استخدام JPEG بدلاً من PNG وتقليل الجودة
				byte[] imageBytes = baos.toByteArray();
				String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP); // استخدام NO_WRAP للأداء
				jsonObject.put("logo", encodedImage);
				} else {
				jsonObject.put("logo", JSONObject.NULL);
			}
			
			return jsonObject.toString();
			} catch (JSONException e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
	// إضافة دالة لتصغير الصورة
	private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
		int width = originalBitmap.getWidth();
		int height = originalBitmap.getHeight();
		
		float ratio = Math.min(
		(float) maxWidth / width,
		(float) maxHeight / height
		);
		
		int newWidth = Math.round(width * ratio);
		int newHeight = Math.round(height * ratio);
		
		return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
	}
	
	// Create UserData from JSON string
	public static UserData fromJson(String jsonString) {
		UserData userData = new UserData();
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			userData.setFirstName(jsonObject.optString("firstName", ""));
			userData.setAddress(jsonObject.optString("address", ""));
			userData.setPhone(jsonObject.optString("phone", ""));
			userData.setCompany(jsonObject.optString("company", ""));
			
			// Convert Base64 string to bitmap if exists
			if (!jsonObject.isNull("logo")) {
				String encodedImage = jsonObject.getString("logo");
				if (!encodedImage.isEmpty()) {
					try {
						byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
						userData.setLogoBitmap(bitmap);
						} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			} catch (JSONException e) {
			e.printStackTrace();
		}
		return userData;
	}
	
	// Check if any field has data
	public boolean hasAnyData() {
		return !firstName.isEmpty() || !address.isEmpty() || !phone.isEmpty() || !company.isEmpty() || logoBitmap != null;
	}
	
	// Get a summary of available data
	public String getDataSummary() {
		StringBuilder summary = new StringBuilder();
		if (!firstName.isEmpty()) summary.append("الاسم: ").append(firstName).append("\n");
		if (!address.isEmpty()) summary.append("العنوان: ").append(address).append("\n");
		if (!phone.isEmpty()) summary.append("الهاتف: ").append(phone).append("\n");
		if (!company.isEmpty()) summary.append("الشركة: ").append(company).append("\n");
		if (logoBitmap != null) summary.append("الشعار: محفوظ");
		return summary.toString();
	}
}