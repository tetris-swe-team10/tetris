package com.team10.tetris;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.Tetromino;
import com.team10.tetris.game.TetrominoType;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.ui.GameOverView;
import com.team10.tetris.ui.GameView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 프로그램 진입점 및 화면 전환 담당.
 *
 * <p>시작 메뉴와 설정 화면은 아직 구현되지 않아 게임 화면에서 시작한다.
 */
public class App extends Application {

    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 640;

    private final ScoreboardManager scoreboardManager =
            new ScoreboardManager();

    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(createGameView(), WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();
    }

    /** 화면을 교체한다. 시작 메뉴가 추가되면 이 메서드를 재사용할 수 있다. */
    private void showScreen(Parent screen) {
        scene.setRoot(screen);
        screen.requestFocus();
    }

    private GameView createGameView() {
        GameEngine engine = new GameEngine(
                new Board(),
                new Tetromino(TetrominoType.T, 0, 3)
        );

        GameView gameView = new GameView(engine);

        gameView.setOnGameOver(
                () -> showGameOver(
                        gameView.getScore(),
                        engine.getTotalClearedLines()
                )
        );

        return gameView;
    }

    private void showGameOver(int score, int clearedLines) {
        GameOverView gameOverView = new GameOverView(scoreboardManager);

        gameOverView.setOnRestart(() -> showScreen(createGameView()));

        // 시작 메뉴가 구현되면 해당 화면으로 연결
        gameOverView.setOnMainMenu(() -> showScreen(createGameView()));

        gameOverView.setOnExit(Platform::exit);

        showScreen(gameOverView);
        gameOverView.showResult(score, clearedLines);
    }

    public static void main(String[] args) {
        launch();
    }
}