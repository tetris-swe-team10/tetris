package com.team10.tetris.game;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** 누적 가중치 구간을 선택하는 Roulette Wheel Selection. */
public final class WeightedPieceGenerator implements PieceGenerator {
    private final Difficulty difficulty;
    private final RandomGenerator random;
    private final int totalWeight;
    public WeightedPieceGenerator(Difficulty difficulty, RandomGenerator random) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.random = Objects.requireNonNull(random);
        int sum = 0;
        for (TetrominoType type : TetrominoType.values()) sum += difficulty.weight(type);
        totalWeight = sum;
    }
    @Override public Tetromino next() {
        int ticket = random.nextInt(totalWeight);
        for (TetrominoType type : TetrominoType.values()) {
            ticket -= difficulty.weight(type);
            if (ticket < 0) return new Tetromino(type, 0, 3);
        }
        throw new IllegalStateException("Invalid random ticket");
    }
}
