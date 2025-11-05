package com.mayank.movierec.data;

import com.mayank.movierec.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

public class MovieDatabase {
    private static final String JDBC_URL = "jdbc:sqlite:movies.db";

    public MovieDatabase() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS movies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "genre TEXT NOT NULL," +
                    "rating REAL NOT NULL," +
                    "poster_path TEXT," +
                    "emotions TEXT," +
                    "notes TEXT," +
                    "tags TEXT," +
                    "date_tagged TEXT" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAllMovies() {
        String sql = "DELETE FROM movies";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT title, genre, rating, poster_path, emotions, notes, tags, date_tagged FROM movies";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                double rating = rs.getDouble("rating");
                String posterPath = rs.getString("poster_path");
                String emotions = rs.getString("emotions");
                String notes = rs.getString("notes");
                String tags = rs.getString("tags");
                String dateString = rs.getString("date_tagged");
                LocalDate dateTagged = dateString != null ? LocalDate.parse(dateString) : null;

                movies.add(new Movie(title, genre, rating, posterPath, emotions, notes, tags, dateTagged));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<String> getGenres() {
        Set<String> genres = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT genre FROM movies")) {

            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<String> sorted = new ArrayList<>(genres);
        Collections.sort(sorted);
        return sorted;
    }

    public void addMovie(Movie movie) {
        String sql = "INSERT INTO movies(title, genre, rating, poster_path, emotions, notes, tags, date_tagged) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setDouble(3, movie.getRating());
            pstmt.setString(4, movie.getPosterPath());
            pstmt.setString(5, movie.getEmotions());
            pstmt.setString(6, movie.getNotes());
            pstmt.setString(7, movie.getTags());
            pstmt.setString(8, movie.getDateTagged() != null ? movie.getDateTagged().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMovie(Movie movie) {
        String sql = "DELETE FROM movies WHERE title = ? AND genre = ? AND rating = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setDouble(3, movie.getRating());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- THIS IS THE FIX ---
    // The method now correctly accepts 4 arguments.
    public void updateMovieDetails(Movie movie, String newEmotions, String newNotes, String newTags) {
        String sql = "UPDATE movies SET emotions = ?, notes = ?, tags = ?, date_tagged = ? WHERE title = ? AND genre = ? AND rating = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newEmotions);
            pstmt.setString(2, newNotes);
            pstmt.setString(3, newTags);
            pstmt.setString(4, LocalDate.now().toString());
            pstmt.setString(5, movie.getTitle());
            pstmt.setString(6, movie.getGenre());
            pstmt.setDouble(7, movie.getRating());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}