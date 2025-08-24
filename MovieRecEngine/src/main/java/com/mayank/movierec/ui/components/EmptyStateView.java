package com.mayank.movierec.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class EmptyStateView extends VBox {

    public enum EmptyStateType {
        NO_MOVIES_FOUND,
        NO_SEARCH_RESULTS,
        NO_GENRE_RESULTS,
        NO_RATING_RESULTS,
        FIRST_TIME_USER
    }

    private final EmptyStateType type;
    private Runnable onActionCallback;

    public EmptyStateView(EmptyStateType type) {
        this(type, null);
    }

    public EmptyStateView(EmptyStateType type, Runnable onActionCallback) {
        super(20);
        this.type = type;
        this.onActionCallback = onActionCallback;

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(40));
        this.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 15;
            -fx-border-color: #e9ecef;
            -fx-border-radius: 15;
            -fx-border-width: 1;
            """);

        buildEmptyState();
    }

    private void buildEmptyState() {
        Label iconLabel = new Label();
        iconLabel.setFont(Font.font(48));
        iconLabel.setAlignment(Pos.CENTER);

        Label titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setWrapText(true);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web("#7f8c8d"));
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        Button actionButton = new Button();
        actionButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-padding: 12 24 12 24;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """);

        // Configure based on type
        switch (type) {
            case NO_MOVIES_FOUND -> {
                iconLabel.setText("🎬");
                titleLabel.setText("No Movies in Database");
                messageLabel.setText("Your movie collection is empty. Start by adding some movies to get personalized recommendations!");
                actionButton.setText("Add Your First Movie");
                actionButton.setOnAction(e -> {
                    if (onActionCallback != null) onActionCallback.run();
                });
            }

            case NO_SEARCH_RESULTS -> {
                iconLabel.setText("🔍");
                titleLabel.setText("No Movies Found");
                messageLabel.setText("We couldn't find any movies matching your search. Try different keywords or check your spelling.");
                actionButton.setText("Clear Search");
                actionButton.setOnAction(e -> {
                    if (onActionCallback != null) onActionCallback.run();
                });
            }

            case NO_GENRE_RESULTS -> {
                iconLabel.setText("🎭");
                titleLabel.setText("No Movies in This Genre");
                messageLabel.setText("Looks like you don't have any movies in this genre yet. Explore different genres or add some movies!");
                actionButton.setText("Browse All Genres");
                actionButton.setOnAction(e -> {
                    if (onActionCallback != null) onActionCallback.run();
                });
            }

            case NO_RATING_RESULTS -> {
                iconLabel.setText("⭐");
                titleLabel.setText("No Movies Match Rating Filter");
                messageLabel.setText("No movies found with the selected rating range. Try adjusting your rating filters.");
                actionButton.setText("Reset Filters");
                actionButton.setOnAction(e -> {
                    if (onActionCallback != null) onActionCallback.run();
                });
            }

            case FIRST_TIME_USER -> {
                iconLabel.setText("🌟");
                titleLabel.setText("Welcome to Movie Recommendations!");
                messageLabel.setText("Get started by adding your favorite movies. The more you add, the better our recommendations become.");
                actionButton.setText("Add Movies");
                actionButton.setOnAction(e -> {
                    if (onActionCallback != null) onActionCallback.run();
                });
            }
        }

        // Add hover effects to button
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle("""
                -fx-background-color: #2980b9;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-padding: 12 24 12 24;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                -fx-scale-x: 1.05;
                -fx-scale-y: 1.05;
                """);
        });

        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle("""
                -fx-background-color: #3498db;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-padding: 12 24 12 24;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                -fx-scale-x: 1.0;
                -fx-scale-y: 1.0;
                """);
        });

        this.getChildren().addAll(iconLabel, titleLabel, messageLabel, actionButton);
    }

    public void setActionCallback(Runnable callback) {
        this.onActionCallback = callback;
    }
}