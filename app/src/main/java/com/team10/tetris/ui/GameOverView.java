package com.team10.tetris.ui;

import com.team10.tetris.score.GameRecord;
import com.team10.tetris.score.ScoreboardManager;

import java.io.IOException;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 게임 종료 화면.
 * 결과 확인 → 이름 입력 → 순위 강조 표시 순서로 내용이 바뀐다.
 */
public class GameOverView extends BorderPane {

    private static final int MAX_NAME_LENGTH = 10;

    private static final String FONT_FAMILY =
            "'Noto Sans KR', 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif";

    // 시안 색상
    private static final String COLOR_BACKGROUND = "#101820";
    private static final String COLOR_PANEL = "#16202C";
    private static final String COLOR_ACCENT = "#63D4C5";
    private static final String COLOR_TEXT = "#E8EEF2";
    private static final String COLOR_TEXT_DIM = "#A3B1BE";
    private static final String COLOR_BORDER = "#22303F";
    private static final String COLOR_HIGHLIGHT = "#1B3B3A";

    private final ScoreboardManager manager;

    private final Label headlineLabel = new Label("수고했어요!");
    private final Label descriptionLabel =
            new Label("이번 판의 기록을 확인해 보세요.");

    // FINAL SCORE 박스와 순위표가 번갈아 들어가는 자리
    private final VBox contentArea = new VBox();

    private final TextField nameField = new TextField();
    private final Button saveButton = new Button("저장");
    private final HBox nameInputRow = new HBox(12);

    private Runnable onRestart = () -> {};
    private Runnable onMainMenu = () -> {};
    private Runnable onExit = () -> {};

    private int score;
    private int clearedLines;

    public GameOverView(ScoreboardManager manager) {
        this.manager = manager;

        setTop(createHeader());
        setCenter(createBody());
        setBottom(createButtonRow());

        setPadding(new Insets(40, 48, 36, 48));
        setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";"
                + "-fx-font-family: " + FONT_FAMILY + ";");
    }

    // ---------------------------------------------------------------
    // 외부 연결 지점
    // ---------------------------------------------------------------

    public void setOnRestart(Runnable callback) {
        onRestart = callback != null ? callback : () -> {};
    }

    public void setOnMainMenu(Runnable callback) {
        onMainMenu = callback != null ? callback : () -> {};
    }

    public void setOnExit(Runnable callback) {
        onExit = callback != null ? callback : () -> {};
    }

    /**
     * 게임이 끝났을 때 호출해 결과를 표시한다.
     * 점수가 순위에 들면 이름 입력을 요청하고, 아니면 순위표를 바로 보여준다.
     */
    public void showResult(int score, int clearedLines) {
        this.score = Math.max(0, score);
        this.clearedLines = Math.max(0, clearedLines);

        headlineLabel.setText("수고했어요!");
        descriptionLabel.setText("이번 판의 기록을 확인해 보세요.");

        boolean canRegister = false;

        try {
            canRegister = manager.isHighScore(this.score);
        } catch (IOException exception) {
            // 기록을 읽지 못하면 등록 단계를 건너뛴다
        }

        showScorePanel();
        nameInputRow.setVisible(canRegister);
        nameInputRow.setManaged(canRegister);

        if (canRegister) {
            nameField.clear();
            nameField.requestFocus();
        }
    }

    // ---------------------------------------------------------------
    // 화면 구성
    // ---------------------------------------------------------------

    private VBox createHeader() {
        Label eyebrow = new Label("GAME OVER  /  결과");
        eyebrow.setStyle(labelStyle(13, COLOR_ACCENT, true));

        headlineLabel.setStyle(labelStyle(38, COLOR_TEXT, true));
        descriptionLabel.setStyle(labelStyle(14, COLOR_TEXT_DIM, false));

        VBox header = new VBox(8, eyebrow, headlineLabel, descriptionLabel);
        header.setPadding(new Insets(0, 0, 24, 0));

        return header;
    }

    private VBox createBody() {
        contentArea.setStyle(panelStyle());
        contentArea.setPadding(new Insets(24, 28, 24, 28));
        contentArea.setMinHeight(190);
        contentArea.setPrefHeight(330);
        contentArea.setMaxHeight(370);

        buildNameInputRow();

        return new VBox(18, contentArea, nameInputRow);
    }

    private void buildNameInputRow() {
        Label prompt = new Label("이름을 입력하세요");
        prompt.setStyle(labelStyle(14, COLOR_TEXT, false));

        nameField.setPromptText("최대 " + MAX_NAME_LENGTH + "자");
        nameField.setPrefWidth(240);
        nameField.setPrefHeight(38);
        nameField.setStyle(
                "-fx-background-color: " + COLOR_PANEL + ";"
                        + "-fx-text-fill: " + COLOR_TEXT + ";"
                        + "-fx-prompt-text-fill: " + COLOR_TEXT_DIM + ";"
                        + "-fx-border-color: " + COLOR_BORDER + ";"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-font-family: " + FONT_FAMILY + ";"
                        + "-fx-font-size: 14px;"
                        + "-fx-padding: 6 12 6 12;"
        );

        // 길이 제한
        nameField.textProperty().addListener(
                (observable, oldText, newText) -> {
                    if (newText.length() > MAX_NAME_LENGTH) {
                        nameField.setText(
                                newText.substring(0, MAX_NAME_LENGTH)
                        );
                    }
                }
        );

        // 빈 이름이면 저장 버튼 비활성화
        saveButton.disableProperty().bind(
                nameField.textProperty().isEmpty()
        );

        saveButton.setStyle(accentButtonStyle());
        saveButton.setOnAction(event -> saveRecord());
        nameField.setOnAction(event -> {
            if (!saveButton.isDisabled()) {
                saveRecord();
            }
        });

        nameInputRow.setAlignment(Pos.CENTER_LEFT);
        nameInputRow.getChildren().addAll(prompt, nameField, saveButton);
        nameInputRow.setVisible(false);
        nameInputRow.setManaged(false);
    }

    private HBox createButtonRow() {
        Button restartButton = new Button("다시 시작");
        Button menuButton = new Button("시작 화면으로");
        Button exitButton = new Button("게임 종료");

        restartButton.setStyle(accentButtonStyle());
        menuButton.setStyle(plainButtonStyle());
        exitButton.setStyle(plainButtonStyle());

        restartButton.setOnAction(event -> onRestart.run());
        menuButton.setOnAction(event -> onMainMenu.run());
        exitButton.setOnAction(event -> onExit.run());

        for (Button button : List.of(restartButton, menuButton, exitButton)) {
            button.setPrefHeight(44);
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
        }

        HBox row = new HBox(16, restartButton, menuButton, exitButton);
        row.setPadding(new Insets(24, 0, 0, 0));

        return row;
    }

    // ---------------------------------------------------------------
    // 1단계: 이번 판 결과
    // ---------------------------------------------------------------

    private void showScorePanel() {
        int highScore = 0;

        try {
            highScore = manager.getHighScore();
        } catch (IOException exception) {
            // 최고 점수를 읽지 못하면 0으로 표시
        }

        Label caption = new Label("FINAL SCORE");
        caption.setStyle(labelStyle(13, COLOR_ACCENT, true));

        Label scoreLabel = new Label(formatScore(score));
        scoreLabel.setStyle(labelStyle(54, COLOR_TEXT, true));

        VBox left = new VBox(6, caption, scoreLabel);
        left.setAlignment(Pos.CENTER_LEFT);

        VBox right = new VBox(
                16,
                createStatRow("지운 줄", String.valueOf(clearedLines)),
                createStatRow("최고 점수", String.valueOf(highScore))
        );
        right.setMinWidth(240);
        right.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox panel = new HBox(left, spacer, right);
        panel.setAlignment(Pos.CENTER_LEFT);

        contentArea.getChildren().setAll(panel);
        contentArea.setAlignment(Pos.CENTER_LEFT);
    }

    private HBox createStatRow(String name, String value) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle(labelStyle(13, COLOR_TEXT_DIM, false));

        Label valueLabel = new Label(value);
        valueLabel.setStyle(labelStyle(20, COLOR_TEXT, true));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(nameLabel, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // ---------------------------------------------------------------
    // 2단계: 순위표
    // ---------------------------------------------------------------

    private void saveRecord() {
        String playerName = nameField.getText().trim();

        if (playerName.isEmpty()) {
            return;
        }

        int rank = ScoreboardManager.UNRANKED;

        try {
            rank = manager.saveAndGetRank(playerName, score, clearedLines);
        } catch (IOException exception) {
            descriptionLabel.setText("기록을 저장하지 못했습니다.");
        }

        nameInputRow.setVisible(false);
        nameInputRow.setManaged(false);

        if (rank != ScoreboardManager.UNRANKED) {
            headlineLabel.setText(rank + "위에 올랐어요!");
            descriptionLabel.setText("이번 기록이 스코어보드에 등록되었습니다.");
        }

        showScoreboard(rank);
    }

    private void showScoreboard(int highlightRank) {
        List<GameRecord> records;

        try {
            records = manager.getTopRecords(ScoreboardManager.MAX_RECORDS);
        } catch (IOException exception) {
            contentArea.getChildren().setAll(
                    createMessageLabel("기록을 불러오지 못했습니다.")
            );
            return;
        }

        if (records.isEmpty()) {
            contentArea.getChildren().setAll(
                    createMessageLabel("아직 게임 기록이 없습니다.")
            );
            return;
        }

        VBox table = new VBox(3);
        table.getChildren().add(createHeaderRow());

        for (int index = 0; index < records.size(); index++) {
            int rank = index + 1;

            table.getChildren().add(
                    createRecordRow(
                            rank,
                            records.get(index),
                            rank == highlightRank
                    )
            );
        }

        contentArea.getChildren().setAll(table);
        contentArea.setAlignment(Pos.TOP_LEFT);
    }

    private HBox createHeaderRow() {
        HBox row = new HBox(
                createCell("순위", 90, COLOR_ACCENT, true),
                createCell("이름", 220, COLOR_ACCENT, true),
                createCell("점수", 160, COLOR_ACCENT, true),
                createCell("지운 줄", 100, COLOR_ACCENT, true)
        );

        row.setPadding(new Insets(0, 10, 10, 10));

        return row;
    }

    private HBox createRecordRow(
            int rank,
            GameRecord record,
            boolean highlighted
    ) {
        String textColor = highlighted ? COLOR_ACCENT : COLOR_TEXT;

        HBox row = new HBox(
                createCell(String.valueOf(rank), 90, textColor, highlighted),
                createCell(record.playerName(), 220, textColor, highlighted),
                createCell(
                        formatScore(record.score()),
                        160,
                        textColor,
                        highlighted
                ),
                createCell(
                        String.valueOf(record.clearedLines()),
                        100,
                        textColor,
                        highlighted
                )
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 10, 7, 10));

        if (highlighted) {
            row.setStyle(
                    "-fx-background-color: " + COLOR_HIGHLIGHT + ";"
                            + "-fx-background-radius: 6;"
            );
        }

        return row;
    }

    private Label createCell(
            String text,
            double width,
            String color,
            boolean bold
    ) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setStyle(labelStyle(15, color, bold));

        return label;
    }

    private Label createMessageLabel(String text) {
        Label label = new Label(text);
        label.setStyle(labelStyle(14, COLOR_TEXT_DIM, false));

        return label;
    }

    // ---------------------------------------------------------------
    // 공통 스타일
    // ---------------------------------------------------------------

    /** 시안처럼 점수를 6자리로 맞춰 표시한다. */
    private String formatScore(int value) {
        return String.format("%06d", value);
    }

    /** 폰트를 명시해 시스템 기본 폰트가 기울어 보이는 문제를 막는다. */
    private String labelStyle(int fontSize, String color, boolean bold) {
        return "-fx-font-family: " + FONT_FAMILY + ";"
                + "-fx-font-size: " + fontSize + "px;"
                + "-fx-text-fill: " + color + ";"
                + "-fx-font-style: normal;"
                + (bold
                ? "-fx-font-weight: bold;"
                : "-fx-font-weight: normal;");
    }

    private String panelStyle() {
        return "-fx-background-color: " + COLOR_PANEL + ";"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-radius: 10;";
    }

    private String accentButtonStyle() {
        return "-fx-background-color: " + COLOR_ACCENT + ";"
                + "-fx-text-fill: #0B1620;"
                + "-fx-font-family: " + FONT_FAMILY + ";"
                + "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 8 20 8 20;"
                + "-fx-cursor: hand;";
    }

    private String plainButtonStyle() {
        return "-fx-background-color: " + COLOR_PANEL + ";"
                + "-fx-text-fill: " + COLOR_TEXT + ";"
                + "-fx-font-family: " + FONT_FAMILY + ";"
                + "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-cursor: hand;";
    }
}