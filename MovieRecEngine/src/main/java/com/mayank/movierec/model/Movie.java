package com.mayank.movierec.model;

import java.util.Objects;

public class Movie {
    private final String title;
    private final String genre;
    private final double rating;

    public Movie(String title, String genre, double rating) {
        this.title = title.trim();
        this.genre = genre.trim();
        this.rating = rating;

    }


    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public double getRating() { return rating; }

    @Override
    public String toString() {
        return title + " (" + genre + ") - " + String.format("%.1f", rating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        Movie movie = (Movie) o;
        return Double.compare(movie.rating, rating) == 0 &&
                title.equals(movie.title) &&
                genre.equals(movie.genre);

    }

    @Override
    public int hashCode() {
        return Objects.hash(title, genre, rating);
    }
}