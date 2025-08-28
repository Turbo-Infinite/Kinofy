package com.mayank.movierec.logic;

import com.mayank.movierec.model.Movie;
import java.io.*;
import java.util.*;

public class Recommender {
    public static List<Movie> loadMovies(String filePath) {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String title = parts[0].trim();
                    String genre = parts[1].trim();
                    double rating = Double.parseDouble(parts[2].trim());
                    movies.add(new Movie(title, genre, rating,null));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movies;
    }
}