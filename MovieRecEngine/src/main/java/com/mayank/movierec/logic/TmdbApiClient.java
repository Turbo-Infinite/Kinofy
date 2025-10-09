package com.mayank.movierec.logic;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TmdbApiClient {

    private final String API_KEY = "2b65711735fb0f9be83e0f6fd6a270db";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private Map<Integer, String> genreMap = new HashMap<>();

    public static class MovieApiResponse {
        public List<TmdbMovie> results;
    }

    public static class GenreListResponse {
        public List<Genre> genres;
    }

    public static class Genre {
        public int id;
        public String name;
    }

    public void fetchGenres() throws IOException {
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch genres: " + response);
            }
            GenreListResponse genreListResponse = gson.fromJson(Objects.requireNonNull(response.body()).string(), GenreListResponse.class);
            if (genreListResponse != null && genreListResponse.genres != null) {
                genreMap = genreListResponse.genres.stream()
                        .collect(Collectors.toMap(genre -> genre.id, genre -> genre.name));
            }
        }
    }

    public String getGenreName(int genreId) {
        return genreMap.getOrDefault(genreId, "Unknown");
    }

    public static class TmdbMovie {
        public String title;
        @SerializedName("genre_ids")
        public List<Integer> genreIds;
        @SerializedName("vote_average")
        public double voteAverage;
        @SerializedName("poster_path")
        public String posterPath;
    }

    public List<TmdbMovie> getMoviesFrom2000Onwards() throws IOException {
        List<TmdbMovie> allMovies = new ArrayList<>();

        for (int page = 1; page <= 5; page++) {
            String url = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&primary_release_date.gte=2000-01-01&page=" + page;
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }
                MovieApiResponse apiResponse = gson.fromJson(Objects.requireNonNull(response.body()).string(), MovieApiResponse.class);
                if (apiResponse != null && apiResponse.results != null) {
                    allMovies.addAll(apiResponse.results);
                }
            }
        }
        return allMovies;
    }
}