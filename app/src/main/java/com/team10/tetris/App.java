package com.team10.tetris;

import com.team10.tetris.controller.AppController;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.settings.GameSettings;
import com.team10.tetris.settings.SettingsManager;

import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX 진입점. 모델과 애플리케이션 컨트롤러를 조립한다. */
public class App extends Application {
    @Override
    public void start(Stage stage) {
        new AppController(stage, new GameSettings(new SettingsManager()),
                new ScoreboardManager()).start();
    }

    public static void main(String[] args) { launch(); }
}
