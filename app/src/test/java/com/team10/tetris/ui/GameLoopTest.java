package com.team10.tetris.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/** JavaFX 타이머를 쓰지 않고 가짜 시계로 경과 시간 계산만 검증한다. */
class GameLoopTest {

    private final AtomicLong clock = new AtomicLong(1_000_000_000L);
    private final List<Integer> frames = new ArrayList<>();
    private final GameLoop loop = new GameLoop(50, frames::add, clock::get);

    private void advanceMs(long ms) {
        clock.addAndGet(ms * 1_000_000L);
    }

    @Test void passesElapsedMillisSincePreviousFrame() {
        advanceMs(50);
        loop.frame();
        advanceMs(70);
        loop.frame();
        assertEquals(List.of(50, 70), frames);
    }

    @Test void resetClockDropsTimeSpentPaused() {
        advanceMs(50);
        loop.frame();
        advanceMs(5_000); // 설정 화면에 머문 시간
        loop.resetClock();
        advanceMs(50);
        loop.frame();
        assertEquals(List.of(50, 50), frames);
    }

    @Test void subMillisecondRemainderIsNotCarried() {
        clock.addAndGet(1_500_000L); // 1.5ms
        loop.frame();
        clock.addAndGet(1_500_000L);
        loop.frame();
        assertEquals(List.of(1, 1), frames, "기존 GameView와 같이 프레임마다 ms 미만은 버린다");
    }

    @Test void rejectsNonPositiveStep() {
        assertThrows(IllegalArgumentException.class, () -> new GameLoop(0, ms -> { }));
    }
}
