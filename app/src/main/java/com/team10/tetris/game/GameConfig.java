package com.team10.tetris.game;
import java.util.Objects;

/** 게임 시작 시 확정하며 한 판 동안 변경하지 않는다. */
public record GameConfig(GameMode mode, Difficulty difficulty) {
    public static final GameConfig DEFAULT = new GameConfig(GameMode.NORMAL, Difficulty.NORMAL);
    public GameConfig { Objects.requireNonNull(mode); Objects.requireNonNull(difficulty); }
}
