package com.team10.tetris.game;

public class Tetromino {

    private final TetrominoType type;
    private int row;
    private int col;

    public Tetromino(TetrominoType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public TetrominoType getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int[][] getShape() {
        return switch (type) {
            case I -> new int[][] {
                {1, 1, 1, 1}
            };

            case O -> new int[][] {
                {1, 1},
                {1, 1}
            };

            case T -> new int[][] {
                {1, 1, 1},
                {0, 1, 0}
            };

            case S -> new int[][] {
                {0, 1, 1},
                {1, 1, 0}
            };

            case Z -> new int[][] {
                {1, 1, 0},
                {0, 1, 1}
            };

            case J -> new int[][] {
                {1, 0, 0},
                {1, 1, 1}
            };

            case L -> new int[][] {
                {0, 0, 1},
                {1, 1, 1}
            };
        };
    }

    public void move(int rowChange, int colChange) {
        row += rowChange;
        col += colChange;
    }

    public void moveDown() {
        row++;
    }
}