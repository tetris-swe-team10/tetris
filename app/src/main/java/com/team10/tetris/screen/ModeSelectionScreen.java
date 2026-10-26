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
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
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
        setPadding(Insets.EMPTY);

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
        GridPane row = new GridPane();
        row.setId("mode-cards");
        row.setMinWidth(0);
        row.add(normal, 0, 0); row.add(item, 1, 0);

        Button records = new Button("기록 보기");
        records.getStyleClass().add("mode-secondary-button");
        records.setOnAction(event -> onScoreboard.run());
        Button back = new Button("시작 화면으로");
        back.getStyleClass().add("mode-secondary-button");
        back.setOnAction(event -> onBack.run());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(12, back, spacer, records);
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox content = new VBox(24, heading, row, footer);
        content.setId("mode-selection-content");
        content.setMinSize(0, VBox.USE_PREF_SIZE);
        content.setMaxHeight(VBox.USE_PREF_SIZE);
        StackPane centered = new StackPane(content);
        centered.setAlignment(Pos.CENTER);
        centered.setMinWidth(0);
        ScrollPane scroll = new ScrollPane(centered);
        scroll.getStyleClass().add("mode-scroll");
        scroll.setFitToWidth(true);
        scroll.setMinSize(0, 0);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            double width = bounds.getWidth(), height = bounds.getHeight();
            if (width <= 0 || height <= 0) return;
            boolean stacked = width < 760;
            double scale = Math.max(.9, Math.min(1.5, Math.min(width / 960, height / 700)));
            double padding = Math.max(20, Math.min(64, width * .045));
            double gap = 24 * scale;
            content.setMaxWidth(Math.min(1440, width));
            content.setPadding(new Insets(padding));
            content.setSpacing(gap);
            centered.setMinHeight(height);
            row.setHgap(gap); row.setVgap(16 * scale);
            int columns = stacked ? 1 : 2;
            if (row.getColumnConstraints().size() != columns) {
                row.getColumnConstraints().clear();
                for (int i = 0; i < columns; i++) {
                    ColumnConstraints column = new ColumnConstraints();
                    column.setPercentWidth(100.0 / columns);
                    column.setHgrow(Priority.ALWAYS);
                    row.getColumnConstraints().add(column);
                }
                GridPane.setConstraints(item, stacked ? 0 : 1, stacked ? 1 : 0);
            }
            double cardHeight = stacked ? Math.max(240, Math.min(360, (height - 270) / 2))
                    : Math.max(266, Math.min(520, height * .52));
            for (VBox card : cards) {
                card.setPrefHeight(cardHeight);
                card.setMinHeight(0);
                card.setSpacing(10 * scale);
                card.setPadding(new Insets(20 * scale, 24 * scale, 20 * scale, 24 * scale));
            }
            scaleText(content, scale);
        });
        setCenter(scroll);

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
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox card = new VBox(10, label(overline, "mode-card-overline"), iconLabel,
                label(title, "mode-card-title"), label(description, "mode-card-description"), badgeLabel, spacer, start);
        card.getStyleClass().addAll("mode-card", mode == GameMode.NORMAL ? "normal-card" : "item-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(0);
        card.setOnMouseEntered(event -> { selection.select(modes.indexOf(mode)); updateSelection(); });
        return card;
    }

    private static void scaleText(javafx.scene.Parent parent, double scale) {
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            double base = 0;
            if (child.getStyleClass().contains("mode-title")) base = 34;
            else if (child.getStyleClass().contains("mode-card-title")) base = 22;
            else if (child.getStyleClass().contains("mode-card-icon")) base = 34;
            else if (child.getStyleClass().contains("mode-eyebrow") || child.getStyleClass().contains("mode-card-badge")) base = 11;
            else if (child instanceof Label) base = 12;
            else if (child instanceof Button) base = 13;
            if (base > 0) child.setStyle("-fx-font-size: " + base * scale + "px;");
            if (child instanceof javafx.scene.Parent nested) scaleText(nested, scale);
        }
    }

    private void updateSelection() {
        for (int index = 0; index < cards.size(); index++)
            cards.get(index).pseudoClassStateChanged(SELECTED, index == selection.selectedIndex());
    }

    private Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        return label;
    }
}
