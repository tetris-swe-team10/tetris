package com.team10.tetris.net;

import java.util.Objects;

import com.team10.tetris.game.GameSnapshot;

/**
 * 게임 중 내 보드 상태를 상대에게 보내는 메시지. (소유: 역할 C)
 *
 * <p>변환 책임: 보내는 쪽은 {@code engine.snapshot()}을 그대로 담고, 받는 쪽은 {@link #snapshot()}을
 * 변환 없이 {@code BoardRenderer}에 넘긴다. 별도 DTO·코덱은 두지 않는다.
 * 지연을 줄이기 위한 전송 빈도·크기 최적화는 역할 C가 이 계약 안에서 정한다.
 *
 * @param sequence 보낸 순서 번호. 받는 쪽은 이전 번호보다 작거나 같은 상태를 무시할 수 있다
 * @param snapshot 보내는 쪽 보드의 스냅샷
 */
public record BoardStateMessage(long sequence, GameSnapshot snapshot) implements NetMessage {
    public BoardStateMessage {
        Objects.requireNonNull(snapshot);
    }
}
