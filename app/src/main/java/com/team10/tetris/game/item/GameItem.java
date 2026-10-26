package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;

/** 즉시 발동형 부착 아이템. 고정된 보드에서 한 번만 호출된다. */
public interface GameItem {
    String symbol();
    String description();
    void onLock(Board board, int row, int col);
}
