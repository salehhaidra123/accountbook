package com.my.myapp;

public class DBConstants {

	// Table: accounts
	public static final String TABLE_ACCOUNTS = "accounts_tbl";
	public static final String COL_ACCOUNT_ID = "account_id";
	public static final String COL_CREATED_DATE = "account_created_date";
	public static final String COL_ACCOUNT_NAME = "account_name";
	public static final String COL_ACCOUNT_TYPE = "account_type";
	public static final String COL_ACCOUNT_PHONE = "account_phone";
	public static final String COL_ACCOUNT_GROUP = "account_group";

	// Table: constants
	public static final String TABLE_CONSTRIANTS = "constraints_tbl";
	public static final String COL_CONST_ID = "const_id";
	public static final String COL_CONST_ACCOUNT_ID = "const_account_id";
	public static final String COL_CONST_TYPE = "const_type_id";
	public static final String COL_CONST_DATE = "const_date";
	public static final String COL_CONST_DETAILS = "const_details";
	public static final String COL_CONST_DEBIT = "const_debit";
	public static final String COL_CONST_CREDIT = "const_credit";

	// Table: Account Type
	public static final String TABLE_ACCOUNTS_TYPE = "accounts_type_tbl";
	public static final String COL_ACCOUNT_TYPE_ID = "account_type_id";
	public static final String COL_ACCOUNT_TYPE_NAME = "account_type_name";

	// Table: Account Group
	public static final String TABLE_ACCOUNTS_GROUP = "accounts_group_tbl";
	public static final String COL_ACCOUNT_GROUP_ID = "account_group_id";
	public static final String COL_ACCOUNT_GROUP_NAME = "account_group_name";

	// Table: Constraints Type
	public static final String TABLE_CONSTRAINT_TYPE = "constraints_type_tbl";
	public static final String COL_CONSTRAINT_TYPE_ID = "constraint_type_id";
	public static final String COL_CONSTRAINT_TYPE_NAME = "constraints_type_name";

}