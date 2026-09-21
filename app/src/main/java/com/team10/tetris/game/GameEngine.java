package com.team10.tetris.game;

public class GameEngine {

    private final Board board;
    private Tetromino currentBlock;
    private boolean gameOver = false;

    public GameEngine(Board board, Tetromino currentBlock) {
        this.board = board;
        this.currentBlock = currentBlock;
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
            currentBlock.move(-1, 0);
            board.lock(currentBlock);
            board.clearLines();
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
        Tetromino newBlock = createRandomTetromino();

        if (!board.canPlace(newBlock)) {
            return false;
        }

        currentBlock = newBlock;
        return true;
    }

    public void hardDrop() {
        while (moveDown()) {
            // 더 이상 내려갈 수 없을 때까지 반복
        }
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        moveDown();
    }

}