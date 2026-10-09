package com.team10.tetris.game;

/** ITEM은 팀원의 아이템 엔진 및 기록 연동을 위한 식별자다. */
public enum GameMode {
    NORMAL("일반"), ITEM("아이템");
    private final String label;
    GameMode(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
