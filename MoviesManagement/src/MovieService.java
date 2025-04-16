
// Copyright (c) 2025. Created By Yash Somani
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Provides methods for displaying movie information from the movies and reviews tables in the Movie Review Application.
public class MovieService {

	// Displays a list of all movies in the database, sorted by title.
	public void displayAllMovies() {
		// SQL query to select movie details, ordered alphabetically by title.
		String sql = "SELECT id, title, rel_date, genre FROM movies ORDER BY title";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			// Print header for the movies section.
			System.out.println("\n--- All Movies ---");
			// Track if any movies are found.
			boolean hasMovies = false;
			// Iterate through results and display movie details.
			while (rs.next()) {
				hasMovies = true;
				System.out.printf("ID: %d, Title: %s, Release Date: %s, Genre: %s%n",
						rs.getInt("id"), rs.getString("title"), rs.getString("rel_date"), rs.getString("genre"));
			}
			// Inform user if no movies were found.
			if (!hasMovies) {
				System.out.println("No movies found.");
			}
		} catch (SQLException e) {
			// Handle database errors during movie display.
			System.out.println("Failed to display movies: " + e.getMessage());
		}
	}

	// Displays detailed information about a specific movie, including its reviews
	// and average rating.
	public void displayMovieDetails(int movieId) {
		// SQL query to fetch movie details, associated reviews, and reviewer names
		// using left joins.
		String sql = "SELECT m.id, m.title, m.rel_date, m.genre, r.id AS review_id, r.review, r.rating, u.first_name, u.last_name "
				+
				"FROM movies m " +
				"LEFT JOIN reviews r ON m.id = r.movie_id " +
				"LEFT JOIN users u ON r.user_id = u.id " +
				"WHERE m.id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set movie ID parameter for the query.
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				// Track if the movie is found.
				boolean movieFound = false;
				// Print header for the movie details section.
				System.out.println("\n--- Movie Details ---");
				// Calculate the movie's average rating.
				double avgRating = calculateAverageRating(movieId);
				// Iterate through results to display movie and review details.
				while (rs.next()) {
					if (!movieFound) {
						// Display movie details once, when first encountered.
						System.out.printf("ID: %d, Title: %s, Release Date: %s, Genre: %s, Average Rating: %.1f%n",
								rs.getInt("id"), rs.getString("title"), rs.getString("rel_date"),
								rs.getString("genre"), avgRating);
						movieFound = true;
					}
					// Display review details if a review exists.
					String review = rs.getString("review");
					if (review != null) {
						System.out.printf("Review ID: %d, By %s %s: %s (Rating: %d)%n",
								rs.getInt("review_id"), rs.getString("first_name"), rs.getString("last_name"),
								review, rs.getInt("rating"));
					}
				}
				// Inform user if the movie was not found.
				if (!movieFound) {
					System.out.println("Movie not found.");
				}
			}
		} catch (SQLException e) {
			// Handle database errors during movie details display.
			System.out.println("Failed to display movie details: " + e.getMessage());
		}
	}

	// Calculates the average rating for a movie based on its reviews.
	// Returns the average as a double, or 0.0 if no reviews exist or an error
	// occurs.
	private double calculateAverageRating(int movieId) {
		// SQL query to compute the average rating for reviews of the specified movie.
		String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE movie_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set movie ID parameter for the query.
			stmt.setInt(1, movieId);
			try (ResultSet rs = stmt.executeQuery()) {
				// Check if there is a valid average rating.
				if (rs.next() && rs.getDouble("avg_rating") > 0) {
					return rs.getDouble("avg_rating");
				}
				// Return 0.0 if no reviews are found.
				return 0.0;
			}
		} catch (SQLException e) {
			// Handle database errors during average rating calculation.
			System.out.println("Failed to calculate average rating: " + e.getMessage());
			return 0.0;
		}
	}
}