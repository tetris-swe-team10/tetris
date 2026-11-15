package com.team10.tetris.game.versus;

/** 대전 모드 종류. 3차 요구사항: 일반 / 아이템 / 시간제한. */
public enum VersusRule {
    NORMAL("일반"),
    ITEM("아이템"),
    TIME_LIMIT("시간제한");

    private final String label;

    VersusRule(String label) {
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
