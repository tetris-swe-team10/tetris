package com.team10.tetris.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameSettingsTest {

    @Test
    void usesSafeDefaults() {
        GameSettings settings = new GameSettings();

        assertEquals(DropSpeed.NORMAL, settings.getDropSpeed());
        assertEquals(DisplayMode.WINDOWED, settings.getDisplayMode());
        assertFalse(settings.isColorBlindMode());
        assertFalse(settings.isDemoMode());
    }

    @Test
    void keepsChangedSettings() {
        GameSettings settings = new GameSettings();

        settings.setDropSpeed(DropSpeed.FAST);
        settings.setDisplayMode(DisplayMode.PORTRAIT);
        settings.setColorBlindMode(true);
        settings.setDemoMode(true);

        assertEquals(DropSpeed.FAST, settings.getDropSpeed());
        assertEquals(DisplayMode.PORTRAIT, settings.getDisplayMode());
        assertTrue(settings.isColorBlindMode());
        assertTrue(settings.isDemoMode());
    }

    @Test
    void adjustsDropIntervalForEachSpeed() {
        GameSettings settings = new GameSettings();

        settings.setDropSpeed(DropSpeed.SLOW);
        assertEquals(1250, settings.adjustDropInterval(1000));

        settings.setDropSpeed(DropSpeed.NORMAL);
        assertEquals(1000, settings.adjustDropInterval(1000));

        settings.setDropSpeed(DropSpeed.FAST);
        assertEquals(750, settings.adjustDropInterval(1000));
    }

    @Test
    void rejectsInvalidBaseDropInterval() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DropSpeed.NORMAL.adjustInterval(0));
    }
}
