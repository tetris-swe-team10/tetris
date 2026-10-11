package com.team10.tetris.game;
public interface ScoringPolicy {
    int dropPoints(int distance, int dropIntervalMs);
    int linePoints(int clearedLines);
}
