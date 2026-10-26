package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;

/** 무게추 W. 처음 닿은 뒤 아래 4열을 한 칸씩 뚫고 내려가며 지우고, 바닥에서 사라진다. */
public final class WeightItem implements PieceItem {
    @Override public String symbol() { return "W"; }
    @Override public String description() { return "무게추 · 닿으면 아래 4열을 뚫고 내려가며 삭제"; }
    @Override public Tetromino createPiece(int row, int col) { return new WeightPiece(this, row, col); }
}