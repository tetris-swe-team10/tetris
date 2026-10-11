package com.team10.tetris.score;

import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class ScoreboardGroupsTest {
    @TempDir Path temp;
    private GameRecord record(String name, int score, GameMode mode, Difficulty difficulty) {
        return new GameRecord(name, score, 1, LocalDateTime.of(2026, 10, 9, 12, 0), mode, difficulty);
    }
    @Test void ranksAndTrimsEachModeAndDifficultyIndependently() throws Exception {
        ScoreboardManager manager = new ScoreboardManager(temp.resolve("scores.tsv"));
        for (GameMode mode : GameMode.values()) {
            for (Difficulty difficulty : Difficulty.values()) {
                for (int i = 1; i <= 12; i++)
                    manager.saveAndGetRank(record(mode.name() + difficulty.name() + i, i * 100, mode, difficulty));
            }
        }
        assertEquals(60, manager.getRecords().size());
        for (GameMode mode : GameMode.values()) {
            for (Difficulty difficulty : Difficulty.values()) {
                var records = manager.getRecords(mode, difficulty);
                assertEquals(10, records.size());
                assertEquals(1200, manager.getHighScore(mode, difficulty));
                assertEquals(300, records.get(9).score());
                assertFalse(manager.isHighScore(300, mode, difficulty));
                assertEquals(-1, manager.saveAndGetRank(record("Tie", 300, mode, difficulty)));
                assertEquals(1, manager.saveAndGetRank(record("Best", 1500, mode, difficulty)));
                assertEquals(10, manager.getRecords(mode, difficulty).size());
            }
        }
    }

    @Test void oldFourColumnRecordsRemainNormalAndNormalAndSurviveRewrite() throws Exception {
        Path file = temp.resolve("scores.tsv");
        String name = Base64.getEncoder().encodeToString("기존기록".getBytes(StandardCharsets.UTF_8));
        Files.writeString(file, name + "\t900\t4\t2026-10-01T12:00\n", StandardCharsets.UTF_8);
        ScoreboardManager manager = new ScoreboardManager(file);
        assertEquals("기존기록", manager.getRecords(GameMode.NORMAL, Difficulty.NORMAL).get(0).playerName());
        assertEquals(0, manager.getHighScore(GameMode.NORMAL, Difficulty.HARD));
        for (int i = 0; i < 12; i++)
            manager.saveAndGetRank(record("Hard" + i, 100 + i * 100, GameMode.NORMAL, Difficulty.HARD));
        ScoreboardManager reopened = new ScoreboardManager(file);
        assertEquals(900, reopened.getHighScore(GameMode.NORMAL, Difficulty.NORMAL));
        assertEquals(10, reopened.getRecords(GameMode.NORMAL, Difficulty.HARD).size());
    }

    @Test void modeAndDifficultyRoundTripAndCorruptRowsAreIgnored() throws Exception {
        Path file = temp.resolve("scores.tsv");
        ScoreboardManager manager = new ScoreboardManager(file);
        GameRecord expected = record("한글\t이름", 1000, GameMode.ITEM, Difficulty.EASY);
        manager.save(expected);
        Files.writeString(file, "bad\t-1\t0\tinvalid\tINVALID\tHARD\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
        assertEquals(java.util.List.of(expected), new ScoreboardManager(file).getRecords());
        assertTrue(manager.getRecords(GameMode.NORMAL, Difficulty.EASY).isEmpty());
    }
}
