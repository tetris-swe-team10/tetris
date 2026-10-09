package com.team10.tetris;

import java.util.Map;

import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameMode;
import com.team10.tetris.game.Difficulty;
import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.StartMenuAction;
import com.team10.tetris.screen.StartScreen;
import com.team10.tetris.ui.GameOverView;
import com.team10.tetris.ui.GameView;
import com.team10.tetris.ui.ScoreboardView;
import javafx.scene.control.ChoiceDialog;

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
        private Difficulty lastSelectedDifficulty = Difficulty.NORMAL;

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
                                                this::chooseDifficulty,
                                                StartMenuAction.OPEN_SCOREBOARD,
                                                this::showScoreboard,
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

        private void chooseDifficulty() {
                ChoiceDialog<Difficulty> dialog = new ChoiceDialog<>(
                                lastSelectedDifficulty, Difficulty.values());
                dialog.initOwner(scene.getWindow());
                dialog.setTitle("게임 난이도 선택");
                dialog.setHeaderText("시작할 난이도를 선택하세요. 한 판 동안 변경되지 않습니다.");
                dialog.setContentText("난이도");
                ((javafx.scene.control.Button) dialog.getDialogPane()
                        .lookupButton(javafx.scene.control.ButtonType.OK)).setText("게임 시작");
                dialog.showAndWait().ifPresent(difficulty -> {
                        lastSelectedDifficulty = difficulty;
                        showScreen(createGameView(new GameConfig(GameMode.NORMAL, difficulty)));
                });
        }

        private void showScoreboard() {
                ScoreboardView view = new ScoreboardView(scoreboardManager);
                view.setOnBackRequested(this::showStartMenu);
                showScreen(view);
        }

        private GameView createGameView(GameConfig config) {
                GameEngine engine = new GameEngine(config);

                GameView gameView = new GameView(engine);
                gameView.setOnGameOver(
                                () -> showGameOver(
                                                gameView.getScore(),
                                                engine.getTotalClearedLines(), config));

                return gameView;
        }

        private void showGameOver(int score, int clearedLines, GameConfig config) {
                GameOverView gameOverView = new GameOverView(scoreboardManager);

                gameOverView.setOnRestart(
                                () -> showScreen(createGameView(config)));
                gameOverView.setOnMainMenu(this::showStartMenu);
                gameOverView.setOnExit(Platform::exit);

                showScreen(gameOverView);
                gameOverView.showResult(score, clearedLines, config);
        }

        public static void main(String[] args) {
                launch();
        }
}
