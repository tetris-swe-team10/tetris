package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;

/** 지급 시점에 새 NEXT를 장식한다. 블록 종류 생성과 아이템 선택은 별개다. */
@FunctionalInterface
public interface ItemAssigner {
    void attachTo(Tetromino piece);

    default void attachTo(Tetromino piece, com.team10.tetris.game.PenaltyController penalties) {
        attachTo(piece);
    }

    /**
     * 엔진의 지급 진입점. 부착형 아이템은 같은 블록에 붙여 그대로 반환하고,
     * 블록형 아이템(무게추)은 같은 위치의 새 블록을 반환한다.
     * 기본 구현은 기존 람다 assigner와 호환되도록 attachTo 후 같은 블록을 반환한다.
     */
    default Tetromino award(Tetromino piece, com.team10.tetris.game.PenaltyController penalties) {
        attachTo(piece, penalties);
        return piece;
    }
}