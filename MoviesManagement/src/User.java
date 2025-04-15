public class User {
	private final int id;
	private String firstName;
	private String lastName;
	private String email;
	private String mobile;
	private String birthDate;
	private final String accountType;

	public User(int id, String firstName, String lastName, String email, String mobile, String birthDate,
			String accountType) {
		this.id = id;
		this.firstName = firstName != null ? firstName : "";
		this.lastName = lastName != null ? lastName : "";
		this.email = email != null ? email : "";
		this.mobile = mobile != null ? mobile : "";
		this.birthDate = birthDate != null ? birthDate : "";
		this.accountType = accountType != null ? accountType : "Regular";
	}

	public String getAccountType() {
		return accountType;
	}

	public int getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName != null ? firstName : this.firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName != null ? lastName : this.lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email != null ? email : this.email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile != null ? mobile : this.mobile;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate != null ? birthDate : this.birthDate;
	}
}