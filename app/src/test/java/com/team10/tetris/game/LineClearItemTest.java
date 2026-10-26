package com.team10.tetris.game;

import com.team10.tetris.game.item.*;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LineClearItemTest {
    private final LineClearItem lineClear = new LineClearItem();

    /** 다음 블록은 항상 O, 지급 시 항상 L을 (0,0)에 붙이는 테스트용 엔진. */
    private GameEngine game(Board board, Tetromino piece, GameMode mode) {
        return new GameEngine(board, piece, new GameConfig(mode, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                next -> next.attachItem(lineClear, 0, 0));
    }

    private static int filled(Board board) {
        int count = 0;
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) if (!board.isEmpty(r, c)) count++;
        return count;
    }

    private static void reset(Board board) {
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
    }

    /** 일반 완성 행으로 rows줄을 삭제한다. */
    private void clearFull(GameEngine game, int rows) {
        Board board = game.getBoard();
        reset(board);
        for (int r = Board.HEIGHT - rows; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) if (c != 4) board.setCell(r, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 16, 4);
        piece.rotateClockwise();
        game.setCurrentBlock(piece);
        game.moveDown();
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void symbolAndDescriptionIdentifyLineClear() {
        assertEquals("L", lineClear.symbol());
        assertTrue(lineClear.description().startsWith("줄 삭제"));
        assertNotEquals(new SmallBomb().symbol(), lineClear.symbol());
        assertDoesNotThrow(() -> new RandomItemAssigner(List.of(new SmallBomb(), lineClear), new Random()));
    }

    @Test void requestsOnlyItsOwnRowAndDoesNotTouchBoard() {
        Board board = new Board();
        board.setCell(7, 0, 1); board.setCell(7, 2, 1); board.setCell(3, 5, 1);
        lineClear.onLock(board, 7, 2);
        assertEquals(3, filled(board));
        assertEquals(Set.of(7), lineClear.rowsToClear(board, 7, 2));
        assertEquals(3, filled(board));
        assertThrows(IllegalArgumentException.class, () -> lineClear.rowsToClear(board, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> lineClear.rowsToClear(board, Board.HEIGHT, 0));
        assertThrows(IllegalArgumentException.class, () -> lineClear.rowsToClear(board, 0, Board.WIDTH));
    }

    @Test void partialRowIsClearedWithAnimationAndNormalScore() {
        Board board = new Board();
        board.setCell(19, 0, 1); board.setCell(19, 1, 1);
        board.setCell(18, 9, 1); // 삭제 줄 위의 칸은 한 줄 내려와야 한다.
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(lineClear, 0, 0);
        GameEngine game = game(board, piece, GameMode.ITEM);

        assertFalse(game.moveDown());
        assertEquals(GameState.CLEARING, game.getState());
        assertEquals(List.of(19), game.getClearingRows());
        assertEquals(1, board.getCell(19, 0), "연출 중에는 아직 삭제하지 않는다");

        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(1000, game.getScore());
        assertEquals(1, game.getTotalClearedLines());
        assertEquals(1, game.getLastClearedLines());
        assertEquals(1, board.getCell(19, 9));
        assertEquals(1, filled(board));
    }

    @Test void itemRowAndFullRowAreClearedTogetherWithComboScore() {
        Board board = new Board();
        for (int c = 0; c < Board.WIDTH; c++) if (c != 3 && c != 4) board.setCell(19, c, 1);
        board.setCell(18, 9, 1);
        board.setCell(17, 0, 1);
        Tetromino piece = new Tetromino(TetrominoType.O, 18, 3);
        piece.attachItem(lineClear, 0, 0); // 보드 18행 (미완성), 19행은 O로 완성
        GameEngine game = game(board, piece, GameMode.ITEM);

        game.moveDown();
        assertEquals(List.of(18, 19), game.getClearingRows());
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(2500, game.getScore()); // 2줄 동시 삭제: 1000*2 + 500
        assertEquals(2, game.getTotalClearedLines());
        assertEquals(1, board.getCell(19, 0)); // 17행이 두 줄 내려옴
        assertEquals(1, filled(board));
    }

    @Test void itemOnAlreadyFullRowCountsOnce() {
        Board board = new Board();
        for (int c = 0; c < Board.WIDTH; c++) if (c < 3 || c > 6) board.setCell(19, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(lineClear, 0, 2);
        GameEngine game = game(board, piece, GameMode.ITEM);

        game.moveDown();
        assertEquals(List.of(19), game.getClearingRows());
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(1000, game.getScore());
        assertEquals(1, game.getTotalClearedLines());
    }

    @Test void normalModeIgnoresLineClearItem() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(lineClear, 0, 0);
        GameEngine game = game(board, piece, GameMode.NORMAL);

        game.moveDown();
        assertEquals(GameState.PLAYING, game.getState());
        assertTrue(game.getClearingRows().isEmpty());
        assertEquals(1, board.getCell(19, 3));
    }

    @Test void clearedRowFollowsMarkerAfterRotationAndHardDrop() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.I, 0, 3);
        piece.attachItem(lineClear, 0, 0);
        piece.rotateClockwise(); // 세로 I: 마커도 함께 회전
        GameEngine game = game(board, piece, GameMode.ITEM);

        game.hardDrop();
        int markerRow = piece.getRow() + piece.getItem().row();
        assertTrue(markerRow >= 16 && markerRow <= 19);
        assertEquals(List.of(markerRow), game.getClearingRows());
        int before = game.getScore();
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);
        assertEquals(before + 1000, game.getScore());
        assertEquals(3, filled(board)); // 세로 I 4칸 중 L 행 1칸만 삭제
    }

    @Test void itemRowsCountTowardSpeedAndNextItemAward() {
        GameEngine game = game(new Board(), null, GameMode.ITEM);
        clearFull(game, 4); clearFull(game, 4); clearFull(game, 1);
        assertEquals(9, game.getTotalClearedLines());
        assertNull(game.getNextBlock().getItem());

        Board board = game.getBoard();
        reset(board);
        board.setCell(19, 0, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 19, 3);
        piece.attachItem(lineClear, 0, 0);
        game.setCurrentBlock(piece);
        game.moveDown();
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);

        assertEquals(10, game.getTotalClearedLines());
        assertEquals("L", PieceSnapshot.of(game.getNextBlock()).item().symbol());
        assertEquals(800, game.getDropIntervalMs()); // Normal: 5줄·10줄에서 100ms씩 2회 가속
    }

    @Test void attachPositionCanBeEveryCellOfIBlock() {
        Set<Integer> columns = new HashSet<>();
        for (int ticket = 0; ticket < 4; ticket++) {
            final int chosen = ticket;
            Random random = new Random() {
                @Override public int nextInt(int bound) { return bound == 1 ? 0 : chosen; }
            };
            Tetromino piece = new Tetromino(TetrominoType.I, 0, 3);
            new RandomItemAssigner(List.of(lineClear), random).attachTo(piece);
            assertEquals(0, piece.getItem().row());
            columns.add(piece.getItem().col());
        }
        assertEquals(Set.of(0, 1, 2, 3), columns); // LOOO, OLOO, OOLO, OOOL
    }

    @Test void previewMarkerShowsLetterAndDescription() {
        Tetromino piece = new Tetromino(TetrominoType.T, 0, 3);
        piece.attachItem(lineClear, 0, 1);
        var marker = PieceSnapshot.of(piece).item();
        assertEquals("L", marker.symbol());
        assertTrue(marker.description().contains("줄 삭제"));
        assertEquals(0, marker.row());
        assertEquals(1, marker.col());
    }
}