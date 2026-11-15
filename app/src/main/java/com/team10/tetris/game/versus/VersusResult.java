package com.team10.tetris.game.versus;

import java.io.Serializable;
import java.util.Objects;

/**
 * 대전 결과.
 *
 * @param winner 승자. 무승부면 {@code null}
 * @param reason 끝난 이유
 */
public record VersusResult(PlayerSide winner, Reason reason) implements Serializable {

    public enum Reason {
        /** 상대가 더 이상 블록을 쌓을 수 없음 */
        TOPPED_OUT,
        /** 시간제한 만료, 점수 비교 */
        TIME_UP,
        /** P2P 연결 끊김 */
        DISCONNECTED
    }

    public VersusResult {
        Objects.requireNonNull(reason);
    }

    public boolean isDraw() {
        return winner == null;
    }
}
