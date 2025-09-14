package com.my.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database information
	public static final String DATABASE_NAME = "accountbook.db";
	public static final int DATABASE_VERSION = 3; // Version number increased
	
	// Singleton instance
	private static DatabaseHelper instance;
	
	// Private constructor
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// Get singleton instance
	public static synchronized DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context.getApplicationContext());
		}
		return instance;
	}
	
	//======================== TABLE CREATION STATEMENTS ================================================
	
	// Create accounts table
	private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS + " ("
	+ DBConstants.COL_ACCOUNT_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_CREATED_DATE + " TEXT NOT NULL, "
	+ DBConstants.COL_ACCOUNT_NAME + " TEXT NOT NULL UNIQUE, "
	+ DBConstants.COL_ACCOUNT_TYPE + " INTEGER NOT NULL, "
	+ DBConstants.COL_ACCOUNT_PHONE + " TEXT, "
	+ DBConstants.COL_ACCOUNT_GROUP + " INTEGER, "
	+ "FOREIGN KEY(" + DBConstants.COL_ACCOUNT_TYPE
	+ ") REFERENCES " + DBConstants.TABLE_ACCOUNTS_TYPE + "(" + DBConstants.COL_ACCOUNT_TYPE_ID + "), "
	+ "FOREIGN KEY(" + DBConstants.COL_ACCOUNT_GROUP + ") REFERENCES " + DBConstants.TABLE_ACCOUNTS_GROUP + "("
	+ DBConstants.COL_ACCOUNT_GROUP_ID + "));";
	
	// Create account types table
	private static final String CREATE_TABLE_ACCOUNTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_TYPE + " ("
	+ DBConstants.COL_ACCOUNT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_ACCOUNT_TYPE_NAME + " TEXT NOT NULL UNIQUE);";
	
	// Create account groups table
	private static final String CREATE_TABLE_ACCOUNTS_GROUP = "CREATE TABLE " + DBConstants.TABLE_ACCOUNTS_GROUP + " ("
	+ DBConstants.COL_ACCOUNT_GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_ACCOUNT_GROUP_NAME + " TEXT NOT NULL UNIQUE);";
	
	// Create constraints table
	private static final String CREATE_TABLE_CONSTRAINTS = "CREATE TABLE " + DBConstants.TABLE_CONSTRIANTS + " ("
	+ DBConstants.COL_CONST_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_CONST_ACCOUNT_ID + " INTEGER NOT NULL, "
	+ DBConstants.COL_CONST_DATE + " TEXT NOT NULL, "
	+ DBConstants.COL_CONST_DETAILS + " TEXT NOT NULL, "
	+ DBConstants.COL_CONST_DEBIT + " REAL NOT NULL, "
	+ DBConstants.COL_CONST_CREDIT + " REAL NOT NULL, "
	+ DBConstants.COL_CONST_TYPE + " INTEGER NOT NULL, "
	+ DBConstants.COL_CONST_TRANSFER_ID + " INTEGER, " // New field to link constraints to transfers
	+ "FOREIGN KEY(" + DBConstants.COL_CONST_ACCOUNT_ID + ") REFERENCES " + DBConstants.TABLE_ACCOUNTS + "("
	+ DBConstants.COL_ACCOUNT_ID + "), "
	+ "FOREIGN KEY(" + DBConstants.COL_CONST_TYPE + ") REFERENCES "
	+ DBConstants.TABLE_CONSTRAINT_TYPE + "(" + DBConstants.COL_CONSTRAINT_TYPE_ID + "));";
	
	// Create constraint types table
	private static final String CREATE_TABLE_CONSTRAINTS_TYPE = "CREATE TABLE " + DBConstants.TABLE_CONSTRAINT_TYPE
	+ " (" + DBConstants.COL_CONSTRAINT_TYPE_ID + " INTEGER PRIMARY KEY NOT NULL, "
	+ DBConstants.COL_CONSTRAINT_TYPE_NAME + " TEXT NOT NULL UNIQUE);";
	
	// Create transfers table
	private static final String CREATE_TABLE_TRANSFERS = "CREATE TABLE " + DBConstants.TABLE_TRANSFERS + " ("
	+ DBConstants.COL_TRANSFER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ DBConstants.COL_DEBIT_ACCOUNT_ID + " INTEGER NOT NULL, "
	+ DBConstants.COL_CREDIT_ACCOUNT_ID + " INTEGER NOT NULL, "
	+ DBConstants.COL_TRANSFER_DATE + " TEXT NOT NULL, "
	+ DBConstants.COL_TRANSFER_AMOUNT + " REAL NOT NULL, "
	+ DBConstants.COL_TRANSFER_DETAILS + " TEXT NOT NULL, "
	+ DBConstants.COL_TRANSFER_TYPE_ID + " INTEGER NOT NULL, "
	+ "FOREIGN KEY(" + DBConstants.COL_DEBIT_ACCOUNT_ID + ") REFERENCES " + DBConstants.TABLE_ACCOUNTS + "("
	+ DBConstants.COL_ACCOUNT_ID + "), "
	+ "FOREIGN KEY(" + DBConstants.COL_CREDIT_ACCOUNT_ID + ") REFERENCES "
	+ DBConstants.TABLE_ACCOUNTS + "(" + DBConstants.COL_ACCOUNT_ID + "), "
	+ "FOREIGN KEY(" + DBConstants.COL_TRANSFER_TYPE_ID + ") REFERENCES " + DBConstants.TABLE_CONSTRAINT_TYPE + "("
	+ DBConstants.COL_CONSTRAINT_TYPE_ID + "));";
	
	//======================== DATABASE LIFECYCLE METHODS ================================================
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_ACCOUNTS_TYPE);
		db.execSQL(CREATE_TABLE_ACCOUNTS_GROUP);
		db.execSQL(CREATE_TABLE_ACCOUNTS);
		db.execSQL(CREATE_TABLE_CONSTRAINTS_TYPE);
		db.execSQL(CREATE_TABLE_CONSTRAINTS);
		db.execSQL(CREATE_TABLE_TRANSFERS); // Add transfers table
		
		// Insert default values
		insertDefaultValues(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
		if (oldVersion < 2) {
			// Updates for version 2
			insertDefaultValues(db);
		}
		if (oldVersion < 3) {
			// Add transfer_id field to constraints table
			try {
				db.execSQL("ALTER TABLE " + DBConstants.TABLE_CONSTRIANTS + " ADD COLUMN "
				+ DBConstants.COL_CONST_TRANSFER_ID + " INTEGER");
				Log.d("DatabaseHelper", "Added transfer_id column to constraints table");
				} catch (Exception e) {
				Log.e("DatabaseHelper", "Error adding transfer_id column: " + e.getMessage());
			}
			// Add transfers table for version 3
			db.execSQL(CREATE_TABLE_TRANSFERS);
			Log.d("DatabaseHelper", "Added transfers table");
		}
	}
	
	//======================== DEFAULT VALUES METHODS ================================================
	
	/**
	* Function to insert default values
	*/
	public void insertDefaultValues(SQLiteDatabase db) {
		// Insert default value for account types table
		ContentValues accountTypeValues = new ContentValues();
		accountTypeValues.put(DBConstants.COL_ACCOUNT_TYPE_ID, 1);
		accountTypeValues.put(DBConstants.COL_ACCOUNT_TYPE_NAME, "مدين");
		db.insertWithOnConflict(DBConstants.TABLE_ACCOUNTS_TYPE, null, accountTypeValues,
		SQLiteDatabase.CONFLICT_IGNORE);
		
		// Insert default value for account groups table
		ContentValues accountGroupValues = new ContentValues();
		accountGroupValues.put(DBConstants.COL_ACCOUNT_GROUP_ID, 1);
		accountGroupValues.put(DBConstants.COL_ACCOUNT_GROUP_NAME, "عام");
		db.insertWithOnConflict(DBConstants.TABLE_ACCOUNTS_GROUP, null, accountGroupValues,
		SQLiteDatabase.CONFLICT_IGNORE);
		
		// Insert default value for constraint types table
		ContentValues constraintTypeValues = new ContentValues();
		constraintTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_ID, 1);
		constraintTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_NAME, "عام");
		db.insertWithOnConflict(DBConstants.TABLE_CONSTRAINT_TYPE, null, constraintTypeValues,
		SQLiteDatabase.CONFLICT_IGNORE);
		
		// Insert more default values for constraint types
		ContentValues transferTypeValues = new ContentValues();
		transferTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_ID, 2);
		transferTypeValues.put(DBConstants.COL_CONSTRAINT_TYPE_NAME, "تحويل");
		db.insertWithOnConflict(DBConstants.TABLE_CONSTRAINT_TYPE, null, transferTypeValues,
		SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	/**
	* Function to check if default values exist and add them if not
	*/
	public void ensureDefaultValuesExist() {
		SQLiteDatabase db = this.getWritableDatabase();
		insertDefaultValues(db);
		db.close();
	}
	
	//======================== TRANSACTION METHODS ================================================
	
	/**
	* Start a database transaction
	*/
	public SQLiteDatabase beginTransaction() {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		return db;
	}
	
	/**
	* End transaction successfully
	*/
	public void setTransactionSuccessful(SQLiteDatabase db) {
		if (db != null && db.inTransaction()) {
			db.setTransactionSuccessful();
		}
	}
	
	/**
	* End the transaction
	*/
	public void endTransaction(SQLiteDatabase db) {
		if (db != null && db.inTransaction()) {
			db.endTransaction();
		}
	}
	
	//======================== ACCOUNTS TABLE METHODS ================================================
	
	/**
	* Insert a new account
	*/
	public boolean insertAccount(Account account) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		// Convert text to IDs
		int accountTypeId = getAccountTypeIdByName(account.getAccountType());
		int accountGroupId = getAccountGroupIdByName(account.getAccountGroup());
		values.put(DBConstants.COL_CREATED_DATE, account.getCreatedDate());
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId);
		values.put(DBConstants.COL_ACCOUNT_NAME, account.getAccountName());
		values.put(DBConstants.COL_ACCOUNT_PHONE, account.getAccountPhone());
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId);
		long result = db.insert(DBConstants.TABLE_ACCOUNTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Update an account
	*/
	public boolean updateAccount(int id, int accountTypeId, int accountGroupId, String name, String accountPhone) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId);
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId);
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		int result = db.update(DBConstants.TABLE_ACCOUNTS, values, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		return result > 0;
	}
	
	/**
	* Update an account (version 3)
	*/
	public boolean updateAccountV3(int id, String accountTypeText, String accountGroupText, String name,
	String accountPhone) {
		SQLiteDatabase db = this.getWritableDatabase();
		// Convert text to IDs
		int accountTypeId = getAccountTypeIdByName(accountTypeText);
		int accountGroupId = getAccountGroupIdByName(accountGroupText);
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_ACCOUNT_TYPE, accountTypeId);
		values.put(DBConstants.COL_ACCOUNT_GROUP, accountGroupId);
		values.put(DBConstants.COL_ACCOUNT_NAME, name);
		values.put(DBConstants.COL_ACCOUNT_PHONE, accountPhone);
		int result = db.update(DBConstants.TABLE_ACCOUNTS, values, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		return result > 0;
	}
	
	/**
	* Delete an account
	*/
	public boolean deleteAccountById(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int result = db.delete(DBConstants.TABLE_ACCOUNTS, DBConstants.COL_ACCOUNT_ID + "=?",
		new String[] { String.valueOf(id) });
		db.close();
		return result > 0;
	}
	
	/**
	* Get all accounts
	*/
	public ArrayList<Account> getAllAccounts() {
		ArrayList<Account> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		// Using JOIN to get text instead of IDs
		String query = "SELECT a.*, at." + DBConstants.COL_ACCOUNT_TYPE_NAME + ", ag."
		+ DBConstants.COL_ACCOUNT_GROUP_NAME + " FROM " + DBConstants.TABLE_ACCOUNTS + " a" + " LEFT JOIN "
		+ DBConstants.TABLE_ACCOUNTS_TYPE + " at ON a." + DBConstants.COL_ACCOUNT_TYPE + " = at."
		+ DBConstants.COL_ACCOUNT_TYPE_ID + " LEFT JOIN " + DBConstants.TABLE_ACCOUNTS_GROUP + " ag ON a."
		+ DBConstants.COL_ACCOUNT_GROUP + " = ag." + DBConstants.COL_ACCOUNT_GROUP_ID;
		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {
				int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_ID));
				String accountCreationDate = cursor
				.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CREATED_DATE));
				String accountName = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_NAME));
				String accountType = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_TYPE_NAME));
				String accountPhone = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_PHONE));
				String accountGroup = cursor
				.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_GROUP_NAME));
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
	* Get account by ID (version 3)
	*/
	public Account getAccountByIdV3(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		// Using JOIN to get text instead of IDs
		String query = "SELECT a.*, at." + DBConstants.COL_ACCOUNT_TYPE_NAME + ", ag."
		+ DBConstants.COL_ACCOUNT_GROUP_NAME + " FROM " + DBConstants.TABLE_ACCOUNTS + " a" + " LEFT JOIN "
		+ DBConstants.TABLE_ACCOUNTS_TYPE + " at ON a." + DBConstants.COL_ACCOUNT_TYPE + " = at."
		+ DBConstants.COL_ACCOUNT_TYPE_ID + " LEFT JOIN " + DBConstants.TABLE_ACCOUNTS_GROUP + " ag ON a."
		+ DBConstants.COL_ACCOUNT_GROUP + " = ag." + DBConstants.COL_ACCOUNT_GROUP_ID + " WHERE a."
		+ DBConstants.COL_ACCOUNT_ID + " = ?";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(id) });
		Account account = null;
		if (cursor.moveToFirst()) {
			int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_ID));
			String accountCreationDate = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CREATED_DATE));
			String accountName = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_NAME));
			String accountType = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_TYPE_NAME));
			String accountPhone = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_PHONE));
			String accountGroup = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_ACCOUNT_GROUP_NAME));
			account = new Account(accountId, accountName, accountCreationDate, accountType, accountPhone, accountGroup);
		}
		cursor.close();
		db.close();
		return account;
	}
	
	/**
	* Get account type by ID
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
	* Get account type ID by name
	*/
	private int getAccountTypeIdByName(String typeName) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(DBConstants.TABLE_ACCOUNTS_TYPE, new String[] { DBConstants.COL_ACCOUNT_TYPE_ID },
		DBConstants.COL_ACCOUNT_TYPE_NAME + "=?", new String[] { typeName }, null, null, null);
		int typeId = -1;
		if (cursor.moveToFirst()) {
			typeId = cursor.getInt(0);
		}
		cursor.close();
		return typeId;
	}
	
	/**
	* Get account group ID by name
	*/
	private int getAccountGroupIdByName(String groupName) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(DBConstants.TABLE_ACCOUNTS_GROUP, new String[] { DBConstants.COL_ACCOUNT_GROUP_ID },
		DBConstants.COL_ACCOUNT_GROUP_NAME + "=?", new String[] { groupName }, null, null, null);
		int groupId = -1;
		if (cursor.moveToFirst()) {
			groupId = cursor.getInt(0);
		}
		cursor.close();
		return groupId;
	}
	
	//======================== ACCOUNTS TYPE TABLE METHODS ================================================
	
	/**
	* Insert a new account type
	*/
	public boolean insertAccountType(AccountType accountType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DatabaseHelper", "Trying to insert type: " + accountType.getAccountType());
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
	* Delete an account type (with prevention of deleting the default value)
	*/
	public boolean deleteAccountType(int accountTypeId) {
		// Check that the type is not the default type (ID=1)
		if (accountTypeId == 1) {
			return false; // Cannot delete the default type
		}
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_TYPE,
		DBConstants.COL_ACCOUNT_TYPE_ID + " = ? AND " + DBConstants.COL_ACCOUNT_TYPE_ID + " != 1",
		new String[] { String.valueOf(accountTypeId) });
		db.close();
		return rowsDeleted > 0;
	}
	
	//======================== ACCOUNTS GROUP TABLE METHODS ================================================
	
	/**
	* Insert a new account group
	*/
	public boolean insertAccountGroup(AccountGroup accountGroup) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DatabaseHelper", "Trying to insert group: " + accountGroup.getAccountGroupName());
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
	* Delete an account group (with prevention of deleting the default value)
	*/
	public boolean deleteAccountGroup(int accountGroupId) {
		// Check that the group is not the default group (ID=1)
		if (accountGroupId == 1) {
			return false; // Cannot delete the default group
		}
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_ACCOUNTS_GROUP,
		DBConstants.COL_ACCOUNT_GROUP_ID + "=? AND " + DBConstants.COL_ACCOUNT_GROUP_ID + " != 1",
		new String[] { String.valueOf(accountGroupId) });
		db.close();
		return rowsDeleted > 0;
	}
	
	//======================== CONSTRAINTS TABLE METHODS ================================================
	
	/**
	* Insert a constraint using an open database (without closing it)
	*/
	public boolean insertConstraintInTransaction(SQLiteDatabase db, int accountId, String date, String details,
	double debit, double credit, int constraintTypeId) {
		return insertConstraintInTransaction(db, accountId, date, details, debit, credit, constraintTypeId, -1);
	}
	
	public boolean insertConstraintInTransaction(SQLiteDatabase db, int accountId, String date, String details,
	double debit, double credit, int constraintTypeId, long transferId) {
		// Add a log record to track calls
		Log.d("DatabaseHelper", "insertConstraintInTransaction called - Account: " + accountId + ", Debit: " + debit
		+ ", Credit: " + credit + ", TransferId: " + transferId);
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_ACCOUNT_ID, accountId);
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId);
		// Add transferId if it's valid
		if (transferId > 0) {
			values.put(DBConstants.COL_CONST_TRANSFER_ID, transferId);
		}
		long result = db.insert(DBConstants.TABLE_CONSTRIANTS, null, values);
		Log.d("DatabaseHelper", "insertConstraintInTransaction result: " + result);
		return result != -1;
	}
	
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
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId);
		long result = db.insert(DBConstants.TABLE_CONSTRIANTS, null, values);
		db.close();
		return result != -1;
	}
	
	/**
	* Insert a constraint using a Constraint object
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
	* Update a constraint
	*/
	public boolean updateConstraint(int constraintId, String date, String details, double debit, double credit,
	int constraintTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId);
		int result = db.update(DBConstants.TABLE_CONSTRIANTS, values, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(constraintId) });
		db.close();
		return result > 0;
	}
	
	/**
	* Update a constraint using an open database
	*/
	public boolean updateConstraintInTransaction(SQLiteDatabase db, int constraintId, String date, String details,
	double debit, double credit, int constraintTypeId) {
		ContentValues values = new ContentValues();
		values.put(DBConstants.COL_CONST_DATE, date);
		values.put(DBConstants.COL_CONST_DETAILS, details);
		values.put(DBConstants.COL_CONST_DEBIT, debit);
		values.put(DBConstants.COL_CONST_CREDIT, credit);
		values.put(DBConstants.COL_CONST_TYPE, constraintTypeId);
		int result = db.update(DBConstants.TABLE_CONSTRIANTS, values, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(constraintId) });
		Log.d("DatabaseHelper", "Updated constraint ID: " + constraintId + ", rows affected: " + result);
		return result > 0;
	}
	
	/**
	* Update a constraint using a Constraint object
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
		int result = db.update(DBConstants.TABLE_CONSTRIANTS, values, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(constraint.getId()) });
		db.close();
		return result > 0;
	}
	
	/**
	* Delete a constraint
	*/
	public boolean deleteConstraint(int id) {
		if (id <= 0) {
			Log.e("DatabaseHelper", "Invalid constraint ID: " + id);
			return false;
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check if this constraint is part of a transfer
		Constraint constraint = getConstraintById(id);
		if (constraint != null && constraint.getTransferId() != null) {
			// This is part of a transfer, delete the entire transfer
			boolean result = deleteTransferConstraint(constraint.getTransferId());
			db.close();
			return result;
			} else {
			// This is a regular constraint, just delete it
			int result = db.delete(DBConstants.TABLE_CONSTRIANTS, DBConstants.COL_CONST_ID + " = ?",
			new String[] { String.valueOf(id) });
			Log.d("DatabaseHelper", "Deleted constraint ID: " + id + ", rows affected: " + result);
			db.close();
			return result > 0;
		}
	}
	
	/**
	* Delete multiple constraints
	*/
	public boolean deleteConstraints(List<Constraint> constraints) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			for (Constraint constraint : constraints) {
				// Make sure we don't delete a transfer here
				if (constraint.getTransferId() == null) {
					db.delete(DBConstants.TABLE_CONSTRIANTS, DBConstants.COL_CONST_ID + " = ?",
					new String[] { String.valueOf(constraint.getId()) });
				}
			}
			db.setTransactionSuccessful();
			return true;
			} finally {
			db.endTransaction();
		}
	}
	
	public boolean deleteMixedConstraints(List<Constraint> constraints) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			// تجميع الـ transferIds المختلفة
			Set<Integer> transferIds = new HashSet<>();
			List<Integer> regularConstraintIds = new ArrayList<>();
			
			for (Constraint constraint : constraints) {
				if (constraint.getTransferId() != null) {
					transferIds.add(constraint.getTransferId());
					} else {
					regularConstraintIds.add(constraint.getId());
				}
			}
			
			// حذف القيود العادية
			if (!regularConstraintIds.isEmpty()) {
				String whereClause = DBConstants.COL_CONST_ID + " IN (" +
				TextUtils.join(",", Collections.nCopies(regularConstraintIds.size(), "?")) + ")";
				String[] whereArgs = new String[regularConstraintIds.size()];
				for (int i = 0; i < regularConstraintIds.size(); i++) {
					whereArgs[i] = String.valueOf(regularConstraintIds.get(i));
				}
				db.delete(DBConstants.TABLE_CONSTRIANTS, whereClause, whereArgs);
			}
			
			// حذف قيود التحويل
			for (Integer transferId : transferIds) {
				String whereClause = DBConstants.COL_CONST_TRANSFER_ID + " = ?";
				String[] whereArgs = { String.valueOf(transferId) };
				db.delete(DBConstants.TABLE_CONSTRIANTS, whereClause, whereArgs);
			}
			
			db.setTransactionSuccessful();
			return true;
			} finally {
			db.endTransaction();
		}
	}
	
	/**
	* Get a constraint by ID
	*/
	public Constraint getConstraintById(int constraintId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(DBConstants.TABLE_CONSTRIANTS, null, DBConstants.COL_CONST_ID + " = ?",
		new String[] { String.valueOf(constraintId) }, null, null, null);
		Constraint constraint = null;
		if (cursor.moveToFirst()) {
			int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
			int accountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
			int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
			String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
			String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
			double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
			double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
			constraint = new Constraint(constId, accountId, constraintTypeId, date, details, debit, credit);
			// Set transferId if exists
			int transferIdIndex = cursor.getColumnIndex(DBConstants.COL_CONST_TRANSFER_ID);
			if (transferIdIndex != -1 && !cursor.isNull(transferIdIndex)) {
				constraint.setTransferId(cursor.getInt(transferIdIndex));
			}
		}
		cursor.close();
		return constraint;
	}
	
	/**
	* Find constraints related to a transfer
	*/
	
	
	/**
	* Get all constraints
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
	* Get all constraints for a specific account
	*/
	public ArrayList<Constraint> getAllConstraintsByAccountId(int accountId) {
		ArrayList<Constraint> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID
		+ " = ?" + " ORDER BY " + DBConstants.COL_CONST_DATE + " DESC, " + DBConstants.COL_CONST_ID + " DESC";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			do {
				int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
				int constAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
				int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
				// Get transferId
				Integer transferId = null;
				int transferIdIndex = cursor.getColumnIndex(DBConstants.COL_CONST_TRANSFER_ID);
				if (transferIdIndex != -1 && !cursor.isNull(transferIdIndex)) {
					transferId = cursor.getInt(transferIdIndex);
				}
				Constraint constraint = new Constraint(constId, constAccountId, constraintTypeId, date, details, debit,
				credit);
				constraint.setTransferId(transferId);
				list.add(constraint);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	* Get constraints by date and account
	*/
	public Cursor getConstraintsByDateAndAccount(String fromDate, String toDate, int accountId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_ACCOUNT_ID
		+ " = ?";
		List<String> args = new ArrayList<>();
		args.add(String.valueOf(accountId));
		if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
			query += " AND " + DBConstants.COL_CONST_DATE + " BETWEEN ? AND ?";
			args.add(fromDate);
			args.add(toDate);
		}
		query += " ORDER BY " + DBConstants.COL_CONST_DATE + " DESC, " + DBConstants.COL_CONST_ID + " DESC";
		return db.rawQuery(query, args.toArray(new String[0]));
	}
	
	/**
	* Get constraint type name
	*/
	public String getConstraintTypeName(int typeId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String typeName = null;
		Cursor cursor = db.rawQuery(
		"SELECT " + DBConstants.COL_CONSTRAINT_TYPE_NAME + " FROM " + DBConstants.TABLE_CONSTRAINT_TYPE
		+ " WHERE " + DBConstants.COL_CONSTRAINT_TYPE_ID + " = ?",
		new String[] { String.valueOf(typeId) });
		if (cursor.moveToFirst()) {
			typeName = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONSTRAINT_TYPE_NAME));
		}
		cursor.close();
		db.close();
		return typeName != null ? typeName : "Unknown"; // Default value if not found
	}
	
	/**
	* Get number of constraints for a specific account
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
	* Get total debit for a specific account
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
		return totalDebit;
	}
	
	/**
	* Get total credit for a specific account
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
	* Get total debit by date range
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
	* Get total credit by date range
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
	* Delete transfer constraints
	*/
	public boolean deleteTransferConstraint(int transferId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			// Delete all constraints with the same transferId
			String whereClause = DBConstants.COL_CONST_TRANSFER_ID + " = ?";
			String[] whereArgs = { String.valueOf(transferId) };
			int deletedRows = db.delete(DBConstants.TABLE_CONSTRIANTS, whereClause, whereArgs);
			if (deletedRows > 0) {
				db.setTransactionSuccessful();
				return true;
			}
			return false;
			} finally {
			db.endTransaction();
		}
	}
	
	/**
	* Update a transfer
	*/
	/**
	* Update a transfer and its related constraints
	*/
	/**
	* Update a transfer and its related constraints
	*/
	public boolean updateTransfer(int transferId, int newDebitAccountId, int newCreditAccountId, double newAmount,
	String newDate, String newDescription, int newTypeId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		
		try {
			// 1. تحديث القيد المدين
			ContentValues debitValues = new ContentValues();
			debitValues.put(DBConstants.COL_CONST_ACCOUNT_ID, newDebitAccountId);
			debitValues.put(DBConstants.COL_CONST_DATE, newDate);
			debitValues.put(DBConstants.COL_CONST_DETAILS, newDescription);
			debitValues.put(DBConstants.COL_CONST_DEBIT, newAmount);
			debitValues.put(DBConstants.COL_CONST_CREDIT, 0.0);
			debitValues.put(DBConstants.COL_CONST_TYPE, newTypeId);
			
			int debitUpdateResult = db.update(DBConstants.TABLE_CONSTRIANTS, debitValues,
			DBConstants.COL_CONST_TRANSFER_ID + " = ? AND " + DBConstants.COL_CONST_DEBIT + " > 0",
			new String[]{String.valueOf(transferId)});
			
			// 2. تحديث القيد الدائن
			ContentValues creditValues = new ContentValues();
			creditValues.put(DBConstants.COL_CONST_ACCOUNT_ID, newCreditAccountId);
			creditValues.put(DBConstants.COL_CONST_DATE, newDate);
			creditValues.put(DBConstants.COL_CONST_DETAILS, newDescription);
			creditValues.put(DBConstants.COL_CONST_DEBIT, 0.0);
			creditValues.put(DBConstants.COL_CONST_CREDIT, newAmount);
			creditValues.put(DBConstants.COL_CONST_TYPE, newTypeId);
			
			int creditUpdateResult = db.update(DBConstants.TABLE_CONSTRIANTS, creditValues,
			DBConstants.COL_CONST_TRANSFER_ID + " = ? AND " + DBConstants.COL_CONST_CREDIT + " > 0",
			new String[]{String.valueOf(transferId)});
			
			if (debitUpdateResult > 0 && creditUpdateResult > 0) {
				db.setTransactionSuccessful();
				Log.d("DatabaseHelper", "Successfully updated transfer constraints");
				return true;
				} else {
				Log.e("DatabaseHelper", "Failed to update transfer constraints. Debit: " + debitUpdateResult + ", Credit: " + creditUpdateResult);
				return false;
			}
			} catch (Exception e) {
			Log.e("DatabaseHelper", "Error updating transfer: " + e.getMessage(), e);
			return false;
			} finally {
			db.endTransaction();
		}
	}
	
	/**
	* Get filtered constraints by date
	*/
	public List<Constraint> getFilteredConstraintsByDate(int accountId, String fromDate, String toDate) {
		ArrayList<Constraint> list = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		// Unified query that fetches data with filtering
		String query = "SELECT * FROM (" +
		// Regular constraints
		"SELECT " + DBConstants.COL_CONST_ID + " as id, " + DBConstants.COL_CONST_ACCOUNT_ID + ", "
		+ DBConstants.COL_CONST_DATE + " as date, " + DBConstants.COL_CONST_DETAILS + ", "
		+ DBConstants.COL_CONST_DEBIT + ", " + DBConstants.COL_CONST_CREDIT + ", " + DBConstants.COL_CONST_TYPE
		+ ", " + DBConstants.COL_CONST_TRANSFER_ID + ", " + "'constraint' as type " + "FROM "
		+ DBConstants.TABLE_CONSTRIANTS + " " + "WHERE " + DBConstants.COL_CONST_ACCOUNT_ID + " = ? AND "
		+ DBConstants.COL_CONST_TRANSFER_ID + " IS NULL AND " + DBConstants.COL_CONST_DATE + " BETWEEN ? AND ? "
		+
		"UNION ALL " +
		// Transfers as debit constraints
		"SELECT " + "(-(" + DBConstants.COL_TRANSFER_ID + " * 10 + 1)) as id, "
		+ DBConstants.COL_DEBIT_ACCOUNT_ID + " as " + DBConstants.COL_CONST_ACCOUNT_ID + ", "
		+ DBConstants.COL_TRANSFER_DATE + " as date, " + DBConstants.COL_TRANSFER_DETAILS + " as "
		+ DBConstants.COL_CONST_DETAILS + ", " + DBConstants.COL_TRANSFER_AMOUNT + " as "
		+ DBConstants.COL_CONST_DEBIT + ", " + "0 as " + DBConstants.COL_CONST_CREDIT + ", "
		+ DBConstants.COL_TRANSFER_TYPE_ID + " as " + DBConstants.COL_CONST_TYPE + ", "
		+ DBConstants.COL_TRANSFER_ID + " as " + DBConstants.COL_CONST_TRANSFER_ID + ", "
		+ "'transfer_debit' as type " + "FROM " + DBConstants.TABLE_TRANSFERS + " " + "WHERE "
		+ DBConstants.COL_DEBIT_ACCOUNT_ID + " = ? AND " + DBConstants.COL_TRANSFER_DATE + " BETWEEN ? AND ? " +
		"UNION ALL " +
		// Transfers as credit constraints
		"SELECT " + "(-(" + DBConstants.COL_TRANSFER_ID + " * 10 + 2)) as id, "
		+ DBConstants.COL_CREDIT_ACCOUNT_ID + " as " + DBConstants.COL_CONST_ACCOUNT_ID + ", "
		+ DBConstants.COL_TRANSFER_DATE + " as date, " + DBConstants.COL_TRANSFER_DETAILS + " as "
		+ DBConstants.COL_CONST_DETAILS + ", " + "0 as " + DBConstants.COL_CONST_DEBIT + ", "
		+ DBConstants.COL_TRANSFER_AMOUNT + " as " + DBConstants.COL_CONST_CREDIT + ", "
		+ DBConstants.COL_TRANSFER_TYPE_ID + " as " + DBConstants.COL_CONST_TYPE + ", "
		+ DBConstants.COL_TRANSFER_ID + " as " + DBConstants.COL_CONST_TRANSFER_ID + ", "
		+ "'transfer_credit' as type " + "FROM " + DBConstants.TABLE_TRANSFERS + " " + "WHERE "
		+ DBConstants.COL_CREDIT_ACCOUNT_ID + " = ? AND " + DBConstants.COL_TRANSFER_DATE + " BETWEEN ? AND ? "
		+ ")";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId), fromDate, toDate,
		String.valueOf(accountId), fromDate, toDate, String.valueOf(accountId), fromDate, toDate });
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				int accountIdCol = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_ACCOUNT_ID));
				String date = cursor.getString(cursor.getColumnIndex("date"));
				String details = cursor.getString(cursor.getColumnIndex(DBConstants.COL_CONST_DETAILS));
				double debit = cursor.getDouble(cursor.getColumnIndex(DBConstants.COL_CONST_DEBIT));
				double credit = cursor.getDouble(cursor.getColumnIndex(DBConstants.COL_CONST_CREDIT));
				int typeId = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_TYPE));
				String type = cursor.getString(cursor.getColumnIndex("type"));
				Constraint constraint = new Constraint(id, accountIdCol, typeId, date, details, debit, credit);
				// If it's a transfer, set the transferId
				if (type.equals("transfer_debit") || type.equals("transfer_credit")) {
					int transferId = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_TRANSFER_ID));
					constraint.setTransferId(transferId);
				}
				list.add(constraint);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// Sort the list by actual date (newest first)
		list.sort((c1, c2) -> {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
				Date date1 = sdf.parse(c1.getDate());
				Date date2 = sdf.parse(c2.getDate());
				int dateCompare = date2.compareTo(date1); // Descending order
				// If the date is the same, sort by ID (largest first)
				if (dateCompare == 0) {
					return Integer.compare(c2.getId(), c1.getId());
				}
				return dateCompare;
				} catch (Exception e) {
				// In case of date parsing failure, use text comparison
				return c2.getDate().compareTo(c1.getDate());
			}
		});
		return list;
	}
	
	//======================== CONSTRAINT TYPES TABLE METHODS ================================================
	
	/**
	* Insert a new constraint type
	*/
	public boolean insertConstraintType(ConstraintType constraintType) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Log.d("DatabaseHelper", "Trying to insert type: " + constraintType.getConstraintTypeName());
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
	* Delete a constraint type (with prevention of deleting the default value)
	*/
	public boolean deleteConstraintType(int constraintTypeId) {
		// Check that the type is not the default type (ID=1)
		if (constraintTypeId == 1) {
			return false; // Cannot delete the default type
		}
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = db.delete(DBConstants.TABLE_CONSTRAINT_TYPE,
		DBConstants.COL_CONSTRAINT_TYPE_ID + "=? AND " + DBConstants.COL_CONSTRAINT_TYPE_ID + " != 1",
		new String[] { String.valueOf(constraintTypeId) });
		db.close();
		return rowsDeleted > 0;
	}
	
	//======================== TRANSFERS TABLE METHODS ================================================
	
	/**
	* Get the matching transfer constraint
	*/
	public Constraint getMatchingTransferConstraint(int transferId, int currentAccountId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + DBConstants.TABLE_CONSTRIANTS + " WHERE " + DBConstants.COL_CONST_TRANSFER_ID
		+ " = ?" + " AND " + DBConstants.COL_CONST_ACCOUNT_ID + " != ?";
		Cursor cursor = db.rawQuery(query,
		new String[] { String.valueOf(transferId), String.valueOf(currentAccountId) });
		Constraint matchingConstraint = null;
		if (cursor.moveToFirst()) {
			int constId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ID));
			int constAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_ACCOUNT_ID));
			int constraintTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_TYPE));
			String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DATE));
			String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DETAILS));
			double debit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_DEBIT));
			double credit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_CONST_CREDIT));
			matchingConstraint = new Constraint(constId, constAccountId, constraintTypeId, date, details, debit,
			credit);
			// Set transferId
			int tid = cursor.getInt(cursor.getColumnIndex(DBConstants.COL_CONST_TRANSFER_ID));
			matchingConstraint.setTransferId(tid);
		}
		cursor.close();
		return matchingConstraint;
	}
	
	/**
	* Get all transfers
	*/
	public List<Transfer> getAllTransfers() {
		List<Transfer> transferList = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_TRANSFERS, null);
		if (cursor.moveToFirst()) {
			do {
				// Corrected: Use COL_TRANSFER_ID instead of COL_CONST_TRANSFER_ID
				int transferId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_ID));
				int debitAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_DEBIT_ACCOUNT_ID));
				int creditAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CREDIT_ACCOUNT_ID));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_DATE));
				double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_AMOUNT));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_DETAILS));
				int typeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_TYPE_ID));
				Transfer transfer = new Transfer(transferId, debitAccountId, creditAccountId, date, amount, details,
				typeId);
				transferList.add(transfer);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return transferList;
	}
	
	/**
	* Get transfers by account ID
	*/
	public List<Transfer> getTransfersByAccountId(int accountId) {
		List<Transfer> transferList = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + DBConstants.TABLE_TRANSFERS + " WHERE " + DBConstants.COL_DEBIT_ACCOUNT_ID
		+ " = ? OR " + DBConstants.COL_CREDIT_ACCOUNT_ID + " = ?";
		Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId), String.valueOf(accountId) });
		if (cursor.moveToFirst()) {
			do {
				// Corrected: Use COL_TRANSFER_ID instead of COL_CONST_TRANSFER_ID
				int transferId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_ID));
				int debitAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_DEBIT_ACCOUNT_ID));
				int creditAccountId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_CREDIT_ACCOUNT_ID));
				String date = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_DATE));
				double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_AMOUNT));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_DETAILS));
				int typeId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.COL_TRANSFER_TYPE_ID));
				Transfer transfer = new Transfer(transferId, debitAccountId, creditAccountId, date, amount, details,
				typeId);
				transferList.add(transfer);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return transferList;
	}
}