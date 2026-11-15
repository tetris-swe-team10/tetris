package com.team10.tetris.net;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameEngine;
import com.team10.tetris.game.GameMode;
import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.versus.AttackLines;
import com.team10.tetris.game.versus.VersusConfig;
import com.team10.tetris.game.versus.VersusRule;

/** PR #56 리뷰 반영: 보드 상태 전송 형식과 대기실 → 게임 핸들러 전환 순서 계약 검증. */
class P2pContractTest {

    /** 테스트용 대기실 메시지. 실제 대기실 메시지는 역할 B가 정의한다. */
    private record StartGame(VersusConfig config) implements NetMessage { }

    private static GameSnapshot playedSnapshot(GameMode mode) {
        GameEngine engine = new GameEngine(new GameConfig(mode, Difficulty.NORMAL));
        engine.moveLeft();
        engine.hardDrop();
        engine.moveRight();
        return engine.snapshot();
    }

    @Test
    void boardStateMessageCarriesRealSnapshotWithoutConversion() {
        for (GameMode mode : GameMode.values()) {
            GameSnapshot snapshot = playedSnapshot(mode);
            var received = (BoardStateMessage) FakeConnectionChannel.roundTrip(new BoardStateMessage(7, snapshot));
            assertEquals(7, received.sequence());
            assertEquals(snapshot, received.snapshot());
            assertEquals(snapshot.ghostBlock(), received.snapshot().ghostBlock());
        }
    }

    @Test
    void attackMessageRoundTrips() {
        List<Boolean> row = new ArrayList<>(Collections.nCopies(Board.WIDTH, true));
        row.set(4, false);
        AttackLines lines = new AttackLines(List.of(row));
        assertEquals(new AttackMessage(lines), FakeConnectionChannel.roundTrip(new AttackMessage(lines)));
    }

    @Test
    void firstBoardStateRightAfterStartReachesGameHandler() {
        FakeConnectionChannel channel = new FakeConnectionChannel();
        List<NetMessage> lobbyReceived = new ArrayList<>();
        List<NetMessage> gameReceived = new ArrayList<>();
        P2pGameLauncher launcher = (ch, role, config, exit) -> ch.setOnMessage(gameReceived::add);

        channel.setOnMessage(message -> {
            lobbyReceived.add(message);
            if (message instanceof StartGame start) {
                launcher.start(channel, P2pRole.CLIENT, start.config(), new P2pGameLauncher.Exit() {
                    @Override public void returnToLobby() { }
                    @Override public void returnToP2pStart() { }
                });
            }
        });

        VersusConfig config = VersusConfig.of(VersusRule.NORMAL, Difficulty.NORMAL);
        channel.receiveFromPeer(new StartGame(config));
        channel.receiveFromPeer(new BoardStateMessage(1, playedSnapshot(GameMode.NORMAL)));

        assertEquals(List.of(new StartGame(config)), lobbyReceived);
        assertEquals(1, gameReceived.size());
        assertInstanceOf(BoardStateMessage.class, gameReceived.get(0));
    }

    @Test
    void messagesArrivingWithoutHandlerAreKeptInOrder() {
        FakeConnectionChannel channel = new FakeConnectionChannel();
        channel.receiveFromPeer(new BoardStateMessage(1, playedSnapshot(GameMode.NORMAL)));
        channel.receiveFromPeer(new BoardStateMessage(2, playedSnapshot(GameMode.NORMAL)));
        List<Long> sequences = new ArrayList<>();
        channel.setOnMessage(message -> sequences.add(((BoardStateMessage) message).sequence()));
        assertEquals(List.of(1L, 2L), sequences);
    }
}
