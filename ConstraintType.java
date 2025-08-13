package com.my.myapp;

public class ConstraintType {

	private int constraintTypeId;
	private String constraintTypeName;

	// Constructor with all fields
	public ConstraintType(int constraintTypeId, String constraintTypeName) {
		this.constraintTypeId = constraintTypeId;
		this.constraintTypeName = constraintTypeName;
	}

	public ConstraintType(String constraintTypeName) {
		this.constraintTypeName = constraintTypeName;
	}

	// Empty constructor
	public ConstraintType() {
	}

	// Getters and Setters
	public int getConstraintTypeId() {
		return constraintTypeId;
	}

	public void setConstraintTypeId(int constraintTypeId) {
		this.constraintTypeId = constraintTypeId;
	}

	public String getConstraintTypeName() {
		return constraintTypeName;
	}

	public void setConstraintTypeName(String constraintTypeName) {
		this.constraintTypeName = constraintTypeName;
	}
}