
// Copyright (c) 2025. Created By Yash Somani
import java.util.List;

// Main class that serves as the entry point for the movie review application
public class Main {
	// Instance variables for service classes, handling authentication, movies,
	// reviews, and user input
	private final AuthService authService = new AuthService(); // Manages user authentication and profiles
	private final MovieService movieService = new MovieService(); // Manages movie-related operations
	private final ReviewService reviewService = new ReviewService(); // Manages review-related operations
	private final InputHandler inputHandler = new InputHandler(); // Handles user input validation and collection

	// Main method to start the application
	public static void main(String[] args) {
		Main app = new Main(); // Create an instance of the Main class
		try {
			app.run(); // Start the application loop
		} finally {
			app.inputHandler.close(); // Ensure input resources are closed properly
		}
	}

	// Core method that runs the application, displaying menus based on user state
	private void run() {
		// Infinite loop to keep the application running until the user exits
		while (true) {
			User currentUser = authService.getCurrentUser(); // Get the currently logged-in user
			if (currentUser == null) {
				showMainMenu(); // Show the main menu for unauthenticated users
			} else if (currentUser.getAccountType().equals("Admin")) {
				showAdminMenu(); // Show the admin menu for admin users
			} else {
				showSignedInMenu(); // Show the signed-in menu for regular users
			}
		}
	}

	// Displays the admin menu with options for user management and other
	// functionalities
	private void showAdminMenu() {
		// Print the admin menu options
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
		// Get the user's menu choice, ensuring it is within the valid range
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 14);

		// Handle the user's choice using a switch statement
		switch (choice) {
			case 1:
				showUserManagementMenu(); // Navigate to the user management submenu
				break;
			case 2:
				editProfile(); // Allow admin to edit their profile
				break;
			case 3:
				changePassword(); // Allow admin to change their password
				break;
			case 4:
				displayAllMovies(); // Display all available movies
				break;
			case 5:
				createReview(); // Create a new movie review
				break;
			case 6:
				editReview(); // Edit an existing review
				break;
			case 7:
				deleteReview(); // Delete one of the admin's own reviews
				break;
			case 8:
				deleteAnyReview(); // Delete any review (admin privilege)
				break;
			case 9:
				displayAllReviews(); // Display all reviews in the system
				break;
			case 10:
				displayUserReviews(); // Display the admin's own reviews
				break;
			case 11:
				displaySharedReviews(); // Display reviews shared with the admin
				break;
			case 12:
				shareReview(); // Share a review with another user
				break;
			case 13:
				displayMovieDetails(); // Display details of a specific movie
				break;
			case 14:
				authService.logout(); // Log out the admin user
				System.out.println("Signed out successfully.");
				break;
		}
	}

	// Displays the user management submenu for admins to manage user accounts
	private void showUserManagementMenu() {
		// Loop to keep the user management menu active until the user chooses to return
		while (true) {
			// Print the user management menu options
			System.out.println("\n=== User Management Menu ===");
			System.out.println("1. Add Admin User");
			System.out.println("2. Add Regular User");
			System.out.println("3. Update Regular User");
			System.out.println("4. Delete User");
			System.out.println("5. List All Users");
			System.out.println("6. Back to Admin Menu");
			// Get the user's menu choice, ensuring it is within the valid range
			int choice = inputHandler.getIntInRange("Choose an option: ", 1, 6);

			// Handle the user's choice using a switch statement
			switch (choice) {
				case 1:
					addAdminUser(); // Create a new admin user
					break;
				case 2:
					addRegularUser(); // Create a new regular user
					break;
				case 3:
					updateRegularUser(); // Update an existing regular user's details
					break;
				case 4:
					deleteUser(); // Delete a user account
					break;
				case 5:
					listAllUsers(); // List all users in the system
					break;
				case 6:
					return; // Return to the admin menu
			}
		}
	}

	// Creates a new admin user with full privileges
	private void addAdminUser() {
		// Print instructions for creating an admin user
		System.out.println("\n=== Create Admin User ===");
		System.out.println("Admin users have full control, including user management and data access.");
		// Collect user input for admin user details
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email Address: ");
		String mobile = inputHandler.getString("Contact Number: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		// Validate that the passwords match
		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		// Register the new admin user and display the result
		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Admin")) {
			System.out.println("Admin user created successfully.");
		} else {
			System.out.println("Failed to create Admin user. Please check your inputs.");
		}
	}

	// Creates a new regular user with limited access
	private void addRegularUser() {
		// Print instructions for creating a regular user
		System.out.println("\n=== Create Regular User ===");
		System.out.println("Regular users have limited access, such as viewing resources and updating profiles.");
		// Collect user input for regular user details
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email Address: ");
		String mobile = inputHandler.getString("Contact Number: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		// Validate that the passwords match
		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		// Register the new regular user and display the result
		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Regular")) {
			System.out.println("Regular user created successfully.");
		} else {
			System.out.println("Failed to create Regular user. Please check your inputs.");
		}
	}

	// Updates the details of an existing regular user
	private void updateRegularUser() {
		// Print instructions for updating a regular user
		System.out.println("\n=== Update Regular User ===");
		String email = inputHandler.getString("Enter user’s Email Address: ");
		// Validate that the email is not empty
		if (email.isEmpty()) {
			System.out.println("Email cannot be empty.");
			return;
		}

		// Retrieve the list of all users and find the target regular user by email
		List<User> users = authService.listAllUsers();
		User targetUser = users.stream()
				.filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getAccountType().equals("Regular"))
				.findFirst()
				.orElse(null);

		// Check if the user was found
		if (targetUser == null) {
			System.out.println("Regular user with that email not found.");
			return;
		}

		// Collect updated user details, allowing blank inputs to retain current values
		System.out.println("Leave blank to keep current values.");
		String firstName = inputHandler.getString("Update First Name (" + targetUser.getFirstName() + "): ");
		String lastName = inputHandler.getString("Update Last Name (" + targetUser.getLastName() + "): ");
		String newEmail = inputHandler.getString("Update Email Address (" + targetUser.getEmail() + "): ");
		String mobile = inputHandler.getString("Update Contact Number (" + targetUser.getMobile() + "): ");
		String birthDate = inputHandler.getString("Update Birth Date (" + targetUser.getBirthDate() + "): ");
		String resetPassword = inputHandler.getString("Reset Password? (Enter new password or leave blank): ");

		// Handle password reset if a new password is provided
		if (!resetPassword.isEmpty()) {
			String confirmPassword = inputHandler.getString("Confirm New Password: ");
			if (!resetPassword.equals(confirmPassword)) {
				System.out.println("Passwords do not match.");
				return;
			}
			// Update the user's password
			if (authService.changePassword(targetUser.getId(), resetPassword)) {
				System.out.println("Password updated successfully.");
			} else {
				System.out.println("Failed to update password.");
				return;
			}
		}

		// Update the user's profile and display the result
		if (authService.updateProfile(targetUser.getId(), firstName, lastName, newEmail, mobile, birthDate)) {
			System.out.println("Regular user updated successfully.");
		} else {
			System.out.println("Failed to update Regular user. Please check your inputs.");
		}
	}

	// Deletes a user account from the system
	private void deleteUser() {
		// Print instructions for deleting a user
		System.out.println("\n=== Delete User ===");
		System.out.println("This action is irreversible and will permanently remove the user's data.");
		String email = inputHandler.getString("Enter user’s Email Address: ");
		// Validate that the email is not empty
		if (email.isEmpty()) {
			System.out.println("Email cannot be empty.");
			return;
		}

		// Retrieve the list of all users and find the target user by email
		List<User> users = authService.listAllUsers();
		User targetUser = users.stream()
				.filter(u -> u.getEmail().equalsIgnoreCase(email))
				.findFirst()
				.orElse(null);

		// Check if the user was found
		if (targetUser == null) {
			System.out.println("User with that email not found.");
			return;
		}

		// Confirm deletion with the admin
		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		// Delete the user and display the result
		if (authService.deleteUser(authService.getCurrentUser().getId(), targetUser.getId())) {
			System.out.println("User deleted successfully.");
		} else {
			System.out.println("Failed to delete user.");
		}
	}

	// Lists all users in the system
	private void listAllUsers() {
		// Print instructions for listing users
		System.out.println("\n=== List All Users ===");
		String confirm = inputHandler.getString("Display all users? (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Operation cancelled.");
			return;
		}

		// Retrieve and display the list of all users
		List<User> users = authService.listAllUsers();
		System.out.println("\n--- All Users ---");
		if (users.isEmpty()) {
			System.out.println("No users found.");
			return;
		}
		// Print each user's details
		for (User user : users) {
			System.out.printf("ID: %d, Email: %s, Name: %s %s, Type: %s%n",
					user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getAccountType());
		}
	}

	// Deletes any review in the system (admin privilege)
	private void deleteAnyReview() {
		// Print instructions for deleting a review
		System.out.println("\n=== Delete Any Review ===");
		reviewService.displayAllReviews(); // Display all reviews for reference
		int reviewId = inputHandler.getInt("Enter Review ID to delete: ");
		// Validate the review ID
		if (reviewId <= 0) {
			System.out.println("Invalid Review ID.");
			return;
		}

		// Confirm deletion with the admin
		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		// Delete the review and display the result
		if (reviewService.deleteReviewByAdmin(reviewId)) {
			System.out.println("Review deleted successfully.");
		} else {
			System.out.println("Failed to delete review. Please check the review ID.");
		}
	}

	// Displays the main menu for unauthenticated users
	private void showMainMenu() {
		// Print the main menu options
		System.out.println("\n---- MAIN MENU ----");
		System.out.println("1. Sign Up");
		System.out.println("2. Sign In");
		System.out.println("3. Exit");
		// Get the user's menu choice, ensuring it is within the valid range
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 3);

		// Handle the user's choice using a switch statement
		switch (choice) {
			case 1:
				register(); // Register a new user
				break;
			case 2:
				login(); // Log in an existing user
				break;
			case 3:
				System.out.println("Exiting...");
				System.exit(0); // Exit the application
		}
	}

	// Registers a new user (defaulting to regular user type)
	private void register() {
		// Print instructions for signing up
		System.out.println("\n=== Sign Up ===");
		// Collect user input for registration details
		String firstName = inputHandler.getString("First Name: ");
		String lastName = inputHandler.getString("Last Name: ");
		String email = inputHandler.getString("Email: ");
		String mobile = inputHandler.getString("Mobile: ");
		String birthDate = inputHandler.getString("Birth Date (YYYY-MM-DD): ");
		String password = inputHandler.getString("Password: ");
		String confirmPassword = inputHandler.getString("Confirm Password: ");

		// Validate that the passwords match
		if (!password.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		// Register the new user and display the result
		if (authService.register(firstName, lastName, email, mobile, birthDate, password, "Regular")) {
			System.out.println("Registration successful!");
		} else {
			System.out.println("Registration failed. Please check your inputs.");
		}
	}

	// Logs in an existing user
	private void login() {
		// Print instructions for signing in
		System.out.println("\n=== Sign In ===");
		String email = inputHandler.getString("Email: ");
		String password = inputHandler.getString("Password: ");

		// Attempt to log in and display the result
		if (authService.login(email, password)) {
			System.out.println("Login successful! Welcome, " + authService.getCurrentUser().getFirstName());
		}
	}

	// Displays the signed-in menu for regular users
	private void showSignedInMenu() {
		// Print the signed-in menu options
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
		// Get the user's menu choice, ensuring it is within the valid range
		int choice = inputHandler.getIntInRange("Choose an option: ", 1, 11);

		// Handle the user's choice using a switch statement
		switch (choice) {
			case 1:
				editProfile(); // Edit the user's profile
				break;
			case 2:
				changePassword(); // Change the user's password
				break;
			case 3:
				displayAllMovies(); // Display all available movies
				break;
			case 4:
				createReview(); // Create a new movie review
				break;
			case 5:
				editReview(); // Edit an existing review
				break;
			case 6:
				deleteReview(); // Delete one of the user's own reviews
				break;
			case 7:
				displayUserReviews(); // Display the user's own reviews
				break;
			case 8:
				displaySharedReviews(); // Display reviews shared with the user
				break;
			case 9:
				shareReview(); // Share a review with another user
				break;
			case 10:
				displayMovieDetails(); // Display details of a specific movie
				break;
			case 11:
				authService.logout(); // Log out the user
				System.out.println("Signed out successfully.");
				break;
		}
	}

	// Edits the profile of the currently logged-in user
	private void editProfile() {
		User user = authService.getCurrentUser(); // Get the current user
		if (user == null) {
			System.out.println("No user is logged in.");
			return;
		}

		// Print instructions for editing the profile
		System.out.println("\n=== Edit Profile ===");
		System.out.println("Leave blank to keep current values.");
		// Collect updated profile details
		String firstName = inputHandler.getString("First Name (" + user.getFirstName() + "): ");
		String lastName = inputHandler.getString("Last Name (" + user.getLastName() + "): ");
		String email = inputHandler.getString("Email (" + user.getEmail() + "): ");
		String mobile = inputHandler.getString("Mobile (" + user.getMobile() + "): ");
		String birthDate = inputHandler.getString("Birth Date (" + user.getBirthDate() + "): ");

		// Update the profile and display the result
		if (authService.updateProfile(user.getId(), firstName, lastName, email, mobile, birthDate)) {
			System.out.println("Profile updated successfully.");
		} else {
			System.out.println("Profile update failed. Please check your inputs.");
		}
	}

	// Changes the password of the currently logged-in user
	private void changePassword() {
		// Print instructions for changing the password
		System.out.println("\n=== Change Password ===");
		String newPassword = inputHandler.getString("New Password: ");
		String confirmPassword = inputHandler.getString("Confirm New Password: ");
		// Validate that the passwords match
		if (!newPassword.equals(confirmPassword)) {
			System.out.println("Passwords do not match.");
			return;
		}

		// Change the password and display the result
		if (authService.changePassword(authService.getCurrentUser().getId(), newPassword)) {
			System.out.println("Password changed successfully.");
		} else {
			System.out.println("Password change failed. Please try again.");
		}
	}

	// Displays all movies in the system
	private void displayAllMovies() {
		// Print instructions for viewing movies
		System.out.println("\n=== View All Movies ===");
		movieService.displayAllMovies(); // Call the movie service to display movies
	}

	// Creates a new movie review
	private void createReview() {
		// Print instructions for creating a review
		System.out.println("\n=== Create Review ===");
		movieService.displayAllMovies(); // Display movies for reference
		int movieId = inputHandler.getInt("Enter Movie ID to review: ");
		String reviewText = inputHandler.getString("Enter Review Text: ");
		int rating = inputHandler.getIntInRange("Enter Rating (1-5): ", 1, 5);

		// Create the review and display the result
		if (reviewService.createReview(authService.getCurrentUser().getId(), movieId, reviewText, rating)) {
			System.out.println("Review created successfully.");
		} else {
			System.out.println("Failed to create review. Please check your inputs.");
		}
	}

	// Edits an existing review
	private void editReview() {
		// Print instructions for editing a review
		System.out.println("\n=== Edit Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId()); // Display user's reviews
		int reviewId = inputHandler.getInt("Enter Review ID to edit: ");
		String reviewText = inputHandler.getString("Enter new Review Text: ");
		int rating = inputHandler.getIntInRange("Enter new Rating (1-5): ", 1, 5);

		// Edit the review and display the result
		if (reviewService.editReview(reviewId, authService.getCurrentUser().getId(), reviewText, rating)) {
			System.out.println("Review updated successfully.");
		} else {
			System.out.println("Failed to update review. Please check the review ID.");
		}
	}

	// Deletes one of the user's own reviews
	private void deleteReview() {
		// Print instructions for deleting a review
		System.out.println("\n=== Delete My Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId()); // Display user's reviews
		int reviewId = inputHandler.getInt("Enter Review ID to delete: ");

		// Confirm deletion with the user
		String confirm = inputHandler.getString("Confirm deletion (Y/N): ");
		if (!confirm.equalsIgnoreCase("Y")) {
			System.out.println("Deletion cancelled.");
			return;
		}

		// Delete the review and display the result
		if (reviewService.deleteReview(reviewId, authService.getCurrentUser().getId())) {
			System.out.println("Review deleted successfully.");
		} else {
			System.out.println("Failed to delete review. Please check the review ID.");
		}
	}

	// Displays all reviews in the system
	private void displayAllReviews() {
		// Print instructions for viewing reviews
		System.out.println("\n=== View All Reviews ===");
		reviewService.displayAllReviews(); // Call the review service to display reviews
	}

	// Displays the user's own reviews
	private void displayUserReviews() {
		// Print instructions for viewing user's reviews
		System.out.println("\n=== View My Own Reviews ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId()); // Display user's reviews
	}

	// Displays reviews shared with the user
	private void displaySharedReviews() {
		// Print instructions for viewing shared reviews
		System.out.println("\n=== View Shared Reviews ===");
		reviewService.displaySharedReviews(authService.getCurrentUser().getId()); // Display shared reviews
	}

	// Shares a review with another user
	private void shareReview() {
		// Print instructions for sharing a review
		System.out.println("\n=== Share Review ===");
		reviewService.displayUserReviews(authService.getCurrentUser().getId()); // Display user's reviews
		int reviewId = inputHandler.getInt("Enter Review ID to share: ");
		String sharedWithEmail = inputHandler.getString("Enter email of user to share with: ");

		// Share the review and display the result
		if (reviewService.shareReview(reviewId, authService.getCurrentUser().getId(), sharedWithEmail)) {
			System.out.println("Review shared successfully.");
		} else {
			System.out.println("Failed to share review. Please check the review ID or email.");
		}
	}

	// Displays details of a specific movie
	private void displayMovieDetails() {
		// Print instructions for viewing movie details
		System.out.println("\n=== View Movie Details ===");
		movieService.displayAllMovies(); // Display movies for reference
		int movieId = inputHandler.getInt("Enter Movie ID to view details: ");
		movieService.displayMovieDetails(movieId); // Display details of the selected movie
	}
}