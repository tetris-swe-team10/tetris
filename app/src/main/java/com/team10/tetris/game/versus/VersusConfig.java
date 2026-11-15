package com.team10.tetris.game.versus;

import java.io.Serializable;
import java.util.Objects;

import com.team10.tetris.game.Difficulty;

/**
 * 대전 시작 시 확정하는 설정. P2P에서는 서버가 정해 클라이언트에게 보낸다.
 *
 * @param rule             일반 / 아이템 / 시간제한
 * @param difficulty       두 플레이어 공통 난이도
 * @param timeLimitSeconds 시간제한 모드의 제한 시간(초). 다른 모드에서는 0
 */
public record VersusConfig(VersusRule rule, Difficulty difficulty, int timeLimitSeconds)
        implements Serializable {

    /** 시간제한 기본값(초). 팀 결정 전 임시값. */
    public static final int DEFAULT_TIME_LIMIT_SECONDS = 120;

    public VersusConfig {
        Objects.requireNonNull(rule);
        Objects.requireNonNull(difficulty);
        if (rule == VersusRule.TIME_LIMIT && timeLimitSeconds <= 0)
            throw new IllegalArgumentException("시간제한 모드는 제한 시간이 필요합니다.");
        if (rule != VersusRule.TIME_LIMIT) timeLimitSeconds = 0;
    }

    public static VersusConfig of(VersusRule rule, Difficulty difficulty) {
        return new VersusConfig(rule, difficulty,
                rule == VersusRule.TIME_LIMIT ? DEFAULT_TIME_LIMIT_SECONDS : 0);
    }
}
