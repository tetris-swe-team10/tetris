package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;

/** 지급 시점에 새 NEXT를 장식한다. 블록 종류 생성과 아이템 선택은 별개다. */
@FunctionalInterface
public interface ItemAssigner {
    void attachTo(Tetromino piece);
}
