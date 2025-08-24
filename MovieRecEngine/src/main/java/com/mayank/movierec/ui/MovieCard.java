package com.mayank.movierec.ui;

import com.mayank.movierec.model.Movie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MovieCard extends VBox {
    private final Movie movie;
    private Runnable onEditCallback;
    private Runnable onDeleteCallback;

    public MovieCard(Movie movie) {
        this(movie, null, null);
    }

    public MovieCard(Movie movie, Runnable onEditCallback, Runnable onDeleteCallback) {
        super(8);
        this.movie = movie;
        this.onEditCallback = onEditCallback;
        this.onDeleteCallback = onDeleteCallback;

        setupCard();
        createContent();
    }

    private void setupCard() {
        this.getStyleClass().add("movie-card-enhanced");
        this.setPadding(new Insets(15));
        this.setMaxWidth(300);
        this.setPrefWidth(280);

        // Add shadow effect and rounded corners via CSS
        this.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);
            -fx-border-radius: 12;
            """);
    }

    private void createContent() {


        // Title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(250);

        // Genre badge
        Label genreLabel = new Label(movie.getGenre());
        genreLabel.getStyleClass().add("genre-badge");
        genreLabel.setStyle("""
            -fx-background-color: #3498db;
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
                topRow,
                titleLabel,
                ratingBox
        );

        this.getChildren().addAll(contentBox, actionButtons);

        // Add hover effects
        setupHoverEffects();
    }


    private Color getGenreColor(String genre) {
        return switch (genre.toLowerCase()) {
            case "action" -> Color.web("#e74c3c");
            case "comedy" -> Color.web("#f39c12");
            case "drama" -> Color.web("#9b59b6");
            case "horror" -> Color.web("#2c3e50");
            case "romance" -> Color.web("#e91e63");
            case "sci-fi", "science fiction" -> Color.web("#3498db");
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
        viewButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 6;
            -fx-padding: 6 12 6 12;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            """);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("action-button-secondary");
        editButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #3498db;
            -fx-border-color: #3498db;
            -fx-border-width: 1;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            -fx-padding: 6 12 6 12;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            """);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("action-button-danger");
        deleteButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e74c3c;
            -fx-border-color: #e74c3c;
            -fx-border-width: 1;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            -fx-padding: 6 12 6 12;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            """);

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

    private void setupHoverEffects() {
        this.setOnMouseEntered(e -> {
            this.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 4);
                -fx-border-radius: 12;
                -fx-scale-x: 1.02;
                -fx-scale-y: 1.02;
                """);
        });

        this.setOnMouseExited(e -> {
            this.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);
                -fx-border-radius: 12;
                -fx-scale-x: 1.0;
                -fx-scale-y: 1.0;
                """);
        });
    }

    private void showMovieDetails() {
        // This will be implemented when we create the details dialog
        System.out.println("Showing details for: " + movie.getTitle());
        // TODO: Open detailed view dialog
    }

    // Getters
    public Movie getMovie() { return movie; }
}