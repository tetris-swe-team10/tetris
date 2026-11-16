package com.team10.tetris.input;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.versus.PlayerSide;
import javafx.scene.input.KeyCode;
class VersusKeyBindingsTest {
    @TempDir Path dir;
    @Test void routesBothPlayersRejectsConflictsAndPersists() throws Exception {
        var keys=VersusKeyBindings.defaults();
        var input=new VersusKeyBindings.Input(PlayerSide.PLAYER1,GameCommand.LEFT);
        assertEquals(input,keys.inputFor(KeyCode.A).orElseThrow());
        assertEquals(PlayerSide.PLAYER2,keys.inputFor(KeyCode.ENTER).orElseThrow().player());
        assertThrows(IllegalArgumentException.class,()->keys.with(input,KeyCode.LEFT));
        assertThrows(IllegalArgumentException.class,()->keys.with(input,KeyCode.ESCAPE));
        Path file=dir.resolve("keys.properties"); keys.with(input,KeyCode.Q).save(file);
        assertEquals(KeyCode.Q,VersusKeyBindings.load(file).keyFor(input));
        java.nio.file.Files.writeString(file,"broken");
        assertEquals(KeyCode.A,VersusKeyBindings.load(file).keyFor(input));
    }
}
