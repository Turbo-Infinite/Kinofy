package com.mayank.movierec.model;



import java.util.Objects;



public class Movie {

    private final String title;

    private final String genre;

    private final double rating;

    private final String posterPath; // Add this field



    public Movie(String title, String genre, double rating, String posterPath) {

        this.title = title.trim();

        this.genre = genre.trim();

        this.rating = rating;

        this.posterPath = posterPath; // Initialize the new field

    }



    public String getTitle() { return title; }

    public String getGenre() { return genre; }

    public double getRating() { return rating; }

    public String getPosterPath() { return posterPath; } // Add this getter



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

                genre.equals(movie.genre) &&

                Objects.equals(posterPath, movie.posterPath); // Include posterPath in equals check

    }



    @Override

    public int hashCode() {

        return Objects.hash(title, genre, rating, posterPath);

    }

}