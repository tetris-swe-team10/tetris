package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import java.util.Set;

/**
 * 즉시 발동형 부착 아이템. 블록이 고정된 직후 한 번만 호출된다.
 * 엔진은 onLock() → rowsToClear() 순서로 호출한다.
 * - onLock: 보드를 직접 바꾸는 효과 (예: 작은 폭탄)
 * - rowsToClear: 엔진에 삭제를 요청할 행 (예: 줄 삭제). 완성 행과 합쳐 기존 삭제 절차로 처리된다.
 */
public interface GameItem {
    String symbol();
    String description();
    void onLock(Board board, int row, int col);

    /** 보드 좌표 기준 추가 삭제 요청 행. 기본은 없음. */
    default Set<Integer> rowsToClear(Board board, int row, int col) { return Set.of(); }
}