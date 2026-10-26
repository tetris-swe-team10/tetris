package com.team10.tetris.settings;

import com.team10.tetris.input.GameKeyBindings;
import java.util.Objects;

/** 파일과 공유 상태 사이에 전달하는 검증된 설정 값. */
public record SettingsData(boolean colorBlindMode, DropSpeed dropSpeed, DisplayMode displayMode,
        boolean demoMode, int demoItemLines, GameKeyBindings keyBindings) {
    public SettingsData {
        Objects.requireNonNull(dropSpeed); Objects.requireNonNull(displayMode); Objects.requireNonNull(keyBindings);
        if (demoItemLines < 1 || demoItemLines > 100) throw new IllegalArgumentException("Expected 1..100 lines");
    }
    public static SettingsData defaults() {
        return new SettingsData(false, DropSpeed.NORMAL, DisplayMode.WINDOWED, false, 1, GameKeyBindings.defaults());
    }
}
