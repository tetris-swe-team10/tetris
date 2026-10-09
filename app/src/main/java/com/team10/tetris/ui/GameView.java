package com.team10.tetris.ui;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameController;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.GameState;
import com.team10.tetris.game.PieceSnapshot;
import com.team10.tetris.settings.GameSettings;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
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

    // 무늬 간격은 칸 폭의 약 22%, 선 굵기는 1.75px
    private static final double PATTERN_SPACING_RATIO = 0.22;
    private static final double PATTERN_LINE_WIDTH = 1.75;
    private static final double OUTLINE_WIDTH = 1.5;

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

    private BlockPalette palette = BlockPalette.NORMAL;

    // 설정이 바뀌면 즉시 다시 그림 (설정 쪽은 약한 참조로 들고 있어 끝난 게임 화면이 남지 않음)
    private final ChangeListener<Boolean> colorBlindModeListener =
            (observable, oldValue, newValue) -> setColorBlindMode(newValue);

    private Runnable onSettingsRequested = () -> {
    };
    private Runnable onGameOver = () -> {
    };

    public GameView() {
        this(new GameEngine(GameConfig.DEFAULT));
    }

    public GameView(GameEngine engine, GameSettings settings) {
        this(engine);

        setColorBlindMode(settings.isColorBlindMode());
        settings.colorBlindModeProperty().addListener(
                new WeakChangeListener<>(colorBlindModeListener));
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

    public boolean isColorBlindMode() {
        return palette == BlockPalette.COLOR_BLIND;
    }

    // 설정 화면 담당자가 색맹 모드 토글에서 호출
    public void setColorBlindMode(boolean colorBlindMode) {
        palette = BlockPalette.of(colorBlindMode);
        redraw();
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
        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                if (snapshot.cells().get(row).get(col) != 0) {
                    drawCell(
                            gc,
                            row,
                            col,
                            palette.styleOf(snapshot.cellTypes().get(row).get(col)));
                }
            }
        }

        // 현재 블록
        if (snapshot.currentBlock() != null) {
            PieceSnapshot block = snapshot.currentBlock();
            var shape = block.shape();
            BlockStyle style = palette.styleOf(block.type());

            for (int row = 0; row < shape.size(); row++) {
                for (int col = 0; col < shape.get(row).size(); col++) {
                    if (shape.get(row).get(col) == 1) {
                        drawCell(
                                gc,
                                block.row() + row,
                                block.col() + col,
                                style);
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

        PieceSnapshot nextBlock = snapshot.nextBlock();
        var shape = nextBlock.shape();
        BlockStyle style = palette.styleOf(nextBlock.type());

        int previewCellSize = 24;

        int startX = ((int) nextCanvas.getWidth()
                - shape.get(0).size() * previewCellSize) / 2;

        int startY = ((int) nextCanvas.getHeight()
                - shape.size() * previewCellSize) / 2;

        for (int row = 0; row < shape.size(); row++) {
            for (int col = 0; col < shape.get(row).size(); col++) {
                if (shape.get(row).get(col) == 1) {
                    drawBlock(
                            gc,
                            startX + col * previewCellSize,
                            startY + row * previewCellSize,
                            previewCellSize - 2,
                            style);
                }
            }
        }
    }

    private void drawCell(
            GraphicsContext gc,
            int row,
            int col,
            BlockStyle style) {
        drawBlock(
                gc,
                col * CELL_SIZE + 1,
                row * CELL_SIZE + 1,
                CELL_SIZE - 2,
                style);
    }

    // 블록 한 칸: 채우기 → 무늬 → 외곽선
    private void drawBlock(
            GraphicsContext gc,
            double x,
            double y,
            double size,
            BlockStyle style) {
        gc.setFill(style.fill());
        gc.fillRect(x, y, size, size);

        if (style.pattern() != BlockPattern.NONE) {
            // 무늬가 칸 밖으로 나가지 않도록 칸 영역으로 자름
            gc.save();
            gc.beginPath();
            gc.rect(x, y, size, size);
            gc.closePath();
            gc.clip();

            drawPattern(gc, x, y, size, style);

            gc.restore();
        }

        gc.setStroke(BlockPalette.OUTLINE);
        gc.setLineWidth(OUTLINE_WIDTH);
        gc.strokeRect(
                x + OUTLINE_WIDTH / 2,
                y + OUTLINE_WIDTH / 2,
                size - OUTLINE_WIDTH,
                size - OUTLINE_WIDTH);
        gc.setLineWidth(1);
    }

    // 무늬는 화면 기준으로 그려서 블록이 회전해도 방향이 바뀌지 않음
    private void drawPattern(
            GraphicsContext gc,
            double x,
            double y,
            double size,
            BlockStyle style) {
        double spacing = size * PATTERN_SPACING_RATIO;

        gc.setStroke(style.patternColor());
        gc.setFill(style.patternColor());
        gc.setLineWidth(PATTERN_LINE_WIDTH);

        switch (style.pattern()) {
            case HORIZONTAL_LINES -> drawHorizontalLines(gc, x, y, size, spacing);
            case VERTICAL_LINES -> drawVerticalLines(gc, x, y, size, spacing);
            case GRID -> {
                drawHorizontalLines(gc, x, y, size, spacing);
                drawVerticalLines(gc, x, y, size, spacing);
            }
            case DIAGONAL_RIGHT -> drawDiagonalLines(gc, x, y, size, spacing, true);
            case DIAGONAL_LEFT -> drawDiagonalLines(gc, x, y, size, spacing, false);
            case CROSS_DIAGONAL -> {
                drawDiagonalLines(gc, x, y, size, spacing, true);
                drawDiagonalLines(gc, x, y, size, spacing, false);
            }
            case DOTS -> drawDots(gc, x, y, size);
            case NONE -> {
            }
        }
    }

    private void drawHorizontalLines(
            GraphicsContext gc,
            double x,
            double y,
            double size,
            double spacing) {
        for (double offset = spacing; offset < size; offset += spacing) {
            gc.strokeLine(x, y + offset, x + size, y + offset);
        }
    }

    private void drawVerticalLines(
            GraphicsContext gc,
            double x,
            double y,
            double size,
            double spacing) {
        for (double offset = spacing; offset < size; offset += spacing) {
            gc.strokeLine(x + offset, y, x + offset, y + size);
        }
    }

    // rising == true 이면 '/', false 이면 '\'
    private void drawDiagonalLines(
            GraphicsContext gc,
            double x,
            double y,
            double size,
            double spacing,
            boolean rising) {
        // 선 사이의 수직 거리가 spacing이 되도록 가로 방향 간격을 넓힘
        double step = spacing * Math.sqrt(2);

        for (double offset = step; offset < size * 2; offset += step) {
            if (rising) {
                gc.strokeLine(x + offset - size, y + size, x + offset, y);
            } else {
                gc.strokeLine(x + offset - size, y, x + offset, y + size);
            }
        }
    }

    private void drawDots(
            GraphicsContext gc,
            double x,
            double y,
            double size) {
        double radius = size * 0.11;

        for (double cy : new double[] {0.3, 0.7}) {
            for (double cx : new double[] {0.3, 0.7}) {
                gc.fillOval(
                        x + size * cx - radius,
                        y + size * cy - radius,
                        radius * 2,
                        radius * 2);
            }
        }
    }
}
