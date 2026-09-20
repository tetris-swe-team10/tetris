package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import com.team10.tetris.game.Tetromino;
import com.team10.tetris.game.TetrominoType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TetrominoTest {

    @Test
    void createsTetrominoWithCorrectTypeAndPosition() {
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);

        assertEquals(TetrominoType.T, block.getType());
        assertEquals(0, block.getRow());
        assertEquals(4, block.getCol());
    }

    @Test
    void tTetrominoHasCorrectShape() {
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);

        int[][] shape = block.getShape();

        assertEquals(2, shape.length);
        assertEquals(3, shape[0].length);

        assertEquals(1, shape[0][0]);
        assertEquals(1, shape[0][1]);
        assertEquals(1, shape[0][2]);

        assertEquals(0, shape[1][0]);
        assertEquals(1, shape[1][1]);
        assertEquals(0, shape[1][2]);
    }

    @Test
void movesTetromino() {
    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);

    block.move(0, -1);
    assertEquals(0, block.getRow());
    assertEquals(3, block.getCol());

    block.move(0, 1);
    assertEquals(4, block.getCol());

    block.moveDown();
    assertEquals(1, block.getRow());
}

@Test
void rotatesTetrominoClockwise() {
    Tetromino block = new Tetromino(TetrominoType.T, 0, 4);

    block.rotateClockwise();

    assertEquals(1, block.getRotation());

    int[][] shape = block.getShape();

    assertEquals(3, shape.length);
    assertEquals(2, shape[0].length);

    assertEquals(0, shape[0][0]);
    assertEquals(1, shape[0][1]);

    assertEquals(1, shape[1][0]);
    assertEquals(1, shape[1][1]);

    assertEquals(0, shape[2][0]);
    assertEquals(1, shape[2][1]);
}
}