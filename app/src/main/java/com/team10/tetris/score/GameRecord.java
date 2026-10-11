package com.team10.tetris.score;

import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameMode;
import java.time.LocalDateTime;
import java.util.Objects;

public record GameRecord(String playerName, int score, int clearedLines,
        LocalDateTime playedAt, GameMode mode, Difficulty difficulty) {
    public GameRecord {
        Objects.requireNonNull(playerName);
        Objects.requireNonNull(playedAt);
        Objects.requireNonNull(mode);
        Objects.requireNonNull(difficulty);
        if (score < 0 || clearedLines < 0) throw new IllegalArgumentException("Negative game result");
    }
    /** 1차 기록과 기존 호출부는 일반/Normal로 해석한다. */
    public GameRecord(String playerName, int score, int clearedLines, LocalDateTime playedAt) {
        this(playerName, score, clearedLines, playedAt, GameMode.NORMAL, Difficulty.NORMAL);
    }
}
