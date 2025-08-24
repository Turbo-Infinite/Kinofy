package com.mayank.movierec.ui.components;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class LoadingIndicator extends VBox {

    private final ProgressIndicator progressIndicator;
    private final Label messageLabel;
    private final RotateTransition rotateTransition;

    public LoadingIndicator() {
        this("Loading movies...");
    }

    public LoadingIndicator(String message) {
        super(15);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(40));
        this.setStyle("""
            -fx-background-color: rgba(248, 249, 250, 0.9);
            -fx-background-radius: 15;
            -fx-border-color: #e9ecef;
            -fx-border-radius: 15;
            -fx-border-width: 1;
            """);

        // Create spinning progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        progressIndicator.setStyle("""
            -fx-progress-color: #3498db;
            -fx-accent: #3498db;
            """);

        // Create message label
        messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#2c3e50"));

        // Create custom spinning animation for a more polished look
        rotateTransition = new RotateTransition(Duration.seconds(2), progressIndicator);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.setAutoReverse(false);

        this.getChildren().addAll(progressIndicator, messageLabel);

        // Start animation when component is added to scene
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                startAnimation();
            } else {
                stopAnimation();
            }
        });
    }

    public void startAnimation() {
        if (rotateTransition != null) {
            rotateTransition.play();
        }
    }

    public void stopAnimation() {
        if (rotateTransition != null) {
            rotateTransition.stop();
        }
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setProgress(double progress) {
        if (progress >= 0 && progress <= 1) {
            progressIndicator.setProgress(progress);
        } else {
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        }
    }

    // Factory methods for different loading states
    public static LoadingIndicator forMovieLoading() {
        return new LoadingIndicator("Loading your movie collection...");
    }

    public static LoadingIndicator forSearching() {
        return new LoadingIndicator("Searching movies...");
    }

    public static LoadingIndicator forFiltering() {
        return new LoadingIndicator("Applying filters...");
    }

    public static LoadingIndicator forSaving() {
        return new LoadingIndicator("Saving movie...");
    }
}