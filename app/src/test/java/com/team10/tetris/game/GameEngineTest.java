package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTest {

    @Test
    void movesBlockLeft() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(true, engine.moveLeft());
        assertEquals(3, block.getCol());
    }

    @Test
    void doesNotMoveBlockOutsideLeftWall() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 0);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveLeft());
        assertEquals(0, block.getCol());
    }

    @Test
void movesBlockRight() {
    Board board = new Board();
    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
    GameEngine engine = new GameEngine(board, block);

    assertEquals(true, engine.moveRight());
    assertEquals(5, block.getCol());
}

@Test
void doesNotMoveBlockOutsideRightWall() {
    Board board = new Board();
    Tetromino block = new Tetromino(TetrominoType.T, 0, 7);
    GameEngine engine = new GameEngine(board, block);

    assertEquals(false, engine.moveRight());
    assertEquals(7, block.getCol());
}

@Test
void movesBlockDown() {
    Board board = new Board();
    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
    GameEngine engine = new GameEngine(board, block);

    assertEquals(true, engine.moveDown());
    assertEquals(1, block.getRow());
}

@Test
void doesNotMoveBlockBelowBottom() {
    Board board = new Board();
    Tetromino block = new Tetromino(TetrominoType.T, 18, 4);
    GameEngine engine = new GameEngine(board, block);

    assertEquals(false, engine.moveDown());
    assertEquals(18, block.getRow());
}

@Test
void doesNotMoveDownWhenAnotherBlockIsBelow() {
    Board board = new Board();

    // T 블록 바로 아래에 이미 쌓인 블록이 있다고 가정
    board.setCell(2, 5, 1);

    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
    GameEngine engine = new GameEngine(board, block);

    assertEquals(false, engine.moveDown());
    assertEquals(0, block.getRow());
}
}