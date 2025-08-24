package com.mayank.movierec.ui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Cursor;

public class StarRating extends HBox {
    private final DoubleProperty rating = new SimpleDoubleProperty(0);
    private final Label[] stars = new Label[5];
    private final boolean interactive;
    private final int starSize;

    public StarRating() {
        this(true, 20);
    }

    public StarRating(boolean interactive) {
        this(interactive, 20);
    }

    public StarRating(boolean interactive, int starSize) {
        super(2);
        this.interactive = interactive;
        this.starSize = starSize;
        this.setAlignment(Pos.CENTER_LEFT);

        initializeStars();
        updateStarDisplay();

        // Listen for rating changes
        rating.addListener((obs, oldVal, newVal) -> updateStarDisplay());
    }

    private void initializeStars() {
        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            Label star = new Label("☆");
            star.setFont(Font.font(starSize));
            star.setTextFill(Color.web("#bdc3c7"));

            if (interactive) {
                star.setCursor(Cursor.HAND);

                // Click to set rating
                star.setOnMouseClicked(e -> {
                    double newRating = (starIndex + 1) * 2.0; // Convert to 10-point scale
                    setRating(newRating);
                });

                // Hover effects for interactive stars
                star.setOnMouseEntered(e -> {
                    if (interactive) {
                        previewRating(starIndex + 1);
                    }
                });

                star.setOnMouseExited(e -> {
                    if (interactive) {
                        updateStarDisplay(); // Return to actual rating
                    }
                });
            }

            stars[i] = star;
            this.getChildren().add(star);
        }
    }

    private void previewRating(int starCount) {
        for (int i = 0; i < 5; i++) {
            if (i < starCount) {
                stars[i].setText("★");
                stars[i].setTextFill(Color.web("#f1c40f"));
                stars[i].setOpacity(0.8); // Slightly transparent for preview
            } else {
                stars[i].setText("☆");
                stars[i].setTextFill(Color.web("#bdc3c7"));
                stars[i].setOpacity(1.0);
            }
        }
    }

    private void updateStarDisplay() {
        double currentRating = rating.get();
        int fullStars = (int) (currentRating / 2.0); // Convert from 10-point to 5-star scale
        boolean hasHalfStar = (currentRating % 2.0) >= 1.0;

        for (int i = 0; i < 5; i++) {
            Label star = stars[i];
            star.setOpacity(1.0); // Reset opacity

            if (i < fullStars) {
                star.setText("★");
                star.setTextFill(Color.web("#f1c40f"));
            } else if (i == fullStars && hasHalfStar) {
                star.setText("★");
                star.setTextFill(Color.web("#f1c40f"));
                star.setOpacity(0.6); // Half star effect
            } else {
                star.setText("☆");
                star.setTextFill(Color.web("#bdc3c7"));
            }
        }
    }

    // Property methods
    public DoubleProperty ratingProperty() {
        return rating;
    }

    public double getRating() {
        return rating.get();
    }

    public void setRating(double rating) {
        // Clamp rating between 0 and 10
        double clampedRating = Math.max(0, Math.min(10, rating));
        this.rating.set(clampedRating);
    }

    public void setStarColor(Color color) {
        // Allow customization of star color
        updateStarDisplay();
    }
}