package com.mayank.movierec.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.InputStream;
import java.util.List;

public class AdvancedFilterPanel extends VBox {

    private final TextField searchField;
    private final Button clearSearchButton;
    private final ComboBox<String> genreComboBox;
    private final StarRating minRatingFilter;
    private final StarRating maxRatingFilter;
    private final Label ratingRangeLabel;
    private final ComboBox<String> sortComboBox;
    private final ToggleButton sortOrderButton;
    private final ToggleButton highRatedButton;
    private final ToggleButton recentButton;
    private final ToggleButton favoritesButton;
    private Runnable onFilterChanged;

    public AdvancedFilterPanel() {
        super(12);
        this.setPadding(new Insets(15));
        this.getStyleClass().add("filter-panel");

        searchField = createSearchField();
        clearSearchButton = createClearSearchButton();
        genreComboBox = createGenreComboBox();
        minRatingFilter = new StarRating(true, 16);
        maxRatingFilter = new StarRating(true, 16);
        ratingRangeLabel = new Label("0.0 - 10.0");
        sortComboBox = createSortComboBox();
        sortOrderButton = createSortOrderButton();
        highRatedButton = createQuickFilterButton("High Rated (8+)", "⭐");
        recentButton = createQuickFilterButton("Recently Added", "🆕");
        favoritesButton = createQuickFilterButton("Favorites", "❤");

        setupEventHandlers();
        buildLayout();
    }

    private void buildLayout() {
        // Search section
        HBox searchLabel = createSectionHeader("Search", "search.png");
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchField, clearSearchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Genre section
        HBox genreLabel = createSectionHeader("Genres", "genre.png");

        // Rating section
        HBox ratingLabel = createSectionHeader("Rating Range", "rating.png");
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
        HBox sortLabel = createSectionHeader("Sort", "sort.png");
        HBox sortBox = new HBox(8);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.getChildren().addAll(sortComboBox, sortOrderButton);

        // Quick filters section
        HBox quickLabel = createSectionHeader("Quick Filters", "quick_filters.png");
        FlowPane quickFilters = new FlowPane(8, 8);
        quickFilters.getChildren().addAll(highRatedButton, recentButton, favoritesButton);

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

        maxRatingFilter.setRating(10.0);
        updateRatingRangeLabel();
    }

    // --- NEW HELPER METHOD ---
    // This method creates a header with both an icon and a text label.
    private HBox createSectionHeader(String text, String iconName) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView iconView = new ImageView();
        String imagePath = "/images/icons/" + iconName;
        try (InputStream is = getClass().getResourceAsStream(imagePath)) {
            if (is == null) {
                System.err.println("CRITICAL: Icon not found at path: " + imagePath);
            } else {
                iconView.setImage(new Image(is));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label label = new Label(text);
        label.getStyleClass().add("section-label");

        header.getChildren().addAll(iconView, label);
        return header;
    }

    // --- All other methods remain unchanged ---
    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Search movies...");
        field.getStyleClass().add("search-field");
        return field;
    }
    private Button createClearSearchButton() {
        Button button = new Button("✕");
        button.getStyleClass().add("clear-search-button");
        button.setTooltip(new Tooltip("Clear search"));
        button.setVisible(false);
        return button;
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
        combo.getItems().addAll("Rating", "Title (A-Z)", "Genre", "Date Added", "Release Year");
        combo.setValue("Rating");
        combo.setPrefWidth(150);
        combo.getStyleClass().add("sort-combo-box");
        return combo;
    }
    private ToggleButton createSortOrderButton() {
        ToggleButton button = new ToggleButton("↓");
        button.setSelected(true);
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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearSearchButton.setVisible(!newVal.isEmpty());
            triggerFilterChanged();
        });
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            clearSearchButton.setVisible(false);
        });
        minRatingFilter.ratingProperty().addListener((obs, oldVal, newVal) -> {
            updateRatingRangeLabel();
            triggerFilterChanged();
        });
        maxRatingFilter.ratingProperty().addListener((obs, oldVal, newVal) -> {
            updateRatingRangeLabel();
            triggerFilterChanged();
        });
        sortComboBox.setOnAction(e -> triggerFilterChanged());
        sortOrderButton.setOnAction(e -> {
            sortOrderButton.setText(sortOrderButton.isSelected() ? "↓" : "↑");
            sortOrderButton.setTooltip(new Tooltip(sortOrderButton.isSelected() ? "Descending order" : "Ascending order"));
            triggerFilterChanged();
        });
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
        if (max < min) {
            maxRatingFilter.setRating(min);
            max = min;
        }
        ratingRangeLabel.setText(String.format("%.1f - %.1f", min, max));
    }
    private void triggerFilterChanged() {
        if (onFilterChanged != null) {
            onFilterChanged.run();
        }
    }
    public String getSearchText() { return searchField.getText().trim(); }
    public double getMinRating() { return minRatingFilter.getRating(); }
    public double getMaxRating() { return maxRatingFilter.getRating(); }
    public String getSortBy() { return sortComboBox.getValue(); }
    public boolean isSortDescending() { return sortOrderButton.isSelected(); }
    public boolean isHighRatedFilterActive() { return highRatedButton.isSelected(); }
    public boolean isRecentFilterActive() { return recentButton.isSelected(); }
    public boolean isFavoritesFilterActive() { return favoritesButton.isSelected(); }
    public String getGenreComboBoxValue() { return genreComboBox.getValue(); }
    public void setOnFilterChanged(Runnable callback) { this.onFilterChanged = callback; }
    public void updateGenres(List<String> genres) {
        genreComboBox.getItems().clear();
        genreComboBox.getItems().add("All Genres");
        genreComboBox.getItems().addAll(genres);
        genreComboBox.setValue("All Genres");
    }
    public void clearAllFilters() {
        searchField.clear();
        genreComboBox.setValue("All Genres");
        minRatingFilter.setRating(0);
        maxRatingFilter.setRating(10);
        sortComboBox.setValue("Rating");
        sortOrderButton.setSelected(true);
        sortOrderButton.setText("↓");
        highRatedButton.setSelected(false);
        recentButton.setSelected(false);
        favoritesButton.setSelected(false);
        updateButtonStyle(highRatedButton, false);
        updateButtonStyle(recentButton, false);
        updateButtonStyle(favoritesButton, false);
        triggerFilterChanged();
    }
}