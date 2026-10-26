package com.team10.tetris.screen;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.team10.tetris.input.MenuCommand;
import com.team10.tetris.input.MenuKeyBindings;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class StartScreen extends BorderPane {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final Map<StartMenuAction, Runnable> actionHandlers;
    private final MenuKeyBindings keyBindings;
    private final List<StartMenuAction> menuActions = List.of(StartMenuAction.values());
    private final List<Button> menuButtons;
    private final MenuSelectionModel selection = new MenuSelectionModel(menuActions.size());

    public StartScreen(
            Map<StartMenuAction, Runnable> actionHandlers,
            MenuKeyBindings keyBindings) {
        com.team10.tetris.ui.AppFonts.apply(this);
        this.actionHandlers = Map.copyOf(Objects.requireNonNull(actionHandlers));
        this.keyBindings = Objects.requireNonNull(keyBindings);

        getStyleClass().add("start-screen");
        Label brand = new Label("TETRIS");
        brand.getStyleClass().add("start-brand");
        HBox header = new HBox(brand);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 28, 16, 28));
        header.getStyleClass().add("start-header");
        setTop(header);

        Label eyebrow = new Label("SEOULTECH / SOFTWARE ENGINEERING");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("TETRIS");
        title.getStyleClass().add("title");

        VBox heading = new VBox(8, eyebrow, title);

        menuButtons = menuActions.stream()
                .map(this::createMenuButton)
                .toList();

        VBox menu = new VBox(12);
        menu.getStyleClass().add("menu");
        menu.setMaxWidth(Double.MAX_VALUE);
        menu.getChildren().addAll(menuButtons);

        Label guide = new Label("게임 시작 후 일반 모드 또는 아이템 모드를 선택합니다.\n↑ ↓ / W S 이동 · Enter / Space 선택");
        guide.setWrapText(true);
        guide.getStyleClass().add("guide");

        VBox content = new VBox(34, heading, menu, guide);
        content.setAlignment(Pos.CENTER_LEFT);
        Canvas artwork = new Canvas();
        artwork.setMouseTransparent(true);
        Pane stage = new Pane(content, artwork) {
            @Override protected void layoutChildren() {
                double w = getWidth(), h = getHeight();
                boolean portrait = w < 720 || h > w * 1.15;
                double pad = Math.max(20, Math.min(w * .085, 120));
                double usable = Math.max(0, w - 2 * pad);
                double scale = Math.max(.72, Math.min(1.6, Math.min(w / 960, h / 620)));
                title.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-font-size: " + (64 * scale) + "px;");
                eyebrow.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-font-size: " + (11 * scale) + "px;");
                guide.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-font-size: " + (11 * scale) + "px;");
                menu.setSpacing(10 * scale);
                content.setSpacing(28 * scale);
                for (Button button : menuButtons) {
                    button.setStyle("-fx-font-family: 'Noto Sans KR'; -fx-font-size: " + (14 * scale) + "px; -fx-min-height: "
                            + (44 * scale) + "px; -fx-padding: " + (10 * scale) + "px 16px;");
                }
                double textWidth = portrait ? usable : usable * .44;
                double contentHeight = content.prefHeight(textWidth);
                double artSize = portrait
                        ? Math.max(0, Math.min(usable * .72, h - contentHeight - 64))
                        : Math.min(usable * .45, h * .72);
                double top = portrait ? Math.max(20, (h - contentHeight - artSize - 24) / 2)
                        : Math.max(20, (h - contentHeight) / 2);
                content.resizeRelocate(pad, top, textWidth, contentHeight);
                artwork.setWidth(artSize); artwork.setHeight(artSize);
                artwork.relocate(portrait ? (w - artSize) / 2 : w - pad - artSize,
                        portrait ? top + contentHeight + 24 : (h - artSize) / 2);
                drawArtwork(artwork);
            }
        };
        stage.setMinSize(0, 0);
        setCenter(stage);

        setFocusTraversable(true);
        setOnKeyPressed(event -> keyBindings.commandFor(event.getCode())
                .ifPresent(command -> {
                    handleCommand(command);
                    event.consume();
                }));

        updateSelection();
    }

    private static void drawArtwork(Canvas canvas) {
        double size = canvas.getWidth();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size, size);
        if (size <= 0) return;
        gc.setFill(Color.web("#0B1320"));
        gc.fillRoundRect(0, 0, size, size, 14, 14);
        gc.setStroke(Color.web("#202B3B"));
        gc.strokeRoundRect(1, 1, size - 2, size - 2, 14, 14);
        double unit = size / 13, gap = unit * .12;
        paintPiece(gc, unit, gap, 1.8, 2.4, "#21BED7", new int[][]{{0,0},{1,0},{2,0},{3,0}});
        paintPiece(gc, unit, gap, 6.2, 4.8, "#A855F7", new int[][]{{1,0},{0,1},{1,1},{2,1}});
        paintPiece(gc, unit, gap, 2.3, 7.4, "#FBBF24", new int[][]{{0,0},{1,0},{0,1},{1,1}});
        paintPiece(gc, unit, gap, 7.4, 8.9, "#00B4AA", new int[][]{{1,0},{2,0},{0,1},{1,1}});
    }

    private static void paintPiece(GraphicsContext gc, double unit, double gap,
            double x, double y, String color, int[][] cells) {
        gc.setFill(Color.web(color));
        for (int[] cell : cells) {
            gc.fillRoundRect((x + cell[0]) * unit, (y + cell[1]) * unit,
                    unit - gap, unit - gap, unit * .2, unit * .2);
        }
    }

    public void requestMenuFocus() {
        requestFocus();
    }

    private void handleCommand(MenuCommand command) {
        switch (command) {
            case MOVE_PREVIOUS -> selection.movePrevious();
            case MOVE_NEXT -> selection.moveNext();
            case CONFIRM -> activateSelectedMenu();
        }
        updateSelection();
    }

    private void activateSelectedMenu() {
        menuButtons.get(selection.selectedIndex()).fire();
    }

    private Button createMenuButton(StartMenuAction action) {
        Button button = new Button(action.label());
        Runnable handler = actionHandlers.get(action);

        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(handler == null);
        if (handler != null) {
            button.setOnAction(event -> handler.run());
        }
        button.setOnMouseEntered(event -> selectMenuAction(action));
        button.setOnMouseMoved(event -> selectMenuAction(action));
        return button;
    }

    private void selectMenuAction(StartMenuAction action) {
        selection.select(menuActions.indexOf(action));
        updateSelection();
    }

    private void updateSelection() {
        for (int index = 0; index < menuButtons.size(); index++) {
            menuButtons.get(index).pseudoClassStateChanged(SELECTED, index == selection.selectedIndex());
        }
    }
}
