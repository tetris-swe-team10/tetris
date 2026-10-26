package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.PenaltyController;

/** 공통 부착 아이템. 즉시 효과는 기존 콜백, 지속 효과는 상태를 받는 콜백을 사용한다. */
public interface GameItem {
    default boolean isAvailable(PenaltyController penalties) { return true; }
    default void onAssigned(PenaltyController penalties) {}
    String symbol();
    String description();
    default void onLock(Board board, int row, int col) {}

    /** 엔진 호출 지점. 기존 즉시 효과 구현은 변경 없이 동작한다. */
    default void onLock(Board board, int row, int col, PenaltyController penalties) {
        onLock(board, row, col);
    }
}
