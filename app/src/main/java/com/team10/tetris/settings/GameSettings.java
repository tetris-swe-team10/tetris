package com.team10.tetris.settings;

import java.io.IOException;
import java.util.Objects;
import com.team10.tetris.input.GameKeyBindings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/** 모든 설정 화면과 게임이 공유하는 설정 상태. */
public final class GameSettings {
    private final SettingsManager settingsManager;
    private final BooleanProperty colorBlindMode =
            new SimpleBooleanProperty(this, "colorBlindMode", false);

    private DropSpeed dropSpeed = DropSpeed.NORMAL;
    private DisplayMode displayMode = DisplayMode.WINDOWED;
    private boolean demoMode;
    private boolean loading;
    private int demoItemLines = 1;
    private GameKeyBindings keyBindings = GameKeyBindings.defaults();

    public GameKeyBindings getKeyBindings() { return keyBindings; }
    public void setKeyBindings(GameKeyBindings keys) { keyBindings = Objects.requireNonNull(keys); }
    public SettingsData snapshot() {
        return new SettingsData(isColorBlindMode(), dropSpeed, displayMode, demoMode, demoItemLines, keyBindings);
    }
    public void save() throws IOException { settingsManager.saveSettings(snapshot()); }
    public void resetDefaults() throws IOException {
        SettingsData defaults = SettingsData.defaults();
        settingsManager.saveSettings(defaults);
        apply(defaults);
    }
    private void apply(SettingsData data) {
        loading = true;
        try {
            colorBlindMode.set(data.colorBlindMode());
            dropSpeed = data.dropSpeed(); displayMode = data.displayMode();
            demoMode = data.demoMode(); demoItemLines = data.demoItemLines(); keyBindings = data.keyBindings();
        } finally { loading = false; }
    }
    public int getDemoItemLines() { return demoItemLines; }
    public void setDemoItemLines(int lines) {
        if (lines < 1 || lines > 100) throw new IllegalArgumentException("Expected 1..100 lines");
        demoItemLines = lines;
    }
    public int getItemLineInterval() { return demoMode ? demoItemLines : 10; }

    public GameSettings() {
        this(new SettingsManager());
    }

    public GameSettings(SettingsManager settingsManager) {
        this.settingsManager = Objects.requireNonNull(settingsManager);
        apply(settingsManager.loadSettings());
        colorBlindMode.addListener(
                (observable, oldValue, newValue) -> saveColorBlindMode(newValue));
    }

    public DropSpeed getDropSpeed() {
        return dropSpeed;
    }

    public void setDropSpeed(DropSpeed dropSpeed) {
        this.dropSpeed = Objects.requireNonNull(dropSpeed);
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = Objects.requireNonNull(displayMode);
    }

    public BooleanProperty colorBlindModeProperty() {
        return colorBlindMode;
    }

    public boolean isColorBlindMode() {
        return colorBlindMode.get();
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode.set(colorBlindMode);
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    public int adjustDropInterval(int baseIntervalMs) {
        return dropSpeed.adjustInterval(baseIntervalMs);
    }

    private void saveColorBlindMode(boolean value) {
        if (loading) return;
        try {
            settingsManager.saveColorBlindMode(value);
        } catch (IOException exception) {
            System.err.println("색맹 모드 설정을 저장하지 못했습니다: " + exception.getMessage());
        }
    }
}
