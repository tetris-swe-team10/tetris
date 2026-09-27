package com.team10.tetris;

import java.util.Map;

import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.screen.StartMenuAction;
import com.team10.tetris.screen.StartScreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        MenuKeyBindings keyBindings = MenuKeyBindings.defaults();
        StartScreen startScreen = new StartScreen(
                Map.of(StartMenuAction.EXIT, Platform::exit),
                keyBindings);
        Scene scene = new Scene(startScreen, 960, 640);
        scene.getStylesheets().add(
                App.class.getResource("/com/team10/tetris/start-screen.css").toExternalForm());

        stage.setTitle("Tetris");
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
        startScreen.requestMenuFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}
