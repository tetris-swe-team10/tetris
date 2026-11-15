package com.team10.tetris.net;

import com.team10.tetris.game.versus.VersusConfig;

/**
 * P2P 대기실(역할 B) → 게임 화면(역할 C) 전환 지점. (구현: 역할 C, 호출: 역할 B)
 *
 * <p>양쪽이 '게임 시작'을 누르면 역할 B가 {@link #start}를 호출한다.
 *
 * <p>전환 순서 계약 (첫 보드 상태 유실 방지):
 * <ol>
 *   <li>역할 B는 게임 시작 메시지를 처리하는 대기실 핸들러 안에서 {@link #start}를 <b>동기적으로</b> 호출한다.
 *       {@code Platform.runLater} 등으로 미루지 않는다.</li>
 *   <li>역할 C는 {@link #start}가 반환되기 전에 {@code channel.setOnMessage}로 게임 핸들러를 설치한다.</li>
 *   <li>역할 C는 게임 핸들러 설치 후에만 게임 메시지를 보낸다.</li>
 * </ol>
 * {@link ConnectionChannel}의 순서 보장과 합쳐, 상대가 시작 직후 보낸 메시지도 게임 핸들러가 받는다.
 * 게임이 끝나 대기실로 돌아갈 때도 같은 방식으로 역할 B가 {@link Exit#returnToLobby()} 안에서 대기실 핸들러를 다시 설치한다.
 *
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
