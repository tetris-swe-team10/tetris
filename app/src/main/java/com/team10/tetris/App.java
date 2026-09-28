package com.team10.tetris;

import com.team10.tetris.ui.GameView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();

        Scene scene = new Scene(gameView, 540, 620);

        stage.setTitle("Tetris Team 10");
        stage.setScene(scene);
        stage.show();

        gameView.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}