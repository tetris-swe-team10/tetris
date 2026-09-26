package com.team10.tetris.score;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreboardManagerTest {

    @TempDir
    Path tempDir;

    private ScoreboardManager createManager() {
        return new ScoreboardManager(
                tempDir.resolve("scores.tsv")
        );
    }

    /** 스코어보드를 가득 채운다. 점수는 100, 200, ... 순으로 저장된다. */
    private void fillScoreboard(ScoreboardManager manager)
            throws IOException {

        for (int index = 1; index <= ScoreboardManager.MAX_RECORDS; index++) {
            manager.save("Player" + index, index * 100, index);
        }
    }

    @Test
    void returnsEmptyListWhenNoRecordsExist()
            throws IOException {

        ScoreboardManager manager = createManager();

        assertEquals(0, manager.getRecords().size());
        assertEquals(0, manager.getHighScore());
    }

    @Test
    void savesAndLoadsRecord()
            throws IOException {

        ScoreboardManager manager = createManager();

        GameRecord record = new GameRecord(
                "홍찰",
                1500,
                8,
                LocalDateTime.of(2026, 9, 25, 20, 0)
        );

        manager.save(record);

        List<GameRecord> records = manager.getRecords();

        assertEquals(1, records.size());
        assertEquals(record, records.get(0));
    }

    @Test
    void sortsRecordsByScoreDescending()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 300, 2);
        manager.save("B", 1200, 5);
        manager.save("C", 700, 3);

        List<GameRecord> records = manager.getRecords();

        assertEquals("B", records.get(0).playerName());
        assertEquals("C", records.get(1).playerName());
        assertEquals("A", records.get(2).playerName());
    }

    @Test
    void returnsOnlyRequestedNumberOfRecords()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 100, 1);
        manager.save("B", 200, 2);
        manager.save("C", 300, 3);

        List<GameRecord> records =
                manager.getTopRecords(2);

        assertEquals(2, records.size());
        assertEquals(300, records.get(0).score());
        assertEquals(200, records.get(1).score());
    }

    @Test
    void returnsHighestScore()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 500, 2);
        manager.save("B", 1800, 6);
        manager.save("C", 900, 4);

        assertEquals(1800, manager.getHighScore());
    }

    @Test
    void rejectsNegativeScore()
            throws IOException {

        ScoreboardManager manager = createManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.save("A", -100, 1)
        );
    }

    @Test
    void rejectsNegativeRecordLimit()
            throws IOException {

        ScoreboardManager manager = createManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getTopRecords(-1)
        );
    }

    @Test
    void loadsRecordsFromAnotherManagerInstance()
            throws IOException {

        Path file = tempDir.resolve("scores.tsv");

        ScoreboardManager first =
                new ScoreboardManager(file);

        first.save("Player", 2500, 10);

        ScoreboardManager second =
                new ScoreboardManager(file);

        assertEquals(2500, second.getHighScore());
    }
    // ---------------------------------------------------------------
    // isHighScore
    // ---------------------------------------------------------------

    @Test
    void treatsAnyPositiveScoreAsHighScoreWhenBoardIsEmpty()
            throws IOException {

        ScoreboardManager manager = createManager();

        assertTrue(manager.isHighScore(1));
    }

    @Test
    void rejectsZeroScoreAsHighScore()
            throws IOException {

        ScoreboardManager manager = createManager();

        assertFalse(manager.isHighScore(0));
    }

    @Test
    void treatsLowScoreAsHighScoreWhenBoardIsNotFull()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 5000, 20);

        assertTrue(manager.isHighScore(10));
    }

    @Test
    void acceptsScoreAboveLowestRecordWhenBoardIsFull()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        // 최하위 기록은 100점
        assertTrue(manager.isHighScore(150));
    }

    @Test
    void rejectsScoreBelowLowestRecordWhenBoardIsFull()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        assertFalse(manager.isHighScore(50));
    }

    @Test
    void rejectsScoreEqualToLowestRecordWhenBoardIsFull()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        assertFalse(manager.isHighScore(100));
    }

    // ---------------------------------------------------------------
    // saveAndGetRank
    // ---------------------------------------------------------------

    @Test
    void returnsFirstRankForHighestScore()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 500, 2);
        manager.save("B", 900, 4);

        assertEquals(1, manager.saveAndGetRank("C", 1500, 7));
    }

    @Test
    void returnsMatchingRankForMiddleScore()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 1500, 7);
        manager.save("B", 500, 2);

        assertEquals(2, manager.saveAndGetRank("C", 900, 4));
    }

    @Test
    void returnsUnrankedWhenScoreDoesNotEnterScoreboard()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        assertEquals(
                ScoreboardManager.UNRANKED,
                manager.saveAndGetRank("Late", 10, 0)
        );
    }

    @Test
    void keepsOnlyMaxRecordsAfterSaving()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        manager.saveAndGetRank("Best", 99999, 50);

        assertEquals(
                ScoreboardManager.MAX_RECORDS,
                manager.getRecords().size()
        );
        assertEquals(99999, manager.getHighScore());
    }

    @Test
    void removesLowestRecordWhenScoreboardOverflows()
            throws IOException {

        ScoreboardManager manager = createManager();
        fillScoreboard(manager);

        manager.saveAndGetRank("Best", 99999, 50);

        List<GameRecord> records = manager.getRecords();
        GameRecord lowest = records.get(records.size() - 1);

        // 원래 최하위였던 100점 기록이 밀려나고 200점이 최하위가 된다
        assertEquals(200, lowest.score());
    }

    // ---------------------------------------------------------------
    // clear
    // ---------------------------------------------------------------

    @Test
    void removesAllRecordsOnClear()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 500, 2);
        manager.save("B", 900, 4);

        manager.clear();

        assertEquals(0, manager.getRecords().size());
        assertEquals(0, manager.getHighScore());
    }

    @Test
    void allowsSavingAfterClear()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.save("A", 500, 2);
        manager.clear();
        manager.save("B", 300, 1);

        assertEquals(1, manager.getRecords().size());
        assertEquals(300, manager.getHighScore());
    }

    @Test
    void doesNotFailWhenClearingEmptyScoreboard()
            throws IOException {

        ScoreboardManager manager = createManager();

        manager.clear();

        assertEquals(0, manager.getRecords().size());
    }
}