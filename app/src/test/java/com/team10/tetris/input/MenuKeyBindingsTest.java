package com.team10.tetris.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;

class MenuKeyBindingsTest {
    private final MenuKeyBindings bindings = MenuKeyBindings.defaults();

    @Test
    void mapsUpArrowAndWToPreviousMenu() {
        assertEquals(
                MenuCommand.MOVE_PREVIOUS,
                bindings.commandFor(KeyCode.UP).orElseThrow());
        assertEquals(
                MenuCommand.MOVE_PREVIOUS,
                bindings.commandFor(KeyCode.W).orElseThrow());
    }

    @Test
    void mapsDownArrowAndSToNextMenu() {
        assertEquals(
                MenuCommand.MOVE_NEXT,
                bindings.commandFor(KeyCode.DOWN).orElseThrow());
        assertEquals(
                MenuCommand.MOVE_NEXT,
                bindings.commandFor(KeyCode.S).orElseThrow());
    }

    @Test
    void mapsEnterToConfirm() {
        assertEquals(
                MenuCommand.CONFIRM,
                bindings.commandFor(KeyCode.ENTER).orElseThrow());
    }

    @Test
    void ignoresUnassignedKey() {
        assertTrue(bindings.commandFor(KeyCode.A).isEmpty());
    }
}
