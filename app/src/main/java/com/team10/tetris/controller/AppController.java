package com.team10.tetris.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.team10.tetris.App;
import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameMode;
import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.ModeSelectionScreen;
import com.team10.tetris.screen.StartMenuAction;
import com.team10.tetris.screen.StartScreen;
import com.team10.tetris.settings.DisplayMode;
import com.team10.tetris.settings.GameSettings;
import com.team10.tetris.settings.PauseSettingsPane;
import com.team10.tetris.settings.SettingsScreen;
import com.team10.tetris.ui.GameOverView;
import com.team10.tetris.ui.GameView;
import com.team10.tetris.ui.ScoreboardView;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** 화면 전환과 사용자 흐름을 담당하는 애플리케이션 컨트롤러. */
public final class AppController {
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 640;

    private final Stage stage;
    private final GameSettings settings;
    private final ScoreboardManager scoreboardManager;
    private Scene scene;
    private Difficulty lastSelectedDifficulty = Difficulty.NORMAL;

    public AppController(Stage stage, GameSettings settings, ScoreboardManager scoreboardManager) {
        this.stage = Objects.requireNonNull(stage);
        this.settings = Objects.requireNonNull(settings);
        this.scoreboardManager = Objects.requireNonNull(scoreboardManager);
    }

    public void start() {
        StartScreen startScreen = createStartScreen();
        scene = new Scene(startScreen, WINDOW_WIDTH, WINDOW_HEIGHT);
        addStylesheet("start-screen.css");
        addStylesheet("settings.css");
        stage.setTitle("Tetris");
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
        startScreen.requestMenuFocus();
    }

    private void addStylesheet(String name) {
        scene.getStylesheets().add(Objects.requireNonNull(
                App.class.getResource("/com/team10/tetris/" + name)).toExternalForm());
    }

    private StartScreen createStartScreen() {
        return new StartScreen(Map.of(
                StartMenuAction.START_GAME, this::showModeSelection,
                StartMenuAction.OPEN_SETTINGS, this::showSettingsScreen,
                StartMenuAction.OPEN_SCOREBOARD, this::showScoreboard,
                StartMenuAction.EXIT, Platform::exit), MenuKeyBindings.defaults());
    }

    private void showStartMenu() {
        StartScreen view = createStartScreen();
        showScreen(view);
        view.requestMenuFocus();
    }

    private void showModeSelection() {
        ModeSelectionScreen view = new ModeSelectionScreen(
                this::chooseDifficulty, this::showScoreboard, this::showStartMenu);
        showScreen(view);
        view.requestMenuFocus();
    }

    private void chooseDifficulty(GameMode mode) {
        ChoiceDialog<Difficulty> dialog = new ChoiceDialog<>(lastSelectedDifficulty, Difficulty.values());
        dialog.initOwner(scene.getWindow());
        dialog.setTitle("게임 난이도 선택");
        dialog.setHeaderText(mode + " 모드의 난이도를 선택하세요.");
        dialog.setContentText("난이도");
        styleDialog(dialog.getDialogPane(), "difficulty-dialog");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("게임 시작");
        dialog.showAndWait().ifPresent(difficulty -> {
            lastSelectedDifficulty = difficulty;
            showScreen(createGameView(new GameConfig(mode, difficulty)));
        });
    }

    private GameView createGameView(GameConfig config) {
        GameEngine engine = new GameEngine(config);
        GameView view = new GameView(engine, settings);
        view.setOnSettingsRequested(() -> showPauseSettings(view));
        view.setOnGameOver(() -> showGameOver(view.getScore(), engine.getTotalClearedLines(), config));
        return view;
    }

    private void showScoreboard() {
        ScoreboardView view = new ScoreboardView(scoreboardManager);
        view.setOnBackRequested(this::showStartMenu);
        showScreen(view);
    }

    private void showSettingsScreen() {
        showScreen(new SettingsScreen(settings, this::applyDisplayMode,
                this::resetScores, this::showStartMenu));
    }

    private void showPauseSettings(GameView gameView) {
        gameView.setDisable(true);
        StackPane layeredGame = new StackPane();
        PauseSettingsPane settingsPane = new PauseSettingsPane(settings, this::applyDisplayMode,
                this::resetScores, () -> {
                    layeredGame.getChildren().remove(gameView);
                    gameView.setDisable(false);
                    showScreen(gameView);
                    gameView.returnFromSettings();
                }, this::showStartMenu);
        layeredGame.getChildren().addAll(gameView, settingsPane);
        showScreen(layeredGame);
        settingsPane.requestFocus();
    }

    private void applyDisplayMode(DisplayMode mode) {
        stage.setFullScreen(mode == DisplayMode.FULL_SCREEN);
        if (mode == DisplayMode.FULL_SCREEN) return;
        stage.setWidth(mode == DisplayMode.PORTRAIT ? 640 : WINDOW_WIDTH);
        stage.setHeight(mode == DisplayMode.PORTRAIT ? 900 : WINDOW_HEIGHT);
        stage.centerOnScreen();
    }

    private void resetScores() {
        ButtonType confirmReset = new ButtonType("예", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelReset = new ButtonType("아니요", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "저장된 모든 점수를 삭제할까요?", confirmReset, cancelReset);
        confirmation.initOwner(stage);
        confirmation.setHeaderText("점수 초기화");
        styleDialog(confirmation.getDialogPane(), "score-reset-dialog");
        ButtonType selected = confirmation.showAndWait().orElse(cancelReset);
        if (!isResetConfirmed(selected)) return;
        Alert result;
        try {
            scoreboardManager.clear();
            result = new Alert(Alert.AlertType.INFORMATION);
            result.setHeaderText("점수가 초기화되었습니다.");
        } catch (IOException exception) {
            result = new Alert(Alert.AlertType.ERROR);
            result.setHeaderText("점수를 초기화하지 못했습니다.");
            result.setContentText(exception.getMessage());
        }
        result.initOwner(stage);
        styleDialog(result.getDialogPane(), "score-reset-dialog");
        result.showAndWait();
    }

    static boolean isResetConfirmed(ButtonType selected) {
        return selected != null && selected.getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    private void styleDialog(DialogPane pane, String styleClass) {
        pane.getStylesheets().add(Objects.requireNonNull(
                App.class.getResource("/com/team10/tetris/dialog.css")).toExternalForm());
        pane.getStyleClass().addAll("tetris-dialog", styleClass);
    }

    private void showGameOver(int score, int clearedLines, GameConfig config) {
        GameOverView view = new GameOverView(scoreboardManager);
        view.setOnRestart(() -> showScreen(createGameView(config)));
        view.setOnMainMenu(this::showStartMenu);
        view.setOnExit(Platform::exit);
        showScreen(view);
        view.showResult(score, clearedLines, config);
    }

    private void showScreen(Parent screen) {
        scene.setRoot(screen);
        screen.requestFocus();
    }
}
