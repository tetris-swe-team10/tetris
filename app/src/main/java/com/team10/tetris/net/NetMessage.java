package com.team10.tetris.net;

import java.io.Serializable;

/**
 * 네트워크로 주고받는 모든 메시지의 공통 타입.
 *
 * <p>소유 범위: 연결·대기실 메시지(핑, 준비, 모드 선택, 게임 시작)는 역할 B,
 * 게임 중 메시지(보드 스냅샷, 공격 줄, 게임 종료)는 역할 C가 이 타입을 구현해 추가한다.
 * 각 메시지는 record로 만들고 직렬화 가능한 값만 담는다.
 */
public interface NetMessage extends Serializable {
}
