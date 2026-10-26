package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.PenaltyController;

/** 보드를 바꾸지 않고 다음 블록이 고정될 때까지 원형 시야를 예약한다. */
public final class Blackout implements GameItem {
    @Override public boolean isAvailable(PenaltyController penalties) { return penalties.canReserve(); }
    @Override public void onAssigned(PenaltyController penalties) { penalties.reserve(); }
    @Override public String symbol() { return "B"; }
    @Override public String description() { return "암전 · 고정 후 다음 블록 착지까지 원형 시야"; }
    @Override public void onLock(Board board, int row, int col, PenaltyController penalties) {
        penalties.landed();
    }
}
