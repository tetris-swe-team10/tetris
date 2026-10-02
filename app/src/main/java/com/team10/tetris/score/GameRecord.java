package com.team10.tetris.score;

import java.time.LocalDateTime;

public record GameRecord(
        String playerName,
        int score,
        int clearedLines,
        LocalDateTime playedAt
) {
}