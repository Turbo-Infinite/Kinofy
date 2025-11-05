package com.mayank.movierec.ui;

import com.mayank.movierec.model.Movie;
import com.mayank.movierec.ui.components.MovieDetailDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MovieCard extends VBox {
    private final Movie movie;
    private Runnable onEditCallback;
    private Runnable onDeleteCallback;
    private final ImageView posterView;


    private static final Map<String, Image> imageCache = new HashMap<>();

    public MovieCard(Movie movie) {
        this(movie, null, null);
    }

    public MovieCard(Movie movie, Runnable onEditCallback, Runnable onDeleteCallback) {
        super(8);
        this.movie = movie;
        this.onEditCallback = onEditCallback;
        this.onDeleteCallback = onDeleteCallback;
        this.posterView = new ImageView();
        this.posterView.setFitWidth(250);
        this.posterView.setFitHeight(375);

        setupCard();
        createContent();
        loadImage();
    }

    private Image getImageFromCache(String imageUrl) {
        if (imageCache.containsKey(imageUrl)) {
            return imageCache.get(imageUrl);
        } else {
            Image image = new Image(imageUrl, true);
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                    imageCache.put(imageUrl, image);
                }
            });
            return image;
        }
    }

    private void setupCard() {
        this.getStyleClass().add("movie-card-enhanced");
        this.setPadding(new Insets(15));
        this.setMaxWidth(300);
        this.setPrefWidth(280);

        // Add shadow effect and rounded corners via CSS
    }

    private void createContent() {

         // Standard movie poster aspect ratio

        // Placeholder for when a poster is not available
        Image placeholderImage = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
        // Construct the full image URL and load the image


        // Title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#6340CF"));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(250);

        // Genre badge
        Label genreLabel = new Label(movie.getGenre());
        genreLabel.getStyleClass().add("genre-badge");
        genreLabel.setStyle("""
            -fx-background-color: #6340cf;
            -fx-text-fill: white;
            -fx-background-radius: 15;
            -fx-padding: 4 12 4 12;
            -fx-font-size: 12px;
            """);

        // Rating with stars
        HBox ratingBox = createRatingStars(movie.getRating());

        // Action buttons
        HBox actionButtons = createActionButtons();

        // Layout
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(genreLabel);

        VBox contentBox = new VBox(8);
        contentBox.getChildren().addAll(
                posterView,
                topRow,
                titleLabel,
                ratingBox
        );

        this.getChildren().addAll(contentBox, actionButtons);
    }
    public void loadImage() {
        Image placeholderImage = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
        this.posterView.setImage(placeholderImage);

        String posterPath = movie.getPosterPath();

        if (posterPath != null && !posterPath.isEmpty()) {
            Image posterImage;
            // --- THIS IS THE NEW LOGIC ---
            // Check if the path is a web URL or a local file path
            if (posterPath.startsWith("http") || posterPath.startsWith("/")) {
                // It's a web URL from TMDb
                String imageUrl = posterPath.startsWith("http") ? posterPath : "https://image.tmdb.org/t/p/w185" + posterPath;

                if (imageCache.containsKey(imageUrl)) {
                    posterView.setImage(imageCache.get(imageUrl));
                    return;
                }
                posterImage = new Image(imageUrl, true);
            } else {
                // It's a local file path from our posters directory
                try {
                    posterImage = new Image(new File(posterPath).toURI().toString(), true);
                } catch (Exception e) {
                    System.err.println("Error loading local poster: " + posterPath);
                    e.printStackTrace();
                    return; // Stop if the local file can't be loaded
                }
            }
            // --- END OF NEW LOGIC ---

            posterImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    if (posterImage.isError()) {
                        System.err.println("Failed to load image for: " + movie.getTitle());
                    } else {
                        posterView.setImage(posterImage);
                        // Cache web images, no need to cache local files
                        if (posterPath.startsWith("http") || posterPath.startsWith("/")) {
                            imageCache.put(posterPath, posterImage);
                        }
                    }
                }
            });
        }
    }


    private Color getGenreColor(String genre) {
        return switch (genre.toLowerCase()) {
            case "action" -> Color.web("#e74c3c");
            case "comedy" -> Color.web("#f39c12");
            case "drama" -> Color.web("#9b59b6");
            case "horror" -> Color.web("#2c3e50");
            case "romance" -> Color.web("#e91e63");
            case "sci-fi", "science fiction" -> Color.web("#6340cf");
            case "thriller" -> Color.web("#34495e");
            case "animation" -> Color.web("#1abc9c");
            default -> Color.web("#95a5a6");
        };
    }

    private HBox createRatingStars(double rating) {
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        int fullStars = (int) (rating / 2); // Convert 10-point scale to 5-star
        boolean hasHalfStar = (rating % 2) >= 1;

        // Add star symbols
        for (int i = 0; i < 5; i++) {
            Label star = new Label();
            star.setFont(Font.font(14));

            if (i < fullStars) {
                star.setText("★");
                star.setTextFill(Color.web("#f1c40f"));
            } else if (i == fullStars && hasHalfStar) {
                star.setText("★");
                star.setTextFill(Color.web("#f1c40f"));
                star.setOpacity(0.5);
            } else {
                star.setText("☆");
                star.setTextFill(Color.web("#bdc3c7"));
            }

            ratingBox.getChildren().add(star);
        }

        // Add numeric rating
        Label ratingText = new Label(String.format("%.1f", rating));
        ratingText.setFont(Font.font("System", FontWeight.BOLD, 12));
        ratingText.setTextFill(Color.web("#7f8c8d"));
        ratingText.setStyle("-fx-padding: 0 0 0 8;");

        ratingBox.getChildren().add(ratingText);

        return ratingBox;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));

        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("action-button-primary");


        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("action-button-secondary");


        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("action-button-danger");


        // Add tooltips
        viewButton.setTooltip(new Tooltip("View movie details"));
        editButton.setTooltip(new Tooltip("Edit movie information"));
        deleteButton.setTooltip(new Tooltip("Delete this movie"));

        // Add event handlers
        viewButton.setOnAction(e -> showMovieDetails());
        editButton.setOnAction(e -> {
            if (onEditCallback != null) onEditCallback.run();
        });
        deleteButton.setOnAction(e -> {
            if (onDeleteCallback != null) onDeleteCallback.run();
        });

        buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);

        return buttonBox;
    }



    private void showMovieDetails() {
        // This will be implemented when we create the details dialog
        MovieDetailDialog dialog = new MovieDetailDialog(movie); // Pass the movie object
        dialog.getDialogPane().getStylesheets().addAll(this.getScene().getStylesheets());
        dialog.showAndWait();
    }

    // Getters
    public Movie getMovie() { return movie; }
}