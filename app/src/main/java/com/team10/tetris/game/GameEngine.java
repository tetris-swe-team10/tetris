package com.team10.tetris.game;

public class GameEngine {

    private final Board board;
    private Tetromino currentBlock;
    private boolean gameOver = false;
    private Tetromino nextBlock;
    private boolean paused = false;
    private int dropIntervalMs = 1000;
    private int totalClearedLines = 0;

    public GameEngine(Board board, Tetromino currentBlock) {
        this.board = board;
        this.currentBlock = currentBlock;
        this.nextBlock = createRandomTetromino();
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrentBlock() {
        return currentBlock;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Tetromino getNextBlock() {
        return nextBlock;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getDropIntervalMs() {
        return dropIntervalMs;
    }

    public int getTotalClearedLines() {
        return totalClearedLines;
    }

    public boolean moveLeft() {
        currentBlock.move(0, -1);

        if (!board.canPlace(currentBlock)) {
            currentBlock.move(0, 1);
            return false;
        }

        return true;
    }

    public boolean moveRight() {
        currentBlock.move(0, 1);

        if (!board.canPlace(currentBlock)) {
            currentBlock.move(0, -1);
            return false;
        }

        return true;
    }

    public boolean moveDown() {
        currentBlock.moveDown();

        if (!board.canPlace(currentBlock)) {
            // 이동 실패 → 원래 위치로 복구
            currentBlock.move(-1, 0);

            // 현재 위치에 블록 고정
            board.lock(currentBlock);

            // 라인 삭제 및 누적
            int clearedLines = board.clearLines();
            totalClearedLines += clearedLines;

            if (clearedLines > 0 && totalClearedLines % 5 == 0) {
                increaseSpeed();
            }

            // 다음 블록 생성
            if (!spawnBlock()) {
                gameOver = true;
            }

            return false;
        }

        return true;
    }

    public boolean rotateClockwise() {
        currentBlock.rotateClockwise();

        if (!board.canPlace(currentBlock)) {
            currentBlock.rotateCounterClockwise();
            return false;
        }

        return true;
    }

    public Tetromino createRandomTetromino() {
        TetrominoType[] types = TetrominoType.values();

        int randomIndex = (int) (Math.random() * types.length);
        TetrominoType randomType = types[randomIndex];

        return new Tetromino(randomType, 0, 3);
    }

    public boolean spawnBlock() {
        Tetromino newBlock = nextBlock;

        if (!board.canPlace(newBlock)) {
            return false;
        }

        currentBlock = newBlock;
        nextBlock = createRandomTetromino();

        return true;
    }

    public void hardDrop() {
        while (moveDown()) {
            // 더 이상 내려갈 수 없을 때까지 반복
        }
    }

    public void tick() {
        if (gameOver || paused) {
            return;
        }

        moveDown();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void increaseSpeed() {
        if (dropIntervalMs > 200) {
            dropIntervalMs -= 100;
        }
    }

    public void setCurrentBlock(Tetromino block) {
        this.currentBlock = block;
    }

}