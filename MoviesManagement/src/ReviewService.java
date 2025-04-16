
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

// Manages the creation, editing, deletion, and sharing of movie reviews in the Movie Review Application.
public class ReviewService {
	// Defines a date format for displaying timestamps in a consistent format
	// (yyyy-MM-dd HH:mm:ss).
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Creates a new review for a movie by a user.
	// Returns true if creation succeeds, false otherwise.
	public boolean createReview(int userId, int movieId, String reviewText, int rating) {
		// Validate that review text is not null or empty.
		if (reviewText == null || reviewText.trim().isEmpty()) {
			System.out.println("Review text cannot be empty.");
			return false;
		}
		// Ensure review text does not exceed 1024 characters.
		if (reviewText.length() > 1024) {
			System.out.println("Review text exceeds 1024 characters.");
			return false;
		}
		// Validate rating is between 1 and 5 inclusive.
		if (rating < 1 || rating > 5) {
			System.out.println("Rating must be between 1 and 5.");
			return false;
		}

		// Establish database connection to perform review creation.
		try (Connection conn = DatabaseConnection.getConnection()) {
			// Verify that the movie ID exists in the database.
			if (!movieExists(conn, movieId)) {
				System.out.println("Invalid movie ID.");
				return false;
			}

			// Check if the user has already reviewed this movie to prevent duplicates.
			if (userHasReviewed(conn, userId, movieId)) {
				System.out.println("You have already reviewed this movie.");
				return false;
			}

			// SQL query to insert a new review into the reviews table.
			String sql = "INSERT INTO reviews (movie_id, review, rating, user_id) VALUES (?, ?, ?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				// Set prepared statement parameters with validated inputs.
				stmt.setInt(1, movieId);
				stmt.setString(2, reviewText.trim());
				stmt.setInt(3, rating);
				stmt.setInt(4, userId);
				// Execute insert and return true if at least one row is affected.
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			// Handle database errors and inform user.
			System.out.println("Failed to create review: " + e.getMessage());
			return false;
		}
	}

	// Edits an existing review if it belongs to the user.
	// Returns true if the update succeeds, false otherwise.
	public boolean editReview(int reviewId, int userId, String reviewText, int rating) {
		// Validate that review text is not null or empty.
		if (reviewText == null || reviewText.trim().isEmpty()) {
			System.out.println("Review text cannot be empty.");
			return false;
		}
		// Ensure review text does not exceed 1024 characters.
		if (reviewText.length() > 1024) {
			System.out.println("Review text exceeds 1024 characters.");
			return false;
		}
		// Validate rating is between 1 and 5 inclusive.
		if (rating < 1 || rating > 5) {
			System.out.println("Rating must be between 1 and 5.");
			return false;
		}

		// Connect to the database to verify and update the review.
		try (Connection conn = DatabaseConnection.getConnection()) {
			// Check if the review exists and is owned by the user.
			String checkReviewSql = "SELECT id FROM reviews WHERE id = ? AND user_id = ?";
			try (PreparedStatement checkStmt = conn.prepareStatement(checkReviewSql)) {
				checkStmt.setInt(1, reviewId);
				checkStmt.setInt(2, userId);
				try (ResultSet rs = checkStmt.executeQuery()) {
					if (!rs.next()) {
						System.out.println("Review not found or you don't have permission to edit it.");
						return false;
					}
				}
			}

			// SQL query to update the review's text and rating.
			String sql = "UPDATE reviews SET review = ?, rating = ? WHERE id = ? AND user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				// Set prepared statement parameters.
				stmt.setString(1, reviewText.trim());
				stmt.setInt(2, rating);
				stmt.setInt(3, reviewId);
				stmt.setInt(4, userId);
				// Execute update and return true if successful.
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			// Handle database errors during review update.
			System.out.println("Failed to edit review: " + e.getMessage());
			return false;
		}
	}

	// Deletes any review (admin-only functionality).
	// Returns true if deletion succeeds, false otherwise.
	public boolean deleteReviewByAdmin(int reviewId) {
		// Connect to the database to verify and delete the review.
		try (Connection conn = DatabaseConnection.getConnection()) {
			// Verify that the review exists.
			String checkSql = "SELECT id FROM reviews WHERE id = ?";
			try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
				checkStmt.setInt(1, reviewId);
				try (ResultSet rs = checkStmt.executeQuery()) {
					if (!rs.next()) {
						System.out.println("Review not found.");
						return false;
					}
				}
			}

			// SQL query to delete the review by ID.
			String sql = "DELETE FROM reviews WHERE id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				// Execute deletion and return true if successful.
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			// Handle database errors during deletion.
			System.out.println("Failed to delete review: " + e.getMessage());
			return false;
		}
	}

	// Deletes a review if it belongs to the user.
	// Returns true if deletion succeeds, false otherwise.
	public boolean deleteReview(int reviewId, int userId) {
		// Connect to the database to perform deletion.
		try (Connection conn = DatabaseConnection.getConnection()) {
			// SQL query to delete the review, ensuring user ownership.
			String sql = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				stmt.setInt(2, userId);
				// Execute deletion and check if any rows were affected.
				int rowsAffected = stmt.executeUpdate();
				if (rowsAffected == 0) {
					System.out.println("Review not found or you don't have permission to delete it.");
					return false;
				}
				return true;
			}
		} catch (SQLException e) {
			// Handle database errors during deletion.
			System.out.println("Failed to delete review: " + e.getMessage());
			return false;
		}
	}

	// Displays all reviews by a specific user.
	public void displayUserReviews(int userId) {
		// SQL query to fetch user's reviews, joining with movies table, ordered by
		// modification date.
		String sql = "SELECT r.id, r.review, r.rating, m.title, r.modified_at " +
				"FROM reviews r JOIN movies m ON r.movie_id = m.id " +
				"WHERE r.user_id = ? ORDER BY r.modified_at DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set user ID parameter for the query.
			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				// Print header for the reviews section.
				System.out.println("\n--- Your Reviews ---");
				// Track if any reviews are found.
				boolean hasReviews = false;
				// Iterate through results and display review details.
				while (rs.next()) {
					hasReviews = true;
					System.out.printf("Review ID: %d, Movie: %s, Review: %s, Rating: %d, Modified: %s%n",
							rs.getInt("id"), rs.getString("title"), rs.getString("review"),
							rs.getInt("rating"), DATE_FORMAT.format(rs.getTimestamp("modified_at")));
				}
				// Inform user if no reviews were found.
				if (!hasReviews) {
					System.out.println("No reviews found.");
				}
			}
		} catch (SQLException e) {
			// Handle database errors during review display.
			System.out.println("Failed to display reviews: " + e.getMessage());
		}
	}

	// Displays all reviews in the system.
	public void displayAllReviews() {
		// SQL query to fetch all reviews, joining with movies and users tables, ordered
		// by modification date.
		String sql = "SELECT r.id, r.review, r.rating, m.title, u.first_name, u.last_name, r.modified_at " +
				"FROM reviews r " +
				"JOIN movies m ON r.movie_id = m.id " +
				"JOIN users u ON r.user_id = u.id " +
				"ORDER BY r.modified_at DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			// Print header for the reviews section.
			System.out.println("\n--- All Reviews ---");
			// Track if any reviews are found.
			boolean hasReviews = false;
			// Iterate through results and display review details with reviewer names.
			while (rs.next()) {
				hasReviews = true;
				System.out.printf("Review ID: %d, Movie: %s, By: %s %s, Review: %s, Rating: %d, Modified: %s%n",
						rs.getInt("id"), rs.getString("title"), rs.getString("first_name"),
						rs.getString("last_name"), rs.getString("review"), rs.getInt("rating"),
						DATE_FORMAT.format(rs.getTimestamp("modified_at")));
			}
			// Inform user if no reviews were found.
			if (!hasReviews) {
				System.out.println("No reviews found.");
			}
		} catch (SQLException e) {
			// Handle database errors during review display.
			System.out.println("Failed to display reviews: " + e.getMessage());
		}
	}

	// Displays reviews shared with a specific user.
	public void displaySharedReviews(int userId) {
		// SQL query to fetch shared reviews, joining reviews, movies, users, and shares
		// tables, ordered by share date.
		String sql = "SELECT r.id, r.review, r.rating, m.title, u.first_name, u.last_name, r.modified_at " +
				"FROM reviews r " +
				"JOIN movies m ON r.movie_id = m.id " +
				"JOIN users u ON r.user_id = u.id " +
				"JOIN shares s ON r.id = s.review_id " +
				"WHERE s.user_id = ? ORDER BY s.share_date DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set user ID parameter for the query.
			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				// Print header for the shared reviews section.
				System.out.println("\n--- Reviews Shared with You ---");
				// Track if any shared reviews are found.
				boolean hasShares = false;
				// Iterate through results and display shared review details.
				while (rs.next()) {
					hasShares = true;
					System.out.printf("Review ID: %d, Movie: %s, By: %s %s, Review: %s, Rating: %d, Modified: %s%n",
							rs.getInt("id"), rs.getString("title"), rs.getString("first_name"),
							rs.getString("last_name"), rs.getString("review"), rs.getInt("rating"),
							DATE_FORMAT.format(rs.getTimestamp("modified_at")));
				}
				// Inform user if no shared reviews were found.
				if (!hasShares) {
					System.out.println("No reviews shared with you.");
				}
			}
		} catch (SQLException e) {
			// Handle database errors during shared review display.
			System.out.println("Failed to display shared reviews: " + e.getMessage());
		}
	}

	// Shares a review with another user via their email.
	// Returns true if sharing succeeds, false otherwise.
	public boolean shareReview(int reviewId, int userId, String sharedWithEmail) {
		// Validate that the recipient email is not null or empty.
		if (sharedWithEmail == null || sharedWithEmail.trim().isEmpty()) {
			System.out.println("Email cannot be empty.");
			return false;
		}

		// Connect to the database to perform sharing.
		try (Connection conn = DatabaseConnection.getConnection()) {
			// Verify that the review exists and is owned by the user.
			String checkReviewSql = "SELECT id FROM reviews WHERE id = ? AND user_id = ?";
			try (PreparedStatement checkStmt = conn.prepareStatement(checkReviewSql)) {
				checkStmt.setInt(1, reviewId);
				checkStmt.setInt(2, userId);
				try (ResultSet rs = checkStmt.executeQuery()) {
					if (!rs.next()) {
						System.out.println("Review not found or you don't own it.");
						return false;
					}
				}
			}

			// Retrieve the ID of the user with the provided email.
			String getUserSql = "SELECT id FROM users WHERE email = ?";
			int sharedWithId;
			try (PreparedStatement stmt = conn.prepareStatement(getUserSql)) {
				stmt.setString(1, sharedWithEmail.trim().toLowerCase());
				try (ResultSet rs = stmt.executeQuery()) {
					if (!rs.next()) {
						System.out.println("User with that email not found.");
						return false;
					}
					sharedWithId = rs.getInt("id");
				}
			}

			// Prevent users from sharing reviews with themselves.
			if (sharedWithId == userId) {
				System.out.println("You cannot share a review with yourself.");
				return false;
			}

			// Check if the review has already been shared with this user.
			String checkShareSql = "SELECT review_id FROM shares WHERE review_id = ? AND user_id = ?";
			try (PreparedStatement checkStmt = conn.prepareStatement(checkShareSql)) {
				checkStmt.setInt(1, reviewId);
				checkStmt.setInt(2, sharedWithId);
				try (ResultSet rs = checkStmt.executeQuery()) {
					if (rs.next()) {
						System.out.println("Review already shared with this user.");
						return false;
					}
				}
			}

			// SQL query to insert a new share record into the shares table.
			String sql = "INSERT INTO shares (review_id, user_id) VALUES (?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				stmt.setInt(2, sharedWithId);
				// Execute insert and return true if successful.
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			// Handle database errors during sharing.
			System.out.println("Failed to share review: " + e.getMessage());
			return false;
		}
	}

	// Helper method to check if a movie exists in the database.
	// Returns true if the movie ID is found, false otherwise.
	private boolean movieExists(Connection conn, int movieId) throws SQLException {
		String sql = "SELECT id FROM movies WHERE id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Helper method to check if a user has already reviewed a movie.
	// Returns true if a review exists, false otherwise.
	private boolean userHasReviewed(Connection conn, int userId, int movieId) throws SQLException {
		String sql = "SELECT id FROM reviews WHERE user_id = ? AND movie_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}
}