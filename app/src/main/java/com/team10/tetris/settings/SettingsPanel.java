package com.team10.tetris.settings;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** 전체 설정 화면과 일시정지 창이 함께 사용하는 설정 항목 모음. */
public final class SettingsPanel extends VBox {
    private final GameSettings settings;
    private final Consumer<DisplayMode> onDisplayModeChanged;
    private final Runnable onScoreReset;

    public SettingsPanel(
            GameSettings settings,
            Consumer<DisplayMode> onDisplayModeChanged,
            Runnable onScoreReset,
            Runnable onKeyChangeRequested) {
        this.settings = Objects.requireNonNull(settings);
        this.onDisplayModeChanged = Objects.requireNonNull(onDisplayModeChanged);
        this.onScoreReset = Objects.requireNonNull(onScoreReset);

        getStyleClass().add("settings-panel");
        setSpacing(18);

        getChildren().addAll(
                createDropSpeedSection(),
                new Separator(),
                createDisplayModeSection(),
                new Separator(),
                createControlsSection(onKeyChangeRequested),
                new Separator(),
                createToggleSection(
                        "색맹 모드",
                        "색상 외에도 블록을 쉽게 구분할 수 있도록 표시합니다.",
                        settings.isColorBlindMode(),
                        settings::setColorBlindMode),
                createToggleSection(
                        "데모 모드",
                        "게임 동작을 빠르게 확인하기 위한 상태를 저장합니다.",
                        settings.isDemoMode(),
                        settings::setDemoMode),
                new Separator(),
                createScoreSection());
    }

    private VBox createDropSpeedSection() {
        VBox section = createSection(
                "낙하 속도",
                "자동으로 블록이 내려오는 속도를 선택합니다.");

        ToggleGroup group = new ToggleGroup();
        HBox choices = new HBox(10);
        choices.getStyleClass().add("settings-choice-row");

        for (DropSpeed speed : DropSpeed.values()) {
            ToggleButton button = new ToggleButton(speed.label());
            button.setToggleGroup(group);
            button.setSelected(speed == settings.getDropSpeed());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> {
                settings.setDropSpeed(speed);
                button.setSelected(true);
            });
            HBox.setHgrow(button, Priority.ALWAYS);
            choices.getChildren().add(button);
        }

        section.getChildren().add(choices);
        return section;
    }

    private VBox createDisplayModeSection() {
        VBox section = createSection(
                "화면 모드",
                "목록을 열어 애플리케이션 표시 방식을 선택합니다.");

        ComboBox<DisplayMode> modes = new ComboBox<>();
        modes.getItems().setAll(DisplayMode.values());
        modes.setValue(settings.getDisplayMode());
        modes.setMaxWidth(Double.MAX_VALUE);
        modes.setOnShown(event -> {
            if (modes.getSkin() instanceof ComboBoxListViewSkin<?> skin
                    && skin.getPopupContent() instanceof ListView<?> list) {
                list.getFocusModel().focus(modes.getSelectionModel().getSelectedIndex());
                if (list.getProperties().putIfAbsent("settings-mode-keys", true) == null) {
                    list.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
                        KeyCode code = key.getCode();
                        if (code == KeyCode.UP || code == KeyCode.DOWN
                                || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                            int delta = code == KeyCode.UP || code == KeyCode.LEFT ? -1 : 1;
                            int next = Math.floorMod(list.getFocusModel().getFocusedIndex() + delta,
                                    modes.getItems().size());
                            list.getFocusModel().focus(next);
                            list.scrollTo(next);
                            key.consume();
                        } else if (code == KeyCode.HOME || code == KeyCode.END
                                || code == KeyCode.PAGE_UP || code == KeyCode.PAGE_DOWN) {
                            int next = code == KeyCode.HOME || code == KeyCode.PAGE_UP
                                    ? 0 : modes.getItems().size() - 1;
                            list.getFocusModel().focus(next);
                            list.scrollTo(next);
                            key.consume();
                        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                            int index = list.getFocusModel().getFocusedIndex();
                            modes.hide();
                            if (index >= 0) modes.setValue(modes.getItems().get(index));
                            modes.requestFocus();
                            key.consume();
                        } else if (code == KeyCode.ESCAPE || code == KeyCode.TAB) {
                            modes.hide();
                            modes.requestFocus();
                            key.consume();
                        } else {
                            // 기본 문자 검색 등으로 확인 없이 값이 적용되는 것을 막는다.
                            key.consume();
                        }
                    });
                }
            }
        });
        modes.setOnAction(event -> {
            DisplayMode selected = modes.getValue();
            if (selected != null) {
                settings.setDisplayMode(selected);
                onDisplayModeChanged.accept(selected);
            }
        });

        section.getChildren().add(modes);
        return section;
    }

    private VBox createControlsSection(Runnable onKeyChangeRequested) {
        VBox section = createSection(
                "조작 키",
                "← → 이동    ↑ 회전    ↓ 내리기    SPACE 바로 내리기");
        Button changeButton = new Button("키 변경");
        changeButton.getStyleClass().add("secondary-button");
        changeButton.setDisable(onKeyChangeRequested == null);
        if (onKeyChangeRequested != null) {
            changeButton.setOnAction(event -> onKeyChangeRequested.run());
        }
        section.getChildren().add(changeButton);
        return section;
    }

    private VBox createToggleSection(
            String title,
            String description,
            boolean selected,
            Consumer<Boolean> onChanged) {
        VBox text = createSection(title, description);
        ToggleButton toggle = new ToggleButton();
        toggle.getStyleClass().add("switch-button");
        toggle.setSelected(selected);
        updateToggleText(toggle);
        toggle.setOnAction(event -> {
            onChanged.accept(toggle.isSelected());
            updateToggleText(toggle);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(16, text, spacer, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        return new VBox(row);
    }

    private VBox createScoreSection() {
        VBox section = createSection(
                "스코어보드",
                "저장된 게임 기록을 모두 삭제합니다.");
        Button resetButton = new Button("점수 초기화");
        resetButton.getStyleClass().add("danger-button");
        resetButton.setOnAction(event -> onScoreReset.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox resetRow = new HBox(spacer, resetButton);
        resetRow.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(resetRow);
        return section;
    }

    private VBox createSection(String title, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("settings-section-title");
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("settings-description");
        descriptionLabel.setWrapText(true);
        return new VBox(6, titleLabel, descriptionLabel);
    }

    private void updateToggleText(ToggleButton toggle) {
        toggle.setText(toggle.isSelected() ? "ON" : "OFF");
    }
}
