package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BlockTypeTrackingTest {

    @Test
    void emptyBoardHasNoCellTypes() {
        Board board = new Board();

        assertNull(board.getCellType(0, 0));
        assertNull(board.getCellType(Board.HEIGHT - 1, Board.WIDTH - 1));
    }

    @Test
    void lockRecordsBlockType() {
        Board board = new Board();

        // S 블록: {0,1,1},{1,1,0}
        board.lock(new Tetromino(TetrominoType.S, 18, 4));

        assertEquals(TetrominoType.S, board.getCellType(18, 5));
        assertEquals(TetrominoType.S, board.getCellType(18, 6));
        assertEquals(TetrominoType.S, board.getCellType(19, 4));
        assertEquals(TetrominoType.S, board.getCellType(19, 5));

        // 블록이 차지하지 않은 칸은 그대로 비어 있음
        assertNull(board.getCellType(18, 4));
        assertNull(board.getCellType(19, 6));
    }

    @Test
    void lockKeepsCellValueAsOne() {
        Board board = new Board();

        board.lock(new Tetromino(TetrominoType.Z, 18, 0));

        assertEquals(1, board.getCell(18, 0));
        assertEquals(1, board.getCell(19, 2));
    }

    @Test
    void differentBlocksKeepTheirOwnTypes() {
        Board board = new Board();

        board.lock(new Tetromino(TetrominoType.J, 18, 0));
        board.lock(new Tetromino(TetrominoType.L, 18, 4));

        assertEquals(TetrominoType.J, board.getCellType(18, 0));
        assertEquals(TetrominoType.J, board.getCellType(19, 2));
        assertEquals(TetrominoType.L, board.getCellType(18, 6));
        assertEquals(TetrominoType.L, board.getCellType(19, 4));
    }

    @Test
    void setCellClearsCellType() {
        Board board = new Board();

        board.lock(new Tetromino(TetrominoType.O, 18, 0));
        board.setCell(18, 0, 0);

        assertNull(board.getCellType(18, 0));
        assertEquals(TetrominoType.O, board.getCellType(18, 1));
    }

    @Test
    void clearLinesMovesCellTypesDown() {
        Board board = new Board();

        // 맨 아래 줄을 I 블록으로 채우고 마지막 두 칸은 O 블록 아래쪽으로 채움
        board.lock(new Tetromino(TetrominoType.I, 19, 0));
        board.lock(new Tetromino(TetrominoType.I, 19, 4));
        board.lock(new Tetromino(TetrominoType.O, 18, 8));

        // 지워질 줄 위에 T 블록을 둠
        board.lock(new Tetromino(TetrominoType.T, 16, 0));

        int cleared = board.clearLines();

        assertEquals(1, cleared);

        // O 블록 윗부분이 한 줄 내려옴
        assertEquals(TetrominoType.O, board.getCellType(19, 8));
        assertEquals(TetrominoType.O, board.getCellType(19, 9));

        // T 블록도 한 줄씩 내려옴 ({1,1,1},{0,1,0})
        assertEquals(TetrominoType.T, board.getCellType(17, 0));
        assertEquals(TetrominoType.T, board.getCellType(17, 2));
        assertEquals(TetrominoType.T, board.getCellType(18, 1));

        // I 블록이 있던 칸은 비워짐
        assertNull(board.getCellType(19, 0));
        assertNull(board.getCellType(19, 7));
    }

    @Test
    void clearLinesEmptiesTopRowTypes() {
        Board board = new Board();

        for (int col = 0; col < Board.WIDTH; col += 2) {
            board.lock(new Tetromino(TetrominoType.O, 0, col));
        }

        board.clearLines();

        for (int col = 0; col < Board.WIDTH; col++) {
            assertNull(board.getCellType(0, col));
            assertNull(board.getCellType(1, col));
        }
    }
}
