
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Utility class for establishing a connection to the MySQL database
public class DatabaseConnection {
	// Database connection URL for the MySQL movie_reviews_db database
	private static final String URL = "jdbc:mysql://localhost:3306/movie_reviews_db";
	// Database username (Note: Hardcoded for simplicity; should be replaced with
	// secure credentials in production)
	private static final String USER = "root";
	// Database password (Note: Hardcoded for simplicity; should be replaced with
	// secure credentials in production)
	private static final String PASSWORD = "manager";

	// Static block to load the MySQL JDBC driver when the class is initialized
	static {
		try {
			// Load the MySQL JDBC driver class to register it with the DriverManager
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// Handle the case where the JDBC driver class is not found
			e.printStackTrace();
			// Exit the application with an error code if the driver cannot be loaded
			System.exit(1);
		}
	}

	// Retrieves a connection to the MySQL database
	// Throws SQLException if the connection cannot be established
	public static Connection getConnection() throws SQLException {
		// Use DriverManager to create and return a database connection using the
		// specified URL, username, and password
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}