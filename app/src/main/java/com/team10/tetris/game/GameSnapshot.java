package com.team10.tetris.game;
import java.util.List;

import com.team10.tetris.game.versus.AttackLines;

/**
 * UI에 전달하는 읽기 전용 값. CLEARING에서는 압축 전 보드와 행 좌표를 유지한다.
 * cellTypes는 cells와 같은 좌표의 고정 블록 종류이며, 빈칸이나 종류를 모르는 칸은 null이다.
 *
 * <p>대전용(3차): garbageCells는 cells와 같은 좌표에서 상대에게 받은 회색 줄 칸이면 true다.
 * 대전이 아니면 빈 목록이다. pendingAttack은 아직 보드에 들어오지 않은 대기 공격 줄이다.
 */
public record GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
        PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
        int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows,
        List<List<TetrominoType>> cellTypes, boolean blackoutActive, boolean rotationLocked,
        List<CellPosition> effectCells, boolean effectFlashVisible,
        List<List<Boolean>> garbageCells, AttackLines pendingAttack) {
    /** 대전 정보가 없는 기존 호출용. 회색 칸 없음, 대기 공격 없음. */
    public GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
            PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
            int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows,
            List<List<TetrominoType>> cellTypes, boolean blackoutActive, boolean rotationLocked,
            List<CellPosition> effectCells, boolean effectFlashVisible) {
        this(cells, currentBlock, nextBlock, config, state, score, totalClearedLines,
                dropIntervalMs, clearingRows, cellTypes, blackoutActive, rotationLocked,
                effectCells, effectFlashVisible, List.of(), AttackLines.EMPTY);
    }

    /** 회색(공격 받은) 칸이면 true. 대전이 아니거나 정보가 없으면 false. */
    public boolean isGarbage(int row, int col) {
        return !garbageCells.isEmpty() && garbageCells.get(row).get(col);
    }

    /** 읽기 전용 보드로 착지 위치만 예측한다. 점수·아이템·게임 상태를 바꾸지 않는다. */
    public PieceSnapshot ghostBlock() {
        if (blackoutActive || currentBlock == null
                || (state != GameState.PLAYING && state != GameState.PAUSED)
                || !canPlaceGhost(currentBlock.row())) return null;
        int row = currentBlock.row();
        while (canPlaceGhost(row + 1)) row++;
        return new PieceSnapshot(currentBlock.type(), row, currentBlock.col(), currentBlock.shape());
    }

    private boolean canPlaceGhost(int originRow) {
        for (int r = 0; r < currentBlock.shape().size(); r++) {
            for (int c = 0; c < currentBlock.shape().get(r).size(); c++) {
                if (currentBlock.shape().get(r).get(c) == 0) continue;
                int row = originRow + r, col = currentBlock.col() + c;
                if (row < 0 || row >= Board.HEIGHT || col < 0 || col >= Board.WIDTH
                        || cells.get(row).get(col) != 0) return false;
            }
        }
        return true;
    }

    public GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
            PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
            int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows,
            List<List<TetrominoType>> cellTypes, boolean blackoutActive, boolean rotationLocked) {
        this(cells, currentBlock, nextBlock, config, state, score, totalClearedLines,
                dropIntervalMs, clearingRows, cellTypes, blackoutActive, rotationLocked, List.of(), false);
    }
    public GameSnapshot(List<List<Integer>> cells, PieceSnapshot currentBlock,
            PieceSnapshot nextBlock, GameConfig config, GameState state, int score,
            int totalClearedLines, int dropIntervalMs, List<Integer> clearingRows,
            List<List<TetrominoType>> cellTypes) {
        this(cells, currentBlock, nextBlock, config, state, score, totalClearedLines,
                dropIntervalMs, clearingRows, cellTypes, false, false);
    }
    public GameSnapshot {
        cells = cells.stream().map(List::copyOf).toList();
        clearingRows = List.copyOf(clearingRows);
        effectCells = List.copyOf(effectCells);
        garbageCells = garbageCells == null ? List.of() : garbageCells.stream().map(List::copyOf).toList();
        pendingAttack = pendingAttack == null ? AttackLines.EMPTY : pendingAttack;
        // null을 담을 수 있도록 List.copyOf 대신 Stream.toList()로 복사
        cellTypes = cellTypes.stream().map(row -> row.stream().toList()).toList();
    }
}
