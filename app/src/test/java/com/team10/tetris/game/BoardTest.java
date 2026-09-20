package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import com.team10.tetris.game.Board;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardTest {

    @Test
    void boardHasCorrectSize() {
        Board board = new Board();

        assertEquals(0, board.getCell(0, 0));
        assertEquals(0, board.getCell(Board.HEIGHT - 1, Board.WIDTH - 1));
    }

    @Test
    void canSetAndGetCell() {
        Board board = new Board();

        board.setCell(5, 3, 1);

        assertEquals(1, board.getCell(5, 3));
    }

    @Test
void checksWhetherPositionIsInsideBoard() {
    Board board = new Board();

    assertEquals(true, board.isInside(0, 0));
    assertEquals(true, board.isInside(19, 9));

    assertEquals(false, board.isInside(-1, 0));
    assertEquals(false, board.isInside(20, 0));
    assertEquals(false, board.isInside(0, -1));
    assertEquals(false, board.isInside(0, 10));
}

@Test
void checksWhetherCellIsEmpty() {
    Board board = new Board();

    assertEquals(true, board.isEmpty(5, 3));

    board.setCell(5, 3, 1);

    assertEquals(false, board.isEmpty(5, 3));
}

@Test
void checksWhetherTetrominoCanBePlaced() {
    Board board = new Board();

    // 정상적인 위치
    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
    assertEquals(true, board.canPlace(block));

    // 오른쪽 벽을 벗어나는 위치
    Tetromino outsideBlock = new Tetromino(TetrominoType.T, 0, 9);
    assertEquals(false, board.canPlace(outsideBlock));

    // 이미 블록이 있는 위치
    board.setCell(0, 4, 1);
    assertEquals(false, board.canPlace(block));
}

@Test
void locksTetrominoOnBoard() {
    Board board = new Board();
    Tetromino block = new Tetromino(TetrominoType.T, 18, 4);

    board.lock(block);

    assertEquals(1, board.getCell(18, 4));
    assertEquals(1, board.getCell(18, 5));
    assertEquals(1, board.getCell(18, 6));
    assertEquals(1, board.getCell(19, 5));
}

@Test
void clearsFullLine() {
    Board board = new Board();

    // 맨 아래 줄을 전부 채움
    for (int col = 0; col < Board.WIDTH; col++) {
        board.setCell(19, col, 1);
    }

    int clearedLines = board.clearLines();

    assertEquals(1, clearedLines);

    // 삭제 후 맨 아래 줄이 비어 있어야 함
    for (int col = 0; col < Board.WIDTH; col++) {
        assertEquals(0, board.getCell(19, col));
    }
}

@Test
void clearsMultipleFullLines() {
    Board board = new Board();

    // 아래 두 줄을 전부 채움
    for (int col = 0; col < Board.WIDTH; col++) {
        board.setCell(18, col, 1);
        board.setCell(19, col, 1);
    }

    int clearedLines = board.clearLines();

    assertEquals(2, clearedLines);

    for (int col = 0; col < Board.WIDTH; col++) {
        assertEquals(0, board.getCell(18, col));
        assertEquals(0, board.getCell(19, col));
    }
}

}