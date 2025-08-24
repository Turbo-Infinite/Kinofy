package com.mayank.movierec.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.CheckComboBox; // You'll need to add ControlsFX dependency

import java.util.List;
import java.util.Set;

public class AdvancedFilterPanel extends VBox {

    // Search components
    private final TextField searchField;
    private final Button clearSearchButton;

    // Genre filtering
    private final ListView<CheckBox> genreList;
    private final ComboBox<String> genreComboBox; // Fallback if CheckComboBox unavailable

    // Rating filtering
    private final StarRating minRatingFilter;
    private final StarRating maxRatingFilter;
    private final Label ratingRangeLabel;

    // Sorting options
    private final ComboBox<String> sortComboBox;
    private final ToggleButton sortOrderButton;

    // Quick filters
    private final ToggleButton highRatedButton;
    private final ToggleButton recentButton;
    private final ToggleButton favoritesButton;

    // Callbacks
    private Runnable onFilterChanged;

    public AdvancedFilterPanel() {
        super(12);
        this.setPadding(new Insets(15));
        this.getStyleClass().add("filter-panel");


        // Initialize components
        searchField = createSearchField();
        clearSearchButton = createClearSearchButton();
        genreList = createGenreList();
        genreComboBox = createGenreComboBox();
        minRatingFilter = new StarRating(true, 16);
        maxRatingFilter = new StarRating(true, 16);
        ratingRangeLabel = new Label("0.0 - 10.0");
        sortComboBox = createSortComboBox();
        sortOrderButton = createSortOrderButton();

        // Quick filter buttons
        highRatedButton = createQuickFilterButton("High Rated (8+)", "⭐");
        recentButton = createQuickFilterButton("Recently Added", "🆕");
        favoritesButton = createQuickFilterButton("Favorites", "❤");

        setupEventHandlers();
        buildLayout();
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("🔍 Search movies...");
        field.getStyleClass().add("search-field");
        return field;
    }

    private Button createClearSearchButton() {
        Button button = new Button("✕");
        button.getStyleClass().add("clear-search-button");
        button.setTooltip(new Tooltip("Clear search"));
        button.setVisible(false); // Initially hidden
        return button;
    }

    private ListView<CheckBox> createGenreList() {
        ListView<CheckBox> list = new ListView<>();
        list.setPrefHeight(120);
        list.getStyleClass().add("genre-list");
        return list;
    }

    private ComboBox<String> createGenreComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPromptText("Select Genre(s)");
        combo.setPrefWidth(200);
        combo.getStyleClass().add("genre-combo");
        return combo;
    }

    private ComboBox<String> createSortComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(
                "Rating",
                "Title (A-Z)",
                "Genre",
                "Date Added",
                "Release Year"
        );
        combo.setValue("Rating");
        combo.setPrefWidth(150);
        combo.getStyleClass().add("sort-combo-box");
        return combo;
    }

    private ToggleButton createSortOrderButton() {
        ToggleButton button = new ToggleButton("↓");
        button.setSelected(true); // Default: descending
        button.setTooltip(new Tooltip("Toggle sort order"));
        button.getStyleClass().add("sort-order-button");
        return button;
    }

    private ToggleButton createQuickFilterButton(String text, String emoji) {
        ToggleButton button = new ToggleButton(emoji + " " + text);
        button.getStyleClass().add("quick-filter-button");
        return button;
    }

    private void setupEventHandlers() {
        // Search field events
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearSearchButton.setVisible(!newVal.isEmpty());
            triggerFilterChanged();
        });

        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            clearSearchButton.setVisible(false);
        });

        // Rating range events
        minRatingFilter.ratingProperty().addListener((obs, oldVal, newVal) -> {
            updateRatingRangeLabel();
            triggerFilterChanged();
        });

        maxRatingFilter.ratingProperty().addListener((obs, oldVal, newVal) -> {
            updateRatingRangeLabel();
            triggerFilterChanged();
        });

        // Sort events
        sortComboBox.setOnAction(e -> triggerFilterChanged());

        sortOrderButton.setOnAction(e -> {
            sortOrderButton.setText(sortOrderButton.isSelected() ? "↓" : "↑");
            sortOrderButton.setTooltip(new Tooltip(
                    sortOrderButton.isSelected() ? "Descending order" : "Ascending order"
            ));
            triggerFilterChanged();
        });

        // Quick filter events
        highRatedButton.setOnAction(e -> {
            if (highRatedButton.isSelected()) {
                minRatingFilter.setRating(8.0);
                updateButtonStyle(highRatedButton, true);
            } else {
                minRatingFilter.setRating(0.0);
                updateButtonStyle(highRatedButton, false);
            }
        });

        recentButton.setOnAction(e -> {
            updateButtonStyle(recentButton, recentButton.isSelected());
            triggerFilterChanged();
        });

        favoritesButton.setOnAction(e -> {
            updateButtonStyle(favoritesButton, favoritesButton.isSelected());
            triggerFilterChanged();
        });

        // Genre ComboBox event handler
        genreComboBox.setOnAction(e -> triggerFilterChanged());
    }

    private void updateButtonStyle(ToggleButton button, boolean selected) {
        if (selected) {
            button.getStyleClass().add("active");
        } else {
            button.getStyleClass().remove("active");
        }
    }

    private void updateRatingRangeLabel() {
        double min = minRatingFilter.getRating();
        double max = maxRatingFilter.getRating();

        // Ensure max is at least equal to min
        if (max < min) {
            maxRatingFilter.setRating(min);
            max = min;
        }

        ratingRangeLabel.setText(String.format("%.1f - %.1f", min, max));
    }

    private void buildLayout() {
        // Search section
        Label searchLabel = createSectionLabel("🔍 Search");
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchField, clearSearchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Genre section
        Label genreLabel = createSectionLabel("🎭 Genres");

        // Rating section
        Label ratingLabel = createSectionLabel("⭐ Rating Range");
        VBox ratingBox = new VBox(8);

        HBox minRatingBox = new HBox(8);
        minRatingBox.setAlignment(Pos.CENTER_LEFT);
        Label minRatingLabel = new Label("Min:");
        minRatingLabel.getStyleClass().add("rating-label");
        minRatingBox.getChildren().addAll(minRatingLabel, minRatingFilter);

        HBox maxRatingBox = new HBox(8);
        maxRatingBox.setAlignment(Pos.CENTER_LEFT);
        Label maxRatingLabel = new Label("Max:");
        maxRatingLabel.getStyleClass().add("rating-label");
        maxRatingBox.getChildren().addAll(maxRatingLabel, maxRatingFilter);

        ratingRangeLabel.getStyleClass().add("rating-range-label");
        ratingBox.getChildren().addAll(minRatingBox, maxRatingBox, ratingRangeLabel);

        // Sort section
        Label sortLabel = createSectionLabel("📊 Sort");
        HBox sortBox = new HBox(8);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.getChildren().addAll(sortComboBox, sortOrderButton);

        // Quick filters section
        Label quickLabel = createSectionLabel("⚡ Quick Filters");
        FlowPane quickFilters = new FlowPane(8, 8);
        quickFilters.getChildren().addAll(highRatedButton, recentButton, favoritesButton);

        // Add all sections
        this.getChildren().addAll(
                searchLabel, searchBox,
                new Separator(),
                genreLabel, genreComboBox,
                new Separator(),
                ratingLabel, ratingBox,
                new Separator(),
                sortLabel, sortBox,
                new Separator(),
                quickLabel, quickFilters
        );

        // Initialize rating range
        maxRatingFilter.setRating(10.0);
        updateRatingRangeLabel();
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    private void triggerFilterChanged() {
        if (onFilterChanged != null) {
            onFilterChanged.run();
        }
    }

    // Public getters for filter values
    public String getSearchText() {
        return searchField.getText().trim();
    }

    public double getMinRating() {
        return minRatingFilter.getRating();
    }

    public double getMaxRating() {
        return maxRatingFilter.getRating();
    }

    public String getSortBy() {
        return sortComboBox.getValue();
    }

    public boolean isSortDescending() {
        return sortOrderButton.isSelected();
    }

    public boolean isHighRatedFilterActive() {
        return highRatedButton.isSelected();
    }

    public boolean isRecentFilterActive() {
        return recentButton.isSelected();
    }

    public boolean isFavoritesFilterActive() {
        return favoritesButton.isSelected();
    }

    // NEW: Getter for the genre ComboBox value
    public String getGenreComboBoxValue() {
        return genreComboBox.getValue();
    }

    // Setter for callback
    public void setOnFilterChanged(Runnable callback) {
        this.onFilterChanged = callback;
    }

    // Method to update genres
    public void updateGenres(List<String> genres) {
        genreComboBox.getItems().clear();
        genreComboBox.getItems().add("All Genres");
        genreComboBox.getItems().addAll(genres);
        genreComboBox.setValue("All Genres");
    }

    // Clear all filters
    public void clearAllFilters() {
        searchField.clear();
        genreComboBox.setValue("All Genres");
        minRatingFilter.setRating(0);
        maxRatingFilter.setRating(10);
        sortComboBox.setValue("Rating");
        sortOrderButton.setSelected(true);
        sortOrderButton.setText("↓");

        // Clear quick filters
        highRatedButton.setSelected(false);
        recentButton.setSelected(false);
        favoritesButton.setSelected(false);
        updateButtonStyle(highRatedButton, false);
        updateButtonStyle(recentButton, false);
        updateButtonStyle(favoritesButton, false);

        triggerFilterChanged();
    }
}