// Copyright (c) 2025. Created By Yash Somani

// Represents a user in the Movie Review Application with attributes and methods for managing user data.
public class User {
	// Unique identifier for the user; immutable once set.
	private final int id;
	// User's first name; mutable via setter.
	private String firstName;
	// User's last name; mutable via setter.
	private String lastName;
	// User's email address; mutable via setter.
	private String email;
	// User's mobile phone number; mutable via setter.
	private String mobile;
	// User's birth date; mutable via setter.
	private String birthDate;
	// User's account type ("Admin" or "Regular"); immutable once set.
	private final String accountType;

	// Constructs a new User object with the provided attributes.
	// Handles null inputs by assigning default values.
	public User(int id, String firstName, String lastName, String email, String mobile, String birthDate,
			String accountType) {
		// Assign the immutable ID.
		this.id = id;
		// Set firstName to empty string if null to avoid null references.
		this.firstName = firstName != null ? firstName : "";
		// Set lastName to empty string if null.
		this.lastName = lastName != null ? lastName : "";
		// Set email to empty string if null.
		this.email = email != null ? email : "";
		// Set mobile to empty string if null.
		this.mobile = mobile != null ? mobile : "";
		// Set birthDate to empty string if null.
		this.birthDate = birthDate != null ? birthDate : "";
		// Set accountType to "Regular" if null to ensure valid default.
		this.accountType = accountType != null ? accountType : "Regular";
	}

	// Returns the user's account type.
	// No setter provided as accountType is immutable.
	public String getAccountType() {
		return accountType;
	}

	// Returns the user's unique ID.
	// No setter provided as ID is immutable.
	public int getId() {
		return id;
	}

	// Returns the user's first name.
	public String getFirstName() {
		return firstName;
	}

	// Updates the user's first name.
	// Retains current value if input is null to prevent accidental nullification.
	public void setFirstName(String firstName) {
		this.firstName = firstName != null ? firstName : this.firstName;
	}

	// Returns the user's last name.
	public String getLastName() {
		return lastName;
	}

	// Updates the user's last name.
	// Retains current value if input is null.
	public void setLastName(String lastName) {
		this.lastName = lastName != null ? lastName : this.lastName;
	}

	// Returns the user's email address.
	public String getEmail() {
		return email;
	}

	// Updates the user's email address.
	// Retains current value if input is null.
	public void setEmail(String email) {
		this.email = email != null ? email : this.email;
	}

	// Returns the user's mobile phone number.
	public String getMobile() {
		return mobile;
	}

	// Updates the user's mobile phone number.
	// Retains current value if input is null.
	public void setMobile(String mobile) {
		this.mobile = mobile != null ? mobile : this.mobile;
	}

	// Returns the user's birth date.
	public String getBirthDate() {
		return birthDate;
	}

	// Updates the user's birth date.
	// Retains current value if input is null.
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate != null ? birthDate : this.birthDate;
	}
}