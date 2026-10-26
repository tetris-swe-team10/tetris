package com.team10.tetris.game;

import com.team10.tetris.game.item.SmallBomb;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GhostBlockTest {
    @Test void matchesHardDropForEveryShapeRotationAndUnevenStack() {
        for (TetrominoType type : TetrominoType.values()) {
            for (int rotation = 0; rotation < 4; rotation++) {
                Board board = new Board();
                for (int col = 0; col < Board.WIDTH; col++)
                    for (int row = 19; row >= 19 - col % 4; row--) board.setCell(row, col, 1);
                Tetromino piece = new Tetromino(type, 2, 3);
                for (int i = 0; i < rotation; i++) piece.rotateClockwise();
                GameEngine engine = new GameEngine(board, piece);
                GameSnapshot before = engine.snapshot();
                PieceSnapshot ghost = before.ghostBlock();
                assertNotNull(ghost);
                assertEquals(before, engine.snapshot(), "Projection must not mutate the game");
                assertEquals(before.currentBlock().shape(), ghost.shape());
                assertEquals(before.currentBlock().col(), ghost.col());
                int distance = engine.hardDrop();
                assertEquals(ghost.row() - before.currentBlock().row(), distance, type + "/" + rotation);
                for (int r = 0; r < ghost.shape().size(); r++)
                    for (int c = 0; c < ghost.shape().get(r).size(); c++)
                        if (ghost.shape().get(r).get(c) != 0)
                            assertEquals(type, board.getCellType(ghost.row() + r, ghost.col() + c));
            }
        }
    }

    @Test void followsMovementRotationAndBothWalls() {
        GameEngine engine = new GameEngine(new Board(), new Tetromino(TetrominoType.I, 1, 3));
        while (engine.moveLeft()) { }
        assertEquals(0, engine.snapshot().ghostBlock().col());
        assertEquals(19, engine.snapshot().ghostBlock().row());
        assertTrue(engine.rotateClockwise());
        assertEquals(16, engine.snapshot().ghostBlock().row());
        while (engine.moveRight()) { }
        assertEquals(9, engine.snapshot().ghostBlock().col());
    }

    @Test void stopsAtFirstObstacleInsteadOfEnteringCoveredHoles() {
        Board board = new Board(); board.setCell(10, 4, 1);
        GameEngine engine = new GameEngine(board, new Tetromino(TetrominoType.O, 0, 3));
        assertEquals(8, engine.snapshot().ghostBlock().row());
        GameSnapshot frozen = engine.snapshot();
        board.setCell(10, 4, 0);
        assertEquals(8, frozen.ghostBlock().row());
        assertEquals(18, engine.snapshot().ghostBlock().row());
    }

    @Test void pausedAndGroundedProjectionDoNotExecuteItemOrAwardPoints() {
        Tetromino bomb = new Tetromino(TetrominoType.O, 18, 3);
        bomb.attachItem(new SmallBomb(), 0, 0);
        GameEngine engine = new GameEngine(new Board(), bomb,
                new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
        engine.pause();
        assertEquals(18, engine.snapshot().ghostBlock().row());
        assertNull(engine.snapshot().ghostBlock().item());
        assertEquals(0, engine.getScore());
        assertEquals(0, engine.getBoard().getCell(18, 3));
        engine.resume(); engine.hardDrop();
        assertNull(engine.snapshot().ghostBlock());
        engine.pause();
        assertNull(engine.snapshot().ghostBlock());
    }

    @Test void blackoutHidesProjectionIndependentlyOfState() {
        GameSnapshot base = new GameEngine(new Board(), new Tetromino(TetrominoType.T, 0, 3)).snapshot();
        for (GameState state : GameState.values()) {
            GameSnapshot hidden = new GameSnapshot(base.cells(), base.currentBlock(), base.nextBlock(),
                    base.config(), state, 0, 0, 1000, java.util.List.of(), base.cellTypes(), true, false);
            assertNull(hidden.ghostBlock());
        }
    }

    @Test void stateAloneControlsProjectionWithValidCurrentBlockAndNoBlackout() {
        GameSnapshot base = new GameEngine(new Board(), new Tetromino(TetrominoType.T, 0, 3)).snapshot();
        PieceSnapshot expected = base.ghostBlock();
        assertNotNull(expected, "Fixture must have a valid landing position");
        for (GameState state : GameState.values()) {
            GameSnapshot snapshot = new GameSnapshot(base.cells(), base.currentBlock(), base.nextBlock(),
                    base.config(), state, 0, 0, 1000, java.util.List.of(), base.cellTypes(), false, false);
            assertNotNull(snapshot.currentBlock());
            assertFalse(snapshot.blackoutActive());
            switch (state) {
                case PLAYING, PAUSED -> assertEquals(expected, snapshot.ghostBlock(), state.name());
                case CLEARING, ITEM_EFFECT, GAME_OVER -> assertNull(snapshot.ghostBlock(), state.name());
            }
        }
    }

    @Test void engineHidesProjectionWhileClearingAndAfterGameOver() {
        Board full = new Board();
        for (int col = 0; col < Board.WIDTH; col++) full.setCell(19, col, 1);
        GameEngine clearing = new GameEngine(full, new Tetromino(TetrominoType.O, 17, 3));
        clearing.hardDrop();
        assertEquals(GameState.CLEARING, clearing.getState());
        assertNull(clearing.snapshot().ghostBlock());
        full.setCell(0, 3, 1);
        GameEngine over = new GameEngine(full, new Tetromino(TetrominoType.O, 0, 3));
        assertTrue(over.isGameOver());
        assertNull(over.snapshot().ghostBlock());
    }
}
