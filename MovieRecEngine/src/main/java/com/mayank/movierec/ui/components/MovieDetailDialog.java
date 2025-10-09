package com.mayank.movierec.ui.components;

import com.mayank.movierec.model.Movie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

public class MovieDetailDialog extends Dialog<Void> {

    public MovieDetailDialog(Movie movie) {
        setTitle("Movie Details: " + movie.getTitle());
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL); // Makes it a true modal

        // Set the dialog icon (optional)
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        try (InputStream is = getClass().getResourceAsStream("/images/app_icon.png")) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Content ---
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefWidth(500);

        // 1. Poster (Reusing the Image loading logic from MovieCard for simplicity)
        ImageView posterView = new ImageView();
        posterView.setFitWidth(180);
        posterView.setFitHeight(270);

        Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/placeholder.png")));
        posterView.setImage(placeholderImage);

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String imageUrl = "https://image.tmdb.org/t/p/w185" + movie.getPosterPath();
            // Load the actual poster. Since this is modal, immediate load is fine.
            Image posterImage = new Image(imageUrl, true);
            posterView.setImage(posterImage);
        }

        VBox posterContainer = new VBox(posterView);
        posterContainer.setAlignment(Pos.CENTER);

        // 2. Title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setWrapText(true);

        // 3. Rating
        Label ratingLabel = new Label(String.format("Rating: %.1f / 10.0", movie.getRating()));
        ratingLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));

        // 4. Genre
        Label genreLabel = new Label("Genre: " + movie.getGenre());
        genreLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // 5. Add all to content
        content.getChildren().addAll(
                posterContainer,
                titleLabel,
                new Label("---"),
                ratingLabel,
                genreLabel
        );

        getDialogPane().setContent(content);

        // Add a close button
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }
}