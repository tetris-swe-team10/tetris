package com.team10.tetris.screen;

/** Declaration order defines the order shown on the start screen. */
public enum StartMenuAction {
    START_GAME("게임 시작"),
    OPEN_SETTINGS("설정"),
    OPEN_SCOREBOARD("스코어보드"),
    EXIT("게임 종료");

    private final String label;

    StartMenuAction(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
