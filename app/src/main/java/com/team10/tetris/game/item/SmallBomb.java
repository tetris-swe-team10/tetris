package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;

/** 주변 칸만 비운다. 행 압축, 중력, 점수 계산, 다른 아이템 호출은 하지 않는다. */
public final class SmallBomb implements GameItem {
    @Override public String symbol() { return "E"; }
    @Override public String description() { return "작은 폭탄 · 고정 후 주변 3×3 삭제"; }

    @Override
    public void onLock(Board board, int row, int col) {
        if (!board.isInside(row, col)) throw new IllegalArgumentException("Bomb outside board");
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (board.isInside(r, c)) board.setCell(r, c, 0);
            }
        }
    }
}
