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
        this.actionHandlers = Map.copyOf(Objects.requireNonNull(actionHandlers));
        this.keyBindings = Objects.requireNonNull(keyBindings);

        getStyleClass().add("start-screen");
        setPadding(new Insets(64, 80, 48, 80));

        Label title = new Label("TETRIS");
        title.getStyleClass().add("title");

        VBox heading = new VBox(8, title);

        menuButtons = menuActions.stream()
                .map(this::createMenuButton)
                .toList();

        VBox menu = new VBox(12);
        menu.getStyleClass().add("menu");
        menu.setMaxWidth(340);
        menu.getChildren().addAll(menuButtons);

        Label guide = new Label("↑ ↓ 또는 W S 메뉴 이동    Enter 선택");
        guide.getStyleClass().add("guide");

        VBox content = new VBox(42, heading, menu, guide);
        content.setAlignment(Pos.CENTER_LEFT);
        setCenter(content);

        setFocusTraversable(true);
        setOnKeyPressed(event -> keyBindings.commandFor(event.getCode())
                .ifPresent(command -> {
                    handleCommand(command);
                    event.consume();
                }));

        updateSelection();
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
