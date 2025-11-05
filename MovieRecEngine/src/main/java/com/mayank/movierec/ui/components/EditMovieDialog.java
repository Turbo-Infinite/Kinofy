package com.mayank.movierec.ui.components;

import com.mayank.movierec.model.Movie;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.InputStream;

public class EditMovieDialog extends Dialog<Movie> {

    public EditMovieDialog(Movie movie) {
        setTitle("Edit Movie: " + movie.getTitle());
        setHeaderText("Add your emotions, notes, and tags.");

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        try (InputStream is = getClass().getResourceAsStream("/images/app_icon.png")) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ImageEmotionSelector emotionSelector = new ImageEmotionSelector(movie.getEmotions());

        TextArea notesArea = new TextArea(movie.getNotes());
        notesArea.setPromptText("e.g., Watched on a rainy day...");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(3);
        notesArea.getStyleClass().add("notes-area");

        // Add a TextField for tags
        TextField tagsField = new TextField(movie.getTags());
        tagsField.setPromptText("e.g., favorite, holiday, rewatchable");

        grid.add(new Label("Emotions:"), 0, 0);
        grid.add(emotionSelector, 1, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(notesArea, 1, 1);
        grid.add(new Label("Tags:"), 0, 2); // Add tags label and field
        grid.add(tagsField, 1, 2);

        getDialogPane().setContent(grid);

        // Update the constructor call to include the new tags field
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Movie(
                        movie.getTitle(),
                        movie.getGenre(),
                        movie.getRating(),
                        movie.getPosterPath(),
                        emotionSelector.getSelectedEmotions(),
                        notesArea.getText(),
                        tagsField.getText(), // Get text from the new tags field
                        movie.getDateTagged()
                );
            }
            return null;
        });
    }
}