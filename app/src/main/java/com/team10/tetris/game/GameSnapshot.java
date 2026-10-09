package com.team10.tetris.game;
import java.util.List;

/** UI에 전달하는 읽기 전용 값. CLEARING에서는 압축 전 보드와 행 좌표를 유지한다. */
public record GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
        PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
        int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows) {
    public GameSnapshot {
        cells = cells.stream().map(List::copyOf).toList();
        clearingRows = List.copyOf(clearingRows);
    }
}
