package com.team10.tetris.ui;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameMode;
import com.team10.tetris.score.GameRecord;
import com.team10.tetris.score.ScoreboardManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** 저장된 기록을 모드와 난이도별로 보여주는 View. */
public final class ScoreboardView extends BorderPane {
    private final ScoreboardManager manager;
    private final VBox recordsBox = new VBox();
    private final Label summary = new Label();
    private final ComboBox<GameMode> modeChoice = new ComboBox<>();
    private final ComboBox<Difficulty> difficultyChoice = new ComboBox<>();
    private final Button backButton = new Button("시작 화면");
    private final List<Control> navigationControls;
    private int focusedControlIndex;
    private Runnable onBackRequested = () -> {};

    public ScoreboardView(ScoreboardManager manager) {
        com.team10.tetris.ui.AppFonts.apply(this);
        this.manager = manager;
        getStyleClass().add("scoreboard-screen");
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/team10/tetris/scoreboard.css")).toExternalForm());
        setPadding(new Insets(38, 70, 42, 70));

        Label title = new Label("LEADERBOARD");
        title.getStyleClass().add("scoreboard-title");
        modeChoice.getItems().setAll(GameMode.values());
        difficultyChoice.getItems().setAll(Difficulty.values());
        modeChoice.setValue(GameMode.NORMAL);
        difficultyChoice.setValue(Difficulty.NORMAL);
        modeChoice.getStyleClass().add("scoreboard-filter");
        difficultyChoice.getStyleClass().add("scoreboard-filter");
        modeChoice.setOnAction(event -> refresh());
        difficultyChoice.setOnAction(event -> refresh());

        HBox filters = new HBox(10, new Label("모드"), modeChoice,
                new Label("난이도"), difficultyChoice, summary);
        filters.getStyleClass().add("scoreboard-filters");
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(12, title, filters);

        recordsBox.getStyleClass().add("scoreboard-records");
        ScrollPane scroll = new ScrollPane(recordsBox);
        scroll.getStyleClass().add("scoreboard-scroll");
        scroll.setFitToWidth(true);

        backButton.getStyleClass().add("scoreboard-back");
        backButton.setOnAction(event -> onBackRequested.run());
        navigationControls = List.of(modeChoice, difficultyChoice, backButton);
        for (int index = 0; index < navigationControls.size(); index++) {
            int controlIndex = index;
            navigationControls.get(index).focusedProperty().addListener(
                    (observable, oldValue, focused) -> {
                        if (focused) focusedControlIndex = controlIndex;
                    });
        }
        setTop(header);
        setCenter(scroll);
        setBottom(backButton);
        BorderPane.setMargin(scroll, new Insets(24, 0, 28, 0));
        BorderPane.setAlignment(backButton, Pos.CENTER_LEFT);

        setFocusTraversable(true);
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.ESCAPE) {
                onBackRequested.run();
                event.consume();
                return;
            }
            if (modeChoice.isShowing() || difficultyChoice.isShowing()) return;

            if (key == KeyCode.UP || key == KeyCode.W) {
                focusControl(-1);
            } else if (key == KeyCode.DOWN || key == KeyCode.S) {
                focusControl(1);
            } else if (key == KeyCode.LEFT || key == KeyCode.A) {
                changeFocusedFilter(-1);
            } else if (key == KeyCode.RIGHT || key == KeyCode.D) {
                changeFocusedFilter(1);
            } else if (key == KeyCode.ENTER || key == KeyCode.SPACE) {
                Control focused = navigationControls.get(focusedControlIndex);
                if (focused == backButton) backButton.fire();
                else if (focused instanceof ComboBox<?> comboBox) comboBox.show();
            } else {
                return;
            }
            event.consume();
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) javafx.application.Platform.runLater(modeChoice::requestFocus);
        });
        refresh();
    }

    private void focusControl(int delta) {
        focusedControlIndex = Math.floorMod(focusedControlIndex + delta, navigationControls.size());
        navigationControls.get(focusedControlIndex).requestFocus();
    }

    private void changeFocusedFilter(int delta) {
        Control focused = navigationControls.get(focusedControlIndex);
        if (focused == modeChoice) selectRelative(modeChoice, delta);
        else if (focused == difficultyChoice) selectRelative(difficultyChoice, delta);
    }

    private <T> void selectRelative(ComboBox<T> comboBox, int delta) {
        int count = comboBox.getItems().size();
        if (count == 0) return;
        int selected = comboBox.getSelectionModel().getSelectedIndex();
        comboBox.getSelectionModel().select(Math.floorMod(selected + delta, count));
    }

    public void setOnBackRequested(Runnable callback) {
        onBackRequested = callback != null ? callback : () -> {};
    }

    public void refresh() {
        recordsBox.getChildren().clear();
        try {
            GameMode mode = modeChoice.getValue();
            Difficulty difficulty = difficultyChoice.getValue();
            List<GameRecord> records = manager.getTopRecords(mode, difficulty, 10);
            summary.setText("HIGH SCORE  " + String.format("%06d", manager.getHighScore(mode, difficulty)));
            recordsBox.getChildren().add(row(true, "순위", "플레이어", "모드", "난이도", "점수", "삭제 줄"));
            if (records.isEmpty()) {
                Label empty = new Label("아직 게임 기록이 없습니다.");
                empty.getStyleClass().add("scoreboard-empty");
                recordsBox.getChildren().add(empty);
                return;
            }
            for (int index = 0; index < records.size(); index++) {
                GameRecord record = records.get(index);
                recordsBox.getChildren().add(row(false,
                        String.format("%02d", index + 1),
                        record.playerName(),
                        modeText(record.mode()),
                        difficultyText(record.difficulty()),
                        String.format("%06d", record.score()),
                        String.valueOf(record.clearedLines())));
            }
        } catch (IOException exception) {
            summary.setText("기록을 불러올 수 없습니다.");
            Label error = new Label(exception.getMessage());
            error.getStyleClass().add("scoreboard-empty");
            recordsBox.getChildren().add(error);
        }
    }

    private String modeText(GameMode mode) {
        return mode == GameMode.ITEM ? "✦ ITEM" : "◆ NORMAL";
    }

    private String difficultyText(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "◆ EASY";
            case NORMAL -> "● NORMAL";
            case HARD -> "▲ HARD";
        };
    }

    private HBox row(boolean header, String... values) {
        double[] widths = {80, 200, 130, 130, 140, 90};
        HBox row = new HBox();
        row.getStyleClass().add(header ? "scoreboard-header-row" : "scoreboard-record-row");
        for (int index = 0; index < values.length; index++) {
            Label value = new Label(values[index]);
            value.setPrefWidth(widths[index]);
            value.getStyleClass().add("scoreboard-cell");
            if (!header && index == 0) value.getStyleClass().add("scoreboard-rank");
            if (!header && index == 2 && values[index].contains("ITEM")) value.getStyleClass().add("scoreboard-item");
            row.getChildren().add(value);
        }
        return row;
    }
}
