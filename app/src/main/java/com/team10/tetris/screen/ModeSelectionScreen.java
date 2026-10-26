package com.team10.tetris.screen;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.team10.tetris.game.GameMode;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** 게임 시작 전에 일반 모드와 아이템 모드를 선택하는 화면. */
public final class ModeSelectionScreen extends BorderPane {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private final List<GameMode> modes = List.of(GameMode.NORMAL, GameMode.ITEM);
    private final MenuSelectionModel selection = new MenuSelectionModel(2);
    private final List<VBox> cards;
    private final List<Button> startButtons;
    private final Consumer<GameMode> onModeSelected;

    public ModeSelectionScreen(Consumer<GameMode> onModeSelected, Runnable onScoreboard, Runnable onBack) {
        com.team10.tetris.ui.AppFonts.apply(this);
        this.onModeSelected = Objects.requireNonNull(onModeSelected);
        Objects.requireNonNull(onScoreboard);
        Objects.requireNonNull(onBack);
        getStyleClass().add("mode-selection-screen");
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/team10/tetris/mode-selection.css")).toExternalForm());
        setPadding(new Insets(42, 70, 42, 70));

        VBox heading = new VBox(6,
                label("SELECT SESSION", "mode-eyebrow"),
                label("Mode Selection", "mode-title"),
                label("게임 규칙과 난이도를 고른 뒤 바로 시작할 수 있습니다.", "mode-description"));
        VBox normal = createCard(GameMode.NORMAL, "NORMAL MODE", "▦", "일반 모드",
                "기본 규칙 · 난이도별 I 블록 확률", "◆  EASY · NORMAL · HARD");
        VBox item = createCard(GameMode.ITEM, "ITEM MODE", "✦", "아이템 모드",
                "10줄마다 NEXT에 아이템 신호 표시", "●  COLOR + SYMBOL + KEY");
        cards = List.of(normal, item);
        startButtons = cards.stream().map(card -> (Button) card.getChildren()
                .get(card.getChildren().size() - 1)).toList();
        HBox row = new HBox(30, normal, item);
        HBox.setHgrow(normal, Priority.ALWAYS);
        HBox.setHgrow(item, Priority.ALWAYS);

        Button records = new Button("기록 보기");
        records.getStyleClass().add("mode-secondary-button");
        records.setOnAction(event -> onScoreboard.run());
        HBox footer = new HBox(records);
        footer.setAlignment(Pos.CENTER_RIGHT);
        setCenter(new VBox(28, heading, row, footer));

        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.LEFT || key == KeyCode.UP || key == KeyCode.A || key == KeyCode.W) selection.movePrevious();
            else if (key == KeyCode.RIGHT || key == KeyCode.DOWN || key == KeyCode.D || key == KeyCode.S) selection.moveNext();
            else if (key == KeyCode.ENTER || key == KeyCode.SPACE) startButtons.get(selection.selectedIndex()).fire();
            else if (key == KeyCode.ESCAPE) onBack.run();
            else return;
            updateSelection();
            event.consume();
        });
        updateSelection();
    }

    public void requestMenuFocus() { requestFocus(); }

    private VBox createCard(GameMode mode, String overline, String icon, String title,
            String description, String badge) {
        Label iconLabel = label(icon, "mode-card-icon");
        Label badgeLabel = label(badge, "mode-card-badge");
        if (mode == GameMode.ITEM) {
            iconLabel.getStyleClass().add("item-accent");
            badgeLabel.getStyleClass().add("item-accent");
        }
        Button start = new Button(mode == GameMode.NORMAL ? "일반 모드 시작" : "아이템 모드 시작");
        start.getStyleClass().add("mode-primary-button");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(event -> onModeSelected.accept(mode));
        VBox card = new VBox(10, label(overline, "mode-card-overline"), iconLabel,
                label(title, "mode-card-title"), label(description, "mode-card-description"), badgeLabel, start);
        card.getStyleClass().addAll("mode-card", mode == GameMode.NORMAL ? "normal-card" : "item-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseEntered(event -> { selection.select(modes.indexOf(mode)); updateSelection(); });
        return card;
    }

    private void updateSelection() {
        for (int index = 0; index < cards.size(); index++)
            cards.get(index).pseudoClassStateChanged(SELECTED, index == selection.selectedIndex());
    }

    private Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
