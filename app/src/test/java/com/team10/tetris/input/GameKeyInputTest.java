package com.team10.tetris.input;

import com.team10.tetris.game.GameCommand;
import com.team10.tetris.input.GameKeyInput.Action;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameKeyInputTest {

    private final GameKeyBindings defaults = GameKeyBindings.defaults();

    private Optional<Action> resolve(KeyCode key, boolean demo) {
        return GameKeyInput.resolve(key, false, defaults, demo);
    }

    @Test void mapsBoundKeysToCommands() {
        assertEquals(Optional.of(new Action.Command(GameCommand.HARD_DROP)), resolve(KeyCode.SPACE, false));
        KeyCode left = defaults.keyFor(GameCommand.LEFT);
        assertEquals(Optional.of(new Action.Command(GameCommand.LEFT)), resolve(left, false));
    }

    @Test void usesGivenBindings() {
        GameKeyBindings custom = new GameKeyBindings(Map.of(GameCommand.LEFT, KeyCode.A, GameCommand.RIGHT, KeyCode.D,
                GameCommand.ROTATE, KeyCode.W, GameCommand.DOWN, KeyCode.S));
        assertEquals(Optional.of(new Action.Command(GameCommand.LEFT)),
                GameKeyInput.resolve(KeyCode.A, false, custom, false));
    }

    @Test void ignoresKeysWithModifiers() {
        assertTrue(GameKeyInput.resolve(KeyCode.SPACE, true, defaults, false).isEmpty());
        assertTrue(GameKeyInput.resolve(KeyCode.ESCAPE, true, defaults, false).isEmpty());
        assertTrue(GameKeyInput.resolve(KeyCode.DIGIT1, true, defaults, true).isEmpty());
    }

    @Test void escapeOpensSettings() {
        assertEquals(Optional.of(new Action.OpenSettings()), resolve(KeyCode.ESCAPE, false));
    }

    @Test void demoKeysOneToThreeAlwaysRequestItems() {
        assertEquals(Optional.of(new Action.DemoItem(1)), resolve(KeyCode.DIGIT1, false));
        assertEquals(Optional.of(new Action.DemoItem(2)), resolve(KeyCode.NUMPAD2, false));
        assertEquals(Optional.of(new Action.DemoItem(3)), resolve(KeyCode.DIGIT3, true));
    }

    @Test void demoKeysFourAndFiveOnlyWhenItemDemoEnabled() {
        assertEquals(Optional.of(new Action.DemoItem(4)), resolve(KeyCode.DIGIT4, true));
        assertEquals(Optional.of(new Action.DemoItem(5)), resolve(KeyCode.NUMPAD5, true));
        assertTrue(resolve(KeyCode.DIGIT4, false).isEmpty(), "데모가 꺼지면 일반 키로 해석 (기본 바인딩에 없음)");
    }

    @Test void unboundKeyResolvesToNothing() {
        assertTrue(resolve(KeyCode.F12, false).isEmpty());
    }
}
