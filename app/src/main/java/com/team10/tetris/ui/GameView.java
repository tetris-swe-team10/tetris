package com.team10.tetris.ui;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameController;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.GameState;
import com.team10.tetris.game.PieceSnapshot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameView extends BorderPane {

    private static final int CELL_SIZE = 28;
    private static final int TIMER_STEP_MS = 50;

    private final GameEngine engine;
    private final GameController controller;
    private GameSnapshot snapshot;
    private final Canvas boardCanvas;
    private final Canvas nextCanvas;

    private final Label scoreLabel = new Label();
    private final Label linesLabel = new Label();
    private final Label statusLabel = new Label();

    private final Button pauseButton = new Button("일시정지");
    private final Button settingsButton = new Button("설정");

    private final Timeline dropTimer;

    private long lastUpdateNanos;

    private boolean settingsOpenedWhilePaused = false;
    private boolean gameOverNotified = false;

    private Runnable onSettingsRequested = () -> {
    };
    private Runnable onGameOver = () -> {
    };

    public GameView() {
        this(new GameEngine(GameConfig.DEFAULT));
    }

    public GameView(GameEngine engine) {
        this.engine = engine;
        controller = new GameController(engine);

        boardCanvas = new Canvas(
                Board.WIDTH * CELL_SIZE,
                Board.HEIGHT * CELL_SIZE);

        nextCanvas = new Canvas(120, 120);

        VBox rightPanel = new VBox(
                18,
                new Label(engine.getConfig().mode() + " / " + engine.getConfig().difficulty()),
                new Label("SCORE"),
                scoreLabel,
                new Label("LINES"),
                linesLabel,
                new Label("NEXT"),
                nextCanvas,
                statusLabel,
                pauseButton,
                settingsButton);

        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(180);
        for (var node : rightPanel.getChildren()) {
            if (node instanceof Label label) label.setStyle("-fx-text-fill: #E8EEF2;");
        }

        setCenter(boardCanvas);
        setRight(rightPanel);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #101820;");

        pauseButton.setFocusTraversable(false);
        settingsButton.setFocusTraversable(false);
        settingsButton.setDisable(true);

        pauseButton.setOnAction(event -> togglePause());
        settingsButton.setOnAction(event -> requestSettings());

        setFocusTraversable(true);

        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> controller.handle(GameCommand.LEFT);
                case RIGHT -> controller.handle(GameCommand.RIGHT);
                case DOWN -> controller.handle(GameCommand.DOWN);
                case UP -> controller.handle(GameCommand.ROTATE);
                case SPACE -> controller.handle(GameCommand.HARD_DROP);

                case ESCAPE -> togglePause();

                default -> {
                    return;
                }
            }

            redraw();
            checkGameOver();
            event.consume();
        });

        dropTimer = new Timeline(
                new KeyFrame(
                        Duration.millis(TIMER_STEP_MS),
                        event -> updateGame()));

        dropTimer.setCycleCount(Timeline.INDEFINITE);

        sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    if (newScene == null) {
                        dropTimer.stop();
                    } else {
                        lastUpdateNanos = System.nanoTime();

                        if (!engine.isGameOver()) {
                            dropTimer.play();
                        }

                        Platform.runLater(this::requestFocus);
                    }
                });

        setOnMouseClicked(event -> requestFocus());

        redraw();
    }

    public GameEngine getEngine() {
        return engine;
    }

    public int getScore() {
        return engine.getScore();
    }

    public void setOnSettingsRequested(Runnable callback) {
        settingsButton.setDisable(callback == null || engine.isGameOver());
        onSettingsRequested = callback != null
                ? callback
                : () -> {
                };
    }

    public void setOnGameOver(Runnable callback) {
        onGameOver = callback != null
                ? callback
                : () -> {
                };
    }

    private void updateGame() {
        long now = System.nanoTime();
        int elapsed = (int) Math.min(Integer.MAX_VALUE, (now - lastUpdateNanos) / 1_000_000L);
        lastUpdateNanos = now;
        controller.advance(elapsed);
        redraw();
        checkGameOver();
    }

    private void togglePause() {
        if (engine.isGameOver()) {
            return;
        }

        if (engine.isPaused()) {
            controller.resume();
        } else {
            controller.pause();
        }

        lastUpdateNanos = System.nanoTime();
        redraw();
        requestFocus();
    }

    private void requestSettings() {
        if (engine.isGameOver()) {
            return;
        }

        // 설정 화면에 들어가기 전 상태를 기억
        settingsOpenedWhilePaused = engine.isPaused();

        controller.pause();
        lastUpdateNanos = System.nanoTime();

        redraw();
        onSettingsRequested.run();
    }

    // 설정 화면 담당자가 돌아가기 버튼에서 호출
    public void returnFromSettings() {
        if (!settingsOpenedWhilePaused && !engine.isGameOver()) {
            controller.resume();
        }

        lastUpdateNanos = System.nanoTime();
        redraw();

        Platform.runLater(this::requestFocus);
    }

    private void checkGameOver() {
        if (!engine.isGameOver() || gameOverNotified) {
            return;
        }

        gameOverNotified = true;
        dropTimer.stop();

        redraw();
        onGameOver.run();
    }

    public void redraw() {
        snapshot = controller.snapshot();
        drawBoard();
        drawNextBlock();

        scoreLabel.setText(String.valueOf(snapshot.score()));
        linesLabel.setText(
                String.valueOf(snapshot.totalClearedLines()));

        if (engine.isGameOver()) {
            statusLabel.setText("GAME OVER");
            pauseButton.setDisable(true);
            settingsButton.setDisable(true);
        } else if (engine.isPaused()) {
            statusLabel.setText("PAUSED");
            pauseButton.setText("계속하기");
        } else if (snapshot.state() == GameState.CLEARING) {
            statusLabel.setText("LINE CLEAR");
            pauseButton.setText("일시정지");
        } else {
            statusLabel.setText("PLAYING");
            pauseButton.setText("일시정지");
        }
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#17212B"));
        gc.fillRect(
                0,
                0,
                boardCanvas.getWidth(),
                boardCanvas.getHeight());

        // 고정된 블록
        gc.setFill(Color.web("#527A83"));

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                if (snapshot.cells().get(row).get(col) != 0) {
                    drawCell(gc, row, col);
                }
            }
        }

        // 현재 블록
        if (snapshot.currentBlock() != null) {
            PieceSnapshot block = snapshot.currentBlock();
            var shape = block.shape();

            gc.setFill(Color.web("#63D4C5"));

            for (int row = 0; row < shape.size(); row++) {
                for (int col = 0; col < shape.get(row).size(); col++) {
                    if (shape.get(row).get(col) == 1) {
                        drawCell(
                                gc,
                                block.row() + row,
                                block.col() + col);
                    }
                }
            }
        }

        // 삭제 예정 행은 압축 전 위치에 밝은 색으로 표시한다.
        gc.setFill(Color.rgb(255, 255, 255, 0.75));
        for (int row : snapshot.clearingRows()) {
            gc.fillRect(0, row * CELL_SIZE, boardCanvas.getWidth(), CELL_SIZE);
        }

        // 격자
        gc.setStroke(Color.web("#344653"));

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                gc.strokeRect(
                        col * CELL_SIZE,
                        row * CELL_SIZE,
                        CELL_SIZE,
                        CELL_SIZE);
            }
        }
    }

    private void drawNextBlock() {
        GraphicsContext gc = nextCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#17212B"));
        gc.fillRect(
                0,
                0,
                nextCanvas.getWidth(),
                nextCanvas.getHeight());

        var shape = snapshot.nextBlock().shape();

        gc.setFill(Color.web("#63D4C5"));

        int previewCellSize = 24;

        int startX = ((int) nextCanvas.getWidth()
                - shape.get(0).size() * previewCellSize) / 2;

        int startY = ((int) nextCanvas.getHeight()
                - shape.size() * previewCellSize) / 2;

        for (int row = 0; row < shape.size(); row++) {
            for (int col = 0; col < shape.get(row).size(); col++) {
                if (shape.get(row).get(col) == 1) {
                    gc.fillRect(
                            startX + col * previewCellSize,
                            startY + row * previewCellSize,
                            previewCellSize - 2,
                            previewCellSize - 2);
                }
            }
        }
    }

    private void drawCell(
            GraphicsContext gc,
            int row,
            int col) {
        gc.fillRect(
                col * CELL_SIZE + 1,
                row * CELL_SIZE + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2);
    }
}
