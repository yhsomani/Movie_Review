
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieService {
	public void displayAllMovies() {
		String sql = "SELECT id, title, rel_date, genre FROM movies ORDER BY title";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			System.out.println("\n--- All Movies ---");
			boolean hasMovies = false;
			while (rs.next()) {
				hasMovies = true;
				System.out.printf("ID: %d, Title: %s, Release Date: %s, Genre: %s%n",
						rs.getInt("id"), rs.getString("title"), rs.getString("rel_date"), rs.getString("genre"));
			}
			if (!hasMovies) {
				System.out.println("No movies found.");
			}
		} catch (SQLException e) {
			System.out.println("Failed to display movies: " + e.getMessage());
		}
	}

	public void displayMovieDetails(int movieId) {
		String sql = "SELECT m.id, m.title, m.rel_date, m.genre, r.id AS review_id, r.review, r.rating, u.first_name, u.last_name "
				+
				"FROM movies m " +
				"LEFT JOIN reviews r ON m.id = r.movie_id " +
				"LEFT JOIN users u ON r.user_id = u.id " +
				"WHERE m.id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				boolean movieFound = false;
				System.out.println("\n--- Movie Details ---");
				double avgRating = calculateAverageRating(movieId);
				while (rs.next()) {
					if (!movieFound) {
						System.out.printf("ID: %d, Title: %s, Release Date: %s, Genre: %s, Average Rating: %.1f%n",
								rs.getInt("id"), rs.getString("title"), rs.getString("rel_date"),
								rs.getString("genre"), avgRating);
						movieFound = true;
					}
					String review = rs.getString("review");
					if (review != null) {
						System.out.printf("Review ID: %d, By %s %s: %s (Rating: %d)%n",
								rs.getInt("review_id"), rs.getString("first_name"), rs.getString("last_name"),
								review, rs.getInt("rating"));
					}
				}
				if (!movieFound) {
					System.out.println("Movie not found.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Failed to display movie details: " + e.getMessage());
		}
	}

	private double calculateAverageRating(int movieId) {
		String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE movie_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next() && rs.getDouble("avg_rating") > 0) {
					return rs.getDouble("avg_rating");
				}
				return 0.0;
			}
		} catch (SQLException e) {
			System.out.println("Failed to calculate average rating: " + e.getMessage());
			return 0.0;
		}
	}
}