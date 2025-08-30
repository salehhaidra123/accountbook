package com.my.myapp;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.my.myapp.Constraint;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database information
	public static final String DATABASE_NAME = "accountbook.db";
	public static final int DATABASE_VERSION = 1;
	
	// Create table statements
	// Create accounts table
	String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS + " (" + DBConstants.COL_ACCOUNT_ID
	+ " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_CREATED_DATE + " TEXT NOT NULL, "
	+ DBConstants.COL_ACCOUNT_NAME + " TEXT NOT NULL UNIQUE, " + DBConstants.COL_ACCOUNT_TYPE
	+ " INTEGER NOT NULL, " + DBConstants.COL_ACCOUNT_PHONE + " TEXT, " + DBConstants.COL_ACCOUNT_GROUP
	+ " INTEGER, " + "FOREIGN KEY(" + DBConstants.COL_ACCOUNT_TYPE + ") REFERENCES "
	+ DBConstants.TABLE_ACCOUNTS_TYPE + "(" + DBConstants.COL_ACCOUNT_TYPE_ID + "), " + "FOREIGN KEY("
	+ DBConstants.COL_ACCOUNT_GROUP + ") REFERENCES " + DBConstants.TABLE_ACCOUNTS_GROUP + "("
	+ DBConstants.COL_ACCOUNT_GROUP_ID + "));";
	
	// Create account types table
	String CREATE_TABLE_ACCOUNTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_TYPE + " ("
	+ DBConstants.COL_ACCOUNT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_ACCOUNT_TYPE_NAME
	+ " TEXT NOT NULL UNIQUE);";
	
	// Create account groups table
	String CREATE_TABLE_ACCOUNTS_GROUP = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_GROUP + " ("
	+ DBConstants.COL_ACCOUNT_GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_ACCOUNT_GROUP_NAME
	+ " TEXT NOT NULL UNIQUE);";
	
	// Create constraints table
	String CREATE_TABLE_CONSTRAINTS = "CREATE TABLE " + DBConstants.TABLE_CONSTRIANTS + " (" + DBConstants.COL_CONST_ID
	+ " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_CONST_ACCOUNT_ID + " INTEGER NOT NULL, "
	+ DBConstants.COL_CONST_DATE + " TEXT NOT NULL, " + DBConstants.COL_CONST_DETAILS + " TEXT NOT NULL, "
	+ DBConstants.COL_CONST_DEBIT + " REAL NOT NULL, " + DBConstants.COL_CONST_CREDIT + " REAL NOT NULL, "
	+ DBConstants.COL_CONST_TYPE + " INTEGER NOT NULL, " + "FOREIGN KEY(" + DBConstants.COL_CONST_ACCOUNT_ID
	+ ") REFERENCES " + DBConstants.TABLE_ACCOUNTS + "(" + DBConstants.COL_ACCOUNT_ID + "), " + "FOREIGN KEY("
	+ DBConstants.COL_CONST_TYPE + ") REFERENCES " + DBConstants.TABLE_CONSTRAINT_TYPE + "("
	+ DBConstants.COL_CONSTRAINT_TYPE_ID + "));";
	
	// Create constraint types table
	String CREATE_TABLE_CONSTRAINTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_CONSTRAINT_TYPE + " ("
	+ DBConstants.COL_CONSTRAINT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_CONSTRAINT_TYPE_NAME + " TEXT NOT NULL UNIQUE);";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_ACCOUNTS_TYPE);
		db.execSQL(CREATE_TABLE_ACCOUNTS_GROUP);
		db.execSQL(CREATE_TABLE_ACCOUNTS);
		db.execSQL(CREATE_TABLE_CONSTRAINTS_TYPE);
		db.execSQL(CREATE_TABLE_CONSTRAINTS);
		
		// إضافة القيم الافتراضية
		insertDefaultValues(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(" DROP TABLE IF EXISTS " + DBConstants.TABLE_ACCOUNTS);
		db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_CONSTRIANTS);
		db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_CONSTRAINT_TYPE);
		db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_ACCOUNTS_TYPE);
		db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_ACCOUNTS_GROUP);
		
		// إعادة إنشاء الجداول مع القيم الافتراضية
		onCreate(db);
	}
	
	// دالة لإضافة القيم الافتراضية
	private void insertDefaultValues(SQLiteDatabase db) {
		// إضافة القيمة الافتراضية لجدول أنواع الحسابات
		ContentValues accountTypeValues = new ContentValues();
		accountTypeValues.put(DBConstants.COL_ACCOUNT_TYPE_ID, 1);
		accountTypeValues.put(DBConstants.COL_ACCOUNT_TYPE_NAME, "مدين");
		db.insertWithOnConflict(DBConstants.TABLE_ACCOUNTS_TYPE, null, accountTypeValues, SQLiteDatabase.CONFLICT_IGNORE);
		
		// إضافة القيمة الافتراضية لجدول مجموعات الحسابات
		ContentValues accountGroupValues = new ContentValues();
		accountGroupValues.put(DBConstants.COL_ACCOUNT_GROUP_ID, 1);
		accountGroupValues.put(DBConstants.COL_ACCOUNT_GROUP_NAME, "عام");
		db.insertWithOnConflict(DBConstants.TABLE_ACCOUNTS_GROUP, null, accountGroupValues, SQLiteDatabase.CONFLICT_IGNORE);
		
		// إضافة القيمة الافتراضية لجدول أنواع القيود
		ContentValues constraintTypeValues = new ContentValues();
		constraintTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_ID, 1);
		constraintTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_NAME, "عام");
		db.insertWithOnConflict(DBConstants.TABLE_CONSTRAINT_TYPE, null, constraintTypeValues, SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	// دالة للتحقق من وجود القيم الافتراضية وإضافتها إذا لم تكن موجودة
	public void ensureDefaultValuesExist() {
		SQLiteDatabase db = this.getWritableDatabase();
		insertDefaultValues(db);
		db.close();
	}
	
	//======================== ACCOUNT TABLE METHODS ================================================
	/**
	* Insert a new account with parameters
	*/
	public boolean insertAccount(String creationDate, String accountType, String name, String accountPhone,
	String accountGroup) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CREATED_DATE, creationDate);
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountType);
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroup);
		long result = db.insert(DBConstants.TABLE_ACCOUNTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Insert a new account using Account object
	*/
	public boolean insertAccount(Account account) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CREATED_DATE, account.getCreatedDate());
		values.put(DBConstants.COL_ACCOUNT_TYPE, account.getAccountType());
		values.put(DBConstants.COL_ACCOUNT_NAME, account.getAccountName());
		values.put(DBConstants.COL_ACCOUNT_PHONE, account.getAccountPhone());
		values.put(DBConstants.COL_ACCOUNT_GROUP, account.getAccountGroup());
		long result = db.insert(DBConstants.TABLE_ACCOUNTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Insert a new account with foreign key references
	*/
	public boolean insertAccount(Account account, int accountTypeId, int accountGroupId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CREATED_DATE, account.getCreatedDate());
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId); // Type ID
		values.put(DBConstants.COL_ACCOUNT_NAME, account.getAccountName());
		values.put(DBConstants.COL_ACCOUNT_PHONE, account.getAccountPhone());
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId); // Group ID
		long result = db.insert(DBConstants.TABLE_ACCOUNTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Get all accounts from database
	*/
	public ArrayList<Account> getAllAccounts() {
		ArrayList<Account> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_ACCOUNTS, null);
		if (cursor.moveToFirst()) {
			do {
				int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_ID));
				String accountCreationDate = cursor
				.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CREATED_DATE));
				String accountName = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_NAME));
				String accountType = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_TYPE));
				String accountPhone = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_PHONE));
				String accountGroup = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_GROUP));
				Account account = new Account(accountId, accountName, accountCreationDate, accountType, accountPhone,
				accountGroup);
				list.add(account);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	* Delete account by ID
	*/
	public boolean deleteAccountById(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int result = db.delete(DBConstants.TABLE_ACCOUNTS, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		db.close();
		return result > 0;
	}
	
	/**
	* Update account information
	*/
	public boolean updateAccount(int id, String accountType, String name, String accountPhone) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountType);
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		int result = db.update(DBConstants.TABLE_ACCOUNTS, values, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		return result > 0;
	}
	
	/**
	* Update account with foreign key references
	*/
	public boolean updateAccountV2(int id, int accountTypeId, int accountGroupId, String name, String accountPhone) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId); // Using account type ID
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId); // Using account group ID
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		int result = db.update(DBConstants.TABLE_ACCOUNTS, values, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		return result > 0;
	}
	
	//======================== CONSTRAINTS TABLE METHODS ================================================
	/**
	* Insert a new constraint
	*/
	public boolean insertConstraint(int accountId, String date, String details, double debit, double credit,
	int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_ACCOUNT_ID, accountId);
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId); // Constraint type field
		long result = db.insert(DBConstants.TABLE_CONSTRIANTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Insert a new constraint using Constraint object
	*/
	public boolean insertConstraintV2(Constraint constraint) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_ACCOUNT_ID, constraint.getAccountId());
		values.put(DBConstants.COL_CONST_DATE, constraint.getDate());
		values.put(DBConstants.COL_CONST_DETAILS, constraint.getDetails());
		values.put(DBConstants.COL_CONST_DEBIT, constraint.getDebit());
		values.put(DBConstants.COL_CONST_CREDIT, constraint.getCredit());
		values.put(DBConstants.COL_CONST_TYPE, constraint.getConstraintTypeId());
		long result = db.insert(DBConstants.TABLE_CONSTRIANTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Update constraint information
	*/
	public boolean updateConstraint(int constraintId, String date, String details, double debit, double credit,
	int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId); // Constraint type field
		int result = db.update(DBConstants.TABLE_CONSTRIANTS, values, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(constraintId) });
		db.close();
		return result > 0;
	}
	
	/**
	* Update constraint using Constraint object
	*/
	public boolean updateConstraintV2(Constraint constraint) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_ACCOUNT_ID, constraint.getAccountId());
		values.put(DBConstants.COL_CONST_DATE, constraint.getDate());
		values.put(DBConstants.COL_CONST_DETAILS, constraint.getDetails());
		values.put(DBConstants.COL_CONST_DEBIT, constraint.getDebit());
		values.put(DBConstants.COL_CONST_CREDIT, constraint.getCredit());
		values.put(DBConstants.COL_CONST_TYPE, constraint.getConstraintTypeId());
		int result = db.update("constraints_tbl", values, "const_id = ?",
		new String[] { String.valueOf(constraint.getId()) });
		return result > 0;
	}
	
	/**
	* Get all constraints from database
	*/
	public ArrayList<Constraint> getAllConstraints() {
		ArrayList<Constraint> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS, null);
		if (cursor.moveToFirst()) {
			do {
				int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
				int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
				Constraint constraint = new Constraint(constId, accountId, date, details, debit, credit);
				list.add(constraint);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	* Get all constraints by account ID
	*/
	public ArrayList<Constraint> getAllConstraintsByAccountId(int accountId) {
		ArrayList<Constraint> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
		"SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?"
		+ " ORDER BY " + DBConstants.COL_CONST_DATE + " ASC",
		new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			do {
				int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
				int constAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
				int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
				Constraint constraint = new Constraint(constId, constAccountId, constraintTypeId, date, details, debit,
				credit);
				list.add(constraint);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	* Get all constraints by account ID (version 2)
	*/
	public List<Constraint> getAllConstraintsByAccountIdV2(int accountId) {
		List<Constraint> constraintList = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM constraints_tbl WHERE const_account_id = ?",
		new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			do {
				int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
				int constAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
				int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
				Constraint constraint = new Constraint(constId, accountId, date, details, debit, credit);
				constraint.setConstraintTypeId(constraintTypeId);
				constraintList.add(constraint);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return constraintList;
	}
	
	/**
	* Get account type by account ID
	*/
	public String getAccountTypeById(int accountId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String accountType = "";
		Cursor cursor = db.rawQuery("SELECT " + DBConstants.COL_ACCOUNT_TYPE + " FROM " + DBConstants.TABLE_ACCOUNTS
		+ " WHERE " + DBConstants.COL_ACCOUNT_ID + " = ?", new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			accountType = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return accountType;
	}
	
	/**
	* Get constraints count by account ID
	*/
	public int getConstraintsCountByAccountId(int accountId) {
		int count = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = null;
		try {
			String query = "SELECT COUNT(*) FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE "
			+ DBConstants.COL_CONST_ACCOUNT_ID + " = ?";
			cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			} finally {
			if (cursor != null) {
				cursor.close();
			}
			db.close();
		}
		return count;
	}
	
	/**
	* Get total debit for specific account
	*/
	public double getTotalDebitByAccountId(int accountId) {
		double totalDebit = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT SUM(" + DBConstants.COL_CONST_DEBIT + ") FROM " + DBConstants.TABLE_CONSTRIANTS
		+ " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			totalDebit = cursor.getDouble(0);
		}
		cursor.close();
		db.close();
		return totalDebit;
	}
	
	/**
	* Get total credit for specific account
	*/
	public double getTotalCreditByAccountId(int accountId) {
		double totalCredit = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT SUM(" + DBConstants.COL_CONST_CREDIT + ") FROM " + DBConstants.TABLE_CONSTRIANTS
		+ " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			totalCredit = cursor.getDouble(0);
		}
		cursor.close();
		db.close();
		return totalCredit;
	}
	
	/**
	* Get account balance by type
	*/
	public double getAccountBalanceByType(int accountId, String accountType) {
		double totalDebit = getTotalDebitByAccountId(accountId);
		double totalCredit = getTotalCreditByAccountId(accountId);
		double balance;
		if ("صندوق".equals(accountType) || "مدين".equals(accountType)) {
			balance = totalDebit - totalCredit;
			} else if ("دائن".equals(accountType)) {
			balance = totalCredit - totalDebit;
			} else {
			// Default behavior if type is unknown
			balance = totalDebit - totalCredit;
		}
		return balance;
	}
	
	/**
	* Get total debit by date range for specific account
	*/
	public double getTotalDebitByDate(int accountId, String fromDate, String toDate) {
		double totalDebit = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		// Base query
		String query = "SELECT SUM(" + DBConstants.COL_CONST_DEBIT + ") FROM " + DBConstants.TABLE_CONSTRIANTS
		+ " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?";
		List<String> args = new ArrayList<>();
		args.add(String.valueOf(accountId));
		// If dates are not empty, add filter condition
		if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
			query += " AND " + DBConstants.COL_CONST_DATE + " BETWEEN ? AND ?";
			args.add(fromDate);
			args.add(toDate);
		}
		Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
		if (cursor.moveToFirst()) {
			totalDebit = cursor.getDouble(0);
		}
		cursor.close();
		db.close();
		return totalDebit;
	}
	
	/**
	* Get total credit by date range for specific account
	*/
	public double getTotalCreditByDate(int accountId, String fromDate, String toDate) {
		double totalCredit = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT SUM(" + DBConstants.COL_CONST_CREDIT + ") FROM " + DBConstants.TABLE_CONSTRIANTS
		+ " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?";
		List<String> args = new ArrayList<>();
		args.add(String.valueOf(accountId));
		if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
			query += " AND " + DBConstants.COL_CONST_DATE + " BETWEEN ? AND ?";
			args.add(fromDate);
			args.add(toDate);
		}
		Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
		if (cursor.moveToFirst()) {
			totalCredit = cursor.getDouble(0);
		}
		cursor.close();
		db.close();
		return totalCredit;
	}
	
	/**
	* Get account balance by date range
	*/
	public double getAccountBalanceByDate(int accountId, String accountType, String fromDate, String toDate) {
		double totalDebit = getTotalDebitByDate(accountId, fromDate, toDate);
		double totalCredit = getTotalCreditByDate(accountId, fromDate, toDate);
		double balance;
		if ("صندوق".equals(accountType) || "مدين".equals(accountType)) {
			balance = totalDebit - totalCredit;
			} else if ("دائن".equals(accountType)) {
			balance = totalCredit - totalDebit;
			} else {
			// Default behavior if type is unknown
			balance = totalDebit - totalCredit;
		}
		return balance;
	}
	
	/**
	* Get constraints by date range and account
	*/
	public Cursor getConstraintsByDateAndAccount(String fromDate, String toDate, int accountId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID
		+ " = ? " + " AND " + DBConstants.COL_CONST_DATE + " >= ? " + " AND " + DBConstants.COL_CONST_DATE
		+ " <= ?" + " ORDER BY " + DBConstants.COL_CONST_DATE + " ASC"; // Order by date
		String[] args = { String.valueOf(accountId), fromDate, toDate };
		return db.rawQuery(query, args);
	}
	
	/**
	* Delete constraint by ID
	*/
	public boolean deleteConstraint(int id) {
		if (id <= 0) {
			Log.e("DB_DELETE", "Invalid constraint ID: " + id);
			return false;
		}
		SQLiteDatabase db = this.getWritableDatabase();
		int result = db.delete(DBConstants.TABLE_CONSTRIANTS, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(id) });
		Log.d("DB_DELETE", "Deleted constraint ID: " + id + ", rows affected: " + result);
		db.close();
		return result > 0;
	}
	
	/**
	* Delete multiple constraints
	*/
	public boolean deleteConstraints(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return false; // لا يوجد شيء للحذف
		}
		SQLiteDatabase db = this.getWritableDatabase();
		// بناء جملة الاستعلام: DELETE FROM constraints WHERE id IN (id1, id2, id3)
		StringBuilder placeholders = new StringBuilder();
		for (int i = 0; i < ids.size(); i++) {
			placeholders.append("?");
			if (i < ids.size() - 1) {
				placeholders.append(",");
			}
		}
		String[] idsArray = new String[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			idsArray[i] = String.valueOf(ids.get(i));
		}
		int rowsDeleted = db.delete(DBConstants.TABLE_CONSTRIANTS,
		DBConstants.COL_CONST_ID + " IN (" + placeholders.toString() + ")", idsArray);
		db.close();
		// العودة true إذا تم حذف صف واحد على الأقل
		return rowsDeleted > 0;
	}
	
	//======================== CONSTRAINT TYPE TABLE METHODS ================================================
	/**
	* Insert a new constraint type
	*/
	public boolean insertConstraintType(ConstraintType constraintType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + constraintType.getConstraintTypeName());
		values.put(DBConstants.COL_CONSTRAINT_TYPE_NAME, constraintType.getConstraintTypeName());
		long result = db.insert(DBConstants.TABLE_CONSTRAINT_TYPE, null, values);
		return result != -1;
	}
	
	/**
	* Get all constraint types
	*/
	public List<ConstraintType> getAllConstraintTypes() {
		List<ConstraintType> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_CONSTRAINT_TYPE, null);
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONSTRAINT_TYPE_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONSTRAINT_TYPE_NAME));
				list.add(new ConstraintType(id, name));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}
	
	/**
	* Delete constraint type by ID (مع منع حذف القيمة الافتراضية)
	*/
	public boolean deleteConstraintType(int constraintTypeId) {
		// التحقق من أن النوع ليس النوع الافتراضي (ID=1)
		if (constraintTypeId == 1) {
			return false; // لا يمكن حذف النوع الافتراضي
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_CONSTRAINT_TYPE,
		DBConstants.COL_CONSTRAINT_TYPE_ID + "=? AND " + DBConstants.COL_CONSTRAINT_TYPE_ID + " != 1",
		new String[] { String.valueOf(constraintTypeId) });
		db.close();
		return rowsDeleted > 0;
	}
	
	//======================== ACCOUNT TYPE TABLE METHODS ================================================
	/**
	* Insert a new account type
	*/
	public boolean insertAccountType(AccountType accountType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + accountType.getAccountType());
		values.put(DBConstants.COL_ACCOUNT_TYPE_NAME, accountType.getAccountType());
		long result = db.insert(DBConstants.TABLE_ACCOUNTS_TYPE, null, values);
		return result != -1;
	}
	
	/**
	* Get all account types
	*/
	public List<AccountType> getAllAccountTypes() {
		List<AccountType> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_ACCOUNTS_TYPE, null);
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_TYPE_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_TYPE_NAME));
				list.add(new AccountType(id, name));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}
	
	/**
	* Delete account type by ID (مع منع حذف القيمة الافتراضية)
	*/
	public boolean deleteAccountType(int accountTypeId) {
		// التحقق من أن النوع ليس النوع الافتراضي (ID=1)
		if (accountTypeId == 1) {
			return false; // لا يمكن حذف النوع الافتراضي
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_TYPE,
		DBConstants.COL_ACCOUNT_TYPE_ID + " = ? AND " + DBConstants.COL_ACCOUNT_TYPE_ID + " != 1",
		new String[] { String.valueOf(accountTypeId) });
		db.close();
		return rowsDeleted > 0;
	}
	
	//======================== ACCOUNT GROUP TABLE METHODS ================================================
	/**
	* Insert a new account group
	*/
	public boolean insertAccountGroup(AccountGroup accountGroup) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + accountGroup.getAccountGroupName());
		values.put(DBConstants.COL_ACCOUNT_GROUP_NAME, accountGroup.getAccountGroupName());
		long result = db.insert(DBConstants.TABLE_ACCOUNTS_GROUP, null, values);
		return result != -1;
	}
	
	/**
	* Get all account groups
	*/
	public List<AccountGroup> getAllAccountGroup() {
		List<AccountGroup> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_ACCOUNTS_GROUP, null);
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_GROUP_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_GROUP_NAME));
				list.add(new AccountGroup(id, name));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}
	
	/**
	* Delete account group by ID (مع منع حذف القيمة الافتراضية)
	*/
	public boolean deleteAccountGroup(int accountGroupId) {
		// التحقق من أن المجموعة ليست المجموعة الافتراضية (ID=1)
		if (accountGroupId == 1) {
			return false; // لا يمكن حذف المجموعة الافتراضية
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_GROUP,
		DBConstants.COL_ACCOUNT_GROUP_ID + "=? AND " + DBConstants.COL_ACCOUNT_GROUP_ID + " != 1",
		new String[] { String.valueOf(accountGroupId) });
		db.close();
		return rowsDeleted > 0;
	}
}