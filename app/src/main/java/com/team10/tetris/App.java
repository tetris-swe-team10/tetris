package com.team10.tetris;

import java.util.Map;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.Tetromino;
import com.team10.tetris.game.TetrominoType;
import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.StartMenuAction;
import com.team10.tetris.screen.StartScreen;
import com.team10.tetris.ui.GameOverView;
import com.team10.tetris.ui.GameView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** 프로그램 진입점 및 화면 전환 담당. */
public class App extends Application {

        private static final int WINDOW_WIDTH = 960;
        private static final int WINDOW_HEIGHT = 640;

        private final ScoreboardManager scoreboardManager = new ScoreboardManager();

        private Scene scene;

        @Override
        public void start(Stage stage) {
                StartScreen startScreen = createStartScreen();
                scene = new Scene(startScreen, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene.getStylesheets().add(
                                App.class.getResource(
                                                "/com/team10/tetris/start-screen.css").toExternalForm());

                stage.setTitle("Tetris");
                stage.setMinWidth(800);
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

                GameView gameView = new GameView(engine);
                gameView.setOnGameOver(
                                () -> showGameOver(
                                                gameView.getScore(),
                                                engine.getTotalClearedLines()));

                return gameView;
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