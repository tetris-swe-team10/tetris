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
}