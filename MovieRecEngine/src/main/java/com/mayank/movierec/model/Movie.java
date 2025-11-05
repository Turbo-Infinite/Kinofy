package com.mayank.movierec.model;


import java.time.LocalDate;
import java.util.Objects;



public class Movie {

    private final String title;

    private final String genre;

    private final double rating;

    private final String posterPath; // Add this field

    private final String emotions;
    private final String notes;
    private final String tags;
    private final LocalDate dateTagged;

    public Movie(String title, String genre, double rating, String posterPath, String emotions,String notes, String tags, LocalDate dateTagged) {

        this.title = title.trim();

        this.genre = genre.trim();

        this.rating = rating;

        this.posterPath = posterPath; // Initialize the new field

        this.emotions = emotions != null ? emotions.trim() : "";
        this.notes = notes != null ? notes.trim() : "";
        this.tags = tags != null ? tags.trim() : "";
        this.dateTagged = dateTagged;
    }



    public String getTitle() { return title; }

    public String getGenre() { return genre; }

    public double getRating() { return rating; }

    public String getPosterPath() { return posterPath; } // Add this getter

    public String getEmotions() { return emotions; }
    public String getNotes() { return notes; }
    public String getTags() { return tags; }
    public LocalDate getDateTagged() { return dateTagged; }

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

                Objects.equals(posterPath, movie.posterPath)&& // Include posterPath in equals check

                Objects.equals(emotions, movie.emotions)&&
                Objects.equals(tags, movie.tags)&&
                Objects.equals(dateTagged, movie.dateTagged);


    }



    @Override

    public int hashCode() {

        return Objects.hash(title, genre, rating, posterPath, emotions, tags, dateTagged);

    }

}