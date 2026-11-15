package com.team10.tetris.game.versus;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameConfig;
import com.team10.tetris.game.GameEngine;

/** 대전 인터페이스 값 타입의 형식 규칙과, 껍데기 추가가 기존 1인 게임에 영향을 주지 않는지 확인한다. */
class VersusInterfaceTypesTest {

    private static List<Boolean> row(int hole) {
        List<Boolean> cells = new ArrayList<>(Collections.nCopies(Board.WIDTH, true));
        cells.set(hole, false);
        return cells;
    }

    @Test
    void attackLinesKeepsRowsInOrderAndRejectsWrongWidth() {
        AttackLines lines = new AttackLines(List.of(row(2), row(5)));
        assertEquals(2, lines.size());
        assertFalse(lines.rows().get(0).get(2));
        assertFalse(lines.rows().get(1).get(5));
        assertTrue(AttackLines.EMPTY.isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> new AttackLines(List.of(List.of(true, false))));
    }

    @Test
    void attackLinesIsImmutableCopy() {
        List<Boolean> source = row(0);
        AttackLines lines = new AttackLines(List.of(source));
        source.set(0, true);
        assertFalse(lines.rows().get(0).get(0));
        assertThrows(UnsupportedOperationException.class, () -> lines.rows().get(0).set(1, false));
    }

    @Test
    void versusConfigOnlyKeepsTimeLimitForTimeLimitRule() {
        assertEquals(VersusConfig.DEFAULT_TIME_LIMIT_SECONDS,
                VersusConfig.of(VersusRule.TIME_LIMIT, Difficulty.NORMAL).timeLimitSeconds());
        assertEquals(0, new VersusConfig(VersusRule.NORMAL, Difficulty.EASY, 90).timeLimitSeconds());
        assertThrows(IllegalArgumentException.class,
                () -> new VersusConfig(VersusRule.TIME_LIMIT, Difficulty.HARD, 0));
    }

    @Test
    void resultAndPlayerSideHelpers() {
        assertEquals(PlayerSide.PLAYER2, PlayerSide.PLAYER1.opponent());
        assertEquals(PlayerSide.PLAYER1, PlayerSide.PLAYER2.opponent());
        assertTrue(new VersusResult(null, VersusResult.Reason.TIME_UP).isDraw());
        assertFalse(new VersusResult(PlayerSide.PLAYER1, VersusResult.Reason.TOPPED_OUT).isDraw());
        assertThrows(NullPointerException.class, () -> new VersusResult(PlayerSide.PLAYER1, null));
    }

    @Test
    void networkValueTypesSurviveSerialization() throws Exception {
        AttackLines lines = new AttackLines(List.of(row(3)));
        VersusConfig config = VersusConfig.of(VersusRule.ITEM, Difficulty.NORMAL);
        assertEquals(lines, roundTrip(lines));
        assertEquals(config, roundTrip(config));
    }

    @Test
    void singlePlayerSnapshotHasNoVersusData() {
        GameEngine engine = new GameEngine(GameConfig.DEFAULT);
        engine.receiveAttack(new AttackLines(List.of(row(1))));
        var snapshot = engine.snapshot();
        assertTrue(snapshot.garbageCells().isEmpty());
        assertTrue(snapshot.pendingAttack().isEmpty());
        assertFalse(snapshot.isGarbage(Board.HEIGHT - 1, 0));
    }

    private static Object roundTrip(Object value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return in.readObject();
        }
    }
}
