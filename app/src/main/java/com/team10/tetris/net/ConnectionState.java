package com.team10.tetris.net;

/**
 * P2P 연결 상태. 판정 기준(랙 ms, 끊김 초)은 역할 B가 구현하고 팀 결정 후 상수로 둔다.
 */
public enum ConnectionState {
    /** 서버: 접속 대기 중 / 클라이언트: 접속 시도 중 */
    CONNECTING,
    /** 정상 연결 */
    CONNECTED,
    /** 연결은 유지되지만 응답이 느림 ("랙 걸린 상태") */
    LAGGING,
    /** 일정 시간 이상 응답 없음 또는 연결 종료 */
    DISCONNECTED
}
