package com.my.myapp;

package com.my.myapp.models;

public class AccountGroup {
	
	private int accountGroupId;
	private String accountGroupName;
	
	// Constructor with all fields
	public AccountGroup(int accountGroupId, String accountGroupName) {
		this.accountGroupId = accountGroupId;
		this.accountGroupName = accountGroupName;
	}
	public AccountGroup(String accountGroupName) {
		this.accountGroupName = accountGroupName;
	}
	
	// Empty constructor
	public AccountGroup() {
	}
	
	// Getters and Setters
	public int getAccountGroupId() {
		return accountGroupId;
	}
	
	public void setAccountGroupId(int accountGroupId) {
		this.accountGroupId = accountGroupId;
	}
	
	public String getAccountGroupName() {
		return accountGroupName;
	}
	
	public void setAccountGroupName(String accountGroupName) {
		this.accountGroupName = accountGroupName;
	}
}