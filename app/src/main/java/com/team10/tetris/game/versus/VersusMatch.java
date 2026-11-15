package com.team10.tetris.game.versus;

import java.util.Optional;

import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.GameSnapshot;

/**
 * 같은 PC 대전 한 판. 엔진 두 개를 묶어 공격 전달·승패·시간제한을 관리한다. (구현: 역할 A)
 *
 * <p>JavaFX에 의존하지 않는다. 화면(역할 A)은 키 입력을 {@link #handle}로,
 * 타이머 경과 시간을 {@link #advance}로 전달하고 {@link #snapshot}으로 그린다.
 */
public interface VersusMatch {

    VersusConfig config();

    /** 해당 플레이어의 조작. 게임이 끝났으면 무시한다. */
    void handle(PlayerSide player, GameCommand command);

    /** 경과 시간을 두 엔진과 시간제한 타이머에 전달한다. */
    void advance(int elapsedMs);

    GameSnapshot snapshot(PlayerSide player);

    /** 시간제한 모드의 남은 시간(ms). 다른 모드에서는 -1 */
    long remainingTimeMs();

    /** 끝났으면 결과, 진행 중이면 empty */
    Optional<VersusResult> result();

    void pause();

    void resume();
}
