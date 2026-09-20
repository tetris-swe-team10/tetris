package com.team10.tetris.game;

public class Board {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;

    private final int[][] cells;

    public Board() {
        cells = new int[HEIGHT][WIDTH];
    }

    public int getCell(int row, int col) {
        return cells[row][col];
    }

    public void setCell(int row, int col, int value) {
        cells[row][col] = value;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < HEIGHT
                && col >= 0 && col < WIDTH;
    }

    public boolean isEmpty(int row, int col) {
    return cells[row][col] == 0;
}

public boolean canPlace(Tetromino block) {
    int[][] shape = block.getShape();

    for (int row = 0; row < shape.length; row++) {
        for (int col = 0; col < shape[row].length; col++) {

            if (shape[row][col] == 0) {
                continue;
            }

            int boardRow = block.getRow() + row;
            int boardCol = block.getCol() + col;

            if (!isInside(boardRow, boardCol)) {
                return false;
            }

            if (!isEmpty(boardRow, boardCol)) {
                return false;
            }
        }
    }

    return true;
}
}