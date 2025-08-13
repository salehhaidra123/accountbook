package com.my.myapp;

public class AccountType {
	
	private int accountTypeId;
	private String accountType;
	
	// Constructor فارغ
	public AccountType() {
	}
	
	// Constructor بجميع الحقول
	public AccountType(int accountTypeId, String accountType) {
		this.accountTypeId = accountTypeId;
		this.accountType = accountType;
	}
	public AccountType(String accountType) {
		this.accountType = accountType;
	}
	
	// Getter و Setter لـ accountTypeId
	public int getAccountTypeId() {
		return accountTypeId;
	}
	
	public void setAccountTypeId(int accountTypeId) {
		this.accountTypeId = accountTypeId;
	}
	
	// Getter و Setter لـ accountType
	public String getAccountType() {
		return accountType;
	}
	
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
}