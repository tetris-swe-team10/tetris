package com.team10.tetris.game;
import java.util.List;

/**
 * UI에 전달하는 읽기 전용 값. CLEARING에서는 압축 전 보드와 행 좌표를 유지한다.
 * cellTypes는 cells와 같은 좌표의 고정 블록 종류이며, 빈칸이나 종류를 모르는 칸은 null이다.
 */
public record GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
        PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
        int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows,
        List<List<TetrominoType>> cellTypes) {
    public GameSnapshot {
        cells = cells.stream().map(List::copyOf).toList();
        clearingRows = List.copyOf(clearingRows);
        // null을 담을 수 있도록 List.copyOf 대신 Stream.toList()로 복사
        cellTypes = cellTypes.stream().map(row -> row.stream().toList()).toList();
    }
}
