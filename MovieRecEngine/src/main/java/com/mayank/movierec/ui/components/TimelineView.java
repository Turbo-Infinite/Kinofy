package com.mayank.movierec.ui.components;

import com.mayank.movierec.model.Movie;
import javafx.application.Platform; // Import Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip; // Import Tooltip
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class TimelineView extends BorderPane {

    private final Runnable onBackCallback;
    private final List<Movie> movies;
    private StackedAreaChart<String, Number> areaChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private AggregationPeriod currentAggregation = AggregationPeriod.DAILY;

    private enum AggregationPeriod { DAILY, WEEKLY, MONTHLY }

    // Store formatters for easy access
    private final DateTimeFormatter dailyFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter weeklyFormatter = DateTimeFormatter.ofPattern("'Week' w, yyyy");
    private final DateTimeFormatter monthlyFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());


    public TimelineView(List<Movie> movies, Runnable onBackCallback) {
        this.movies = movies;
        this.onBackCallback = onBackCallback;
        setupUI();
    }

    private void setupUI() {
        this.setPadding(new Insets(20));

        // --- Header (includes aggregation buttons) ---
        Button backButton = new Button("← Back to Movies");
        backButton.getStyleClass().add("theme-button");
        backButton.setOnAction(e -> onBackCallback.run());

        Label titleLabel = new Label("Your Emotional Timeline");
        titleLabel.getStyleClass().add("app-title");

        ToggleGroup periodToggleGroup = new ToggleGroup();
        RadioButton dailyButton = createPeriodButton("Daily", AggregationPeriod.DAILY, periodToggleGroup);
        RadioButton weeklyButton = createPeriodButton("Weekly", AggregationPeriod.WEEKLY, periodToggleGroup);
        RadioButton monthlyButton = createPeriodButton("Monthly", AggregationPeriod.MONTHLY, periodToggleGroup);
        dailyButton.setSelected(true);

        HBox periodSelectorBox = new HBox(10, new Label("View By:"), dailyButton, weeklyButton, monthlyButton);
        periodSelectorBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(20, backButton, titleLabel, periodSelectorBox);
        header.setAlignment(Pos.CENTER_LEFT);
        this.setTop(header);
        // --- End Header ---

        // --- Chart Setup ---
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Emotion Percentage (%)");
        areaChart = new StackedAreaChart<>(xAxis, yAxis);
        areaChart.setTitle("Proportion of Emotions Over Time");
        areaChart.setLegendVisible(true);
        areaChart.setAnimated(true); // Disable animation for easier tooltip attachment
        // --- End Chart Setup ---

        periodToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentAggregation = (AggregationPeriod) newVal.getUserData();
                updateChart();
            }
        });

        updateChart(); // Initial population
    }

    // Helper for creating radio buttons
    private RadioButton createPeriodButton(String text, AggregationPeriod period, ToggleGroup group) {
        RadioButton button = new RadioButton(text);
        button.setToggleGroup(group);
        button.setUserData(period);
        button.setStyle("-fx-text-fill: -fx-text-base-color;"); // For dark theme text
        return button;
    }


    private void updateChart() {
        ObservableList<XYChart.Series<String, Number>> chartData = getChartDataAsPercentage(currentAggregation);

        if (chartData.isEmpty()) {
            EmptyStateView emptyView = new EmptyStateView(EmptyStateView.EmptyStateType.NO_TIMELINE_DATA, onBackCallback);
            this.setCenter(emptyView);
        } else {
            switch (currentAggregation) {
                case DAILY: xAxis.setLabel("Date Tagged (Day)"); break;
                case WEEKLY: xAxis.setLabel("Date Tagged (Week)"); break;
                case MONTHLY: xAxis.setLabel("Date Tagged (Month)"); break;
            }
            areaChart.setData(chartData);
            this.setCenter(areaChart);

            // --- ADD TOOLTIPS AFTER DATA IS SET ---
            // We need to wait briefly for JavaFX to render the nodes
            Platform.runLater(this::addTooltipsToChart);
        }
    }

    // --- NEW METHOD TO ADD TOOLTIPS ---
    private void addTooltipsToChart() {
        // Tooltips work best on the symbols, not the area fill itself
        areaChart.setCreateSymbols(true); // Ensure symbols are created

        for (XYChart.Series<String, Number> series : areaChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode(); // This gets the symbol (dot)
                if (node != null && data.getYValue().doubleValue() > 0) { // Only add tooltips to non-zero points
                    node.setStyle("-fx-cursor: hand;"); // Change cursor on hover

                    // Find movies for this data point
                    List<String> movieTitles = findMoviesForDataPoint(data.getXValue(), series.getName(), currentAggregation);
                    String tooltipText = series.getName() + " (" + String.format("%.1f%%", data.getYValue().doubleValue()) + "):\n" +
                            String.join("\n", movieTitles);

                    Tooltip tooltip = new Tooltip(tooltipText);
                    Tooltip.install(node, tooltip);
                }
            }
        }
    }


    private ObservableList<XYChart.Series<String, Number>> getChartDataAsPercentage(AggregationPeriod period) {
        // --- Filtering and grouping movies remains the same ---
        List<Movie> taggedMovies = movies.stream()
                .filter(m -> m.getDateTagged() != null && m.getEmotions() != null && !m.getEmotions().isEmpty())
                .collect(Collectors.toList());

        if (taggedMovies.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        DateTimeFormatter formatter = getFormatter(period);
        // Using TreeMap ensures keys (dates/weeks/months) are naturally sorted
        Map<String, Map<String, Integer>> countsByPeriod = new TreeMap<>(getDateComparator(period));
        Set<String> allEmotions = new HashSet<>();

        for (Movie movie : taggedMovies) {
            String periodCategory = getPeriodCategory(movie.getDateTagged(), period);
            String[] emotions = movie.getEmotions().split(" ");
            Map<String, Integer> periodCounts = countsByPeriod.computeIfAbsent(periodCategory, k -> new HashMap<>());

            for (String emotion : emotions) {
                periodCounts.put(emotion, periodCounts.getOrDefault(emotion, 0) + 1);
                allEmotions.add(emotion);
            }
        }

        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        List<String> sortedPeriodCategories = new ArrayList<>(countsByPeriod.keySet()); // Keys are already sorted

        for (String periodCategory : sortedPeriodCategories) {
            Map<String, Integer> periodCounts = countsByPeriod.get(periodCategory);
            double totalEmotionsThisPeriod = periodCounts.values().stream().mapToInt(Integer::intValue).sum();

            if (totalEmotionsThisPeriod == 0) continue;

            for (String emotion : allEmotions) {
                int count = periodCounts.getOrDefault(emotion, 0);
                double percentage = (count / totalEmotionsThisPeriod) * 100.0;

                XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(emotion, e -> {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(e);
                    // Pre-fill with zero points for all categories
                    for (String cat : sortedPeriodCategories) {
                        newSeries.getData().add(new XYChart.Data<>(cat, 0.0));
                    }
                    return newSeries;
                });

                // Find and update the correct data point
                series.getData().stream()
                        .filter(d -> d.getXValue().equals(periodCategory))
                        .findFirst()
                        .ifPresent(d -> d.setYValue(percentage));
            }
        }

        // --- THIS IS THE FIX: REVERSE THE DEFAULT ORDER ---
        // Convert map values to a list
        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>(seriesMap.values());

        // Simply reverse the list to flip the stacking order
        Collections.reverse(seriesList);
        // --- END OF FIX ---


        // Final sort of data points within each series (important)
        Comparator<String> keyComparator = getDateComparator(period);
        for(XYChart.Series<String, Number> series : seriesList){ // Use the (now reversed) list
            series.getData().sort(Comparator.comparing(XYChart.Data::getXValue, keyComparator));
        }

        return FXCollections.observableArrayList(seriesList); // Return the reversed list
    }

    // --- NEW HELPER METHODS ---
    private String getPeriodCategory(LocalDate date, AggregationPeriod period) {
        switch (period) {
            case WEEKLY:
                int weekOfYear = date.get(weekFields.weekOfWeekBasedYear());
                int year = date.get(weekFields.weekBasedYear());
                return String.format("Week %d, %d", weekOfYear, year); // Use for sorting/grouping key
            case MONTHLY:
                return date.format(monthlyFormatter);
            case DAILY:
            default:
                return date.format(dailyFormatter);
        }
    }

    private DateTimeFormatter getFormatter(AggregationPeriod period){
        switch (period) {
            case WEEKLY: return weeklyFormatter;
            case MONTHLY: return monthlyFormatter;
            case DAILY: default: return dailyFormatter;
        }
    }

    private Comparator<String> getDateComparator(AggregationPeriod period) {
        DateTimeFormatter parser;
        switch (period) {
            case WEEKLY:
                // Custom parsing for "Week W, YYYY" format
                return Comparator.comparing(weekStr -> {
                    try {
                        String[] parts = weekStr.replace("Week ", "").split(", ");
                        int week = Integer.parseInt(parts[0]);
                        int year = Integer.parseInt(parts[1]);
                        // Return a date representing the start of that week for comparison
                        return LocalDate.now().withYear(year).with(weekFields.weekOfWeekBasedYear(), week).with(weekFields.dayOfWeek(), 1);
                    } catch (Exception e) { return LocalDate.MIN; } // Fallback for parse errors
                });
            case MONTHLY:
                parser = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
                return Comparator.comparing(monthStr -> LocalDate.parse("01 " + monthStr, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)));
            case DAILY:
            default:
                parser = dailyFormatter;
                return Comparator.comparing(dateStr -> LocalDate.parse(dateStr, parser));
        }
    }

    // Updated to handle different date formats based on aggregation period
    private List<String> findMoviesForDataPoint(String periodCategory, String emotion, AggregationPeriod period) {
        DateTimeFormatter currentFormatter = getFormatter(period);
        return movies.stream()
                .filter(m -> m.getDateTagged() != null)
                .filter(m -> getPeriodCategory(m.getDateTagged(), period).equals(periodCategory)) // Filter by the correct period string
                .filter(m -> m.getEmotions() != null && Arrays.asList(m.getEmotions().split(" ")).contains(emotion))
                .map(Movie::getTitle)
                .collect(Collectors.toList());
    }
}