
// Copyright (c) 2025. Created By Yash Somani
import java.util.List;

public class Main {
	private final AuthService authService = new AuthService();
	private final MovieService movieService = new MovieService();
	private final ReviewService reviewService = new ReviewService();
	private final InputHandler inputHandler = new InputHandler();

	public static void main(String[] args) {
		Main app = new Main();
		try {
			app.run();
		} finally {
			app.inputHandler.close();
		}
	}

	private void run() {
		while (true) {
			User currentUser = authService.getCurrentUser();
			if (currentUser == null) {
				showMainMenu();
			} else if (currentUser.getAccountType().equals("Admin")) {
				showAdminMenu();
			} else {
				showSignedInMenu();
			}
		}
	}

	private void showAdminMenu() {
		System.out.println("\n--- ADMIN MENU ---");
		System.out.println("1. User Management");
		System.out.println("2. Edit Profile");
		System.out.println("3. Change Password");
		System.out.println("4. View All Movies");
		System.out.println("5. Create a Review");
		System.out.println("6. Edit a Review");
		System.out.println("7. Delete My Review");
		System.out.println("8. Delete Any Review");
		System.out.println("9. View All Reviews");
		System.out.println("10. View My Own Reviews");
		System.out.println("11. View Shared Reviews");
		System.out.println("12. Share a Review");
		System.out.println("13. View Movie Details");
		System.out.println("14. Sign Out");
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 14);

		switch (choice) {
			case 1:
				showUserManagementMenu();
				break;
			case 2:
				editProfile();
				break;
			case 3:
				changePassword();
				break;
			case 4:
				displayAllMovies();
				break;
			case 5:
				createReview();
				break;
			case 6:
				editReview();
				break;
			case 7:
				deleteReview();
				break;
			case 8:
				deleteAnyReview();
				break;
			case 9:
				displayAllReviews();
				break;
			case 10:
				displayUserReviews();
				break;
			case 11:
				displaySharedReviews();
				break;
			case 12:
				shareReview();
				break;
			case 13:
				displayMovieDetails();
				break;
			case 14: {
				authService.logout();
				System.out.println("Signed out successfully.");
			}
		}
	}

	private void showUserManagementMenu() {
		while (true) {
			System.out.println("\n=== User Management Menu ===");
			System.out.println("1. Add Admin User");
			System.out.println("2. Add Regular User");
			System.out.println("3. Update Regular User");
			System.out.println("4. Delete User");
			System.out.println("5. List All Users");
			System.out.println("6. Back to Admin Menu");
			int choice = inputHandler.getIntInRange("Choose an option: ", 1, 6);

			switch (choice) {
				case 1:
					addAdminUser();
					break;
				case 2:
					addRegularUser();
					break;
				case 3:
					updateRegularUser();
					break;
				case 4:
					deleteUser();
					break;
				case 5:
					listAllUsers();
					break;
				case 6: {
					return;
				}
			}
		}
	}

	private void addAdminUser() {
		System.out.println("\n=== Create Admin User ===");
		System.out.println("Admin users have full control, including user management and data access.");
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email Address: ");
		String mobile = inputHandler.getString("Contact Number: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Admin")) {
			System.out.println("Admin user created successfully.");
		} else {
			System.out.println("Failed to create Admin user. Please check your inputs.");
		}
	}

	private void addRegularUser() {
		System.out.println("\n=== Create Regular User ===");
		System.out.println("Regular users have limited access, such as viewing resources and updating profiles.");
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email Address: ");
		String mobile = inputHandler.getString("Contact Number: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Regular")) {
			System.out.println("Regular user created successfully.");
		} else {
			System.out.println("Failed to create Regular user. Please check your inputs.");
		}
	}

	private void updateRegularUser() {
		System.out.println("\n=== Update Regular User ===");
		String email = inputHandler.getString("Enter user’s Email Address: ");
		if (email.isEmpty()) {
			System.out.println("Email cannot be empty.");
			return;
		}

		List<User> users = authService.listAllUsers();
		User targetUser = users.stream()
				.filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getAccountType().equals("Regular"))
				.findFirst()
				.orElse(null);

		if (targetUser == null) {
			System.out.println("Regular user with that email not found.");
			return;
		}

		System.out.println("Leave blank to keep current values.");
		String firstName = inputHandler.getString("Update First Name (" + targetUser.getFirstName() + "): ");
		String lastName = inputHandler.getString("Update Last Name (" + targetUser.getLastName() + "): ");
		String newEmail = inputHandler.getString("Update Email Address (" + targetUser.getEmail() + "): ");
		String mobile = inputHandler.getString("Update Contact Number (" + targetUser.getMobile() + "): ");
		String birthDate = inputHandler.getString("Update Birth Date (" + targetUser.getBirthDate() + "): ");
		String resetPassword = inputHandler.getString("Reset Password? (Enter new password or leave blank): ");

		if (!resetPassword.isEmpty()) {
			String confirmPassword = inputHandler.getString("Confirm New Password: ");
			if (!resetPassword.equals(confirmPassword)) {
				System.out.println("Passwords do not match.");
				return;
			}
			if (authService.changePassword(targetUser.getId(), resetPassword)) {
				System.out.println("Password updated successfully.");
			} else {
				System.out.println("Failed to update password.");
				return;
			}
		}

		if (authService.updateProfile(targetUser.getId(), firstName, lastName, newEmail, mobile, birthDate)) {
			System.out.println("Regular user updated successfully.");
		} else {
			System.out.println("Failed to update Regular user. Please check your inputs.");
		}
	}

	private void deleteUser() {
		System.out.println("\n=== Delete User ===");
		System.out.println("This action is irreversible and will permanently remove the user's data.");
		String email = inputHandler.getString("Enter user’s Email Address: ");
		if (email.isEmpty()) {
			System.out.println("Email cannot be empty.");
			return;
		}

		List<User> users = authService.listAllUsers();
		User targetUser = users.stream()
				.filter(u -> u.getEmail().equalsIgnoreCase(email))
				.findFirst()
				.orElse(null);

		if (targetUser == null) {
			System.out.println("User with that email not found.");
			return;
		}

		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		if (authService.deleteUser(authService.getCurrentUser().getId(), targetUser.getId())) {
			System.out.println("User deleted successfully.");
		} else {
			System.out.println("Failed to delete user.");
		}
	}

	private void listAllUsers() {
		System.out.println("\n=== List All Users ===");
		String confirm = inputHandler.getString("Display all users? (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Operation cancelled.");
			return;
		}

		List<User> users = authService.listAllUsers();
		System.out.println("\n--- All Users ---");
		if (users.isEmpty()) {
			System.out.println("No users found.");
			return;
		}
		for (User user : users) {
			System.out.printf("ID: %d, Email: %s, Name: %s %s, Type: %s%n",
					user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getAccountType());
		}
	}

	private void deleteAnyReview() {
		System.out.println("\n=== Delete Any Review ===");
		reviewService.displayAllReviews();
		int reviewId = inputHandler.getInt("Enter Review ID to delete: ");
		if (reviewId <= 0) {
			System.out.println("Invalid Review ID.");
			return;
		}

		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		if (reviewService.deleteReviewByAdmin(reviewId)) {
			System.out.println("Review deleted successfully.");
		} else {
			System.out.println("Failed to delete review. Please check the review ID.");
		}
	}

	private void showMainMenu() {
		System.out.println("\n---- MAIN MENU ----");
		System.out.println("1. Sign Up");
		System.out.println("2. Sign In");
		System.out.println("3. Exit");
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 3);

		switch (choice) {
			case 1:
				register();
				break;
			case 2:
				login();
				break;
			case 3: {
				System.out.println("Exiting...");
				System.exit(0);
			}
		}
	}

	private void register() {
		System.out.println("\n=== Sign Up ===");
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email: ");
		String mobile = inputHandler.getString("Mobile: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Regular")) {
			System.out.println("Registration successful!");
		} else {
			System.out.println("Registration failed. Please check your inputs.");
		}
	}

	private void login() {
		System.out.println("\n=== Sign In ===");
		String email = inputHandler.getString("Email: ");
		String password = inputHandler.getString("Password: ");

		if (authService.login(email, password)) {
			System.out.println("Login successful! Welcome, " + authService.getCurrentUser().getFirstName());
		}
	}

	private void showSignedInMenu() {
		System.out.println("\n--- SIGNED-IN MENU ---");
		System.out.println("1. Edit Profile");
		System.out.println("2. Change Password");
		System.out.println("3. View All Movies");
		System.out.println("4. Create a Review");
		System.out.println("5. Edit a Review");
		System.out.println("6. Delete My Review");
		System.out.println("7. View My Own Reviews");
		System.out.println("8. View Shared Reviews");
		System.out.println("9. Share a Review");
		System.out.println("10. View Movie Details");
		System.out.println("11. Sign Out");
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 11);

		switch (choice) {
			case 1:
				editProfile();
				break;
			case 2:
				changePassword();
				break;
			case 3:
				displayAllMovies();
				break;
			case 4:
				createReview();
				break;
			case 5:
				editReview();
				break;
			case 6:
				deleteReview();
				break;
			case 7:
				displayUserReviews();
				break;
			case 8:
				displaySharedReviews();
				break;
			case 9:
				shareReview();
				break;
			case 10:
				displayMovieDetails();
				break;
			case 11: {
				authService.logout();
				System.out.println("Signed out successfully.");
			}
		}
	}

	private void editProfile() {
		User user = authService.getCurrentUser();
		if (user == null) {
			System.out.println("No user is logged in.");
			return;
		}

		System.out.println("\n=== Edit Profile ===");
		System.out.println("Leave blank to keep current values.");
		String firstName = inputHandler.getString("First Name (" + user.getFirstName() + "): ");
		String lastName = inputHandler.getString("Last Name (" + user.getLastName() + "): ");
		String email = inputHandler.getString("Email (" + user.getEmail() + "): ");
		String mobile = inputHandler.getString("Mobile (" + user.getMobile() + "): ");
		String birthDate = inputHandler.getString("Birth Date (" + user.getBirthDate() + "): ");

		if (authService.updateProfile(user.getId(), firstName, lastName, email, mobile, birthDate)) {
			System.out.println("Profile updated successfully.");
		} else {
			System.out.println("Profile update failed. Please check your inputs.");
		}
	}

	private void changePassword() {
		System.out.println("\n=== Change Password ===");
		String newPassword = inputHandler.getString("New Password: ");
		String confirmPassword = inputHandler.getString("Confirm New Password: ");
		if (!newPassword.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		if (authService.changePassword(authService.getCurrentUser().getId(), newPassword)) {
			System.out.println("Password changed successfully.");
		} else {
			System.out.println("Password change failed. Please try again.");
		}
	}

	private void displayAllMovies() {
		System.out.println("\n=== View All Movies ===");
		movieService.displayAllMovies();
	}

	private void createReview() {
		System.out.println("\n=== Create Review ===");
		movieService.displayAllMovies();
		int movieId = inputHandler.getInt("Enter Movie ID to review: ");
		String reviewText = inputHandler.getString("Enter Review Text: ");
		int rating = inputHandler.getIntInRange("Enter Rating (1-5): ", 1, 5);

		if (reviewService.createReview(authService.getCurrentUser().getId(), movieId, reviewText, rating)) {
			System.out.println("Review created successfully.");
		} else {
			System.out.println("Failed to create review. Please check your inputs.");
		}
	}

	private void editReview() {
		System.out.println("\n=== Edit Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId());
		int reviewId = inputHandler.getInt("Enter Review ID to edit: ");
		String reviewText = inputHandler.getString("Enter new Review Text: ");
		int rating = inputHandler.getIntInRange("Enter new Rating (1-5): ", 1, 5);

		if (reviewService.editReview(reviewId, authService.getCurrentUser().getId(), reviewText, rating)) {
			System.out.println("Review updated successfully.");
		} else {
			System.out.println("Failed to update review. Please check the review ID.");
		}
	}

	private void deleteReview() {
		System.out.println("\n=== Delete My Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId());
		int reviewId = inputHandler.getInt("Enter Review ID to delete: ");

		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		if (reviewService.deleteReview(reviewId, authService.getCurrentUser().getId())) {
			System.out.println("Review deleted successfully.");
		} else {
			System.out.println("Failed to delete review. Please check the review ID.");
		}
	}

	private void displayAllReviews() {
		System.out.println("\n=== View All Reviews ===");
		reviewService.displayAllReviews();
	}

	private void displayUserReviews() {
		System.out.println("\n=== View My Own Reviews ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId());
	}

	private void displaySharedReviews() {
		System.out.println("\n=== View Shared Reviews ===");
		reviewService.displaySharedReviews(authService.getCurrentUser().getId());
	}

	private void shareReview() {
		System.out.println("\n=== Share Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId());
		int reviewId = inputHandler.getInt("Enter Review ID to share: ");
		String sharedWithEmail = inputHandler.getString("Enter email of user to share with: ");

		if (reviewService.shareReview(reviewId, authService.getCurrentUser().getId(), sharedWithEmail)) {
			System.out.println("Review shared successfully.");
		} else {
			System.out.println("Failed to share review. Please check the review ID or email.");
		}
	}

	private void displayMovieDetails() {
		System.out.println("\n=== View Movie Details ===");
		movieService.displayAllMovies();
		int movieId = inputHandler.getInt("Enter Movie ID to view details: ");
		movieService.displayMovieDetails(movieId);
	}
}