package com.team10.tetris.game;

import com.team10.tetris.game.item.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class SmallBombTest {
    private final SmallBomb bomb = new SmallBomb();

    private GameEngine game(Board board, Tetromino piece, GameMode mode) {
        return new GameEngine(board, piece, new GameConfig(mode, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                new RandomItemAssigner(List.of(bomb), new Random()));
    }

    @Test void explosionClearsExactlyNineCellsAndTheirTypesWithoutMovingOtherCells() {
        Board board = new Board();
        for (int r = 0; r < Board.HEIGHT; r += 2)
            for (int c = 0; c < Board.WIDTH; c += 2)
                board.lock(new Tetromino(TetrominoType.O, r, c));
        bomb.onLock(board, 10, 5);
        int removed = 0;
        for (int r = 0; r < Board.HEIGHT; r++) {
            for (int c = 0; c < Board.WIDTH; c++) {
                boolean inside = r >= 9 && r <= 11 && c >= 4 && c <= 6;
                assertEquals(inside ? 0 : 1, board.getCell(r, c), r + "," + c);
                assertEquals(inside ? null : TetrominoType.O, board.getCellType(r, c));
                if (board.isEmpty(r, c)) removed++;
            }
        }
        assertEquals(9, removed);
    }

    @Test void allCornersAndEdgesClipToBoardAndEmptyBoardIsSafe() {
        int[][] cases = {{0, 0, 4}, {0, 9, 4}, {19, 0, 4}, {19, 9, 4},
                {0, 5, 6}, {19, 5, 6}, {10, 0, 6}, {10, 9, 6}};
        for (int[] test : cases) {
            Board board = new Board();
            for (int r = 0; r < Board.HEIGHT; r++)
                for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 1);
            bomb.onLock(board, test[0], test[1]);
            int empty = 0;
            for (int r = 0; r < Board.HEIGHT; r++)
                for (int c = 0; c < Board.WIDTH; c++) if (board.isEmpty(r, c)) empty++;
            assertEquals(test[2], empty);
        }
        assertDoesNotThrow(() -> bomb.onLock(new Board(), 0, 0));
        assertThrows(IllegalArgumentException.class, () -> bomb.onLock(new Board(), -1, 0));
    }

    @Test void bombOnLockedPieceExplodesOnceWithoutPointsOrLineCredits() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(bomb, 0, 1);
        board.setCell(18, 4, 1);
        board.setCell(17, 4, 1); // blast 바로 위는 떠 있어야 한다.
        GameEngine game = game(board, piece, GameMode.ITEM);
        assertFalse(game.moveDown());
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(0, game.getScore());
        assertEquals(0, game.getTotalClearedLines());
        assertEquals(1000, game.getDropIntervalMs());
        assertEquals(1, board.getCell(17, 4));
        assertEquals(0, board.getCell(18, 4));
        assertEquals(1, board.getCell(19, 6)); // 같은 블록의 범위 밖 한 칸 보존
        board.setCell(19, 4, 1);
        game.advanceClear(1000);
        game.tick(); // 새 블록의 한 칸 이동만 수행
        assertEquals(1, board.getCell(19, 4), "Old bomb must not run again");
        assertEquals(100, game.getScore());
    }

    @Test void explosionRunsAfterLockAndBeforeFullRowDetection() {
        Board board = new Board();
        for (int c = 0; c < Board.WIDTH; c++) if (c < 3 || c > 6) board.setCell(19, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(bomb, 0, 1);
        GameEngine game = game(board, piece, GameMode.ITEM);
        game.moveDown();
        assertEquals(GameState.PLAYING, game.getState());
        assertTrue(game.getClearingRows().isEmpty());
        assertEquals(0, game.getScore());
        assertEquals(0, game.getTotalClearedLines());
        assertEquals(1, board.getCell(19, 0));
        assertEquals(1, board.getCell(19, 9));
    }

    @Test void fullRowOutsideExplosionKeepsAnimationAndNormalScore() {
        Board board = new Board();
        for (int c = 0; c < Board.WIDTH; c++) board.setCell(19, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.O, 17, 3);
        piece.attachItem(bomb, 0, 0); // 폭발은 16~18행, 19행은 유지
        GameEngine game = game(board, piece, GameMode.ITEM);
        game.moveDown();
        assertEquals(GameState.CLEARING, game.getState());
        assertEquals(List.of(19), game.getClearingRows());
        assertEquals(0, board.getCell(17, 3));
        game.advanceClear(300);
        assertEquals(1000, game.getScore());
        assertEquals(1, game.getTotalClearedLines());
        game.advanceClear(300);
        assertEquals(1000, game.getScore());
    }

    @Test void pausedAndGameOverInputsCannotTriggerExplosion() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, 18, 3);
        piece.attachItem(bomb, 0, 0);
        GameEngine game = game(board, piece, GameMode.ITEM);
        game.pause();
        assertFalse(game.moveDown());
        assertEquals(0, game.hardDrop());
        assertEquals(0, board.getCell(18, 3));
        game.resume();
        game.moveDown();
        assertNull(board.getCellType(18, 3));

        Board blocked = new Board(); blocked.setCell(18, 3, 1);
        GameEngine over = game(blocked, piece, GameMode.ITEM);
        assertTrue(over.isGameOver());
        over.hardDrop(); over.tick();
        assertEquals(1, blocked.getCell(18, 3));
    }

    @Test void normalModeIgnoresInjectedItemEffects() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, 18, 3);
        piece.attachItem(bomb, 0, 0);
        GameEngine game = game(board, piece, GameMode.NORMAL);
        game.moveDown();
        assertEquals(1, board.getCell(18, 3));
        assertEquals(TetrominoType.O, board.getCellType(18, 3));
    }

    @Test void anotherLockedBombDoesNotChainReact() {
        Board board = new Board();
        Tetromino other = new Tetromino(TetrominoType.O, 17, 5);
        other.attachItem(bomb, 0, 0);
        board.lock(other);
        board.setCell(16, 6, 1); // 두 번째 폭발 범위지만 첫 폭발 범위 밖
        bomb.onLock(board, 18, 4);
        assertEquals(0, board.getCell(17, 5));
        assertEquals(1, board.getCell(16, 6));
        assertEquals(1, board.getCell(17, 6));
    }

    @Test void markersFollowEveryOccupiedCellThroughFourRotationsAndCounterRotation() {
        for (TetrominoType type : TetrominoType.values()) {
            int[][] shape = new Tetromino(type, 0, 3).getShape();
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] == 0) continue;
                    Tetromino piece = new Tetromino(type, 0, 3);
                    piece.attachItem(bomb, r, c);
                    var original = piece.getItem();
                    for (int turn = 0; turn < 4; turn++) {
                        piece.rotateClockwise();
                        var marker = piece.getItem();
                        assertEquals(1, piece.getShape()[marker.row()][marker.col()]);
                    }
                    assertEquals(original, piece.getItem());
                    piece.rotateCounterClockwise(); piece.rotateClockwise();
                    assertEquals(original, piece.getItem());
                }
            }
        }
    }

    @Test void rejectedRotationRestoresMarkerAndMovementKeepsRelativePosition() {
        Tetromino piece = new Tetromino(TetrominoType.I, 16, 9);
        piece.rotateClockwise(); piece.attachItem(bomb, 2, 0);
        GameEngine game = game(new Board(), piece, GameMode.ITEM);
        var original = piece.getItem();
        assertFalse(game.rotateClockwise());
        assertEquals(original, piece.getItem());
        assertTrue(game.moveLeft());
        assertEquals(original, piece.getItem());
        assertEquals(8, game.snapshot().currentBlock().col());
    }

    @Test void rotatedMarkerDeterminesActualExplosionCenterOnLanding() {
        Tetromino piece = new Tetromino(TetrominoType.I, 0, 3);
        piece.attachItem(bomb, 0, 3);
        piece.rotateClockwise();
        Board board = new Board();
        board.setCell(19, 4, 1);
        GameEngine game = game(board, piece, GameMode.ITEM);
        assertEquals(16, game.hardDrop());
        assertEquals(1600, game.getScore());
        assertEquals(0, board.getCell(19, 4));
        assertEquals(1, board.getCell(17, 3));
        assertEquals(0, board.getCell(18, 3));
    }

    @Test void snapshotMarkerIsAStableValueAndInvalidAttachmentsAreRejected() {
        Tetromino piece = new Tetromino(TetrominoType.T, 0, 3);
        assertThrows(IllegalArgumentException.class, () -> piece.attachItem(bomb, 1, 0));
        piece.attachItem(bomb, 0, 2);
        PieceSnapshot snapshot = PieceSnapshot.of(piece);
        assertEquals("E", snapshot.item().symbol());
        assertEquals(2, snapshot.item().col());
        piece.rotateClockwise();
        assertEquals(0, snapshot.item().row());
        assertEquals(2, snapshot.item().col());
        assertThrows(IllegalStateException.class, () -> piece.attachItem(bomb, 0, 1));
        assertThrows(NullPointerException.class, () -> new ItemAttachment(null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ItemAttachment(bomb, -1, 0));
    }

    @Test void randomAttachmentCanChooseEveryRealCellOfEveryShape() {
        for (TetrominoType type : TetrominoType.values()) {
            java.util.Set<String> selected = new java.util.HashSet<>();
            for (int ticket = 0; ticket < 4; ticket++) {
                final int chosen = ticket;
                Random random = new Random() {
                    @Override public int nextInt(int bound) { return bound == 1 ? 0 : chosen; }
                };
                Tetromino piece = new Tetromino(type, 0, 3);
                new RandomItemAssigner(List.of(bomb), random).attachTo(piece);
                var marker = piece.getItem();
                assertEquals(1, piece.getShape()[marker.row()][marker.col()]);
                selected.add(marker.row() + "," + marker.col());
            }
            assertEquals(4, selected.size());
        }
        assertThrows(IllegalArgumentException.class, () -> new RandomItemAssigner(List.of(), new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> new RandomItemAssigner(List.of(bomb, bomb), new Random()));
    }

    @Test void itemRegistrySupportsAnotherEffectWithoutChangingEngine() {
        GameItem another = new GameItem() {
            public String symbol() { return "X"; }
            public String description() { return "테스트 아이템"; }
            public void onLock(Board board, int row, int col) { board.setCell(row, col, 0); }
        };
        Tetromino piece = new Tetromino(TetrominoType.O, 18, 3);
        new RandomItemAssigner(List.of(another), new Random(1)).attachTo(piece);
        var marker = piece.getItem();
        GameEngine game = game(new Board(), piece, GameMode.ITEM);
        game.moveDown();
        assertEquals(0, game.getBoard().getCell(18 + marker.row(), 3 + marker.col()));
    }

    private void clear(GameEngine game, int rows) {
        Board board = game.getBoard();
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
        for (int r = Board.HEIGHT - rows; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) if (c != 4) board.setCell(r, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 16, 4);
        piece.rotateClockwise();
        game.setCurrentBlock(piece);
        game.moveDown(); game.advanceClear(300);
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void tenLineBoundaryDecoratesOnlyNewNextAndPreviewSurvivesPromotion() {
        GameEngine game = game(new Board(), null, GameMode.ITEM);
        clear(game, 4); clear(game, 4); clear(game, 1);
        assertEquals(9, game.getTotalClearedLines());
        assertNull(game.getNextBlock().getItem());
        Tetromino oldNext = game.getNextBlock();
        clear(game, 1);
        assertEquals(10, game.getTotalClearedLines());
        assertSame(oldNext, game.getCurrentBlock());
        assertNull(game.getCurrentBlock().getItem());
        Tetromino awarded = game.getNextBlock();
        var announced = PieceSnapshot.of(awarded).item();
        assertNotNull(announced);
        assertEquals("E", announced.symbol());
        game.hardDrop();
        assertSame(awarded, game.getCurrentBlock());
        assertEquals(announced, game.snapshot().currentBlock().item());
        assertNull(game.getNextBlock().getItem());
    }

    @Test void overflowKeepsLineRemainderAndAwardsAtEachNewBoundaryOnly() {
        GameEngine game = game(new Board(), null, GameMode.ITEM);
        clear(game, 4); clear(game, 4); clear(game, 1); clear(game, 4);
        assertEquals(13, game.getTotalClearedLines());
        assertNotNull(game.getNextBlock().getItem());
        clear(game, 4); clear(game, 2);
        assertEquals(19, game.getTotalClearedLines());
        assertNull(game.getNextBlock().getItem());
        clear(game, 2);
        assertEquals(21, game.getTotalClearedLines());
        assertNotNull(game.getNextBlock().getItem());
        clear(game, 1);
        assertNull(game.getNextBlock().getItem());
    }

    @Test void normalModeNeverUsesAssignerAndRestartResetsRewardProgress() {
        GameEngine normal = new GameEngine(new Board(), null, GameConfig.DEFAULT,
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                piece -> fail("Normal mode must not assign items"));
        clear(normal, 4); clear(normal, 4); clear(normal, 2);
        assertNull(normal.getNextBlock().getItem());
        GameEngine fresh = new GameEngine(new GameConfig(GameMode.ITEM, Difficulty.HARD));
        assertNull(fresh.getCurrentBlock().getItem());
        assertNull(fresh.getNextBlock().getItem());
        clear(fresh, 1);
        assertNull(fresh.getNextBlock().getItem());
    }
}
