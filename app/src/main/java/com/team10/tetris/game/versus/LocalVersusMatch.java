package com.team10.tetris.game.versus;

import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import com.team10.tetris.game.*;

/** 한 스레드에서 두 엔진의 시간을 같은 1ms 단위로 진행한다. */
public final class LocalVersusMatch implements VersusMatch {
    private final VersusConfig config;
    private final GameEngine[] engines;
    private final GameController[] controllers;
    private long remaining;
    private boolean paused;
    private VersusResult result;
    private record Delivery(int receiver, AttackLines lines) {}
    private final List<Delivery> deliveries = new ArrayList<>();

    public LocalVersusMatch(VersusConfig config) {
        this(config, engine(config), engine(config));
    }
    private static GameEngine engine(VersusConfig config) {
        return new GameEngine(new GameConfig(
                config.rule() == VersusRule.ITEM ? GameMode.ITEM : GameMode.NORMAL, config.difficulty()));
    }
    public LocalVersusMatch(VersusConfig config, GameEngine first, GameEngine second) {
        this.config = Objects.requireNonNull(config);
        engines = new GameEngine[]{Objects.requireNonNull(first), Objects.requireNonNull(second)};
        if (first == second) throw new IllegalArgumentException("Two engines required");
        controllers = new GameController[]{new GameController(first), new GameController(second)};
        first.setAttackListener(lines -> deliveries.add(new Delivery(1, lines)));
        second.setAttackListener(lines -> deliveries.add(new Delivery(0, lines)));
        remaining = config.rule() == VersusRule.TIME_LIMIT ? config.timeLimitSeconds() * 1000L : -1;
        checkResult();
    }
    public VersusConfig config() { return config; }
    public void handle(PlayerSide player, GameCommand command) {
        if (result != null || paused) return;
        controllers[player.ordinal()].handle(command);
        deliverAttacks();
        checkResult();
    }
    public void advance(int elapsedMs) {
        if (elapsedMs < 0) throw new IllegalArgumentException("Negative elapsed time");
        for (int i = 0; i < elapsedMs && result == null && !paused; i++) {
            controllers[0].advance(1);
            controllers[1].advance(1);
            deliverAttacks();
            if (remaining > 0) remaining--;
            checkResult();
        }
    }
    private void deliverAttacks() {
        // 같은 시각의 공격은 양쪽 시간 진행 후 전달해 엔진 호출 순서에 따른 유불리를 없앤다.
        for (Delivery delivery : deliveries) engines[delivery.receiver()].receiveAttack(delivery.lines());
        deliveries.clear();
    }
    private void checkResult() {
        boolean first = engines[0].isGameOver(), second = engines[1].isGameOver();
        if (first || second) result = new VersusResult(first == second ? null
                : first ? PlayerSide.PLAYER2 : PlayerSide.PLAYER1, VersusResult.Reason.TOPPED_OUT);
        else if (remaining == 0) {
            int comparison = Integer.compare(engines[0].getScore(), engines[1].getScore());
            result = new VersusResult(comparison == 0 ? null : comparison > 0
                    ? PlayerSide.PLAYER1 : PlayerSide.PLAYER2, VersusResult.Reason.TIME_UP);
        }
    }
    public GameSnapshot snapshot(PlayerSide player) { return engines[player.ordinal()].snapshot(); }
    public long remainingTimeMs() { return remaining; }
    public Optional<VersusResult> result() { return Optional.ofNullable(result); }
    public void pause() {
        if (result != null || paused) return;
        paused = true;
        for (GameController controller : controllers) controller.pause();
    }
    public void resume() {
        if (!paused) return;
        paused = false;
        for (GameController controller : controllers) controller.resume();
    }
}
