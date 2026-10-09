package com.team10.tetris.settings;

/** 애플리케이션 창 표시 방식. */
public enum DisplayMode {
    FULL_SCREEN("전체 화면"),
    WINDOWED("창 모드"),
    PORTRAIT("세로 모드");

    private final String label;

    DisplayMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
