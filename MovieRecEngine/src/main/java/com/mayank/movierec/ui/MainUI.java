package com.mayank.movierec.ui;

import com.mayank.movierec.data.MovieDatabase;
import com.mayank.movierec.logic.NlpUtils;
// REMOVED: import com.mayank.movierec.logic.RecommenderService;
import com.mayank.movierec.logic.TmdbApiClient;
import com.mayank.movierec.model.Movie;
import com.mayank.movierec.ui.components.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MainUI {
    // --- Fields ---
    private final BorderPane root;
    private final AdvancedFilterPanel filterPanel;
    private final FlowPane movieGrid;
    private final ScrollPane movieScrollPane;
    private final MovieDatabase db;
    private final Button themeButton;
    private final Button timelineButton;
    private final VBox addMoviePanel;
    private final StackPane contentArea;
    private ScrollPane leftScrollPane;
    private final List<MovieCard> movieCards = new ArrayList<>();
    private boolean isDarkMode = false;
    private PauseTransition searchDebouncer;
    private File selectedPosterFile;
    private final NlpUtils nlpUtils = new NlpUtils();
    // REMOVED: private final RecommenderService recommenderService;

    // Class field for the Add Movie genre ComboBox
    private ComboBox<String> addMovieGenreInput;

    // --- Constructor ---
    public MainUI() {
        root = new BorderPane();
        root.setId("mainUIRoot");
        root.setPadding(new Insets(20));
        db = new MovieDatabase();
        // REMOVED: recommenderService initialization
        filterPanel = new AdvancedFilterPanel();
        movieGrid = new FlowPane(15, 15);
        movieScrollPane = new ScrollPane();
        themeButton = createThemeButton();
        timelineButton = createTimelineButton();
        // createAddMoviePanel now initializes addMovieGenreInput
        addMoviePanel = createAddMoviePanel();
        contentArea = new StackPane();

        setupComponents();
        setupEventHandlers();
        buildLayout();
        loadMovies();
    }

    // --- UI Creation Methods ---

    private VBox createAddMoviePanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("add-movie-panel");

        Label titleLabel = new Label("➕ Add New Movie");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        TextField titleInput = new TextField();
        titleInput.setPromptText("Movie Title");

        // Initialize the class field
        addMovieGenreInput = new ComboBox<>();
        addMovieGenreInput.setPromptText("Select Genre");
        addMovieGenreInput.setPrefWidth(200);

        Label ratingLabel = new Label("Rating:");
        StarRating ratingInput = new StarRating(true, 18);
        ratingInput.setRating(5.0);
        VBox ratingBox = new VBox(8, ratingLabel, ratingInput);

        Button importPosterButton = new Button("Import Poster");
        Label posterFileLabel = new Label("No poster selected");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        importPosterButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                selectedPosterFile = file;
                posterFileLabel.setText(selectedPosterFile.getName());
            }
        });
        HBox posterBox = new HBox(10, importPosterButton, posterFileLabel);
        posterBox.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("Add Movie");
        addButton.getStyleClass().add("action-button-primary");
        // Pass controls needed for resetting and updating
        addButton.setOnAction(e -> handleAddMovieAction(titleInput, ratingInput, posterFileLabel));

        panel.getChildren().addAll(titleLabel, titleInput, addMovieGenreInput, ratingBox, posterBox, addButton);
        return panel;
    }

    // --- Core Logic Methods ---

    private void loadMovies() {
        List<Movie> existingMovies = db.getAllMovies();
        if (!existingMovies.isEmpty()) {
            Platform.runLater(() -> {
                populateGenreDropdowns(); // Use helper method
                updateMovieDisplay();
            });
            return;
        }

        showLoadingState(LoadingIndicator.forMovieLoading());
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                TmdbApiClient apiClient = new TmdbApiClient();
                apiClient.fetchGenres();
                List<TmdbApiClient.TmdbMovie> popularMovies = apiClient.getMoviesFrom2000Onwards();
                for (TmdbApiClient.TmdbMovie tmdbMovie : popularMovies) {
                    String genreName = "Unknown";
                    if (tmdbMovie.genreIds != null && !tmdbMovie.genreIds.isEmpty()) {
                        genreName = apiClient.getGenreName(tmdbMovie.genreIds.get(0));
                    }
                    Movie movie = new Movie(tmdbMovie.title, genreName, tmdbMovie.voteAverage, tmdbMovie.posterPath, "", "", "", null);
                    db.addMovie(movie);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    populateGenreDropdowns(); // Use helper method
                    hideLoadingState();
                    updateMovieDisplay();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideLoadingState();
                    showAlert(Alert.AlertType.ERROR, "API Error", "Failed to load initial movies from TMDB.");
                });
            }
        };
        new Thread(loadTask).start();
    }

    private void handleAddMovieAction(TextField titleInput, StarRating ratingInput, Label posterFileLabel) {
        String title = titleInput.getText().trim();
        String genre = addMovieGenreInput.getValue(); // Use class field
        double rating = ratingInput.getRating();
        String posterPath = null;

        if (title.isEmpty() || genre == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Information", "Please fill in both title and genre.");
            return;
        }

        if (selectedPosterFile != null) {
            try {
                Path postersDir = Path.of("src/main/resources/images/posters");
                Files.createDirectories(postersDir);
                // Ensure unique filename or handle collisions if necessary
                Path newPath = postersDir.resolve(selectedPosterFile.getName());
                Files.copy(selectedPosterFile.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
                posterPath = newPath.toString(); // Store path relative to project root
            } catch (IOException ioException) {
                ioException.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not save the poster image.");
                // Optionally return or proceed without poster
            }
        }

        Movie newMovie = new Movie(title, genre, rating, posterPath, "", "", "", LocalDate.now());
        db.addMovie(newMovie);

        // Reset UI fields
        titleInput.clear();
        addMovieGenreInput.setValue(null);
        ratingInput.setRating(5.0);
        posterFileLabel.setText("No poster selected");
        selectedPosterFile = null;

        populateGenreDropdowns(); // Repopulate BOTH dropdowns
        updateMovieDisplay();
        showSuccessMessage("Movie added successfully!");
    }

    private void populateGenreDropdowns() {
        List<String> genres = db.getGenres();
        filterPanel.updateGenres(genres); // Update filter panel's dropdown
        if (addMovieGenreInput != null) {
            // Preserve current selection if any, otherwise reset
            String currentSelection = addMovieGenreInput.getValue();
            addMovieGenreInput.getItems().setAll(genres);
            if (currentSelection != null && genres.contains(currentSelection)) {
                addMovieGenreInput.setValue(currentSelection);
            } else {
                addMovieGenreInput.setPromptText("Select Genre"); // Ensure prompt text is shown
            }
        }
    }

    private void editMovie(Movie movie) {
        EditMovieDialog dialog = new EditMovieDialog(movie);
        dialog.getDialogPane().getStylesheets().addAll(root.getScene().getStylesheets());
        Optional<Movie> result = dialog.showAndWait();
        result.ifPresent(updatedMovie -> {
            db.updateMovieDetails(movie, updatedMovie.getEmotions(), updatedMovie.getNotes(), updatedMovie.getTags());
            populateGenreDropdowns(); // Repopulate genres in case new one was added via edit (future)
            updateMovieDisplay();
            showSuccessMessage("Movie updated successfully!");
        });
    }

    // REMOVED: findSimilarMovies method
    // REMOVED: showRecommendations method

    private List<Movie> getFilteredAndSortedMovies() {
        String searchText = filterPanel.getSearchText().toLowerCase();
        String stemmedSearchText = nlpUtils.getStemmedText(searchText);

        List<Movie> filtered = db.getAllMovies().stream()
                .filter(movie -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            nlpUtils.getStemmedText(movie.getTitle()).contains(stemmedSearchText) ||
                            (movie.getNotes() != null && nlpUtils.getStemmedText(movie.getNotes()).contains(stemmedSearchText));

                    boolean matchesRating = movie.getRating() >= filterPanel.getMinRating() && movie.getRating() <= filterPanel.getMaxRating();
                    boolean matchesHighRated = !filterPanel.isHighRatedFilterActive() || movie.getRating() >= 8.0;
                    String selectedGenre = filterPanel.getGenreComboBoxValue();
                    boolean matchesGenre = "All Genres".equals(selectedGenre) || movie.getGenre().equalsIgnoreCase(selectedGenre);

                    return matchesSearch && matchesRating && matchesHighRated && matchesGenre;
                })
                .collect(Collectors.toList());

        Comparator<Movie> comparator;
        switch (Optional.ofNullable(filterPanel.getSortBy()).orElse("Rating")) { // Handle null sort value
            case "Genre":
                comparator = Comparator.comparing(Movie::getGenre, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Title (A-Z)":
                comparator = Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            default: // Default to Rating
                comparator = Comparator.comparingDouble(Movie::getRating);
                break;
        }

        if (filterPanel.isSortDescending()) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);
        return filtered;
    }

    private void displayMovies(List<Movie> movies) {
        movieGrid.getChildren().clear();
        movieCards.clear();
        if (movies.isEmpty()) {
            EmptyStateView.EmptyStateType emptyType = getEmptyStateType();
            showEmptyState(emptyType);
            return;
        }
        for (Movie movie : movies) {
            // REMOVED: recommendation callback from MovieCard creation
            MovieCard card = new MovieCard(movie,
                    () -> editMovie(movie),
                    () -> deleteMovie(movie)
                    // Removed the 4th argument
            );
            movieGrid.getChildren().add(card);
            movieCards.add(card);
        }
        Platform.runLater(this::loadVisibleImages);
    }

    private EmptyStateView.EmptyStateType getEmptyStateType() {
        if (filterPanel.getSearchText() != null && !filterPanel.getSearchText().isEmpty()) {
            return EmptyStateView.EmptyStateType.NO_SEARCH_RESULTS;
        }
        boolean ratingFiltered = filterPanel.getMinRating() > 0 || filterPanel.getMaxRating() < 10;
        boolean genreFiltered = filterPanel.getGenreComboBoxValue() != null && !"All Genres".equals(filterPanel.getGenreComboBoxValue());
        if(ratingFiltered || genreFiltered){
            return EmptyStateView.EmptyStateType.NO_RATING_RESULTS; // Or a specific NO_FILTER_RESULTS if added
        }
        if (db.getAllMovies().isEmpty()) {
            return EmptyStateView.EmptyStateType.NO_MOVIES_FOUND;
        }
        return EmptyStateView.EmptyStateType.NO_MOVIES_FOUND; // Default if filters yield none but DB has movies
    }

    // --- Other Helper Methods (Unchanged unless noted) ---

    private Button createTimelineButton() {
        Button button = new Button("📊 Timeline");
        button.getStyleClass().add("theme-button");
        button.setOnAction(e -> showTimelineView());
        return button;
    }

    private void showTimelineView() {
        List<Movie> allMovies = db.getAllMovies();
        TimelineView timelineView = new TimelineView(allMovies, this::showMainMovieGrid);
        timelineView.getStylesheets().addAll(root.getScene().getStylesheets());
        root.setCenter(timelineView);
        root.setLeft(null);
    }

    private void showMainMovieGrid() {
        root.setCenter(contentArea);
        root.setLeft(leftScrollPane);
    }

    private void setupComponents() {
        movieGrid.setAlignment(Pos.CENTER);
        movieGrid.setPadding(new Insets(20));
        movieGrid.setHgap(15);
        movieGrid.setVgap(15);
        movieScrollPane.setContent(movieGrid);
        movieScrollPane.setFitToWidth(true);
        movieScrollPane.setFitToHeight(true);
        movieScrollPane.setStyle("-fx-background-color: transparent;-fx-background: transparent;");
        contentArea.getChildren().add(movieScrollPane);
        searchDebouncer = new PauseTransition(Duration.millis(300));
        searchDebouncer.setOnFinished(e -> updateMovieDisplay());
    }

    private void setupEventHandlers() {
        filterPanel.setOnFilterChanged(() -> {
            if (filterPanel.getSearchText() != null && !filterPanel.getSearchText().isEmpty()) {
                searchDebouncer.playFromStart();
            } else {
                updateMovieDisplay();
            }
        });
        themeButton.setOnAction(e -> toggleTheme());
        movieScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> loadVisibleImages());
    }

    private Button createThemeButton() {
        Button button = new Button("🌙 Dark Mode");
        button.getStyleClass().add("theme-button");
        return button;
    }

    private HBox createAppHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        Label appTitle = new Label(" Kinofy");
        appTitle.getStyleClass().add("app-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(appTitle, spacer, timelineButton, themeButton);
        return header;
    }

    private void buildLayout() {
        HBox header = createAppHeader();
        VBox leftSidebarVBox = new VBox(20);
        leftSidebarVBox.setPrefWidth(320);
        leftSidebarVBox.getChildren().addAll(filterPanel, addMoviePanel);
        leftScrollPane = new ScrollPane(leftSidebarVBox);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setStyle("-fx-background-color: transparent;");
        root.setTop(header);
        root.setLeft(leftScrollPane);
        root.setCenter(contentArea);
        root.setMinWidth(1000);
        root.setMinHeight(700);
    }

    private void updateMovieDisplay() {
        // Use Platform.runLater for UI updates triggered potentially off-thread
        Platform.runLater(() -> {
            List<Movie> filteredMovies = getFilteredAndSortedMovies();
            displayMovies(filteredMovies);
        });
    }


    private void loadVisibleImages() {
        if (movieGrid.getChildren().isEmpty() || movieCards.isEmpty()) return;
        Platform.runLater(() -> { // Ensure UI updates happen on JavaFX thread
            Bounds viewportBounds = movieScrollPane.getViewportBounds();
            if (viewportBounds == null) return;
            for (MovieCard card : movieCards) {
                if (card == null || card.getParent() == null) continue;
                Bounds cardBounds = card.localToParent(card.getBoundsInLocal());
                if (cardBounds != null && viewportBounds.intersects(cardBounds)) {
                    card.loadImage();
                }
            }
        });
    }

    private void showLoadingState(LoadingIndicator loader) {
        Platform.runLater(() -> { // Ensure UI updates happen on JavaFX thread
            if (!contentArea.getChildren().contains(loader)) {
                contentArea.getChildren().add(loader);
            }
            movieScrollPane.setVisible(false);
        });
    }

    private void hideLoadingState() {
        Platform.runLater(() -> { // Ensure UI updates happen on JavaFX thread
            contentArea.getChildren().removeIf(node -> node instanceof LoadingIndicator);
            movieScrollPane.setVisible(true);
        });
    }

    private void showEmptyState(EmptyStateView.EmptyStateType type) {
        Platform.runLater(() -> { // Ensure UI updates happen on JavaFX thread
            movieGrid.getChildren().clear();
            movieCards.clear();
            EmptyStateView emptyState = new EmptyStateView(type, () -> {
                if (type == EmptyStateView.EmptyStateType.NO_SEARCH_RESULTS || type == EmptyStateView.EmptyStateType.NO_RATING_RESULTS) {
                    filterPanel.clearAllFilters();
                }
            });
            // Ensure EmptyStateView is added correctly
            if (!movieGrid.getChildren().contains(emptyState)) {
                movieGrid.getChildren().add(emptyState);
            }
        });
    }

    private void deleteMovie(Movie movie) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + movie.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Movie");
        confirmation.getDialogPane().getStylesheets().addAll(root.getScene().getStylesheets());
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                db.deleteMovie(movie);
                updateMovieDisplay(); // Refresh the grid
                showSuccessMessage("Movie deleted successfully!"); // Give feedback
            }
        });
    }

    private void toggleTheme() {
        Scene scene = root.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            String cssPath = isDarkMode ? "/styles.css" : "/dark.css";
            try {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
                themeButton.setText(isDarkMode ? "🌙 Dark Mode" : "☀️ Light Mode");
                isDarkMode = !isDarkMode;
            } catch (NullPointerException e) {
                System.err.println("Error loading CSS file: " + cssPath);
                // Optionally load a default or show an error
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (root.getScene() != null) { // Check if scene exists
            alert.getDialogPane().getStylesheets().addAll(root.getScene().getStylesheets());
        }
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Toast.show(message, contentArea); // Use the simpler call
    }

    public BorderPane getRoot() {
        return root;
    }
}