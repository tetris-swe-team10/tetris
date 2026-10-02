package com.team10.tetris.input;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.input.KeyCode;

public final class MenuKeyBindings {
    private final Map<KeyCode, MenuCommand> bindings;

    public MenuKeyBindings(Map<KeyCode, MenuCommand> bindings) {
        this.bindings = Map.copyOf(Objects.requireNonNull(bindings));
    }

    public Optional<MenuCommand> commandFor(KeyCode keyCode) {
        return Optional.ofNullable(bindings.get(keyCode));
    }

    public static MenuKeyBindings defaults() {
        return new MenuKeyBindings(Map.of(
                KeyCode.UP, MenuCommand.MOVE_PREVIOUS,
                KeyCode.W, MenuCommand.MOVE_PREVIOUS,
                KeyCode.DOWN, MenuCommand.MOVE_NEXT,
                KeyCode.S, MenuCommand.MOVE_NEXT,
                KeyCode.ENTER, MenuCommand.CONFIRM));
    }
}
