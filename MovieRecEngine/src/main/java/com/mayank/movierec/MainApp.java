package com.mayank.movierec;

import com.mayank.movierec.ui.MainUI;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {


        MainUI ui = new MainUI();

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        Scene scene = new Scene(ui.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Kinofy");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}