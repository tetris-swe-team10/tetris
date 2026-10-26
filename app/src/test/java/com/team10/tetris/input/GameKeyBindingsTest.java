package com.team10.tetris.input;

import com.team10.tetris.game.GameCommand;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import java.util.EnumMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GameKeyBindingsTest {
    @Test void mapsAllActionsAndLeavesMenusIndependent() {
        GameKeyBindings keys=new GameKeyBindings(Map.of(GameCommand.LEFT,KeyCode.A,GameCommand.RIGHT,KeyCode.D,
                GameCommand.ROTATE,KeyCode.W,GameCommand.DOWN,KeyCode.S));
        assertEquals(GameCommand.LEFT,keys.commandFor(KeyCode.A).orElseThrow());
        assertEquals(GameCommand.RIGHT,keys.commandFor(KeyCode.D).orElseThrow());
        assertEquals(GameCommand.ROTATE,keys.commandFor(KeyCode.W).orElseThrow());
        assertEquals(GameCommand.DOWN,keys.commandFor(KeyCode.S).orElseThrow());
        assertEquals(GameCommand.HARD_DROP,keys.commandFor(KeyCode.SPACE).orElseThrow());
        assertTrue(keys.commandFor(KeyCode.LEFT).isEmpty());
        assertTrue(MenuKeyBindings.defaults().commandFor(KeyCode.UP).isPresent());
        assertThrows(UnsupportedOperationException.class,()->keys.asMap().put(GameCommand.LEFT,KeyCode.Q));
    }
    @Test void rejectsMissingDuplicateReservedAndModifierKeys() {
        assertThrows(IllegalArgumentException.class,()->new GameKeyBindings(Map.of()));
        for(KeyCode key:new KeyCode[]{KeyCode.RIGHT,KeyCode.ESCAPE,KeyCode.SPACE,KeyCode.DIGIT1,
                KeyCode.NUMPAD3,KeyCode.SHIFT,KeyCode.CONTROL,KeyCode.TAB,KeyCode.ENTER}) {
            var map=new EnumMap<GameCommand,KeyCode>(GameKeyBindings.defaults().asMap());map.put(GameCommand.LEFT,key);
            assertThrows(IllegalArgumentException.class,()->new GameKeyBindings(map));
        }
    }
}
