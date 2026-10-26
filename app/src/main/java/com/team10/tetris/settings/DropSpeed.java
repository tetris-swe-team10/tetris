package com.team10.tetris.settings;

/** 기본 낙하 간격에 적용하는 배율. 난이도별 가속량에는 곱하지 않는다. */
public enum DropSpeed {
    SLOW("느리게", 1.25),
    NORMAL("보통", 1.0),
    FAST("빠르게", 0.75);

    private final String label;
    private final double intervalMultiplier;

    DropSpeed(String label, double intervalMultiplier) {
        this.label = label;
        this.intervalMultiplier = intervalMultiplier;
    }

    public String label() {
        return label;
    }

    public int adjustInterval(int baseIntervalMs) {
        if (baseIntervalMs <= 0) {
            throw new IllegalArgumentException("낙하 간격은 0보다 커야 합니다.");
        }
        return Math.max(1, (int) Math.round(baseIntervalMs * intervalMultiplier));
    }
}
