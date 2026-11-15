package com.team10.tetris.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ConnectionChannel} 계약을 따르는 테스트용 가짜 채널. 소켓 없이 한 스레드에서 동작한다.
 *
 * <p>{@link #receiveFromPeer}는 상대가 보낸 메시지가 도착한 상황을 흉내 낸다. 실제 네트워크처럼
 * 직렬화 후 역직렬화한 사본을 전달하고, JavaFX 스레드처럼 한 번에 하나씩 순서대로 처리한다.
 * 역할 C는 진짜 연결이 준비되기 전에 이 클래스로 게임 중 송수신을 개발·테스트할 수 있다.
 */
public final class FakeConnectionChannel implements ConnectionChannel {
    private final Deque<NetMessage> inbox = new ArrayDeque<>();
    private final List<NetMessage> sent = new ArrayList<>();
    private Consumer<NetMessage> onMessage;
    private Consumer<ConnectionState> onStateChanged = state -> { };
    private ConnectionState state = ConnectionState.CONNECTED;
    private boolean dispatching;

    /** 상대가 보낸 메시지 도착. 핸들러가 없으면 보관했다가 다음 핸들러에 전달한다. */
    public void receiveFromPeer(NetMessage message) {
        inbox.add(roundTrip(message));
        drain();
    }

    public List<NetMessage> sentMessages() {
        return List.copyOf(sent);
    }

    public void changeState(ConnectionState newState) {
        state = newState;
        onStateChanged.accept(newState);
    }

    @Override
    public void send(NetMessage message) {
        if (state != ConnectionState.DISCONNECTED) sent.add(roundTrip(message));
    }

    @Override
    public void setOnMessage(Consumer<NetMessage> handler) {
        onMessage = handler;
        drain();
    }

    @Override
    public void setOnStateChanged(Consumer<ConnectionState> handler) {
        onStateChanged = handler == null ? s -> { } : handler;
    }

    @Override
    public ConnectionState state() {
        return state;
    }

    @Override
    public long latencyMs() {
        return 0;
    }

    @Override
    public void close() {
        changeState(ConnectionState.DISCONNECTED);
    }

    /** 실행 중인 핸들러가 끝난 뒤 다음 메시지를 그때의 핸들러로 전달한다. */
    private void drain() {
        if (dispatching) return;
        dispatching = true;
        try {
            while (onMessage != null && !inbox.isEmpty()) {
                onMessage.accept(inbox.poll());
            }
        } finally {
            dispatching = false;
        }
    }

    static NetMessage roundTrip(NetMessage message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(message);
            }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return (NetMessage) in.readObject();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
