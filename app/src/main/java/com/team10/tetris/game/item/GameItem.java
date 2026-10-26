package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.PenaltyController;
import com.team10.tetris.game.CellPosition;
import java.util.List;

/** 공통 부착 아이템. 즉시 효과는 기존 콜백, 지속 효과는 상태를 받는 콜백을 사용한다. */
public interface GameItem {
    default boolean isAvailable(PenaltyController penalties) { return true; }
    default void onAssigned(PenaltyController penalties) {}
    default void onSpawn(PenaltyController penalties) {}
    String symbol();
    String description();
    /** 선택적 삭제 연출. 기존 즉시/지속 아이템은 빈 목록·0ms를 유지한다. */
    default List<CellPosition> previewCells(Board board, int row, int col) { return List.of(); }
    default int effectDurationMs() { return 0; }
    default void onLock(Board board, int row, int col) {}

    /** 엔진 호출 지점. 기존 즉시 효과 구현은 변경 없이 동작한다. */
    default void onLock(Board board, int row, int col, PenaltyController penalties) {
        onLock(board, row, col);
    }
}
