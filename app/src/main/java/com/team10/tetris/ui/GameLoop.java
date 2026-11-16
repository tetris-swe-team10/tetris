package com.team10.tetris.ui;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * 화면용 게임 루프. 일정 간격마다 직전 프레임 이후 경과 시간(ms)을 {@code onFrame}에 넘긴다.
 *
 * <p>1인 게임·P2P 게임 화면이 같이 쓴다. 화면은 {@code onFrame} 안에서
 * 엔진 진행 → 다시 그리기(→ P2P면 스냅샷 전송)를 한다.
 * 시간 계산은 {@link #frame()}에 모여 있어 JavaFX 타이머 없이 가짜 시계로 테스트할 수 있다.
 */
public final class GameLoop {

    private final int stepMs;
    private final IntConsumer onFrame;
    private final LongSupplier nanoTime;
    private Timeline timeline;
    private long lastNanos;

    public GameLoop(int stepMs, IntConsumer onFrame) {
        this(stepMs, onFrame, System::nanoTime);
    }

    GameLoop(int stepMs, IntConsumer onFrame, LongSupplier nanoTime) {
        if (stepMs <= 0) throw new IllegalArgumentException("stepMs must be positive");
        this.stepMs = stepMs;
        this.onFrame = Objects.requireNonNull(onFrame);
        this.nanoTime = Objects.requireNonNull(nanoTime);
        this.lastNanos = nanoTime.getAsLong();
    }

    /** 시계를 지금으로 맞춘 뒤 타이머를 돌린다. 이미 돌고 있으면 시계만 맞춘다. */
    public void start() {
        resetClock();
        timeline().play();
    }

    public void stop() {
        if (timeline != null) timeline.stop();
    }

    /** 멈춰 있던 시간(설정 화면, 결과 창 등)이 다음 프레임에 한꺼번에 흐르지 않게 기준 시각을 지금으로 맞춘다. */
    public void resetClock() {
        lastNanos = nanoTime.getAsLong();
    }

    /** 한 프레임: 경과 시간을 계산해 {@code onFrame}에 넘긴다. */
    void frame() {
        long now = nanoTime.getAsLong();
        int elapsed = (int) Math.min(Integer.MAX_VALUE, (now - lastNanos) / 1_000_000L);
        lastNanos = now;
        onFrame.accept(elapsed);
    }

    private Timeline timeline() {
        if (timeline == null) {
            timeline = new Timeline(new KeyFrame(Duration.millis(stepMs), event -> frame()));
            timeline.setCycleCount(Timeline.INDEFINITE);
        }
        return timeline;
    }
}
