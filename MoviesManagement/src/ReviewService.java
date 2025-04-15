
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class ReviewService {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public boolean createReview(int userId, int movieId, String reviewText, int rating) {
		if (reviewText == null || reviewText.trim().isEmpty()) {
			System.out.println("Review text cannot be empty.");
			return false;
		}
		if (reviewText.length() > 1024) {
			System.out.println("Review text exceeds 1024 characters.");
			return false;
		}
		if (rating < 1 || rating > 5) {
			System.out.println("Rating must be between 1 and 5.");
			return false;
		}

		try (Connection conn = DatabaseConnection.getConnection()) {
			if (!movieExists(conn, movieId)) {
				System.out.println("Invalid movie ID.");
				return false;
			}

			if (userHasReviewed(conn, userId, movieId)) {
				System.out.println("You have already reviewed this movie.");
				return false;
			}

			String sql = "INSERT INTO reviews (movie_id, review, rating, user_id) VALUES (?, ?, ?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, movieId);
				stmt.setString(2, reviewText.trim());
				stmt.setInt(3, rating);
				stmt.setInt(4, userId);
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			System.out.println("Failed to create review: " + e.getMessage());
			return false;
		}
	}

	public boolean editReview(int reviewId, int userId, String reviewText, int rating) {
		if (reviewText == null || reviewText.trim().isEmpty()) {
			System.out.println("Review text cannot be empty.");
			return false;
		}
		if (reviewText.length() > 1024) {
			System.out.println("Review text exceeds 1024 characters.");
			return false;
		}
		if (rating < 1 || rating > 5) {
			System.out.println("Rating must be between 1 and 5.");
			return false;
		}

		try (Connection conn = DatabaseConnection.getConnection()) {
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

			String sql = "UPDATE reviews SET review = ?, rating = ? WHERE id = ? AND user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, reviewText.trim());
				stmt.setInt(2, rating);
				stmt.setInt(3, reviewId);
				stmt.setInt(4, userId);
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			System.out.println("Failed to edit review: " + e.getMessage());
			return false;
		}
	}

	public boolean deleteReviewByAdmin(int reviewId) {
		try (Connection conn = DatabaseConnection.getConnection()) {
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

			String sql = "DELETE FROM reviews WHERE id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			System.out.println("Failed to delete review: " + e.getMessage());
			return false;
		}
	}

	public boolean deleteReview(int reviewId, int userId) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				stmt.setInt(2, userId);
				int rowsAffected = stmt.executeUpdate();
				if (rowsAffected == 0) {
					System.out.println("Review not found or you don't have permission to delete it.");
					return false;
				}
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Failed to delete review: " + e.getMessage());
			return false;
		}
	}

	public void displayUserReviews(int userId) {
		String sql = "SELECT r.id, r.review, r.rating, m.title, r.modified_at " +
				"FROM reviews r JOIN movies m ON r.movie_id = m.id " +
				"WHERE r.user_id = ? ORDER BY r.modified_at DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				System.out.println("\n--- Your Reviews ---");
				boolean hasReviews = false;
				while (rs.next()) {
					hasReviews = true;
					System.out.printf("Review ID: %d, Movie: %s, Review: %s, Rating: %d, Modified: %s%n",
							rs.getInt("id"), rs.getString("title"), rs.getString("review"),
							rs.getInt("rating"), DATE_FORMAT.format(rs.getTimestamp("modified_at")));
				}
				if (!hasReviews) {
					System.out.println("No reviews found.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Failed to display reviews: " + e.getMessage());
		}
	}

	public void displayAllReviews() {
		String sql = "SELECT r.id, r.review, r.rating, m.title, u.first_name, u.last_name, r.modified_at " +
				"FROM reviews r " +
				"JOIN movies m ON r.movie_id = m.id " +
				"JOIN users u ON r.user_id = u.id " +
				"ORDER BY r.modified_at DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			System.out.println("\n--- All Reviews ---");
			boolean hasReviews = false;
			while (rs.next()) {
				hasReviews = true;
				System.out.printf("Review ID: %d, Movie: %s, By: %s %s, Review: %s, Rating: %d, Modified: %s%n",
						rs.getInt("id"), rs.getString("title"), rs.getString("first_name"),
						rs.getString("last_name"), rs.getString("review"), rs.getInt("rating"),
						DATE_FORMAT.format(rs.getTimestamp("modified_at")));
			}
			if (!hasReviews) {
				System.out.println("No reviews found.");
			}
		} catch (SQLException e) {
			System.out.println("Failed to display reviews: " + e.getMessage());
		}
	}

	public void displaySharedReviews(int userId) {
		String sql = "SELECT r.id, r.review, r.rating, m.title, u.first_name, u.last_name, r.modified_at " +
				"FROM reviews r " +
				"JOIN movies m ON r.movie_id = m.id " +
				"JOIN users u ON r.user_id = u.id " +
				"JOIN shares s ON r.id = s.review_id " +
				"WHERE s.user_id = ? ORDER BY s.share_date DESC";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				System.out.println("\n--- Reviews Shared with You ---");
				boolean hasShares = false;
				while (rs.next()) {
					hasShares = true;
					System.out.printf("Review ID: %d, Movie: %s, By: %s %s, Review: %s, Rating: %d, Modified: %s%n",
							rs.getInt("id"), rs.getString("title"), rs.getString("first_name"),
							rs.getString("last_name"), rs.getString("review"), rs.getInt("rating"),
							DATE_FORMAT.format(rs.getTimestamp("modified_at")));
				}
				if (!hasShares) {
					System.out.println("No reviews shared with you.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Failed to display shared reviews: " + e.getMessage());
		}
	}

	public boolean shareReview(int reviewId, int userId, String sharedWithEmail) {
		if (sharedWithEmail == null || sharedWithEmail.trim().isEmpty()) {
			System.out.println("Email cannot be empty.");
			return false;
		}

		try (Connection conn = DatabaseConnection.getConnection()) {
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

			if (sharedWithId == userId) {
				System.out.println("You cannot share a review with yourself.");
				return false;
			}

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

			String sql = "INSERT INTO shares (review_id, user_id) VALUES (?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, reviewId);
				stmt.setInt(2, sharedWithId);
				return stmt.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			System.out.println("Failed to share review: " + e.getMessage());
			return false;
		}
	}

	private boolean movieExists(Connection conn, int movieId) throws SQLException {
		String sql = "SELECT id FROM movies WHERE id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

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