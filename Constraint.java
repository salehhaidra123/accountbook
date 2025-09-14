package com.my.myapp;

import java.io.Serializable;

public class Constraint implements Serializable {
	
	private int id;
	private int accountId;
	private int constraintTypeId; // 🆕 النوع الجديد
	private String date;
	private String details;
	private double debit;
	private double credit;
	private Integer transferId; 
	private String transferGroupId;
	
	public Constraint() {
	}
	
	// Constructor with all fields (used for reading from DB)
	public Constraint(int constId, int accountId, int constraintTypeId, String date, String details, double debit, double credit) {
		this.id = constId;
		this.accountId = accountId;
		this.constraintTypeId = constraintTypeId;
		this.date = date;
		this.details = details;
		this.debit = debit;
		this.credit = credit;
		this.transferId = null;
	}
	
	// Constructor without ID (used when adding a new constraint)
	public Constraint(int accountId, int constraintTypeId, String date, String details, double debit, double credit) {
		this.accountId = accountId;
		this.constraintTypeId = constraintTypeId;
		this.date = date;
		this.details = details;
		this.debit = debit;
		this.credit = credit;
		this.transferId = null;
	}
	
	// Getters
	public int getId() {
		return id;
	}
	
	public int getAccountId() {
		return accountId;
	}
	
	public int getConstraintTypeId() {
		return constraintTypeId;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getDetails() {
		return details;
	}
	
	public double getDebit() {
		return debit;
	}
	
	public double getCredit() {
		return credit;
	}
	
	// Setters
	public void setId(int id) {
		this.id = id;
	}
	
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	
	public void setConstraintTypeId(int constraintTypeId) {
		this.constraintTypeId = constraintTypeId;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public void setDebit(double debit) {
		this.debit = debit;
	}
	
	public void setCredit(double credit) {
		this.credit = credit;
	}
	public Integer getTransferId() {
        return transferId;
    }
    
    public void setTransferId(Integer transferId) {
        this.transferId = transferId;
    }
	public String getTransferGroupId() {
        return transferGroupId;
    }
    
    public void setTransferGroupId(String transferGroupId) {
        this.transferGroupId = transferGroupId;
    }
}