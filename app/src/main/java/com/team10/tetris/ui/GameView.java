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
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
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
    private final Label penaltyLabel = new Label();
    private final Label demoLabel = new Label();
    private final Label itemLabel = new Label();

    private final Button resultButton = new Button("결과 보기");
    private VBox infoPanel;
    private final Button pauseButton = new Button("일시정지");
    private final Button settingsButton = new Button("설정");

    private final Timeline dropTimer;

    private long lastUpdateNanos;

    private boolean settingsOpen = false;
    private GameSettings gameSettings;
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

        this.gameSettings = settings;
        engine.setBaseDropIntervalMs(settings.adjustDropInterval(GameEngine.INITIAL_DROP_INTERVAL_MS));
        engine.configureDemoItems(settings.isDemoMode(), settings.getDemoItemLines());
        setColorBlindMode(settings.isColorBlindMode());
        settings.colorBlindModeProperty().addListener(
                new WeakChangeListener<>(colorBlindModeListener));
    }

    public GameView(GameEngine engine) {
        AppFonts.apply(this);
        this.engine = engine;
        controller = new GameController(engine);

        boardCanvas = new Canvas(
                Board.WIDTH * CELL_SIZE,
                Board.HEIGHT * CELL_SIZE);

        nextCanvas = new Canvas(120, 120);

        VBox scoreCard = infoCard("SCORE", scoreLabel, "#3B82F6");
        VBox linesCard = infoCard("LINES", linesLabel, "#21BED7");
        HBox counters = new HBox(16, scoreCard, linesCard);
        HBox.setHgrow(scoreCard, Priority.ALWAYS);
        HBox.setHgrow(linesCard, Priority.ALWAYS);
        scoreCard.setMaxWidth(Double.MAX_VALUE); linesCard.setMaxWidth(Double.MAX_VALUE);
        VBox previewCard = infoCard("NEXT", new javafx.scene.layout.StackPane(nextCanvas), "#8B5CF6");
        previewCard.getChildren().add(itemLabel);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(16, resultButton, settingsButton);
        for (Button button : new Button[]{resultButton, settingsButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.setFocusTraversable(false);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-background-color: #111827; -fx-text-fill: #F5F7FB; "
                    + "-fx-border-color: #334155; -fx-border-radius: 6; -fx-background-radius: 6; "
                    + "-fx-font-weight: bold; -fx-padding: 12 16;");
        }
        infoPanel = new VBox(16, counters, previewCard, penaltyLabel, demoLabel, statusLabel, spacer, actions);
        infoPanel.setMinWidth(0);
        infoPanel.setPadding(new Insets(0));
        itemLabel.setId("next-item-description");
        itemLabel.setWrapText(true);
        itemLabel.setMaxWidth(Double.MAX_VALUE);
        itemLabel.setStyle("-fx-text-fill: #98A2B3;");
        for (Label label : new Label[]{penaltyLabel, statusLabel}) {
            label.setWrapText(true); label.setStyle("-fx-text-fill: #E8EEF2;");
        }
        setCenter(boardCanvas);
        setRight(infoPanel);
        setPadding(new Insets(32));
        setStyle("-fx-font-family: 'Noto Sans KR'; -fx-background-color: #030712; -fx-border-color: transparent transparent transparent #7C3AED; -fx-border-width: 0 0 0 5;");
        resultButton.setOnAction(event -> showCurrentResult());

        pauseButton.setFocusTraversable(false);
        settingsButton.setFocusTraversable(false);
        settingsButton.setDisable(true);

        pauseButton.setOnAction(event -> togglePause());
        settingsButton.setOnAction(event -> requestSettings());

        setFocusTraversable(true);

        setOnKeyPressed(event -> {
            if (settingsOpen) {
                event.consume();
                return;
            }
            if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown()) return;
            var key = event.getCode();
            if (key == javafx.scene.input.KeyCode.ESCAPE) {
                requestSettings();
            } else if (key == javafx.scene.input.KeyCode.DIGIT1 || key == javafx.scene.input.KeyCode.NUMPAD1) {
                engine.requestDemoItem(1);
            } else if (key == javafx.scene.input.KeyCode.DIGIT2 || key == javafx.scene.input.KeyCode.NUMPAD2) {
                engine.requestDemoItem(2);
            } else if (key == javafx.scene.input.KeyCode.DIGIT3 || key == javafx.scene.input.KeyCode.NUMPAD3) {
                engine.requestDemoItem(3);
            } else {
                var bindings = gameSettings == null ? com.team10.tetris.input.GameKeyBindings.defaults()
                        : gameSettings.getKeyBindings();
                var command = bindings.commandFor(key);
                if (command.isEmpty()) return;
                controller.handle(command.get());
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

                        if (!engine.isGameOver() && !settingsOpen) {
                            dropTimer.play();
                        }

                        Platform.runLater(() -> {
                            if (getScene() != null && !settingsOpen) requestFocus();
                        });
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
        if (engine.isGameOver() || settingsOpen) {
            return;
        }

        settingsOpen = true;
        dropTimer.stop();
        controller.pause();
        lastUpdateNanos = System.nanoTime();

        redraw();
        onSettingsRequested.run();
    }

    // 설정 화면 담당자가 돌아가기 버튼에서 호출
    public void returnFromSettings() {
        if (gameSettings != null) {
            engine.setBaseDropIntervalMs(gameSettings.adjustDropInterval(GameEngine.INITIAL_DROP_INTERVAL_MS));
            engine.configureDemoItems(gameSettings.isDemoMode(), gameSettings.getDemoItemLines());
        }
        settingsOpen = false;
        if (!engine.isGameOver()) {
            controller.resume();
            if (getScene() != null) dropTimer.play();
        }

        lastUpdateNanos = System.nanoTime();
        redraw();

        Platform.runLater(() -> {
            if (getScene() != null && !settingsOpen) requestFocus();
        });
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

    private static VBox infoCard(String title, javafx.scene.Node value, String accent) {
        Label caption = new Label(title);
        caption.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-text-fill: #F5F7FB; -fx-font-size: 12px; -fx-font-weight: bold;");
        if (value instanceof Label label) {
            label.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-text-fill: #F5F7FB; -fx-font-size: 28px; -fx-font-weight: bold;");
        }
        VBox card = new VBox(16, caption, value);
        card.setMinWidth(0);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #0B1320; -fx-background-radius: 6; "
                + "-fx-border-color: #202B3B #202B3B #202B3B " + accent
                + "; -fx-border-width: 1 1 1 3; -fx-border-radius: 6;");
        return card;
    }

    private void showCurrentResult() {
        boolean resume = !engine.isPaused() && !engine.isGameOver();
        controller.pause();
        Alert result = new Alert(Alert.AlertType.INFORMATION,
                "SCORE  " + engine.getScore() + "\nLINES  " + engine.getTotalClearedLines(), ButtonType.OK);
        AppFonts.apply(result.getDialogPane());
        result.setTitle("현재 결과");
        result.setHeaderText("현재 게임 기록");
        if (getScene() != null && getScene().getWindow() != null) result.initOwner(getScene().getWindow());
        result.showAndWait();
        if (resume) controller.resume();
        lastUpdateNanos = System.nanoTime();
        redraw(); requestFocus();
    }

    @Override protected void layoutChildren() {
        super.layoutChildren();
        if (infoPanel == null) return;
        double w = getWidth(), h = getHeight();
        double pad = Math.max(16, Math.min(w * .03, 40));
        double gap = Math.max(20, Math.min(w * .025, 36));
        double available = Math.max(0, w - 2 * pad - gap);
        // 보드는 높이를 최대한 활용하고, 정보 영역은 보조 패널 폭으로 제한한다.
        double panelWidth = Math.min(300, Math.max(220, available * .32));
        double scale = Math.max(.2, Math.min((h - 2 * pad) / (Board.HEIGHT * CELL_SIZE),
                Math.max(0, available - panelWidth) / (Board.WIDTH * CELL_SIZE)));
        double bw = Board.WIDTH * CELL_SIZE * scale, bh = Board.HEIGHT * CELL_SIZE * scale;
        double left = (w - bw - gap - panelWidth) / 2;
        boardCanvas.setScaleX(scale); boardCanvas.setScaleY(scale);
        boardCanvas.relocate(left + (bw - boardCanvas.getWidth()) / 2,
                (h - bh) / 2 + (bh - boardCanvas.getHeight()) / 2);
        infoPanel.resizeRelocate(left + bw + gap, Math.max(pad, (h - bh) / 2), panelWidth, bh);
    }

    public void redraw() {
        snapshot = controller.snapshot();
        drawBoard();
        drawNextBlock();
        penaltyLabel.setText(snapshot.blackoutActive()
                ? "B 암전 · 현재 블록 착지까지"
                : snapshot.rotationLocked() ? "K 회전 잠금 · 현재 블록" : "");

        demoLabel.setWrapText(true);
        demoLabel.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-text-fill: #98A2B3; -fx-font-size: 12px;");
        demoLabel.setVisible(engine.isItemDemoEnabled());
        demoLabel.setManaged(engine.isItemDemoEnabled());
        demoLabel.setText("데모 · 1 폭탄 / 2 암전 / 3 회전 잠금\n새 NEXT에 지급 · 벌칙 중에는 해제 후 지급"
                + (engine.demoRequestedSymbol().isEmpty() ? "" : "\n예약: " + engine.demoRequestedSymbol()));
        scoreLabel.setText(String.valueOf(snapshot.score()));
        linesLabel.setText(
                String.valueOf(snapshot.totalClearedLines()));
        var nextItem = snapshot.nextBlock().item();
        itemLabel.setText(nextItem == null ? "" : nextItem.symbol() + " " + nextItem.description());
        itemLabel.setVisible(nextItem != null);
        itemLabel.setManaged(nextItem != null);
        nextCanvas.setAccessibleText(nextItem == null ? "다음 블록 " + snapshot.nextBlock().type()
                : "다음 블록 " + nextItem.symbol() + " " + nextItem.description());
        var activeItem = snapshot.currentBlock() == null ? null : snapshot.currentBlock().item();
        boardCanvas.setAccessibleText(activeItem == null ? "게임 보드"
                : "게임 보드, 현재 블록 " + activeItem.symbol() + " " + activeItem.description());

        if (engine.isGameOver()) {
            statusLabel.setText("GAME OVER");
            pauseButton.setDisable(true);
            settingsButton.setDisable(true);
        } else if (engine.isPaused()) {
            statusLabel.setText("PAUSED");
            pauseButton.setText("계속하기");
        } else if (snapshot.state() == GameState.ITEM_EFFECT) {
            statusLabel.setText("BOOM!");
            pauseButton.setText("일시정지");
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

        gc.setFill(Color.web(snapshot.blackoutActive() ? "#010205" : "#060B16"));
        gc.fillRect(
                0,
                0,
                boardCanvas.getWidth(),
                boardCanvas.getHeight());

        // 안내선은 고정 블록 뒤에 그려 쌓인 블록을 가리지 않는다.
        PieceSnapshot ghost = snapshot.ghostBlock();
        if (ghost != null) drawDropGuides(gc, snapshot.currentBlock(), ghost);

        // 고정 블록은 조작 블록의 실제 점유 영역 중심에서 지름 4칸만 보인다.
        gc.save();
        if (snapshot.blackoutActive() && snapshot.currentBlock() != null) {
            PieceSnapshot focus = snapshot.currentBlock();
            int minRow = Integer.MAX_VALUE, maxRow = -1, minCol = Integer.MAX_VALUE, maxCol = -1;
            for (int r = 0; r < focus.shape().size(); r++) {
                for (int c = 0; c < focus.shape().get(r).size(); c++) {
                    if (focus.shape().get(r).get(c) != 0) {
                        minRow = Math.min(minRow, r); maxRow = Math.max(maxRow, r);
                        minCol = Math.min(minCol, c); maxCol = Math.max(maxCol, c);
                    }
                }
            }
            double cx = (focus.col() + (minCol + maxCol + 1) / 2.0) * CELL_SIZE;
            double cy = (focus.row() + (minRow + maxRow + 1) / 2.0) * CELL_SIZE;
            gc.beginPath();
            gc.arc(cx, cy, 2 * CELL_SIZE, 2 * CELL_SIZE, 0, 360);
            gc.closePath();
            gc.clip();
            gc.setFill(Color.web("#101D30"));
            gc.fillRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        }
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

        gc.restore();

        if (ghost != null && ghost.row() > snapshot.currentBlock().row()) drawGhost(gc, ghost);

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
            if (block.item() != null) {
                drawItemMarker(gc, (block.col() + block.item().col()) * CELL_SIZE,
                        (block.row() + block.item().row()) * CELL_SIZE, CELL_SIZE, block.item().symbol());
            }
        }

        // 삭제 예정 행은 압축 전 위치에 밝은 색으로 표시한다.
        gc.setFill(Color.rgb(255, 255, 255, 0.75));
        for (int row : snapshot.clearingRows()) {
            gc.fillRect(0, row * CELL_SIZE, boardCanvas.getWidth(), CELL_SIZE);
        }
        // 줄 삭제와 같은 흰색 강조를 실제 폭발로 없어질 칸에만 두 번 깜빡인다.
        if (snapshot.effectFlashVisible()) {
            for (var cell : snapshot.effectCells()) {
                gc.fillRect(cell.col() * CELL_SIZE, cell.row() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 어두운 화면에서도 셀 경계를 구분할 수 있도록 밝기를 확보한다.
        gc.setStroke(Color.web("#344155"));

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

    /** 좌우 끝 열의 실제 점유 칸을 기준으로 현재 블록 아래와 착지 위치 위를 연결한다. */
    private void drawDropGuides(GraphicsContext gc, PieceSnapshot current, PieceSnapshot ghost) {
        if (ghost.row() <= current.row()) return;
        int left = Integer.MAX_VALUE, right = -1;
        for (var row : current.shape()) {
            for (int col = 0; col < row.size(); col++) {
                if (row.get(col) != 0) { left = Math.min(left, col); right = Math.max(right, col); }
            }
        }
        gc.save();
        gc.setStroke(Color.web("#CBD5E1", 0.42));
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        for (int side = 0; side < 2; side++) {
            int col = side == 0 ? left : right;
            int top = -1, bottom = -1;
            for (int row = 0; row < current.shape().size(); row++) {
                if (current.shape().get(row).get(col) != 0) {
                    if (top < 0) top = row;
                    bottom = row;
                }
            }
            double start = (current.row() + bottom + 1) * CELL_SIZE;
            double end = (ghost.row() + top) * CELL_SIZE;
            double x = (current.col() + col + side) * CELL_SIZE + (side == 0 ? 2 : -2);
            if (end > start) gc.strokeLine(x, start, x, end);
        }
        gc.restore();
    }

    private void drawGhost(GraphicsContext gc, PieceSnapshot ghost) {
        gc.save();
        gc.setFill(Color.web("#CBD5E1", 0.08));
        gc.setStroke(Color.web("#CBD5E1", 0.8));
        gc.setLineWidth(1.5);
        for (int row = 0; row < ghost.shape().size(); row++) {
            for (int col = 0; col < ghost.shape().get(row).size(); col++) {
                if (ghost.shape().get(row).get(col) == 0) continue;
                double x = (ghost.col() + col) * CELL_SIZE + 3;
                double y = (ghost.row() + row) * CELL_SIZE + 3;
                gc.fillRect(x, y, CELL_SIZE - 6, CELL_SIZE - 6);
                gc.strokeRect(x, y, CELL_SIZE - 6, CELL_SIZE - 6);
            }
        }
        gc.restore();
    }

    private void drawNextBlock() {
        GraphicsContext gc = nextCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#0B1320"));
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
        if (nextBlock.item() != null) {
            drawItemMarker(gc, startX + nextBlock.item().col() * previewCellSize,
                    startY + nextBlock.item().row() * previewCellSize, previewCellSize,
                    nextBlock.item().symbol());
        }
    }

    /** 문자와 명암 테두리로 구분해 일반·색맹 팔레트 모두에서 읽을 수 있게 한다. */
    private void drawItemMarker(GraphicsContext gc, double x, double y, double size, String symbol) {
        gc.save();
        double inset = 3;
        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x + inset, y + inset, size - 2 * inset, size - 2 * inset, 4, 4);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + inset, y + inset, size - 2 * inset, size - 2 * inset, 4, 4);
        gc.setFill(Color.WHITE);
        gc.setFont(AppFonts.bold(size * 0.64));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(symbol, x + size / 2, y + size / 2, size - 6);
        gc.restore();
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
