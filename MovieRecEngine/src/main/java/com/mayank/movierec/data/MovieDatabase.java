package com.mayank.movierec.data;

import com.mayank.movierec.model.Movie;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MovieDatabase {
    private final List<Movie> movies = new ArrayList<>();
    private final Set<String> genres = new HashSet<>();
    private final String csvPath = "/movies.csv"; // Resource path

    public void loadMovies(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String title = parts[0].trim();
                    String genre = parts[1].trim();
                    double rating = Double.parseDouble(parts[2].trim());
                    movies.add(new Movie(title, genre, rating));
                    genres.add(genre);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Movie> getAllMovies() {
        return movies;
    }

    public List<String> getGenres() {
        List<String> sorted = new ArrayList<>(genres);
        Collections.sort(sorted);
        return sorted;
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
        genres.add(movie.getGenre());

        // Append to CSV file
        try {
            File file = new File("src/main/resources/movies.csv"); // Adjust path if needed
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(String.format("%s,%s,%.1f\n", movie.getTitle(), movie.getGenre(), movie.getRating()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}