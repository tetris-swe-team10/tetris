package com.team10.tetris.game.item;

import java.util.Objects;

/** 회전된 블록 모양을 기준으로 한 아이템의 상대 좌표. */
public record ItemAttachment(GameItem item, int row, int col) {
    public ItemAttachment {
        Objects.requireNonNull(item);
        if (row < 0 || col < 0) throw new IllegalArgumentException("Negative item position");
    }
}
