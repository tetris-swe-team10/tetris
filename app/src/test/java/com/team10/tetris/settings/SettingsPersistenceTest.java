package com.team10.tetris.settings;

import com.team10.tetris.input.GameKeyBindings;
import com.team10.tetris.game.GameCommand;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SettingsPersistenceTest {
    @TempDir Path temp;
    private GameKeyBindings wasd() {
        return new GameKeyBindings(Map.of(GameCommand.LEFT,KeyCode.A,GameCommand.RIGHT,KeyCode.D,
                GameCommand.ROTATE,KeyCode.W,GameCommand.DOWN,KeyCode.S));
    }
    @Test void explicitSaveRestoresEverySettingAndKeepsUnknownProperties() throws Exception {
        Path file=temp.resolve("settings.properties"); Files.writeString(file,"futureOption=keep\ncolorBlindMode=true\n");
        GameSettings settings=new GameSettings(new SettingsManager(file));
        settings.setDropSpeed(DropSpeed.FAST);settings.setDisplayMode(DisplayMode.PORTRAIT);
        settings.setDemoMode(true);settings.setDemoItemLines(7);settings.setKeyBindings(wasd());
        assertEquals(DropSpeed.NORMAL,new GameSettings(new SettingsManager(file)).getDropSpeed());
        settings.save();
        GameSettings reloaded=new GameSettings(new SettingsManager(file));
        assertTrue(reloaded.isColorBlindMode()); assertTrue(reloaded.isDemoMode());
        assertEquals(DropSpeed.FAST,reloaded.getDropSpeed());assertEquals(DisplayMode.PORTRAIT,reloaded.getDisplayMode());
        assertEquals(7,reloaded.getDemoItemLines());assertEquals(wasd().asMap(),reloaded.getKeyBindings().asMap());
        assertTrue(Files.readString(file).contains("futureOption=keep"));
    }
    @Test void resetPersistsDefaultsWithoutTouchingScoreFile() throws Exception {
        Path file=temp.resolve("settings.properties"), scores=temp.resolve("scores.tsv");
        Files.writeString(scores,"score-history");
        GameSettings settings=new GameSettings(new SettingsManager(file));
        settings.setDropSpeed(DropSpeed.SLOW);settings.setDisplayMode(DisplayMode.FULL_SCREEN);
        settings.setColorBlindMode(true);settings.setDemoMode(true);settings.setDemoItemLines(20);settings.setKeyBindings(wasd());
        settings.save();settings.resetDefaults();
        for(GameSettings actual: new GameSettings[]{settings,new GameSettings(new SettingsManager(file))}) {
            assertEquals(DropSpeed.NORMAL,actual.getDropSpeed());assertEquals(DisplayMode.WINDOWED,actual.getDisplayMode());
            assertFalse(actual.isDemoMode());assertFalse(actual.isColorBlindMode());assertEquals(1,actual.getDemoItemLines());
            assertEquals(GameKeyBindings.defaults().asMap(),actual.getKeyBindings().asMap());
        }
        assertEquals("score-history",Files.readString(scores));
    }
    @Test void legacyAndInvalidValuesLoadSafely() throws Exception {
        Path file=temp.resolve("settings.properties");Files.writeString(file,"colorBlindMode=true\n");
        assertTrue(new GameSettings(new SettingsManager(file)).isColorBlindMode());
        Files.writeString(file,"dropSpeed=wrong\ndisplayMode=wrong\ndemoItemLines=999\nkey.LEFT=SPACE\n");
        GameSettings settings=new GameSettings(new SettingsManager(file));
        assertEquals(DropSpeed.NORMAL,settings.getDropSpeed());assertEquals(DisplayMode.WINDOWED,settings.getDisplayMode());
        assertEquals(1,settings.getDemoItemLines());assertEquals(KeyCode.LEFT,settings.getKeyBindings().keyFor(GameCommand.LEFT));
        Files.writeString(file,"demoItemLines=notNumber\nkey.LEFT=notKey\n");
        assertEquals(1,new SettingsManager(file).loadSettings().demoItemLines());
        Files.writeString(file,"bad="+"\\"+"uQQQQ");
        assertEquals(DropSpeed.NORMAL,new SettingsManager(file).loadSettings().dropSpeed());
    }
    @Test void failedResetLeavesRuntimeSettingsUnchanged() throws Exception {
        Path blocker=temp.resolve("blocker");Files.writeString(blocker,"file");
        GameSettings settings=new GameSettings(new SettingsManager(blocker.resolve("settings.properties")));
        settings.setDropSpeed(DropSpeed.FAST);
        assertThrows(IOException.class,settings::save);
        assertThrows(IOException.class,settings::resetDefaults);
        assertEquals(DropSpeed.FAST,settings.getDropSpeed());
    }
}
