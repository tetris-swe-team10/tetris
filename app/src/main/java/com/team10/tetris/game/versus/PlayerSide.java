package com.team10.tetris.game.versus;

/**
 * 대전의 두 플레이어 구분.
 * 같은 PC 대전에서는 왼쪽/오른쪽 보드, P2P에서는 PLAYER1 = 내 보드, PLAYER2 = 상대 보드로 쓴다.
 */
public enum PlayerSide {
    PLAYER1, PLAYER2;

    public PlayerSide opponent() {
        return this == PLAYER1 ? PLAYER2 : PLAYER1;
    }
}
