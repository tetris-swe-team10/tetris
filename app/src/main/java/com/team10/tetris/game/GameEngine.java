package com.team10.tetris.game;

public class GameEngine {

    private static final int INITIAL_DROP_INTERVAL_MS = 1000;
    private static final int MIN_DROP_INTERVAL_MS = 200;
    private static final int SPEED_STEP_MS = 100;
    private static final int LINES_PER_SPEED_UP = 5;

    private final Board board;

    private Tetromino currentBlock;
    private Tetromino nextBlock;

    private boolean gameOver = false;
    private boolean paused = false;

    private int dropIntervalMs = INITIAL_DROP_INTERVAL_MS;
    private int totalClearedLines = 0;
    private int lastClearedLines = 0;

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

    public Tetromino getNextBlock() {
        return nextBlock;
    }

    public boolean isGameOver() {
        return gameOver;
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

    public int getLastClearedLines() {
        return lastClearedLines;
    }

    private boolean cannotControl() {
        return paused || gameOver;
    }

    public boolean moveLeft() {
        if (cannotControl()) {
            return false;
        }

        currentBlock.move(0, -1);

        if (!board.canPlace(currentBlock)) {
            currentBlock.move(0, 1);
            return false;
        }

        return true;
    }

    public boolean moveRight() {
        if (cannotControl()) {
            return false;
        }

        currentBlock.move(0, 1);

        if (!board.canPlace(currentBlock)) {
            currentBlock.move(0, -1);
            return false;
        }

        return true;
    }

    public boolean moveDown() {
        if (cannotControl()) {
            return false;
        }

        currentBlock.moveDown();

        if (board.canPlace(currentBlock)) {
            return true;
        }

        // 이동 실패 시 원래 위치로 복구
        currentBlock.move(-1, 0);

        // 블록 고정 및 라인 삭제
        board.lock(currentBlock);

        int previousTotal = totalClearedLines;

        lastClearedLines = board.clearLines();
        totalClearedLines += lastClearedLines;

        // 여러 줄을 한 번에 삭제해도 5줄 단위 속도 증가 적용
        int previousLevel = previousTotal / LINES_PER_SPEED_UP;
        int currentLevel = totalClearedLines / LINES_PER_SPEED_UP;

        for (int i = previousLevel; i < currentLevel; i++) {
            increaseSpeed();
        }

        if (!spawnBlock()) {
            gameOver = true;
        }

        return false;
    }

    public boolean rotateClockwise() {
        if (cannotControl()) {
            return false;
        }

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

    public int hardDrop() {
        if (cannotControl()) {
            return 0;
        }

        int droppedRows = 0;

        while (moveDown()) {
            droppedRows++;
        }

        return droppedRows;
    }

    public void tick() {
        if (!cannotControl()) {
            moveDown();
        }
    }

    public void pause() {
        if (!gameOver) {
            paused = true;
        }
    }

    public void resume() {
        if (!gameOver) {
            paused = false;
        }
    }

    public void increaseSpeed() {
        dropIntervalMs = Math.max(
                MIN_DROP_INTERVAL_MS,
                dropIntervalMs - SPEED_STEP_MS
        );
    }

    // 기존 테스트에서 사용하는 메서드
    public void setCurrentBlock(Tetromino block) {
        this.currentBlock = block;
    }
}