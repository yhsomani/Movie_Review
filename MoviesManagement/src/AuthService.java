
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Manages user authentication, registration, and profile operations for the Movie Review Application.
public class AuthService {
    // Stores the currently logged-in user; null if no user is logged in.
    private User currentUser;

    // Registers a new user and stores their details in the users table.
    // Returns true if registration succeeds, false otherwise.
    public boolean register(String firstName, String lastName, String email, String mobile, String birthDate,
            String password, String accountType) {
        // Check if any required field is null or empty.
        if (isEmpty(firstName, lastName, email, mobile, birthDate, password)) {
            System.out.println("All fields are required.");
            return false;
        }
        // Validate account type (must be "Admin" or "Regular").
        if (!isValidAccountType(accountType)) {
            System.out.println("Invalid account type. Must be 'Admin' or 'Regular'.");
            return false;
        }

        // Validate email format using regex.
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return false;
        }
        // Validate mobile number format (10-15 digits, optional + prefix).
        if (!isValidMobile(mobile)) {
            System.out.println("Invalid mobile number format (10-15 digits or +).");
            return false;
        }
        // Validate password (8+ chars, mixed case, digit, special char).
        if (!isValidPassword(password)) {
            System.out.println(
                    "Password must be at least 8 characters, with uppercase, lowercase, digit, and special character.");
            return false;
        }

        // Validate and parse birth date; ensure user is at least 13 years old.
        LocalDate parsedBirthDate;
        try {
            parsedBirthDate = LocalDate.parse(birthDate);
            if (parsedBirthDate.isAfter(LocalDate.now().minusYears(13))) {
                System.out.println("You must be at least 13 years old.");
                return false;
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid birth date format (use YYYY-MM-DD).");
            return false;
        }

        // Establish database connection and perform registration.
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if email already exists to prevent duplicates.
            if (emailExists(conn, email)) {
                System.out.println("Email already exists.");
                return false;
            }

            // SQL query to insert new user into the users table.
            String sql = "INSERT INTO users (first_name, last_name, email, mobile, birth_date, password, account_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set prepared statement parameters with trimmed inputs.
                stmt.setString(1, firstName.trim());
                stmt.setString(2, lastName.trim());
                stmt.setString(3, email.trim().toLowerCase());
                stmt.setString(4, mobile.trim());
                stmt.setDate(5, java.sql.Date.valueOf(parsedBirthDate));
                stmt.setString(6, password); // Store password in plain text (insecure).
                stmt.setString(7, accountType);
                // Execute update and return true if at least one row is affected.
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // Handle database errors and inform user.
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    // Authenticates a user and sets currentUser if successful.
    // Returns true if login succeeds, false otherwise.
    public boolean login(String email, String password) {
        // Ensure email and password are provided.
        if (isEmpty(email, password)) {
            System.out.println("Email and password are required.");
            return false;
        }

        // Connect to the database to verify credentials.
        try (Connection conn = DatabaseConnection.getConnection()) {
            // SQL query to retrieve user details by email.
            String sql = "SELECT id, first_name, last_name, email, mobile, birth_date, password, account_type FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Compare provided password with stored password.
                        String storedPassword = rs.getString("password");
                        if (password.equals(storedPassword)) { // Plain-text comparison (insecure).
                            // Create User object and set as currentUser.
                            currentUser = new User(
                                    rs.getInt("id"),
                                    rs.getString("first_name"),
                                    rs.getString("last_name"),
                                    rs.getString("email"),
                                    rs.getString("mobile"),
                                    rs.getDate("birth_date").toString(),
                                    rs.getString("account_type"));
                            return true;
                        } else {
                            System.out.println("Invalid password.");
                            return false;
                        }
                    } else {
                        System.out.println("Email not found.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            // Handle database errors during login.
            System.out.println("Login failed due to a database error: " + e.getMessage());
            return false;
        }
    }

    // Allows an admin to delete a user, with restrictions on deleting other admins.
    // Returns true if deletion succeeds, false otherwise.
    public boolean deleteUser(int adminId, int userIdToDelete) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check the account type of the user to be deleted.
            String checkSql = "SELECT account_type FROM users WHERE id = ?";
            String accountType;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userIdToDelete);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("User not found.");
                        return false;
                    }
                    accountType = rs.getString("account_type");
                }
            }

            // Prevent admins from deleting other admins unless it's themselves.
            if (accountType.equals("Admin") && adminId != userIdToDelete) {
                System.out.println("Admins cannot delete other Admin accounts.");
                return false;
            }

            // SQL query to delete the user by ID.
            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userIdToDelete);
                // Deletion cascades to related tables (e.g., reviews, shares).
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // Handle database errors during deletion.
            System.out.println("Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    // Retrieves a list of all users from the users table.
    // Returns a List<User> containing all user objects.
    public List<User> listAllUsers() {
        // Initialize an empty list to store users.
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id, first_name, last_name, email, mobile, birth_date, account_type FROM users");
                ResultSet rs = stmt.executeQuery()) {
            // Iterate through result set and create User objects.
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("mobile"),
                        rs.getDate("birth_date").toString(),
                        rs.getString("account_type")));
            }
        } catch (SQLException e) {
            // Print error but return empty list to avoid null returns.
            System.out.println("Failed to list users: " + e.getMessage());
        }
        return users;
    }

    // Updates the profile of the specified user.
    // Returns true if the update succeeds, false otherwise.
    public boolean updateProfile(int userId, String firstName, String lastName, String email, String mobile,
            String birthDate) {
        // Ensure a user is logged in.
        User user = getCurrentUser();
        if (user == null) {
            System.out.println("No user is currently logged in.");
            return false;
        }

        // Use existing values if new ones are null or empty.
        firstName = firstName == null || firstName.trim().isEmpty() ? user.getFirstName() : firstName.trim();
        lastName = lastName == null || lastName.trim().isEmpty() ? user.getLastName() : lastName.trim();
        email = email == null || email.trim().isEmpty() ? user.getEmail() : email.trim().toLowerCase();
        mobile = mobile == null || mobile.trim().isEmpty() ? user.getMobile() : mobile.trim();
        birthDate = birthDate == null || birthDate.trim().isEmpty() ? user.getBirthDate() : birthDate.trim();

        // Validate updated email format.
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return false;
        }
        // Validate updated mobile number format.
        if (!isValidMobile(mobile)) {
            System.out.println("Invalid mobile number format (10-15 digits or +).");
            return false;
        }
        // Validate and parse updated birth date; ensure age ≥ 13.
        LocalDate parsedBirthDate;
        try {
            parsedBirthDate = LocalDate.parse(birthDate);
            if (parsedBirthDate.isAfter(LocalDate.now().minusYears(13))) {
                System.out.println("You must be at least 13 years old.");
                return false;
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid birth date format (use YYYY-MM-DD).");
            return false;
        }

        // Connect to database to update user profile.
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if the new email is already used by another user.
            String checkEmailSql = "SELECT email FROM users WHERE email = ? AND id != ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
                checkStmt.setString(1, email);
                checkStmt.setInt(2, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Email already in use by another user.");
                        return false;
                    }
                }
            }

            // SQL query to update user details.
            String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, mobile = ?, birth_date = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set prepared statement parameters.
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, mobile);
                stmt.setDate(5, java.sql.Date.valueOf(parsedBirthDate));
                stmt.setInt(6, userId);
                // Execute update and return true if successful.
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // Handle database errors during profile update.
            System.out.println("Profile update failed: " + e.getMessage());
            return false;
        }
    }

    // Changes the password for the specified user.
    // Returns true if the update succeeds, false otherwise.
    public boolean changePassword(int userId, String newPassword) {
        // Ensure new password is not null or empty.
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        // Validate new password format.
        if (!isValidPassword(newPassword)) {
            System.out.println(
                    "Password must be at least 8 characters, with uppercase, lowercase, digit, and special character.");
            return false;
        }

        // Connect to database to update password.
        try (Connection conn = DatabaseConnection.getConnection()) {
            // SQL query to update password for the user.
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPassword); // Store password in plain text (insecure).
                stmt.setInt(2, userId);
                // Execute update and return true if successful.
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // Handle database errors during password change.
            System.out.println("Password change failed: " + e.getMessage());
            return false;
        }
    }

    // Returns the currently logged-in user.
    // Returns null if no user is logged in.
    public User getCurrentUser() {
        return currentUser;
    }

    // Logs out the current user by clearing currentUser.
    public void logout() {
        currentUser = null;
    }

    // Helper method to check if any provided fields are null or empty.
    // Returns true if any field is invalid, false otherwise.
    private boolean isEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // Checks if the given email already exists in the database.
    // Returns true if the email is found, false otherwise.
    private boolean emailExists(Connection conn, String email) throws SQLException {
        String checkEmailSql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
            checkStmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Validates email format using a regex pattern.
    // Returns true if valid, false otherwise.
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    // Validates mobile number format (10-15 digits, optional + prefix).
    // Returns true if valid, false otherwise.
    private boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^\\+?[0-9]{10,15}$");
    }

    // Validates password (8+ chars, mixed case, digit, special char).
    // Returns true if valid, false otherwise.
    private boolean isValidPassword(String password) {
        return password != null
                && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    // Validates account type (must be "Admin" or "Regular").
    // Returns true if valid, false otherwise.
    private boolean isValidAccountType(String accountType) {
        return accountType != null && (accountType.equals("Admin") || accountType.equals("Regular"));
    }
}