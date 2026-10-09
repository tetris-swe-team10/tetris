package com.team10.tetris.game;

/** I 블록 가중치와 5줄마다 감소하는 낙하 간격(ms). */
public enum Difficulty {
    EASY("Easy", 12, 80), NORMAL("Normal", 10, 100), HARD("Hard", 8, 120);
    private final String label;
    private final int iWeight;
    private final int speedStepMs;
    Difficulty(String label, int iWeight, int speedStepMs) {
        this.label = label; this.iWeight = iWeight; this.speedStepMs = speedStepMs;
    }
    public int weight(TetrominoType type) { return type == TetrominoType.I ? iWeight : 10; }
    public int speedStepMs() { return speedStepMs; }
    @Override public String toString() { return label; }
}
