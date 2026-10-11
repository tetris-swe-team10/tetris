package com.team10.tetris.game;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class ScoringDifficultyTest {
    private GameEngine engine(Board board, Tetromino block, Difficulty difficulty) {
        return new GameEngine(board, block, new GameConfig(GameMode.NORMAL, difficulty),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
    }
    private GameEngine landingEngine(int count, Difficulty difficulty) {
        Board board = new Board();
        for (int row = Board.HEIGHT - count; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) if (col != 4) board.setCell(row, col, 1);
        }
        Tetromino block = new Tetromino(TetrominoType.I, 16, 4);
        block.rotateClockwise();
        return engine(board, block, difficulty);
    }

    @Test void automaticAndManualDropHaveTheSamePoints() {
        GameEngine automatic = engine(new Board(), new Tetromino(TetrominoType.T, 0, 3), Difficulty.NORMAL);
        GameEngine manual = engine(new Board(), new Tetromino(TetrominoType.T, 0, 3), Difficulty.NORMAL);
        automatic.tick(); manual.moveDown();
        assertEquals(100, automatic.getScore());
        assertEquals(automatic.getScore(), manual.getScore());
        manual.moveLeft(); manual.rotateClockwise();
        assertEquals(100, manual.getScore());
    }

    @Test void hardDropCountsOnlySuccessfulDistanceAndLocksOnce() {
        GameEngine game = engine(new Board(), new Tetromino(TetrominoType.O, 0, 3), Difficulty.NORMAL);
        assertEquals(18, game.hardDrop());
        assertEquals(1800, game.getScore());
        assertEquals(1, game.getBoard().getCell(19, 3));
        assertEquals(0, game.getCurrentBlock().getRow());
    }

    @Test void acceleratedDropAddsOneHundredPointsPerCell() {
        GameEngine game = engine(new Board(), new Tetromino(TetrominoType.O, 0, 3), Difficulty.EASY);
        game.increaseSpeed();
        game.tick();
        assertEquals(200, game.getScore());
    }

    @Test void pendingRowsRemainUntilAnimationCompletesAndAreScoredOnce() {
        GameEngine game = landingEngine(2, Difficulty.NORMAL);
        Tetromino next = game.getNextBlock();
        game.moveDown();
        assertEquals(GameState.CLEARING, game.getState());
        assertEquals(List.of(18, 19), game.snapshot().clearingRows());
        assertNull(game.snapshot().currentBlock());
        assertEquals(1, game.snapshot().cells().get(19).get(0));
        assertEquals(0, game.getScore());
        assertEquals(0, game.getTotalClearedLines());
        assertFalse(game.moveLeft());
        assertFalse(game.moveRight());
        assertFalse(game.rotateClockwise());
        assertFalse(game.moveDown());
        assertFalse(game.spawnBlock());
        assertEquals(0, game.hardDrop());
        game.advanceClear(299);
        assertEquals(GameState.CLEARING, game.getState());
        game.advanceClear(1);
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(2500, game.getScore());
        assertEquals(2, game.getTotalClearedLines());
        assertEquals(next, game.getCurrentBlock());
        assertEquals(0, game.getBoard().getCell(19, 0));
        game.advanceClear(1000);
        assertEquals(2500, game.getScore());
    }

    @Test void pausePreservesClearPhaseAndRemainingAnimationTime() {
        GameEngine game = landingEngine(1, Difficulty.NORMAL);
        game.moveDown();
        game.advanceClear(100);
        game.pause(); game.pause();
        game.advanceClear(1000); game.tick();
        assertEquals(GameState.PAUSED, game.getState());
        assertEquals(List.of(19), game.snapshot().clearingRows());
        assertEquals(0, game.getScore());
        game.resume();
        assertEquals(GameState.CLEARING, game.getState());
        game.advanceClear(199);
        assertEquals(GameState.CLEARING, game.getState());
        game.advanceClear(1);
        assertEquals(1000, game.getScore());
    }

    @Test void difficultyChangesSpeedAtFiveLinesAndKeepsTheMinimum() {
        for (Difficulty difficulty : Difficulty.values()) {
            GameEngine game = landingEngine(4, difficulty);
            game.moveDown(); game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
            assertEquals(1000, game.getDropIntervalMs());
            // 아래 한 줄을 추가로 지우면 누적5줄 경계를 넘는다.
            Board board = game.getBoard();
            for (int col = 0; col < Board.WIDTH; col++) board.setCell(19, col, col < 3 || col > 6 ? 1 : 0);
            game.setCurrentBlock(new Tetromino(TetrominoType.I, 19, 3));
            game.moveDown(); game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
            int expected = switch (difficulty) { case EASY -> 920; case NORMAL -> 900; case HARD -> 880; };
            assertEquals(expected, game.getDropIntervalMs());
            for (int i = 0; i < 20; i++) game.increaseSpeed();
            assertEquals(200, game.getDropIntervalMs());
        }
    }

    @Test void snapshotIsImmutableAndDoesNotChangeWithTheEngine() {
        GameEngine game = engine(new Board(), new Tetromino(TetrominoType.O, 0, 3), Difficulty.NORMAL);
        GameSnapshot snapshot = game.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.cells().get(0).set(0, 1));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.currentBlock().shape().get(0).clear());
        game.tick(); game.getBoard().setCell(19, 9, 1);
        assertEquals(0, snapshot.currentBlock().row());
        assertEquals(0, snapshot.cells().get(19).get(9));
        assertEquals(0, snapshot.score());
    }

    @Test void initialAndNextPiecesUseTheInjectedGenerator() {
        int[] calls = {0};
        GameEngine game = new GameEngine(new Board(), null, GameConfig.DEFAULT,
                () -> new Tetromino(calls[0]++ == 0 ? TetrominoType.I : TetrominoType.Z, 0, 3),
                new StandardScoringPolicy());
        assertEquals(TetrominoType.I, game.getCurrentBlock().getType());
        assertEquals(TetrominoType.Z, game.getNextBlock().getType());
        assertEquals(2, calls[0]);
    }

    @Test void spawnFailureAfterClearGivesGameOverAndStopsScoring() {
        GameEngine game = landingEngine(1, Difficulty.NORMAL);
        game.getBoard().setCell(0, 3, 1);
        game.moveDown();
        // 줄 압축 후 O 블록의 생성 위치인 row1을 막는다.
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(GameState.GAME_OVER, game.getState());
        game.resume(); game.tick(); game.hardDrop();
        assertEquals(1000, game.getScore());
    }

    @Test void invalidInitialPlacementStartsAsGameOver() {
        Board board = new Board(); board.setCell(0, 3, 1);
        GameEngine game = engine(board, new Tetromino(TetrominoType.O, 0, 3), Difficulty.NORMAL);
        assertTrue(game.isGameOver());
        assertFalse(game.moveDown());
        assertEquals(0, game.getScore());
    }

    @Test void actualGameGeneratorMatchesAllDifficultyDistributions() {
        int samples = 100_000;
        for (Difficulty difficulty : Difficulty.values()) {
            GameEngine game = new GameEngine(new Board(), null, new GameConfig(GameMode.NORMAL, difficulty),
                    new WeightedPieceGenerator(difficulty, new Random(20261009)), new StandardScoringPolicy());
            int[] counts = new int[TetrominoType.values().length];
            for (int i = 0; i < samples; i++) counts[game.createRandomTetromino().getType().ordinal()]++;
            int totalWeight = java.util.Arrays.stream(TetrominoType.values()).mapToInt(difficulty::weight).sum();
            for (TetrominoType type : TetrominoType.values()) {
                double expected = (double) difficulty.weight(type) / totalWeight;
                double observed = (double) counts[type.ordinal()] / samples;
                assertTrue(Math.abs(observed - expected) <= expected * 0.05,
                        difficulty + " " + type + ": " + observed + " expected " + expected);
            }
        }
    }

    @Test void weightedSelectionUsesEveryIntervalBoundary() {
        for (Difficulty difficulty : Difficulty.values()) {
            int total = java.util.Arrays.stream(TetrominoType.values()).mapToInt(difficulty::weight).sum();
            int[] counts = new int[7];
            // 각 정수 ticket을 정확히 한번씩 선택한다. 실제 next()의 구간 경계를 검사.
            for (int ticket = 0; ticket < total; ticket++) {
                final int chosen = ticket;
                Random random = new Random() {
                    @Override public int nextInt(int bound) { assertEquals(total, bound); return chosen; }
                };
                counts[new WeightedPieceGenerator(difficulty, random).next().getType().ordinal()]++;
            }
            for (TetrominoType type : TetrominoType.values())
                assertEquals(difficulty.weight(type), counts[type.ordinal()]);
        }
    }

    @Test void controllerHandlesTimePauseAndBlockedClearInputs() {
        GameEngine game = landingEngine(1, Difficulty.NORMAL);
        GameController controller = new GameController(game);
        controller.handle(GameCommand.DOWN);
        controller.advance(100);
        controller.pause();
        controller.advance(10_000);
        controller.handle(GameCommand.HARD_DROP);
        assertEquals(0, game.getScore());
        controller.resume(); controller.advance(200);
        assertEquals(1000, game.getScore());
        controller.advance(999);
        assertEquals(0, game.getCurrentBlock().getRow());
        controller.advance(1);
        assertEquals(1, game.getCurrentBlock().getRow());
        assertEquals(1100, game.getScore());
    }

    @Test void boardRemovalPreservesNonAdjacentRowsAndDeduplicates() {
        Board board = new Board();
        board.setCell(15, 0, 5); board.setCell(17, 0, 7); board.setCell(19, 0, 9);
        assertEquals(2, board.removeRows(List.of(16, 18, 18)));
        assertEquals(9, board.getCell(19, 0));
        assertEquals(7, board.getCell(18, 0));
        assertEquals(5, board.getCell(17, 0));
        assertThrows(IllegalArgumentException.class, () -> board.removeRows(List.of(19, 20)));
        assertEquals(9, board.getCell(19, 0));
    }

    @Test void manualLandingResetsTheNextBlocksGravityClock() {
        GameEngine game = engine(new Board(), new Tetromino(TetrominoType.O, 18, 3), Difficulty.NORMAL);
        GameController controller = new GameController(game);
        controller.advance(900);
        controller.handle(GameCommand.DOWN);
        controller.advance(100);
        assertEquals(0, game.getCurrentBlock().getRow());
        controller.advance(900);
        assertEquals(1, game.getCurrentBlock().getRow());
    }
}
