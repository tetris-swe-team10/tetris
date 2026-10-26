package com.team10.tetris.game;

/** 보드 안의 읽기 전용 칸 좌표. 아이템 삭제 예정 영역을 화면에 전달한다. */
public record CellPosition(int row, int col) {
    public CellPosition {
        if (row < 0 || row >= Board.HEIGHT || col < 0 || col >= Board.WIDTH)
            throw new IllegalArgumentException("Cell outside board");
    }
}
