package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotCellTypeTest {

    private GameEngine engine(Board board, Tetromino block) {
        return new GameEngine(board, block, GameConfig.DEFAULT,
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
    }

    /** 맨 아래 줄은 세로 I가 떨어지면 꽉 차고, 그 위에 Z가 쌓여 있는 보드. */
    private GameEngine landingEngine() {
        Board board = new Board();
        board.lock(new Tetromino(TetrominoType.I, 19, 0));   // 19: 0~3
        board.lock(new Tetromino(TetrominoType.I, 19, 5));   // 19: 5~8
        board.setCell(19, 9, 1);                             // 종류를 모르는 칸
        board.lock(new Tetromino(TetrominoType.Z, 17, 0));   // 17: 0~1, 18: 1~2

        Tetromino block = new Tetromino(TetrominoType.I, 16, 4);
        block.rotateClockwise();                             // 16~19: 4

        return engine(board, block);
    }

    @Test
    void snapshotContainsLockedBlockTypes() {
        GameEngine game = engine(new Board(), new Tetromino(TetrominoType.T, 0, 3));

        game.hardDrop();

        List<List<TetrominoType>> types = game.snapshot().cellTypes();

        assertEquals(TetrominoType.T, types.get(18).get(3));
        assertEquals(TetrominoType.T, types.get(18).get(5));
        assertEquals(TetrominoType.T, types.get(19).get(4));
        assertNull(types.get(19).get(3));
        assertNull(types.get(0).get(0));
    }

    @Test
    void snapshotHasSameSizeAsBoard() {
        GameSnapshot snapshot = engine(new Board(), new Tetromino(TetrominoType.T, 0, 3)).snapshot();

        assertEquals(Board.HEIGHT, snapshot.cellTypes().size());
        for (List<TetrominoType> row : snapshot.cellTypes()) {
            assertEquals(Board.WIDTH, row.size());
        }
    }

    @Test
    void typesStayAtOriginalRowsWhileClearing() {
        GameEngine game = landingEngine();

        game.moveDown();

        assertEquals(GameState.CLEARING, game.getState());
        assertEquals(List.of(19), game.snapshot().clearingRows());

        // 삭제 연출 중에는 압축 전 위치 그대로
        List<List<TetrominoType>> types = game.snapshot().cellTypes();
        assertEquals(TetrominoType.I, types.get(19).get(0));
        assertEquals(TetrominoType.I, types.get(19).get(4));
        assertNull(types.get(19).get(9));
        assertEquals(TetrominoType.Z, types.get(17).get(0));
        assertEquals(TetrominoType.Z, types.get(18).get(2));
    }

    @Test
    void typesMoveDownAfterClearing() {
        GameEngine game = landingEngine();

        game.moveDown();
        game.advanceClear(GameEngine.LINE_CLEAR_DURATION_MS);

        assertEquals(GameState.PLAYING, game.getState());

        List<List<TetrominoType>> types = game.snapshot().cellTypes();

        // Z가 한 줄 내려옴
        assertEquals(TetrominoType.Z, types.get(18).get(0));
        assertEquals(TetrominoType.Z, types.get(18).get(1));
        assertEquals(TetrominoType.Z, types.get(19).get(1));
        assertEquals(TetrominoType.Z, types.get(19).get(2));

        // 세로 I의 남은 세 칸도 한 줄 내려옴
        assertEquals(TetrominoType.I, types.get(17).get(4));
        assertEquals(TetrominoType.I, types.get(19).get(4));
        assertNull(types.get(16).get(4));

        // 지워진 줄의 I는 남지 않음
        assertNull(types.get(19).get(0));
        assertNull(types.get(19).get(8));
    }

    @Test
    void snapshotCellTypesAreReadOnly() {
        GameSnapshot snapshot = engine(new Board(), new Tetromino(TetrominoType.T, 0, 3)).snapshot();

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.cellTypes().get(0).set(0, TetrominoType.I));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.cellTypes().set(0, List.of()));
    }
}
