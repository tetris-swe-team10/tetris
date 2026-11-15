package com.team10.tetris.game.versus;

import java.io.Serializable;
import java.util.List;

import com.team10.tetris.game.Board;

/**
 * 상대에게 보내는(또는 대기 중인) 공격 줄 묶음.
 *
 * <p>각 줄은 길이 {@link Board#WIDTH}의 칸 목록이며 {@code true}는 회색 블록, {@code false}는 빈칸이다.
 * 빈칸은 "그 줄을 삭제되게 만든 마지막 블록"이 있던 자리다.
 * 목록의 앞쪽이 위쪽 줄, 뒤쪽이 아래쪽 줄이다.
 *
 * <p>규칙(3차 요구사항): 2줄 이상 삭제했을 때만 생성, 대기 줄은 최대 {@link #MAX_PENDING_LINES}줄.
 * 10줄 제한·절단 처리는 엔진(역할 A) 책임이다.
 */
public record AttackLines(List<List<Boolean>> rows) implements Serializable {

    public static final int MAX_PENDING_LINES = 10;
    public static final AttackLines EMPTY = new AttackLines(List.of());

    public AttackLines {
        rows = rows.stream().map(List::copyOf).toList();
        for (List<Boolean> row : rows) {
            if (row.size() != Board.WIDTH)
                throw new IllegalArgumentException("공격 줄의 칸 수는 " + Board.WIDTH + "이어야 합니다.");
        }
    }

    public int size() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
