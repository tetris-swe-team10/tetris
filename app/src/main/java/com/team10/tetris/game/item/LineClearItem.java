package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import java.util.Set;

/**
 * 줄 삭제(L) 아이템.
 * 보드를 직접 바꾸지 않고, 'L'이 고정된 행의 삭제를 엔진에 요청한다.
 * 엔진은 이 행을 완성 행과 합쳐 기존 삭제 절차(연출·압축·점수·누적 줄·속도·아이템 지급)로 처리한다.
 */
public final class LineClearItem implements GameItem {
    @Override public String symbol() { return "L"; }
    @Override public String description() { return "줄 삭제 · 고정 시 L이 있는 줄을 삭제"; }

    /** 보드를 직접 바꾸지 않는다. 삭제는 rowsToClear()를 통해 엔진이 처리한다. */
    @Override
    public void onLock(Board board, int row, int col) {
        // 의도적으로 비워 둠
    }

    @Override
    public Set<Integer> rowsToClear(Board board, int row, int col) {
        if (!board.isInside(row, col)) throw new IllegalArgumentException("Line clear outside board");
        return Set.of(row);
    }
}