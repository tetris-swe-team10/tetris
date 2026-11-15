package com.team10.tetris.net;

import java.util.function.Consumer;

/**
 * 연결된 상대와 메시지를 주고받는 통로. (구현: 역할 B, 사용: 역할 B·C)
 *
 * <p>전달 순서 계약 (구현: 역할 B):
 * <ul>
 *   <li>받은 메시지는 도착 순서대로, JavaFX 애플리케이션 스레드에서 한 번에 하나씩 핸들러에 전달한다.</li>
 *   <li>핸들러 실행 중에 {@link #setOnMessage}로 핸들러를 바꾸면 <b>바로 다음 메시지부터</b> 새 핸들러가 받는다.
 *       따라서 대기실 핸들러가 게임 시작 메시지를 처리하는 도중에 게임 핸들러를 설치하면, 상대가 시작 직후
 *       보낸 첫 보드 상태도 게임 핸들러로 전달된다.</li>
 *   <li>핸들러가 등록되지 않은 동안 도착한 메시지는 버리지 않고 보관했다가, 다음에 등록된 핸들러에 순서대로 전달한다.</li>
 * </ul>
 * 역할 C는 진짜 소켓 대신 이 인터페이스의 가짜 구현으로 먼저 개발·테스트할 수 있다.
 */
public interface ConnectionChannel extends AutoCloseable {

    /** 메시지를 보낸다. 연결이 끊겼으면 무시한다. */
    void send(NetMessage message);

    /** 메시지를 받으면 호출할 대상. 여러 번 등록하면 마지막 것만 유지한다. 전달 순서는 클래스 설명 참고. */
    void setOnMessage(Consumer<NetMessage> handler);

    /** 연결 상태가 바뀌면 호출할 대상. */
    void setOnStateChanged(Consumer<ConnectionState> handler);

    ConnectionState state();

    /** 최근 왕복 지연(ms). 측정값이 없으면 -1 */
    long latencyMs();

    @Override
    void close();
}
