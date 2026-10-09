package com.team10.tetris.settings;

/** 사용자가 선택할 수 있는 자동 낙하 속도 배율. */
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
