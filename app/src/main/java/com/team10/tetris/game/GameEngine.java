package com.team10.tetris.game;

public class GameEngine {

    private final Board board;
    private Tetromino currentBlock;

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
}