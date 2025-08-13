package com.my.myapp;

import java.io.Serializable;

public class Account implements Serializable {
	
	private int accountId;
	private String accountName;
	private String createdDate;
	private String accountType;
	private String accountPhone;
	private String accountGroup;
	private double accountBalance;
	
	// Constructor كامل
	public Account(int accountId, String accountName, String createdDate, String accountType, String accountPhone, String accountGroup , double accountBalance) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.createdDate = createdDate;
		this.accountType = accountType;
		this.accountPhone = accountPhone;
		this.accountGroup = accountGroup;
		this.accountBalance = accountBalance;
	}
	public Account(int accountId, String accountName, String createdDate, String accountType, String accountPhone, String accountGroup ) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.createdDate = createdDate;
		this.accountType = accountType;
		this.accountPhone = accountPhone;
		this.accountGroup = accountGroup;
		
	}
	
	// Constructor ثاني بدون accountId و accountGroup
	public Account(String accountName, String createdDate, String accountType, String accountPhone , double accountBalance) {
		this.accountName = accountName;
		this.createdDate = createdDate;
		this.accountType = accountType;
		this.accountPhone = accountPhone;
		this.accountBalance = accountBalance;
	}
	
	public Account() {
		
	}
	// Getters و Setters
	
	public int getAccountId() {
		return accountId;
	}
	
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getAccountType() {
		return accountType;
	}
	
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
	public String getAccountPhone() {
		return accountPhone;
	}
	
	public void setAccountPhone(String accountPhone) {
		this.accountPhone = accountPhone;
	}
	
	public String getAccountGroup() {
		return accountGroup;
	}
	
	public void setAccountGroup(String accountGroup) {
		this.accountGroup = accountGroup;
	}
	public  double getAccountBalance (){
		return accountBalance;
	}
	public void setAccountBalance(double accountBalance){
		this.accountBalance= accountBalance;
	}

	
}