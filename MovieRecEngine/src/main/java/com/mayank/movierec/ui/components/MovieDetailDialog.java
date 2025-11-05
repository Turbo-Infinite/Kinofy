package com.mayank.movierec.ui.components;

import com.mayank.movierec.model.Movie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MovieDetailDialog extends Dialog<Void> {

    public MovieDetailDialog(Movie movie) {
        setTitle("Movie Details: " + movie.getTitle());
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        try (InputStream is = getClass().getResourceAsStream("/images/app_icon.png")) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefWidth(500);

        ImageView posterView = new ImageView();
        posterView.setFitWidth(180);
        posterView.setFitHeight(270);

        Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/placeholder.png")));
        posterView.setImage(placeholderImage);

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String imageUrl = "https://image.tmdb.org/t/p/w185" + movie.getPosterPath();
            Image posterImage = new Image(imageUrl, true);
            posterView.setImage(posterImage);
        }

        VBox posterContainer = new VBox(posterView);
        posterContainer.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setWrapText(true);

        Label ratingLabel = new Label(String.format("Rating: %.1f / 10.0", movie.getRating()));
        ratingLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));

        Label genreLabel = new Label("Genre: " + movie.getGenre());
        genreLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // This line is now correct, as createEmotionsDisplay returns an HBox
        HBox emotionsBox = createEmotionsDisplay(movie.getEmotions());

        VBox notesBox = new VBox(5);
        Label notesTitleLabel = new Label("Your Notes:");
        notesTitleLabel.getStyleClass().add("notes-title");

        String notesText = movie.getNotes() != null && !movie.getNotes().isEmpty() ? movie.getNotes() : "None";
        Label notesContentLabel = new Label(notesText);
        notesContentLabel.getStyleClass().add("notes-content");
        notesContentLabel.setWrapText(true);
        notesBox.getChildren().addAll(notesTitleLabel, notesContentLabel);

        HBox tagsBox = createTagsDisplay(movie.getTags());

        content.getChildren().addAll(
                posterContainer,
                titleLabel,
                new Label("---"),
                ratingLabel,
                genreLabel,
                emotionsBox,
                notesBox,
                tagsBox
        );

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    private HBox createTagsDisplay(String tags) {
        HBox hbox = new HBox(8);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Tags:");
        title.getStyleClass().add("notes-title"); // Reusing style for consistency
        hbox.getChildren().add(title);

        FlowPane pane = new FlowPane(5, 5);
        if (tags == null || tags.trim().isEmpty()) {
            Label noTagsLabel = new Label("None");
            noTagsLabel.getStyleClass().add("notes-content");
            pane.getChildren().add(noTagsLabel);
        } else {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                if (!tag.trim().isEmpty()) {
                    Label tagBadge = new Label(tag.trim());
                    tagBadge.getStyleClass().add("tag-badge");
                    pane.getChildren().add(tagBadge);
                }
            }
        }
        hbox.getChildren().add(pane);
        return hbox;
    }

    // --- THIS IS THE FIX ---
    // The method now correctly constructs and returns an HBox.
    private HBox createEmotionsDisplay(String emotions) {
        HBox hbox = new HBox(8);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Emotions:");
        title.setFont(Font.font("System", FontWeight.NORMAL, 14));
        hbox.getChildren().add(title);

        FlowPane pane = new FlowPane(5, 5);
        if (emotions == null || emotions.trim().isEmpty()) {
            Label noEmotionsLabel = new Label("None");
            noEmotionsLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            pane.getChildren().add(noEmotionsLabel);
        } else {
            Map<String, String> emotionImageMap = new LinkedHashMap<>();
            emotionImageMap.put("😄", "/images/emojis/happy.png");
            emotionImageMap.put("😢", "/images/emojis/sad.png");
            emotionImageMap.put("😂", "/images/emojis/laughing.png");
            emotionImageMap.put("😡", "/images/emojis/angry.png");
            emotionImageMap.put("😱", "/images/emojis/shocked.png");
            emotionImageMap.put("😍", "/images/emojis/love.png");
            emotionImageMap.put("🤔", "/images/emojis/thinking.png");

            String[] selectedEmojis = emotions.split(" ");
            for (String emojiChar : selectedEmojis) {
                String imagePath = emotionImageMap.get(emojiChar);
                if (imagePath != null) {
                    ImageView emojiView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
                    emojiView.setFitWidth(24);
                    emojiView.setFitHeight(24);
                    pane.getChildren().add(emojiView);
                }
            }
        }
        hbox.getChildren().add(pane);
        return hbox;
    }
}