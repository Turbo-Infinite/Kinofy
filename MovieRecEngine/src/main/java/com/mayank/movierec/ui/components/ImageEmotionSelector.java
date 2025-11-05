package com.mayank.movierec.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ImageEmotionSelector extends FlowPane {

    private final Set<String> selectedEmotions = new HashSet<>();
    // Maps the emoji character to its image file path
    private final Map<String, String> emotionImageMap = new LinkedHashMap<>();

    public ImageEmotionSelector(String currentEmotions) {
        super(10, 10);
        this.setAlignment(Pos.CENTER_LEFT);

        // Initialize the map of emojis to their image paths
        emotionImageMap.put("😄", "/images/emojis/happy.png");
        emotionImageMap.put("😢", "/images/emojis/sad.png");
        emotionImageMap.put("😂", "/images/emojis/laughing.png");
        emotionImageMap.put("😡", "/images/emojis/angry.png");
        emotionImageMap.put("😱", "/images/emojis/shocked.png");
        emotionImageMap.put("😍", "/images/emojis/love.png");
        emotionImageMap.put("🤔", "/images/emojis/thinking.png");

        if (currentEmotions != null && !currentEmotions.isEmpty()) {
            selectedEmotions.addAll(Arrays.asList(currentEmotions.split(" ")));
        }

        for (Map.Entry<String, String> entry : emotionImageMap.entrySet()) {
            String emojiChar = entry.getKey();
            String imagePath = entry.getValue();

            ImageView emojiView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            emojiView.setFitWidth(32);
            emojiView.setFitHeight(32);

            ToggleButton emojiButton = new ToggleButton();
            emojiButton.setGraphic(emojiView);
            emojiButton.getStyleClass().add("image-emoji-button");
            emojiButton.setUserData(emojiChar); // Store the character

            if (selectedEmotions.contains(emojiChar)) {
                emojiButton.setSelected(true);
            }

            emojiButton.setOnAction(e -> {
                if (emojiButton.isSelected()) {
                    selectedEmotions.add(emojiChar);
                } else {
                    selectedEmotions.remove(emojiChar);
                }
            });
            getChildren().add(emojiButton);
        }
    }

    public String getSelectedEmotions() {
        return selectedEmotions.stream().collect(Collectors.joining(" "));
    }
}