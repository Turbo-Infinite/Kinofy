package com.mayank.movierec.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Toast {

    public static void show(String message, StackPane parent) {
        HBox toastBox = new HBox(10);
        toastBox.setAlignment(Pos.CENTER);
        toastBox.setPadding(new Insets(10, 20, 10, 20));

        toastBox.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        // --- THIS IS THE FIX ---
        // These two lines prevent the HBox from stretching to the full width OR height.
        toastBox.setMaxWidth(Region.USE_PREF_SIZE);
        toastBox.setMaxHeight(Region.USE_PREF_SIZE); // This was the missing line.

        Label label = new Label("✅ " + message);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold;");

        toastBox.getChildren().add(label);

        StackPane.setAlignment(toastBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toastBox, new Insets(0, 0, 30, 0));

        toastBox.setOpacity(0.0);
        toastBox.setTranslateY(20);

        parent.getChildren().add(toastBox);

        // Animations
        Timeline fadeInTimeline = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(toastBox.opacityProperty(), 1.0), new KeyValue(toastBox.translateYProperty(), 0)));
        Timeline fadeOutTimeline = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(toastBox.opacityProperty(), 0.0), new KeyValue(toastBox.translateYProperty(), 20)));
        fadeOutTimeline.setOnFinished(e -> parent.getChildren().remove(toastBox));
        fadeInTimeline.setOnFinished(e -> new Timeline(new KeyFrame(Duration.seconds(2), event -> fadeOutTimeline.play())).play());
        fadeInTimeline.play();
    }
}