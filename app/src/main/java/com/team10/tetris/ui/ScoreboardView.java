package com.team10.tetris.ui;

import com.team10.tetris.score.GameRecord;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameMode;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ScoreboardView extends BorderPane {

    private final ScoreboardManager manager;

    private final VBox recordsBox = new VBox(10);
    private final Label highScoreLabel = new Label();
    private final ComboBox<GameMode> modeChoice = new ComboBox<>();
    private final ComboBox<Difficulty> difficultyChoice = new ComboBox<>();

    private Runnable onBackRequested = () -> {};

    public ScoreboardView(ScoreboardManager manager) {
        this.manager = manager;

        Label title = new Label("SCOREBOARD");
        modeChoice.getItems().setAll(GameMode.values());
        difficultyChoice.getItems().setAll(Difficulty.values());
        modeChoice.setValue(GameMode.NORMAL);
        difficultyChoice.setValue(Difficulty.NORMAL);
        modeChoice.setOnAction(event -> refresh());
        difficultyChoice.setOnAction(event -> refresh());
        HBox filters = new HBox(12, new Label("모드"), modeChoice,
                new Label("난이도"), difficultyChoice);
        filters.setAlignment(Pos.CENTER);
        for (var node : filters.getChildren()) {
            if (node instanceof Label label) label.setStyle("-fx-text-fill: #E8EEF2;");
        }

        Button backButton = new Button("시작 화면으로");
        backButton.setOnAction(
                event -> onBackRequested.run()
        );

        VBox header = new VBox(
                12,
                title,
                filters,
                highScoreLabel
        );

        header.setAlignment(Pos.CENTER);

        recordsBox.setAlignment(Pos.TOP_CENTER);
        recordsBox.setStyle("-fx-background-color: #101820; -fx-padding: 20px;");

        setTop(header);
        ScrollPane scroll = new ScrollPane(recordsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #101820; -fx-background-color: #101820;");
        setCenter(scroll);
        BorderPane.setMargin(scroll, new Insets(20, 0, 15, 0));
        setBottom(backButton);

        BorderPane.setAlignment(backButton, Pos.CENTER);

        setPadding(new Insets(30));
        setStyle("-fx-background-color: #101820;");
        title.setStyle("-fx-text-fill: #63D4C5; -fx-font-size: 28px;");
        highScoreLabel.setStyle("-fx-text-fill: #E8EEF2;");

        refresh();
    }

    public void setOnBackRequested(Runnable callback) {
        onBackRequested = callback != null
                ? callback
                : () -> {};
    }

    public void refresh() {
        recordsBox.getChildren().clear();

        try {
            GameMode mode = modeChoice.getValue();
            Difficulty difficulty = difficultyChoice.getValue();
            List<GameRecord> records = manager.getTopRecords(mode, difficulty, 10);

            highScoreLabel.setText(
                    mode + " / " + difficulty + "  HIGH SCORE: " + manager.getHighScore(mode, difficulty)
            );

            if (records.isEmpty()) {
                recordsBox.getChildren().add(
                        message("아직 게임 기록이 없습니다.")
                );
                return;
            }

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm"
                    );

            recordsBox.getChildren().add(recordRow("순위", "이름", "점수", "지운 줄", "날짜"));

            for (int i = 0; i < records.size(); i++) {
                GameRecord record = records.get(i);

                Label rank = new Label(
                        String.valueOf(i + 1)
                );

                Label name = new Label(
                        record.playerName()
                );

                Label score = new Label(
                        String.valueOf(record.score())
                );

                Label lines = new Label(
                        record.clearedLines() + "줄"
                );

                Label date = new Label(
                        record.playedAt().format(formatter)
                );

                HBox row = recordRow(rank.getText(), name.getText(), score.getText(), lines.getText(), date.getText());

                recordsBox.getChildren().add(row);
            }

        } catch (IOException exception) {
            highScoreLabel.setText("기록을 불러올 수 없습니다.");
            recordsBox.getChildren().add(
                    message(exception.getMessage())
            );
        }
    }

    private Label message(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #E8EEF2;");
        return label;
    }

    private HBox recordRow(String rank, String name, String score, String lines, String date) {
        String[] values = {rank, name, score, lines, date};
        int[] widths = {50, 150, 120, 80, 180};
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < values.length; i++) {
            Label label = message(values[i]);
            label.setPrefWidth(widths[i]);
            row.getChildren().add(label);
        }
        return row;
    }
}
