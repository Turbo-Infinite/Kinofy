package com.mayank.movierec.ui;

import com.mayank.movierec.data.MovieDatabase;
import com.mayank.movierec.model.Movie;
import com.mayank.movierec.ui.components.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainUI {
    private final BorderPane root;
    private final AdvancedFilterPanel filterPanel;
    private final FlowPane movieGrid;
    private final ScrollPane movieScrollPane;
    private final MovieDatabase db;
    private final Button themeButton;
    private final VBox addMoviePanel;
    private final StackPane contentArea;

    private boolean isDarkMode = false;
    private PauseTransition searchDebouncer;

    public MainUI() {
        root = new BorderPane();
        root.setPadding(new Insets(20));

        db = new MovieDatabase();

        // Initialize components
        filterPanel = new AdvancedFilterPanel();
        movieGrid = new FlowPane(15, 15);
        movieScrollPane = new ScrollPane();
        themeButton = createThemeButton();
        addMoviePanel = createAddMoviePanel();
        contentArea = new StackPane();

        setupComponents();
        setupEventHandlers();
        buildLayout();
        loadMovies();
    }

    private void setupComponents() {
        // Configure movie grid
        movieGrid.setAlignment(Pos.CENTER);
        movieGrid.setPadding(new Insets(20));
        movieGrid.setHgap(15);
        movieGrid.setVgap(15);

        // Configure scroll pane
        movieScrollPane.setContent(movieGrid);
        movieScrollPane.setFitToWidth(true);
        movieScrollPane.setFitToHeight(true);
        movieScrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            """);

        // Configure content area
        contentArea.getChildren().add(movieScrollPane);

        // Setup search debouncing (wait 300ms after user stops typing)
        searchDebouncer = new PauseTransition(Duration.millis(300));
        searchDebouncer.setOnFinished(e -> updateMovieDisplay());
    }

    private void setupEventHandlers() {
        // Filter panel callback
        filterPanel.setOnFilterChanged(() -> {
            // Debounce search input, immediate for other filters
            if (hasSearchTextChanged()) {
                searchDebouncer.playFromStart();
            } else {
                updateMovieDisplay();
            }
        });

        // Theme button
        themeButton.setOnAction(e -> toggleTheme());
    }

    private boolean hasSearchTextChanged() {
        // Simple check - in a real app you'd track the previous search text
        return !filterPanel.getSearchText().isEmpty();
    }

    private Button createThemeButton() {
        Button button = new Button("🌙 Dark Mode");
        button.getStyleClass().add("theme-button");
        button.setStyle("""
            -fx-background-color: #34495e;
            -fx-text-fill: white;
            -fx-background-radius: 25;
            -fx-padding: 8 16 8 16;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            """);

        button.setOnMouseEntered(e -> {
            button.setStyle("""
                -fx-background-color: #2c3e50;
                -fx-text-fill: white;
                -fx-background-radius: 25;
                -fx-padding: 8 16 8 16;
                -fx-font-size: 12px;
                -fx-cursor: hand;
                -fx-scale-x: 1.05;
                -fx-scale-y: 1.05;
                """);
        });

        button.setOnMouseExited(e -> {
            button.setStyle("""
                -fx-background-color: #34495e;
                -fx-text-fill: white;
                -fx-background-radius: 25;
                -fx-padding: 8 16 8 16;
                -fx-font-size: 12px;
                -fx-cursor: hand;
                -fx-scale-x: 1.0;
                -fx-scale-y: 1.0;
                """);
        });

        return button;
    }

    private VBox createAddMoviePanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("add-movie-panel"); // Add a new class

        Label titleLabel = new Label("➕ Add New Movie");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Input fields
        TextField titleInput = new TextField();
        titleInput.setPromptText("Movie Title");
        titleInput.setStyle("""
            -fx-font-size: 14px;
            -fx-padding: 10;
            -fx-background-radius: 8;
            -fx-border-color: #ddd;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            """);

        ComboBox<String> genreInput = new ComboBox<>();
        genreInput.setPromptText("Select Genre");
        genreInput.setPrefWidth(200);
        genreInput.setStyle("""
            -fx-font-size: 14px;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            """);

        // Use StarRating component instead of slider
        Label ratingLabel = new Label("Rating:");
        StarRating ratingInput = new StarRating(true, 18);
        ratingInput.setRating(5.0); // Default rating

        VBox ratingBox = new VBox(8);
        ratingBox.getChildren().addAll(ratingLabel, ratingInput);

        Button addButton = new Button("Add Movie");
        addButton.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-padding: 12 24 12 24;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """);

        addButton.setOnAction(e -> {
            String title = titleInput.getText().trim();
            String genre = genreInput.getValue();
            double rating = ratingInput.getRating();

            if (title.isEmpty() || genre == null) {
                showAlert(Alert.AlertType.WARNING, "Incomplete Information",
                        "Please fill in both title and genre.");
                return;
            }

            // Show loading state
            showLoadingState(LoadingIndicator.forSaving());

            // Add movie in background task
            Task<Void> addMovieTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Movie newMovie = new Movie(title, genre, rating,null);
                    db.addMovie(newMovie);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        titleInput.clear();
                        genreInput.setValue(null);
                        ratingInput.setRating(5.0);
                        filterPanel.updateGenres(db.getGenres());
                        hideLoadingState();
                        updateMovieDisplay();
                        showSuccessMessage("Movie added successfully!");
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        hideLoadingState();
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to add movie: " + getException().getMessage());
                    });
                }
            };

            new Thread(addMovieTask).start();
        });

        // Add hover effect to button
        addButton.setOnMouseEntered(e -> {
            addButton.setStyle("""
                -fx-background-color: #229954;
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

        addButton.setOnMouseExited(e -> {
            addButton.setStyle("""
                -fx-background-color: #27ae60;
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

        panel.getChildren().addAll(titleLabel, titleInput, genreInput, ratingBox, addButton);
        return panel;
    }

    private void buildLayout() {
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label appTitle = new Label("🎬 Recommndr");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        appTitle.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(appTitle, spacer, themeButton);

        // Left sidebar with filters and add movie
        VBox leftSidebar = new VBox(20);
        leftSidebar.setPrefWidth(320);
        leftSidebar.getChildren().addAll(filterPanel, addMoviePanel);

        ScrollPane leftScrollPane = new ScrollPane(leftSidebar);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setStyle("-fx-background-color: transparent;");

        // Layout
        root.setTop(header);
        root.setLeft(leftScrollPane);
        root.setCenter(contentArea);

        // Set minimum sizes
        root.setMinWidth(1000);
        root.setMinHeight(700);
    }

    private void loadMovies() {
        showLoadingState(LoadingIndicator.forMovieLoading());

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                InputStream is = getClass().getResourceAsStream("/movies.csv");
                if (is != null) {
                    db.loadMovies(is);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    filterPanel.updateGenres(db.getGenres());

                    // Update genre combo in add panel
                    ComboBox<String> genreCombo = (ComboBox<String>)
                            ((VBox) addMoviePanel).getChildren().get(2);
                    genreCombo.getItems().clear();
                    genreCombo.getItems().addAll(db.getGenres());

                    hideLoadingState();
                    updateMovieDisplay();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideLoadingState();
                    showEmptyState(EmptyStateView.EmptyStateType.FIRST_TIME_USER);
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void updateMovieDisplay() {
        if (db.getAllMovies().isEmpty()) {
            showEmptyState(EmptyStateView.EmptyStateType.NO_MOVIES_FOUND);
            return;
        }

        // Show loading for search/filter operations
        if (!filterPanel.getSearchText().isEmpty()) {
            showLoadingState(LoadingIndicator.forSearching());
        }

        Task<List<Movie>> filterTask = new Task<>() {
            @Override
            protected List<Movie> call() {
                return getFilteredAndSortedMovies();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Movie> filteredMovies = getValue();
                    hideLoadingState();
                    displayMovies(filteredMovies);
                });
            }
        };

        new Thread(filterTask).start();
    }

    private List<Movie> getFilteredAndSortedMovies() {
        String searchText = filterPanel.getSearchText().toLowerCase();
        double minRating = filterPanel.getMinRating();
        double maxRating = filterPanel.getMaxRating();
        String sortBy = filterPanel.getSortBy();
        boolean sortDescending = filterPanel.isSortDescending();

        List<Movie> filtered = new ArrayList<>();

        for (Movie movie : db.getAllMovies()) {
            // Apply filters
            boolean matchesSearch = searchText.isEmpty() ||
                    movie.getTitle().toLowerCase().contains(searchText);

            boolean matchesRating = movie.getRating() >= minRating &&
                    movie.getRating() <= maxRating;

            boolean matchesHighRated = !filterPanel.isHighRatedFilterActive() ||
                    movie.getRating() >= 8.0;

            // Apply genre filter
            String selectedGenre = filterPanel.getGenreComboBoxValue();
            boolean matchesGenre = "All Genres".equals(selectedGenre) || movie.getGenre().equalsIgnoreCase(selectedGenre);

            if (matchesSearch && matchesRating && matchesHighRated && matchesGenre) {
                filtered.add(movie);
            }
        }

        // Apply sorting
        Comparator<Movie> comparator;

        if ("Genre".equals(sortBy)) {
            comparator = Comparator.comparing(Movie::getGenre);
        } else if ("Title (A-Z)".equals(sortBy)) {
            comparator = Comparator.comparing(Movie::getTitle);
        } else if ("Rating".equals(sortBy)) {
            comparator = Comparator.comparingDouble(Movie::getRating);
        } else {
            // Default to Rating if no match
            comparator = Comparator.comparingDouble(Movie::getRating);
        }

        if (sortDescending) {
            comparator = comparator.reversed();
        }

        filtered.sort(comparator);
        return filtered;
    }

    private void displayMovies(List<Movie> movies) {
        movieGrid.getChildren().clear();

        if (movies.isEmpty()) {
            EmptyStateView.EmptyStateType emptyType;

            if (!filterPanel.getSearchText().isEmpty()) {
                emptyType = EmptyStateView.EmptyStateType.NO_SEARCH_RESULTS;
            } else if (filterPanel.getMinRating() > 0 || filterPanel.getMaxRating() < 10) {
                emptyType = EmptyStateView.EmptyStateType.NO_RATING_RESULTS;
            } else {
                emptyType = EmptyStateView.EmptyStateType.NO_MOVIES_FOUND;
            }

            showEmptyState(emptyType);
            return;
        }

        for (Movie movie : movies) {
            MovieCard card = new MovieCard(movie,
                    () -> editMovie(movie),
                    () -> deleteMovie(movie));
            movieGrid.getChildren().add(card);
        }
    }

    private void showLoadingState(LoadingIndicator loader) {
        if (!contentArea.getChildren().contains(loader)) {
            contentArea.getChildren().add(loader);
        }
        movieScrollPane.setVisible(false);
    }

    private void hideLoadingState() {
        contentArea.getChildren().removeIf(node -> node instanceof LoadingIndicator);
        movieScrollPane.setVisible(true);
    }

    private void showEmptyState(EmptyStateView.EmptyStateType type) {
        movieGrid.getChildren().clear();

        EmptyStateView emptyState = new EmptyStateView(type, () -> {
            switch (type) {
                case NO_SEARCH_RESULTS -> filterPanel.clearAllFilters();
                case NO_RATING_RESULTS -> filterPanel.clearAllFilters();
                case NO_MOVIES_FOUND, FIRST_TIME_USER -> {
                    // Scroll to add movie section or focus on it
                    // Implementation depends on your scroll setup
                }
            }
        });

        movieGrid.getChildren().add(emptyState);
    }

    private void editMovie(Movie movie) {
        // TODO: Implement edit dialog
        System.out.println("Edit: " + movie.getTitle());
    }

    private void deleteMovie(Movie movie) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete '" + movie.getTitle() + "'?",
                ButtonType.YES, ButtonType.NO);

        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Movie");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: Implement movie deletion from database
                System.out.println("Delete: " + movie.getTitle());
                updateMovieDisplay();
            }
        });
    }

    private void toggleTheme() {
        Scene scene = root.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                themeButton.setText("🌙 Dark Mode");
            } else {
                scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
                themeButton.setText("☀ Light Mode");
            }
            isDarkMode = !isDarkMode;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        // Create a temporary success notification
        Label successLabel = new Label("✅ " + message);
        successLabel.setStyle("""
            -fx-background-color: #d4edda;
            -fx-text-fill: #155724;
            -fx-padding: 10 15 10 15;
            -fx-background-radius: 5;
            -fx-border-color: #c3e6cb;
            -fx-border-radius: 5;
            -fx-border-width: 1;
            """);

        // Add to top of content area temporarily
        VBox tempContainer = new VBox(10);
        tempContainer.getChildren().addAll(successLabel, movieScrollPane);

        if (contentArea.getChildren().contains(movieScrollPane)) {
            contentArea.getChildren().remove(movieScrollPane);
        }
        contentArea.getChildren().add(tempContainer);

        // Remove after 3 seconds
        PauseTransition hideSuccess = new PauseTransition(Duration.seconds(3));
        hideSuccess.setOnFinished(e -> {
            contentArea.getChildren().remove(tempContainer);
            contentArea.getChildren().add(movieScrollPane);
        });
        hideSuccess.play();
    }

    public BorderPane getRoot() {
        return root;
    }
}