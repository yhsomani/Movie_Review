-- Create and use the database
CREATE DATABASE IF NOT EXISTS movie_reviews_db;
USE movie_reviews_db;

-- Drop tables in reverse order to avoid foreign key constraints
DROP TABLE IF EXISTS shares;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    birth_date DATE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Store plain text passwords
    account_type ENUM('Admin', 'Regular') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample users with plain text passwords
INSERT INTO users (first_name, last_name, email, mobile, birth_date, password, account_type) VALUES
('John', 'Doe', 'john.doe@example.com', '+12025550123', '1990-05-15', 'Password123!', 'Admin'),
('Jane', 'Smith', 'jane.smith@example.com', '+12025550124', '1985-08-22', 'Password123!', 'Regular'),
('Alice', 'Johnson', 'alice.j@example.com', '+12025550125', '1995-03-10', 'Password123!', 'Regular'),
('Bob', 'Brown', 'bob.brown@example.com', '+12025550126', '1988-11-30', 'Password123!', 'Admin');

-- Create movies table
CREATE TABLE movies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) UNIQUE NOT NULL,
    rel_date DATE NOT NULL,
    genre VARCHAR(50) NOT NULL
);

-- Create reviews table
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT NOT NULL,
    review VARCHAR(1024) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    UNIQUE (user_id, movie_id)
);

-- Create shares table
CREATE TABLE shares (
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    share_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (review_id, user_id)
);

-- Insert 10 movies
INSERT INTO movies (title, rel_date, genre) VALUES
('Inception', '2010-07-16', 'Sci-Fi'),
('The Shawshank Redemption', '1994-09-23', 'Drama'),
('The Dark Knight', '2008-07-18', 'Action'),
('Pulp Fiction', '1994-10-14', 'Crime'),
('The Matrix', '1999-03-31', 'Sci-Fi'),
('Forrest Gump', '1994-07-06', 'Drama'),
('Fight Club', '1999-10-15', 'Drama'),
('Interstellar', '2014-11-07', 'Sci-Fi'),
('Gladiator', '2000-05-05', 'Action'),
('Titanic', '1997-12-19', 'Romance');

-- Insert sample reviews
INSERT INTO reviews (movie_id, review, rating, user_id) VALUES
(1, 'Mind-bending and thrilling! A masterpiece of storytelling.', 5, 1),
(1, 'Confusing at times but brilliantly executed.', 4, 2),
(2, 'A timeless masterpiece. Emotional and inspiring.', 5, 3),
(3, 'Heath Ledger’s Joker was phenomenal!', 4, 4),
(4, 'Quentin Tarantino at his best. Witty and bold.', 5, 1),
(5, 'Revolutionary sci-fi with iconic action scenes.', 4, 2),
(6, 'Heartwarming and unforgettable. Tom Hanks shines.', 5, 3),
(7, 'Thought-provoking and intense. A cult classic.', 4, 4),
(8, 'Epic journey through space and time.', 5, 1),
(9, 'Russell Crowe delivers a powerful performance.', 4, 2);

-- Insert sample shares
INSERT INTO shares (review_id, user_id) VALUES
(1, 2),
(2, 3),
(3, 4),
(4, 1);