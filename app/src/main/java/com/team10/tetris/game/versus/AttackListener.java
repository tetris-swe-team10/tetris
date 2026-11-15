package com.team10.tetris.game.versus;

/**
 * 엔진이 공격 줄을 만들었을 때 호출된다. (역할 A가 호출 시점 구현)
 *
 * <p>같은 PC 대전: 상대 엔진의 {@code receiveAttack}으로 바로 전달한다.
 * P2P 대전: 네트워크 메시지로 상대에게 보낸다. (역할 C)
 */
@FunctionalInterface
public interface AttackListener {
    AttackListener NONE = lines -> { };

    void onAttack(AttackLines lines);
}
