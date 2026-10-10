package com.team10.tetris;

import java.io.IOException;
import java.util.Map;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.Tetromino;
import com.team10.tetris.game.TetrominoType;
import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.StartMenuAction;
import com.team10.tetris.screen.StartScreen;
import com.team10.tetris.settings.DisplayMode;
import com.team10.tetris.settings.GameSettings;
import com.team10.tetris.settings.PauseSettingsPane;
import com.team10.tetris.settings.SettingsScreen;
import com.team10.tetris.ui.GameOverView;
import com.team10.tetris.ui.GameView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** 프로그램 진입점 및 화면 전환 담당. */
public class App extends Application {

        private static final int WINDOW_WIDTH = 960;
        private static final int WINDOW_HEIGHT = 640;

        private final ScoreboardManager scoreboardManager = new ScoreboardManager();
        private final GameSettings settings = new GameSettings();

        private Scene scene;
        private Stage stage;

        @Override
        public void start(Stage stage) {
                this.stage = stage;
                StartScreen startScreen = createStartScreen();
                scene = new Scene(startScreen, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene.getStylesheets().add(
                                App.class.getResource(
                                                "/com/team10/tetris/start-screen.css").toExternalForm());
                scene.getStylesheets().add(
                                App.class.getResource(
                                                "/com/team10/tetris/settings.css").toExternalForm());

                stage.setTitle("Tetris");
                stage.setMinWidth(560);
                stage.setMinHeight(560);
                stage.setScene(scene);
                stage.show();

                startScreen.requestMenuFocus();
        }

        private StartScreen createStartScreen() {
                return new StartScreen(
                                Map.of(
                                                StartMenuAction.START_GAME,
                                                () -> showScreen(createGameView()),
                                                StartMenuAction.OPEN_SETTINGS,
                                                this::showSettingsScreen,
                                                StartMenuAction.EXIT,
                                                Platform::exit),
                                MenuKeyBindings.defaults());
        }

        private void showStartMenu() {
                StartScreen startScreen = createStartScreen();
                showScreen(startScreen);
                startScreen.requestMenuFocus();
        }

        private void showScreen(Parent screen) {
                scene.setRoot(screen);
                screen.requestFocus();
        }

        private GameView createGameView() {
                GameEngine engine = new GameEngine(
                                new Board(),
                                new Tetromino(TetrominoType.T, 0, 3));

                GameView gameView = new GameView(engine, settings);
                gameView.setOnSettingsRequested(
                                () -> showPauseSettings(gameView));
                gameView.setOnGameOver(
                                () -> showGameOver(
                                                gameView.getScore(),
                                                engine.getTotalClearedLines()));

                return gameView;
        }

        private void showSettingsScreen() {
                SettingsScreen settingsScreen = new SettingsScreen(
                                settings,
                                this::applyDisplayMode,
                                this::resetScores,
                                this::showStartMenu);
                showScreen(settingsScreen);
        }

        private void showPauseSettings(GameView gameView) {
                gameView.setDisable(true);
                StackPane layeredGame = new StackPane();
                PauseSettingsPane settingsPane = new PauseSettingsPane(
                                settings,
                                this::applyDisplayMode,
                                this::resetScores,
                                () -> {
                                        layeredGame.getChildren().remove(gameView);
                                        gameView.setDisable(false);
                                        showScreen(gameView);
                                        gameView.returnFromSettings();
                                },
                                this::showStartMenu);

                showScreen(layeredGame);
                layeredGame.getChildren().addAll(gameView, settingsPane);
                settingsPane.requestFocus();
        }

        private void applyDisplayMode(DisplayMode mode) {
                stage.setFullScreen(mode == DisplayMode.FULL_SCREEN);
                if (mode == DisplayMode.FULL_SCREEN) {
                        return;
                }

                if (mode == DisplayMode.PORTRAIT) {
                        stage.setWidth(640);
                        stage.setHeight(900);
                } else {
                        stage.setWidth(WINDOW_WIDTH);
                        stage.setHeight(WINDOW_HEIGHT);
                }
                stage.centerOnScreen();
        }

        private void resetScores() {
                Alert confirmation = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "저장된 모든 점수를 삭제할까요?",
                                ButtonType.OK,
                                ButtonType.CANCEL);
                confirmation.initOwner(stage);
                confirmation.setHeaderText("점수 초기화");
                if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return;
                }

                Alert alert;
                try {
                        scoreboardManager.clear();
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("점수가 초기화되었습니다.");
                } catch (IOException exception) {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("점수를 초기화하지 못했습니다.");
                        alert.setContentText(exception.getMessage());
                }
                alert.initOwner(stage);
                alert.showAndWait();
        }

        private void showGameOver(int score, int clearedLines) {
                GameOverView gameOverView = new GameOverView(scoreboardManager);

                gameOverView.setOnRestart(
                                () -> showScreen(createGameView()));
                gameOverView.setOnMainMenu(this::showStartMenu);
                gameOverView.setOnExit(Platform::exit);

                showScreen(gameOverView);
                gameOverView.showResult(score, clearedLines);
        }

        public static void main(String[] args) {
                launch();
        }
}