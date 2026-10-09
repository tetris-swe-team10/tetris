package com.team10.tetris.settings;

import java.util.Objects;

/** 모든 설정 화면과 게임이 공유하는 메모리 기반 설정 상태. */
public final class GameSettings {
    private DropSpeed dropSpeed = DropSpeed.NORMAL;
    private DisplayMode displayMode = DisplayMode.WINDOWED;
    private boolean colorBlindMode;
    private boolean demoMode;

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

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
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
}
