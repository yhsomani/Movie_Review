import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private User currentUser;

    public boolean register(String firstName, String lastName, String email, String mobile, String birthDate,
            String password, String accountType) {
        if (isEmpty(firstName, lastName, email, mobile, birthDate, password)) {
            System.out.println("All fields are required.");
            return false;
        }
        if (!isValidAccountType(accountType)) {
            System.out.println("Invalid account type. Must be 'Admin' or 'Regular'.");
            return false;
        }

        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return false;
        }
        if (!isValidMobile(mobile)) {
            System.out.println("Invalid mobile number format (10-15 digits or +).");
            return false;
        }
        if (!isValidPassword(password)) {
            System.out.println(
                    "Password must be at least 8 characters, with uppercase, lowercase, digit, and special character.");
            return false;
        }

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

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (emailExists(conn, email)) {
                System.out.println("Email already exists.");
                return false;
            }

            String sql = "INSERT INTO users (first_name, last_name, email, mobile, birth_date, password, account_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName.trim());
                stmt.setString(2, lastName.trim());
                stmt.setString(3, email.trim().toLowerCase());
                stmt.setString(4, mobile.trim());
                stmt.setDate(5, java.sql.Date.valueOf(parsedBirthDate));
                stmt.setString(6, password); // Store password in plain text
                stmt.setString(7, accountType);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String email, String password) {
        if (isEmpty(email, password)) {
            System.out.println("Email and password are required.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, first_name, last_name, email, mobile, birth_date, password, account_type FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (password.equals(storedPassword)) { // Compare plain text passwords
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
            System.out.println("Login failed due to a database error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int adminId, int userIdToDelete) {
        try (Connection conn = DatabaseConnection.getConnection()) {
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

            if (accountType.equals("Admin") && adminId != userIdToDelete) {
                System.out.println("Admins cannot delete other Admin accounts.");
                return false;
            }

            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userIdToDelete);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    public List<User> listAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id, first_name, last_name, email, mobile, birth_date, account_type FROM users");
                ResultSet rs = stmt.executeQuery()) {
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
            System.out.println("Failed to list users: " + e.getMessage());
        }
        return users;
    }

    public boolean updateProfile(int userId, String firstName, String lastName, String email, String mobile,
            String birthDate) {
        User user = getCurrentUser();
        if (user == null) {
            System.out.println("No user is currently logged in.");
            return false;
        }

        firstName = firstName == null || firstName.trim().isEmpty() ? user.getFirstName() : firstName.trim();
        lastName = lastName == null || lastName.trim().isEmpty() ? user.getLastName() : lastName.trim();
        email = email == null || email.trim().isEmpty() ? user.getEmail() : email.trim().toLowerCase();
        mobile = mobile == null || mobile.trim().isEmpty() ? user.getMobile() : mobile.trim();
        birthDate = birthDate == null || birthDate.trim().isEmpty() ? user.getBirthDate() : birthDate.trim();

        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return false;
        }
        if (!isValidMobile(mobile)) {
            System.out.println("Invalid mobile number format (10-15 digits or +).");
            return false;
        }
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

        try (Connection conn = DatabaseConnection.getConnection()) {
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

            String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, mobile = ?, birth_date = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, mobile);
                stmt.setDate(5, java.sql.Date.valueOf(parsedBirthDate));
                stmt.setInt(6, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Profile update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        if (!isValidPassword(newPassword)) {
            System.out.println(
                    "Password must be at least 8 characters, with uppercase, lowercase, digit, and special character.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPassword); // Store password in plain text
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Password change failed: " + e.getMessage());
            return false;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    private boolean isEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean emailExists(Connection conn, String email) throws SQLException {
        String checkEmailSql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
            checkStmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^\\+?[0-9]{10,15}$");
    }

    private boolean isValidPassword(String password) {
        return password != null
                && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    private boolean isValidAccountType(String accountType) {
        return accountType != null && (accountType.equals("Admin") || accountType.equals("Regular"));
    }
}