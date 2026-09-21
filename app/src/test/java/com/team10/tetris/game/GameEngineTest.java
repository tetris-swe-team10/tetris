package com.team10.tetris.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTest {

    @Test
    void movesBlockLeft() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(true, engine.moveLeft());
        assertEquals(3, block.getCol());
    }

    @Test
    void doesNotMoveBlockOutsideLeftWall() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 0);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveLeft());
        assertEquals(0, block.getCol());
    }

    @Test
    void movesBlockRight() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(true, engine.moveRight());
        assertEquals(5, block.getCol());
    }

    @Test
    void doesNotMoveBlockOutsideRightWall() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 7);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveRight());
        assertEquals(7, block.getCol());
    }

    @Test
    void movesBlockDown() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(true, engine.moveDown());
        assertEquals(1, block.getRow());
    }

    @Test
    void doesNotMoveBlockBelowBottom() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 18, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveDown());
        assertEquals(18, block.getRow());
    }

    @Test
    void doesNotMoveDownWhenAnotherBlockIsBelow() {
        Board board = new Board();

        // T 블록 바로 아래에 이미 쌓인 블록이 있다고 가정
        board.setCell(2, 5, 1);

        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveDown());
        assertEquals(0, block.getRow());
    }

    @Test
    void rotatesBlockClockwise() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(true, engine.rotateClockwise());
        assertEquals(1, block.getRotation());
    }

    @Test
    void doesNotRotateWhenRotationCausesCollision() {
        Board board = new Board();

        // 회전된 T 블록과 겹치게 기존 블록 배치
        board.setCell(2, 5, 1);

        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.rotateClockwise());

        // 회전이 취소되어 원래 상태로 돌아왔는지 확인
        assertEquals(0, block.getRotation());
    }

    @Test
    void locksBlockWhenItCannotMoveDown() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 18, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveDown());

        assertEquals(1, board.getCell(18, 4));
        assertEquals(1, board.getCell(18, 5));
        assertEquals(1, board.getCell(18, 6));
        assertEquals(1, board.getCell(19, 5));
    }

    @Test
    void clearsLineAfterBlockIsLocked() {
        Board board = new Board();

        // 맨 아래 줄의 4번 칸만 제외하고 모두 채움
        for (int col = 0; col < Board.WIDTH; col++) {
            if (col != 4) {
                board.setCell(19, col, 1);
            }
        }

        // I 블록을 세로로 세워 4번 칸을 채움
        Tetromino block = new Tetromino(TetrominoType.I, 16, 4);
        block.rotateClockwise();

        GameEngine engine = new GameEngine(board, block);

        assertEquals(false, engine.moveDown());

        // 줄이 삭제됐는지 확인
        assertEquals(0, board.getCell(19, 0));
    }

    @Test
    void spawnsNewBlock() {
        Board board = new Board();
        Tetromino initialBlock = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, initialBlock);

        assertEquals(true, engine.spawnBlock());

        Tetromino spawnedBlock = engine.getCurrentBlock();

        assertEquals(0, spawnedBlock.getRow());
        assertEquals(3, spawnedBlock.getCol());
    }

    @Test
    void spawnedBlockHasValidType() {
        Board board = new Board();
        Tetromino initialBlock = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, initialBlock);

        engine.spawnBlock();

        TetrominoType type = engine.getCurrentBlock().getType();

        boolean validType = false;

        for (TetrominoType value : TetrominoType.values()) {
            if (value == type) {
                validType = true;
                break;
            }
        }

        assertEquals(true, validType);
    }

    @Test
    void gameOverWhenNewBlockCannotSpawn() {
        Board board = new Board();

        // 새 블록 생성 위치를 막되, 완성된 줄이 되지 않도록 일부 칸만 채움
        for (int col = 3; col <= 6; col++) {
            board.setCell(0, col, 1);
            board.setCell(1, col, 1);
        }

        Tetromino block = new Tetromino(TetrominoType.T, 18, 4);
        GameEngine engine = new GameEngine(board, block);

        // 현재 블록 고정 → 다음 블록 생성 시도
        engine.moveDown();

        assertEquals(true, engine.isGameOver());
    }

    @Test
    void hardDropsBlockToBottom() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        engine.hardDrop();

        // T 블록이 바닥까지 내려가 고정되었는지 확인
        assertEquals(1, board.getCell(18, 4));
        assertEquals(1, board.getCell(18, 5));
        assertEquals(1, board.getCell(18, 6));
        assertEquals(1, board.getCell(19, 5));
    }

    @Test
    void movesBlockDownOnTick() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        engine.tick();

        assertEquals(1, block.getRow());

        engine.tick();

        assertEquals(2, block.getRow());
    }

    @Test
    void updatesNextBlockAfterSpawn() {
        Board board = new Board();
        Tetromino initialBlock = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, initialBlock);

        Tetromino nextBeforeSpawn = engine.getNextBlock();

        assertEquals(true, engine.spawnBlock());

        assertEquals(nextBeforeSpawn, engine.getCurrentBlock());
    }

    @Test
    void createsNewNextBlockAfterSpawn() {
        Board board = new Board();
        Tetromino initialBlock = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, initialBlock);

        Tetromino nextBeforeSpawn = engine.getNextBlock();

        engine.spawnBlock();

        Tetromino nextAfterSpawn = engine.getNextBlock();

        assertEquals(false, nextBeforeSpawn == nextAfterSpawn);
    }

    @Test
    void doesNotMoveWhilePaused() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        engine.pause();
        engine.tick();

        assertEquals(true, engine.isPaused());
        assertEquals(0, block.getRow());
    }

    @Test
    void movesAgainAfterResume() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        engine.pause();
        engine.tick();

        engine.resume();
        engine.tick();

        assertEquals(false, engine.isPaused());
        assertEquals(1, block.getRow());
    }

    @Test
    void increasesDropSpeed() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        assertEquals(1000, engine.getDropIntervalMs());

        engine.increaseSpeed();

        assertEquals(900, engine.getDropIntervalMs());
    }

    @Test
    void dropSpeedDoesNotGoBelowMinimum() {
        Board board = new Board();
        Tetromino block = new Tetromino(TetrominoType.T, 0, 4);
        GameEngine engine = new GameEngine(board, block);

        for (int i = 0; i < 20; i++) {
            engine.increaseSpeed();
        }

        assertEquals(200, engine.getDropIntervalMs());
    }

    @Test
    void countsClearedLines() {
        Board board = new Board();

        // 맨 아래 줄에서 4번 칸만 비워둠
        for (int col = 0; col < Board.WIDTH; col++) {
            if (col != 4) {
                board.setCell(19, col, 1);
            }
        }

        // 세로 I 블록으로 마지막 한 칸을 채움
        Tetromino block = new Tetromino(TetrominoType.I, 16, 4);
        block.rotateClockwise();

        GameEngine engine = new GameEngine(board, block);

        engine.moveDown();

        assertEquals(1, engine.getTotalClearedLines());
    }

    @Test
    void increasesSpeedAfterFiveClearedLines() {
        Board board = new Board();
        Tetromino initialBlock = new Tetromino(TetrominoType.I, 19, 3);
        GameEngine engine = new GameEngine(board, initialBlock);

        for (int i = 0; i < 5; i++) {

            // 맨 아래 줄에서 I 블록이 들어갈 4칸만 비워둠
            for (int col = 0; col < Board.WIDTH; col++) {
                if (col < 3 || col > 6) {
                    board.setCell(19, col, 1);
                }
            }

            // 가로 I 블록으로 남은 4칸을 채움
            Tetromino block = new Tetromino(TetrominoType.I, 19, 3);
            engine.setCurrentBlock(block);

            // 아래로 갈 수 없으므로 lock → 한 줄 삭제
            engine.moveDown();
        }

        assertEquals(5, engine.getTotalClearedLines());
        assertEquals(900, engine.getDropIntervalMs());
    }

}