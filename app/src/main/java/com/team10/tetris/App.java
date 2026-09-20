package com.team10.tetris;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Tetris Team 10");

        Scene scene = new Scene(label, 800, 600);

        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}