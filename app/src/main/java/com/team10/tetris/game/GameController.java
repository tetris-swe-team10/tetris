package com.team10.tetris.game;

/** 입력과 시간 진행을 조정한다. JavaFX에 의존하지 않아 자동 테스트할 수 있다. */
public final class GameController {
    private final GameEngine engine;
    private long fallElapsedMs;
    public GameController(GameEngine engine) { this.engine = engine; }
    public void handle(GameCommand command) {
        if (engine.getState() != GameState.PLAYING) return;
        Tetromino previous = engine.getCurrentBlock();
        switch (command) {
            case LEFT -> engine.moveLeft();
            case RIGHT -> engine.moveRight();
            case DOWN -> engine.moveDown();
            case ROTATE -> engine.rotateClockwise();
            case HARD_DROP -> { engine.hardDrop(); fallElapsedMs = 0; }
        }
        if (engine.getState() != GameState.PLAYING || previous != engine.getCurrentBlock()) fallElapsedMs = 0;
    }
    public void advance(int elapsedMs) {
        if (elapsedMs < 0) throw new IllegalArgumentException("Negative elapsed time");
        int remaining = elapsedMs;
        while (remaining > 0) {
            if (engine.getState() == GameState.ITEM_EFFECT) {
                int step = Math.min(remaining, engine.getItemEffectRemainingMs());
                engine.advanceItemEffect(step);
                fallElapsedMs = 0;
                remaining -= step;
            } else if (engine.getState() == GameState.CLEARING) {
                int step = Math.min(remaining, engine.getClearRemainingMs());
                engine.advanceClear(step);
                fallElapsedMs = 0;
                remaining -= step;
            } else if (engine.getState() == GameState.PLAYING) {
                int step = (int) Math.min(remaining, Math.max(0, engine.getDropIntervalMs() - fallElapsedMs));
                fallElapsedMs += step;
                remaining -= step;
                if (fallElapsedMs >= engine.getDropIntervalMs()) {
                    fallElapsedMs = 0;
                    engine.tick();
                }
            } else break;
        }
    }

    public void pause() { engine.pause(); fallElapsedMs = 0; }
    public void resume() { engine.resume(); fallElapsedMs = 0; }
    public GameSnapshot snapshot() { return engine.snapshot(); }
}
