package com.team10.tetris.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    private final int[][] cells = new int[HEIGHT][WIDTH];

    // 고정된 칸의 블록 종류 (색상 표시용, 종류를 모르는 칸은 null)
    private final TetrominoType[][] cellTypes = new TetrominoType[HEIGHT][WIDTH];

    public int getCell(int row, int col) { return cells[row][col]; }
    public void setCell(int row, int col, int value) {
        cells[row][col] = value;
        cellTypes[row][col] = null;
    }
    public TetrominoType getCellType(int row, int col) { return cellTypes[row][col]; }
    public boolean isInside(int row, int col) {
        return row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH;
    }
    public boolean isEmpty(int row, int col) { return cells[row][col] == 0; }

    public boolean canPlace(Tetromino block) {
        int[][] shape = block.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) continue;
                int boardRow = block.getRow() + row;
                int boardCol = block.getCol() + col;
                if (!isInside(boardRow, boardCol) || !isEmpty(boardRow, boardCol)) return false;
            }
        }
        return true;
    }

    public void lock(Tetromino block) {
        int[][] shape = block.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    cells[block.getRow() + row][block.getCol() + col] = 1;
                    cellTypes[block.getRow() + row][block.getCol() + col] = block.getType();
                }
            }
        }
    }

    public int clearLines() { return removeRows(findFullRows()); }

    /** 압축 전 좌표. 보드를 변경하지 않아 UI 연출과 아이템에서 재사용 가능. */
    public List<Integer> findFullRows() {
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < HEIGHT; row++) {
            boolean full = true;
            for (int col = 0; col < WIDTH; col++) {
                if (cells[row][col] == 0) { full = false; break; }
            }
            if (full) rows.add(row);
        }
        return List.copyOf(rows);
    }

    /** 중복 행을 한 번만 제거하고 남은 행의 순서를 유지한다. 블록 종류도 함께 옮긴다. */
    public int removeRows(Collection<Integer> rows) {
        Set<Integer> selected = new HashSet<>(rows);
        for (Integer row : selected) {
            if (row == null || row < 0 || row >= HEIGHT)
                throw new IllegalArgumentException("Invalid row");
        }
        int target = HEIGHT - 1;
        for (int source = HEIGHT - 1; source >= 0; source--) {
            if (selected.contains(source)) continue;
            System.arraycopy(cells[source], 0, cells[target], 0, WIDTH);
            System.arraycopy(cellTypes[source], 0, cellTypes[target], 0, WIDTH);
            target--;
        }
        while (target >= 0) {
            Arrays.fill(cells[target], 0);
            Arrays.fill(cellTypes[target], null);
            target--;
        }
        return selected.size();
    }
}
