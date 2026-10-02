package com.team10.tetris.ui;

import com.team10.tetris.score.GameRecord;
import com.team10.tetris.score.ScoreboardManager;

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

    private Runnable onBackRequested = () -> {};

    public ScoreboardView(ScoreboardManager manager) {
        this.manager = manager;

        Label title = new Label("SCOREBOARD");

        Button backButton = new Button("시작 화면으로");
        backButton.setOnAction(
                event -> onBackRequested.run()
        );

        VBox header = new VBox(
                12,
                title,
                highScoreLabel
        );

        header.setAlignment(Pos.CENTER);

        recordsBox.setAlignment(Pos.TOP_CENTER);

        setTop(header);
        setCenter(recordsBox);
        setBottom(backButton);

        BorderPane.setAlignment(backButton, Pos.CENTER);

        setPadding(new Insets(30));
        setStyle("-fx-background-color: #101820;");

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
            List<GameRecord> records = manager.getTopRecords(10);

            highScoreLabel.setText(
                    "HIGH SCORE: " + manager.getHighScore()
            );

            if (records.isEmpty()) {
                recordsBox.getChildren().add(
                        new Label("아직 게임 기록이 없습니다.")
                );
                return;
            }

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm"
                    );

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

                HBox row = new HBox(
                        20,
                        rank,
                        name,
                        score,
                        lines,
                        date
                );

                row.setAlignment(Pos.CENTER);

                recordsBox.getChildren().add(row);
            }

        } catch (IOException exception) {
            highScoreLabel.setText("기록을 불러올 수 없습니다.");
            recordsBox.getChildren().add(
                    new Label(exception.getMessage())
            );
        }
    }
}