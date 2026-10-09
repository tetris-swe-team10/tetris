package com.team10.tetris.game;

/** 낙하100점, 가속 시 추가100점. 줄당1000점 + 추가 줄마다 동시 삭제500점. */
public final class StandardScoringPolicy implements ScoringPolicy {
    @Override public int dropPoints(int distance, int dropIntervalMs) {
        return distance * (dropIntervalMs < GameEngine.INITIAL_DROP_INTERVAL_MS ? 200 : 100);
    }
    @Override public int linePoints(int clearedLines) {
        return clearedLines * 1000 + Math.max(0, clearedLines - 1) * 500;
    }
}
