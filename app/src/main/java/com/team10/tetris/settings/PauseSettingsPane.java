package com.team10.tetris.settings;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** 게임 화면 위에 표시되는 일시정지 설정 창. */
public final class PauseSettingsPane extends StackPane {
    public PauseSettingsPane(
            GameSettings settings,
            Consumer<DisplayMode> onDisplayModeChanged,
            Runnable onScoreReset,
            Runnable onResume,
            Runnable onMainMenu) {
        Objects.requireNonNull(onResume);
        Objects.requireNonNull(onMainMenu);
        getStyleClass().addAll("settings-root", "pause-settings-overlay");
        setPadding(new Insets(16));
        setMinSize(0, 0);

        Label eyebrow = new Label("PAUSE / SETTINGS");
        eyebrow.getStyleClass().add("settings-eyebrow");
        Label title = new Label("설정");
        title.getStyleClass().add("settings-title");
        Label description = new Label("↑ ↓ 항목 이동 · ← → 옵션 이동 · Enter 선택/확정 · ESC 복귀");
        description.getStyleClass().add("settings-description");
        description.setWrapText(true);

        SettingsPanel panel = new SettingsPanel(
                settings,
                onDisplayModeChanged,
                onScoreReset,
                null);

        Button resumeButton = new Button("게임 계속");
        resumeButton.getStyleClass().add("primary-button");
        resumeButton.setOnAction(event -> onResume.run());
        Button mainMenuButton = new Button("시작 화면");
        mainMenuButton.getStyleClass().add("secondary-button");
        mainMenuButton.setOnAction(event -> onMainMenu.run());

        HBox actions = new HBox(10, resumeButton, mainMenuButton);
        actions.getStyleClass().add("settings-actions");
        resumeButton.setMaxWidth(Double.MAX_VALUE);
        mainMenuButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(resumeButton, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(mainMenuButton, javafx.scene.layout.Priority.ALWAYS);

        VBox content = new VBox(10, eyebrow, title, description, panel, actions);
        content.getStyleClass().add("pause-settings-dialog");
        content.setMinWidth(0);
        content.setMinHeight(VBox.USE_PREF_SIZE);
        content.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("pause-settings-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxWidth(608);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        StackPane.setAlignment(scrollPane, Pos.CENTER);
        getChildren().add(scrollPane);

        SettingsNavigation.install(this, content, scrollPane);
        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                onResume.run();
                event.consume();
            }
        });
    }
}
