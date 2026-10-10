package com.team10.tetris.settings;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** 시작 메뉴에서 진입하는 전체 페이지 설정 화면. */
public final class SettingsScreen extends BorderPane {
    public SettingsScreen(
            GameSettings settings,
            Consumer<DisplayMode> onDisplayModeChanged,
            Runnable onScoreReset,
            Runnable onBack) {
        Objects.requireNonNull(onBack);
        getStyleClass().addAll("settings-root", "settings-screen");

        Label eyebrow = new Label("SETTINGS");
        eyebrow.getStyleClass().add("settings-eyebrow");
        Label title = new Label("설정");
        title.getStyleClass().add("settings-title");
        Label description = new Label("↑ ↓ 항목 이동 · ← → 옵션 이동 · Enter 선택/확정 · ESC 뒤로");
        description.getStyleClass().add("settings-description");
        description.setWrapText(true);

        SettingsPanel panel = new SettingsPanel(
                settings,
                onDisplayModeChanged,
                onScoreReset,
                null);

        Button backButton = new Button("시작 화면으로");
        backButton.getStyleClass().add("primary-button");
        backButton.setMaxWidth(220);
        backButton.setOnAction(event -> onBack.run());

        VBox content = new VBox(14, eyebrow, title, description, panel, backButton);
        content.getStyleClass().add("settings-content");
        content.setAlignment(Pos.TOP_CENTER);
        content.setMinWidth(0);
        content.setMinHeight(VBox.USE_PREF_SIZE);
        content.setMaxWidth(608);
        content.setPadding(new Insets(24, 24, 32, 24));

        StackPane centeredContent = new StackPane(content);
        centeredContent.getStyleClass().add("settings-content-wrapper");
        centeredContent.setAlignment(Pos.TOP_CENTER);
        centeredContent.setMinWidth(0);

        ScrollPane scrollPane = new ScrollPane(centeredContent);
        scrollPane.getStyleClass().add("settings-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setMinSize(0, 0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        setCenter(scrollPane);

        SettingsNavigation.install(this, centeredContent, scrollPane);
        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                onBack.run();
                event.consume();
            }
        });
    }

}
