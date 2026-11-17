package com.team10.tetris.ui;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameController;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.GameState;
import com.team10.tetris.input.GameKeyBindings;
import com.team10.tetris.input.GameKeyInput;
import com.team10.tetris.settings.GameSettings;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
    private final Label penaltyLabel = new Label();
    private final Label demoLabel = new Label();
    private final Label itemLabel = new Label();

    private final Button resultButton = new Button("결과 보기");
    private VBox infoPanel;
    private final Button settingsButton = new Button("설정");

    private final GameLoop gameLoop;

    private boolean settingsOpen = false;
    private GameSettings gameSettings;
    private boolean gameOverNotified = false;

    private BlockPalette palette = BlockPalette.NORMAL;
    private final BoardRenderer renderer = new CanvasBoardRenderer();

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

        Label modeBadge = sessionBadge(engine.getConfig().mode() + " 모드", "current-mode", "#60A5FA");
        Label difficultyBadge = sessionBadge("난이도 · " + engine.getConfig().difficulty(), "current-difficulty", "#22D3EE");
        javafx.scene.layout.FlowPane session = new javafx.scene.layout.FlowPane(8, 8, modeBadge, difficultyBadge);
        session.setId("current-session");
        session.setMinWidth(0);

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
        infoPanel = new VBox(16, session, counters, previewCard, penaltyLabel, demoLabel, statusLabel, spacer, actions);
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

        settingsButton.setFocusTraversable(false);
        settingsButton.setDisable(true);

        settingsButton.setOnAction(event -> requestSettings());

        setFocusTraversable(true);

        setOnKeyPressed(event -> {
            if (settingsOpen) {
                event.consume();
                return;
            }
            boolean modifierDown = event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown();
            var bindings = gameSettings == null ? GameKeyBindings.defaults() : gameSettings.getKeyBindings();
            var action = GameKeyInput.resolve(event.getCode(), modifierDown, bindings, engine.isItemDemoEnabled());
            if (action.isEmpty()) return;
            switch (action.get()) {
                case GameKeyInput.Action.OpenSettings open -> requestSettings();
                case GameKeyInput.Action.DemoItem demo -> engine.requestDemoItem(demo.number());
                case GameKeyInput.Action.Command command -> controller.handle(command.command());
            }

            redraw();
            checkGameOver();
            event.consume();
        });

        gameLoop = new GameLoop(TIMER_STEP_MS, this::updateGame);

        sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    if (newScene == null) {
                        gameLoop.stop();
                    } else {
                        gameLoop.resetClock();

                        if (!engine.isGameOver() && !settingsOpen) {
                            gameLoop.start();
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
        renderer.setPalette(palette);
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

    private void updateGame(int elapsed) {
        controller.advance(elapsed);
        redraw();
        checkGameOver();
    }

    private void requestSettings() {
        if (engine.isGameOver() || settingsOpen) {
            return;
        }

        settingsOpen = true;
        gameLoop.stop();
        controller.pause();
        gameLoop.resetClock();

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
            if (getScene() != null) gameLoop.start();
        }

        gameLoop.resetClock();
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
        gameLoop.stop();

        redraw();
        onGameOver.run();
    }

    private static Label sessionBadge(String text, String id, String color) {
        Label label = new Label(text);
        label.setId(id);
        label.setAccessibleText(text);
        label.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + color + "; -fx-background-color: #111827; "
                + "-fx-border-color: #334155; -fx-background-radius: 14; -fx-border-radius: 14; -fx-padding: 6 10;");
        return label;
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
        gameLoop.resetClock();
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
        renderer.drawBoard(boardCanvas, snapshot);
        renderer.drawNext(nextCanvas, snapshot);
        penaltyLabel.setText(snapshot.blackoutActive()
                ? "B 암전 · 현재 블록 착지까지"
                : snapshot.rotationLocked() ? "K 회전 잠금 · 현재 블록" : "");

        demoLabel.setWrapText(true);
        demoLabel.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-text-fill: #98A2B3; -fx-font-size: 12px;");
        demoLabel.setVisible(engine.isItemDemoEnabled());
        demoLabel.setManaged(engine.isItemDemoEnabled());
        demoLabel.setText("데모 · 1 폭탄 / 2 암전 / 3 회전 잠금 / 4 무게추 / 5 줄 삭제\n새 NEXT에 지급 · 벌칙 중에는 해제 후 지급"
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
            settingsButton.setDisable(true);
        } else if (engine.isPaused()) {
            statusLabel.setText("PAUSED");
        } else if (snapshot.state() == GameState.ITEM_EFFECT) {
            statusLabel.setText("BOOM!");
        } else if (snapshot.state() == GameState.CLEARING) {
            statusLabel.setText("LINE CLEAR");
        } else {
            statusLabel.setText("PLAYING");
        }
    }
}
