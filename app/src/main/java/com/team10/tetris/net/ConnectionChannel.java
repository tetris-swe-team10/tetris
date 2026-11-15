package com.team10.tetris.net;

import java.util.function.Consumer;

/**
 * 연결된 상대와 메시지를 주고받는 통로. (구현: 역할 B, 사용: 역할 B·C)
 *
 * <p>콜백은 JavaFX 애플리케이션 스레드에서 호출한다(구현 쪽에서 {@code Platform.runLater} 처리).
 * 역할 C는 진짜 소켓 대신 이 인터페이스의 가짜 구현으로 먼저 개발·테스트할 수 있다.
 */
public interface ConnectionChannel extends AutoCloseable {

    /** 메시지를 보낸다. 연결이 끊겼으면 무시한다. */
    void send(NetMessage message);

    /** 메시지를 받으면 호출할 대상. 여러 번 등록하면 마지막 것만 유지한다. */
    void setOnMessage(Consumer<NetMessage> handler);

    /** 연결 상태가 바뀌면 호출할 대상. */
    void setOnStateChanged(Consumer<ConnectionState> handler);

    ConnectionState state();

    /** 최근 왕복 지연(ms). 측정값이 없으면 -1 */
    long latencyMs();

    @Override
    void close();
}
