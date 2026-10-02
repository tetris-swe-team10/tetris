package com.team10.tetris.ui;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.Tetromino;
import com.team10.tetris.game.TetrominoType;

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
    private final Canvas boardCanvas;
    private final Canvas nextCanvas;

    private final Label scoreLabel = new Label();
    private final Label linesLabel = new Label();
    private final Label statusLabel = new Label();

    private final Button pauseButton = new Button("일시정지");
    private final Button settingsButton = new Button("설정");

    private final Timeline dropTimer;

    private int elapsedMs = 0;
    private int score = 0;

    private boolean settingsOpenedWhilePaused = false;
    private boolean gameOverNotified = false;

    private Runnable onSettingsRequested = () -> {};
    private Runnable onGameOver = () -> {};

    public GameView() {
        this(new GameEngine(
                new Board(),
                new Tetromino(TetrominoType.T, 0, 3)
        ));
    }

    public GameView(GameEngine engine) {
        this.engine = engine;

        boardCanvas = new Canvas(
                Board.WIDTH * CELL_SIZE,
                Board.HEIGHT * CELL_SIZE
        );

        nextCanvas = new Canvas(120, 120);

        VBox rightPanel = new VBox(
                18,
                new Label("SCORE"),
                scoreLabel,
                new Label("LINES"),
                linesLabel,
                new Label("NEXT"),
                nextCanvas,
                statusLabel,
                pauseButton,
                settingsButton
        );

        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(180);

        setCenter(boardCanvas);
        setRight(rightPanel);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #101820;");

        pauseButton.setFocusTraversable(false);
        settingsButton.setFocusTraversable(false);

        pauseButton.setOnAction(event -> togglePause());
        settingsButton.setOnAction(event -> requestSettings());

        setFocusTraversable(true);

        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> engine.moveLeft();
                case RIGHT -> engine.moveRight();
                case DOWN -> engine.moveDown();
                case UP -> engine.rotateClockwise();
                case SPACE -> engine.hardDrop();
                case P -> togglePause();
                case ESCAPE -> requestSettings();

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
                        event -> updateGame()
                )
        );

        dropTimer.setCycleCount(Timeline.INDEFINITE);

        sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    if (newScene == null) {
                        dropTimer.stop();
                    } else {
                        elapsedMs = 0;

                        if (!engine.isGameOver()) {
                            dropTimer.play();
                        }

                        Platform.runLater(this::requestFocus);
                    }
                }
        );

        setOnMouseClicked(event -> requestFocus());

        redraw();
    }

    public GameEngine getEngine() {
        return engine;
    }

    public int getScore() {
        return score;
    }

    // 점수 담당 팀원이 계산한 점수를 전달하는 연결 지점
    public void setScore(int score) {
        this.score = Math.max(0, score);
        redraw();
    }

    public void setOnSettingsRequested(Runnable callback) {
        onSettingsRequested = callback != null
                ? callback
                : () -> {};
    }

    public void setOnGameOver(Runnable callback) {
        onGameOver = callback != null
                ? callback
                : () -> {};
    }

    private void updateGame() {
        if (engine.isPaused() || engine.isGameOver()) {
            return;
        }

        elapsedMs += TIMER_STEP_MS;

        if (elapsedMs >= engine.getDropIntervalMs()) {
            engine.tick();
            elapsedMs = 0;

            redraw();
            checkGameOver();
        }
    }

    private void togglePause() {
        if (engine.isGameOver()) {
            return;
        }

        if (engine.isPaused()) {
            engine.resume();
        } else {
            engine.pause();
        }

        elapsedMs = 0;
        redraw();
        requestFocus();
    }

    private void requestSettings() {
        if (engine.isGameOver()) {
            return;
        }

        // 설정 화면에 들어가기 전 상태를 기억
        settingsOpenedWhilePaused = engine.isPaused();

        engine.pause();
        elapsedMs = 0;

        redraw();
        onSettingsRequested.run();
    }

    // 설정 화면 담당자가 돌아가기 버튼에서 호출
    public void returnFromSettings() {
        if (!settingsOpenedWhilePaused && !engine.isGameOver()) {
            engine.resume();
        }

        elapsedMs = 0;
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
        drawBoard();
        drawNextBlock();

        scoreLabel.setText(String.valueOf(score));
        linesLabel.setText(
                String.valueOf(engine.getTotalClearedLines())
        );

        if (engine.isGameOver()) {
            statusLabel.setText("GAME OVER");
            pauseButton.setDisable(true);
            settingsButton.setDisable(true);
        } else if (engine.isPaused()) {
            statusLabel.setText("PAUSED");
            pauseButton.setText("계속하기");
        } else {
            statusLabel.setText("PLAYING");
            pauseButton.setText("일시정지");
        }
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        Board board = engine.getBoard();

        gc.setFill(Color.web("#17212B"));
        gc.fillRect(
                0,
                0,
                boardCanvas.getWidth(),
                boardCanvas.getHeight()
        );

        // 고정된 블록
        gc.setFill(Color.web("#527A83"));

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                if (board.getCell(row, col) != 0) {
                    drawCell(gc, row, col);
                }
            }
        }

        // 현재 블록
        if (!engine.isGameOver()) {
            Tetromino block = engine.getCurrentBlock();
            int[][] shape = block.getShape();

            gc.setFill(Color.web("#63D4C5"));

            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] == 1) {
                        drawCell(
                                gc,
                                block.getRow() + row,
                                block.getCol() + col
                        );
                    }
                }
            }
        }

        // 격자
        gc.setStroke(Color.web("#344653"));

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                gc.strokeRect(
                        col * CELL_SIZE,
                        row * CELL_SIZE,
                        CELL_SIZE,
                        CELL_SIZE
                );
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
                nextCanvas.getHeight()
        );

        int[][] shape = engine.getNextBlock().getShape();

        gc.setFill(Color.web("#63D4C5"));

        int previewCellSize = 24;

        int startX = (
                (int) nextCanvas.getWidth()
                        - shape[0].length * previewCellSize
        ) / 2;

        int startY = (
                (int) nextCanvas.getHeight()
                        - shape.length * previewCellSize
        ) / 2;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    gc.fillRect(
                            startX + col * previewCellSize,
                            startY + row * previewCellSize,
                            previewCellSize - 2,
                            previewCellSize - 2
                    );
                }
            }
        }
    }

    private void drawCell(
            GraphicsContext gc,
            int row,
            int col
    ) {
        gc.fillRect(
                col * CELL_SIZE + 1,
                row * CELL_SIZE + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2
        );
    }
}