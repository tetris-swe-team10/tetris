package com.team10.tetris.net;

import com.team10.tetris.game.versus.VersusConfig;

/**
 * P2P 대기실(역할 B) → 게임 화면(역할 C) 전환 지점. (구현: 역할 C, 호출: 역할 B)
 *
 * <p>양쪽이 '게임 시작'을 누르면 역할 B가 {@link #start}를 호출한다.
 * 게임이 끝나면 역할 C가 {@link Exit#returnToLobby()}로 대기실에 돌려주고,
 * 게임 중 연결이 끊기면 에러를 보여 준 뒤 {@link Exit#returnToP2pStart()}를 호출한다.
 */
public interface P2pGameLauncher {

    void start(ConnectionChannel channel, P2pRole role, VersusConfig config, Exit exit);

    /** 게임 화면이 끝날 때 돌아갈 곳. 역할 B가 제공한다. */
    interface Exit {
        /** 승패 표시 후: 연결을 유지한 채 대기실로 복귀 */
        void returnToLobby();

        /** 연결 끊김: P2P 대전 첫 화면으로 복귀 */
        void returnToP2pStart();
    }
}
