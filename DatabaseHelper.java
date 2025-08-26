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
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "accountbook.db";
	public static final int DATABASE_VERSION = 1;

	// إنشاء جدول الحسابات
	// إنشاء جدول أنواع الحسابات
	String CREATE_TABLE_ACCOUNTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_TYPE + " ("
			+ DBConstants.COL_ACCOUNT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_ACCOUNT_TYPE_NAME
			+ " TEXT NOT NULL UNIQUE);";

	// إنشاء جدول مجموعات الحسابات
	String CREATE_TABLE_ACCOUNTS_GROUP = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_GROUP + " ("
			+ DBConstants.COL_ACCOUNT_GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_ACCOUNT_GROUP_NAME
			+ " TEXT NOT NULL UNIQUE);";

	// تعديل إنشاء جدول الحسابات ليستخدم مفاتيح أجنبية
	String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS + " (" + DBConstants.COL_ACCOUNT_ID
			+ " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_CREATED_DATE + " TEXT NOT NULL, "
			+ DBConstants.COL_ACCOUNT_NAME + " TEXT NOT NULL UNIQUE, " + DBConstants.COL_ACCOUNT_TYPE
			+ " INTEGER NOT NULL, " + DBConstants.COL_ACCOUNT_PHONE + " TEXT, " + DBConstants.COL_ACCOUNT_GROUP
			+ " INTEGER, " + "FOREIGN KEY(" + DBConstants.COL_ACCOUNT_TYPE + ") REFERENCES "
			+ DBConstants.TABLE_ACCOUNTS_TYPE + "(" + DBConstants.COL_ACCOUNT_TYPE_ID + "), " + "FOREIGN KEY("
			+ DBConstants.COL_ACCOUNT_GROUP + ") REFERENCES " + DBConstants.TABLE_ACCOUNTS_GROUP + "("
			+ DBConstants.COL_ACCOUNT_GROUP_ID + "));";

	// إنشاء جدول القيود (كما هو عندك مع العلاقات)
	String CREATE_TABLE_CONSTRAINTS = "CREATE TABLE " + DBConstants.TABLE_CONSTRIANTS + " (" + DBConstants.COL_CONST_ID
			+ " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_CONST_ACCOUNT_ID + " INTEGER NOT NULL, "
			+ DBConstants.COL_CONST_DATE + " TEXT NOT NULL, " + DBConstants.COL_CONST_DETAILS + " TEXT NOT NULL, "
			+ DBConstants.COL_CONST_DEBIT + " REAL NOT NULL, " + DBConstants.COL_CONST_CREDIT + " REAL NOT NULL, "
			+ DBConstants.COL_CONST_TYPE + " INTEGER NOT NULL, " + "FOREIGN KEY(" + DBConstants.COL_CONST_ACCOUNT_ID
			+ ") REFERENCES " + DBConstants.TABLE_ACCOUNTS + "(" + DBConstants.COL_ACCOUNT_ID + "), " + "FOREIGN KEY("
			+ DBConstants.COL_CONST_TYPE + ") REFERENCES " + DBConstants.TABLE_CONSTRAINT_TYPE + "("
			+ DBConstants.COL_CONSTRAINT_TYPE_ID + "));";

	// إنشاء جدول أنواع القيود
	String CREATE_TABLE_CONSTRAINTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_CONSTRAINT_TYPE + " ("
			+ DBConstants.COL_CONSTRAINT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, "
			+ DBConstants.COL_CONSTRAINT_TYPE_NAME + " TEXT NOT NULL UNIQUE);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CREATE_TABLE_ACCOUNTS);
		db.execSQL(CREATE_TABLE_CONSTRAINTS);
		db.execSQL(CREATE_TABLE_CONSTRAINTS_TYPE);
		db.execSQL(CREATE_TABLE_ACCOUNTS_TYPE);
		db.execSQL(CREATE_TABLE_ACCOUNTS_GROUP);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(" DROP TABLE IF EXEIST " + DBConstants.TABLE_ACCOUNTS);
		db.execSQL("DROP TABLE IF EXIST" + DBConstants.TABLE_CONSTRIANTS);
		db.execSQL("DROP TABLE IF EXIST" + DBConstants.TABLE_CONSTRAINT_TYPE);
	}

	//======================== ACCOUNT TABLE ================================================
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

	public boolean insertAccount(Account account, int accountTypeId, int accountGroupId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CREATED_DATE, account.getCreatedDate());
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId); // رقم معرف النوع
		values.put(DBConstants.COL_ACCOUNT_NAME, account.getAccountName());
		values.put(DBConstants.COL_ACCOUNT_PHONE, account.getAccountPhone());
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId); // رقم معرف المجموعة

		long result = db.insert(DBConstants.TABLE_ACCOUNTS, null, values);
		db.close();

		return result != -1;
	}

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

	public boolean deleteAccountById(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int result = db.delete(DBConstants.TABLE_ACCOUNTS, DBConstants.COL_ACCOUNT_ID + "=?",
				new String[] { String.valueOf(id) });
		db.close();
		return result > 0;
	}

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

	public boolean updateAccountV2(int id, int accountTypeId, int accountGroupId, String name, String accountPhone) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId); // استخدام معرف نوع الحساب
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId); // استخدام معرف مجموعة الحساب
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		int result = db.update(DBConstants.TABLE_ACCOUNTS, values, DBConstants.COL_ACCOUNT_ID + "=?",
				new String[] { String.valueOf(id) });
		return result > 0;
	}

	//======================== CONSTRAINTS TABLE ================================================

	public boolean insertConstraint(int accountId, String date, String details, double debit, double credit,
			int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_ACCOUNT_ID, accountId);
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId); // إضافة حقل نوع القيد
		long result = db.insert(DBConstants.TABLE_CONSTRIANTS, null, values);
		db.close();
		return result != -1;
	}

	public boolean updateConstraint(int constraintId, String date, String details, double debit, double credit,
			int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId); // إضافة حقل نوع القيد
		int result = db.update(DBConstants.TABLE_CONSTRIANTS, values, DBConstants.COL_CONST_ID + " = ?",
				new String[] { String.valueOf(constraintId) });
		db.close();
		return result > 0;
	}

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

	public ArrayList<Constraint> getAllConstraintsByAccountId(int accountId) {
		ArrayList<Constraint> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE "
				+ DBConstants.COL_CONST_ACCOUNT_ID + " = ?", new String[] { String.valueOf(accountId) });

		if (cursor.moveToFirst()) {
			do {
				int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
				int constAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
				int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));

				// إنشاء كائن Constraint كامل
				Constraint constraint = new Constraint(constId, constAccountId, constraintTypeId, date, details, debit,
						credit);
				list.add(constraint);

			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		return list;
	}

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

	// Get total debit for specific account
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

	// Get total credit for specific account
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

	public double getTotalDebitByDate(int accountId, String fromDate, String toDate) {
		double totalDebit = 0;
		SQLiteDatabase db = this.getReadableDatabase();

		// القاعدة الأساسية
		String query = "SELECT SUM(" + DBConstants.COL_CONST_DEBIT + ") FROM " + DBConstants.TABLE_CONSTRIANTS
				+ " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ?";

		List<String> args = new ArrayList<>();
		args.add(String.valueOf(accountId));

		// إذا التواريخ مش فارغة نضيف شرط الفلترة
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

	public Cursor getConstraintsByDateAndAccount(String fromDate, String toDate, int accountId) {
		SQLiteDatabase db = this.getReadableDatabase();

		String query = "SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID
				+ " = ? " + " AND " + DBConstants.COL_CONST_DATE + " >= ? " + " AND " + DBConstants.COL_CONST_DATE
				+ " <= ?" + " ORDER BY " + DBConstants.COL_CONST_DATE + " ASC"; // ترتيب حسب التاريخ

		String[] args = { String.valueOf(accountId), fromDate, toDate };

		return db.rawQuery(query, args);
	}

	/*	public boolean deleteConstraint(int id) {
			Log.d("DB_DEBUG", "Trying to delete constraint with ID: " + id);
			SQLiteDatabase db = this.getWritableDatabase();
			int result = db.delete(DBConstants.TABLE_CONSTRIANTS, DBConstants.COL_CONST_ID + " = ?",
					new String[] { String.valueOf(id) });
			Log.d("DB_DEBUG", "Rows deleted: " + result);
			db.close();
			return result > 0;
		}*/
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

	/*	public boolean deleteConstraint(int constraintId) {
	Log.d("DB_DELETE", "Attempting to delete constraint with ID: " + constraintId);
	
	if (constraintId <= 0) {
	    Log.e("DB_DELETE", "Invalid constraintId: " + constraintId);
	    return false;
	}
	
	SQLiteDatabase db = this.getWritableDatabase();
	int rowsDeleted = 0;
	
	try {
	    rowsDeleted = db.delete(
	            DBConstants.TABLE_CONSTRIANTS,
	            DBConstants.COL_CONST_ID + " = ?",
	            new String[]{String.valueOf(constraintId)}
	    );
	    Log.d("DB_DELETE", "Rows heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeer deleted: " + rowsDeleted);
	} catch (Exception e) {
	    Log.e("DB_DELETE", "Error heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeer deleting constraint", e);
	} finally {
	    db.close();
	}
	
	return rowsDeleted > 0;
	}*/

	//======================== CONSTRAINTS TYPE TABLE ================================================
	//=========================================================================================================

	public boolean insertConstraintType(ConstraintType constraintType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + constraintType.getConstraintTypeName());

		values.put(DBConstants.COL_CONSTRAINT_TYPE_NAME, constraintType.getConstraintTypeName());

		long result = db.insert(DBConstants.TABLE_CONSTRAINT_TYPE, null, values);
		return result != -1;
	}

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

	public boolean deleteConstraintType(int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_CONSTRAINT_TYPE, DBConstants.COL_CONSTRAINT_TYPE_ID + "=?",
				new String[] { String.valueOf(constraintTypeId) });
		db.close();
		return rowsDeleted > 0;
	}

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

	//======================== ACCOUNT TYPE TABLE ================================================
	//=========================================================================================================
	public boolean insertAccountType(AccountType accountType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + accountType.getAccountType());

		values.put(DBConstants.COL_ACCOUNT_TYPE_NAME, accountType.getAccountType());

		long result = db.insert(DBConstants.TABLE_ACCOUNTS_TYPE, null, values);
		return result != -1;
	}

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

	public boolean deleteAccountType(int accountTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_TYPE, DBConstants.COL_ACCOUNT_TYPE_ID + " = ?",
				new String[] { String.valueOf(accountTypeId) });
		db.close();
		return rowsDeleted > 0;
	}

	//======================== ACCOUNT GROUP TABLE ================================================
	//=========================================================================================================
	public boolean insertAccountGroup(AccountGroup accountGroup) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DB", "Trying to insert type: " + accountGroup.getAccountGroupName());

		values.put(DBConstants.COL_ACCOUNT_GROUP_NAME, accountGroup.getAccountGroupName());

		long result = db.insert(DBConstants.TABLE_ACCOUNTS_GROUP, null, values);
		return result != -1;
	}

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

	public boolean deleteAccountGroup(int accountGroupId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_GROUP, DBConstants.COL_ACCOUNT_GROUP_ID + "=?",
				new String[] { String.valueOf(accountGroupId) });
		db.close();
		return rowsDeleted > 0;
	}

}