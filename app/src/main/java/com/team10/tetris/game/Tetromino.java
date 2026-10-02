package com.team10.tetris.game;

public class Tetromino {

    private final TetrominoType type;
    private int row;
    private int col;
    private int rotation = 0;

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

    public int getRotation() {
        return rotation;
    }

    public int[][] getShape() {
        int[][] shape = switch (type) {
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

        for (int i = 0; i < rotation; i++) {
            shape = rotateShapeClockwise(shape);
        }

        return shape;
    }

    private int[][] rotateShapeClockwise(int[][] shape) {
        int rows = shape.length;
        int cols = shape[0].length;

        int[][] rotated = new int[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotated[col][rows - 1 - row] = shape[row][col];
            }
        }

        return rotated;
    }

    public void move(int rowChange, int colChange) {
        row += rowChange;
        col += colChange;
    }

    public void moveDown() {
        row++;
    }

    public void rotateClockwise() {
        rotation = (rotation + 1) % 4;
    }

    public void rotateCounterClockwise() {
    rotation = (rotation + 3) % 4;
}
}