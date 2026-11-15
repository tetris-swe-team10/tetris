package com.team10.tetris.net;

import java.util.Objects;

import com.team10.tetris.game.versus.AttackLines;

/**
 * 게임 중 상대에게 공격 줄을 보내는 메시지. (소유: 역할 C)
 * 받는 쪽은 {@code engine.receiveAttack(lines())}로 넘긴다. 10줄 제한은 받는 쪽 엔진이 처리한다.
 */
public record AttackMessage(AttackLines lines) implements NetMessage {
    public AttackMessage {
        Objects.requireNonNull(lines);
    }
}
