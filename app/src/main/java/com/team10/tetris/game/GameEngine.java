package com.team10.tetris.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** 순수 Java 모델: 점수·난이도·착지·삭제 상태 전이를 관리한다. */
public class GameEngine {
    public static final int INITIAL_DROP_INTERVAL_MS = 1000;
    public static final int LINE_CLEAR_DURATION_MS = 300;
    private static final int MIN_DROP_INTERVAL_MS = 200;
    private static final int LINES_PER_SPEED_UP = 5;
    private final Board board;
    private final GameConfig config;
    private final PieceGenerator generator;
    private final ScoringPolicy scoring;
    private Tetromino currentBlock;
    private Tetromino nextBlock;
    private GameState state = GameState.PLAYING;
    private GameState stateBeforePause = GameState.PLAYING;
    private List<Integer> clearingRows = List.of();
    private int clearingElapsedMs;
    private int score;
    private int dropIntervalMs = INITIAL_DROP_INTERVAL_MS;
    private int totalClearedLines;
    private int lastClearedLines;

    public GameEngine(GameConfig config) {
        this(new Board(), null, config,
                new WeightedPieceGenerator(config.difficulty(), new Random()), new StandardScoringPolicy());
    }
    public GameEngine(Board board, Tetromino currentBlock) {
        this(board, currentBlock, GameConfig.DEFAULT,
                new WeightedPieceGenerator(Difficulty.NORMAL, new Random()), new StandardScoringPolicy());
    }
    public GameEngine(Board board, Tetromino currentBlock, GameConfig config,
            PieceGenerator generator, ScoringPolicy scoring) {
        this.board = Objects.requireNonNull(board);
        this.config = Objects.requireNonNull(config);
        this.generator = Objects.requireNonNull(generator);
        this.scoring = Objects.requireNonNull(scoring);
        this.currentBlock = currentBlock == null ? createRandomTetromino() : currentBlock;
        nextBlock = createRandomTetromino();
        if (!board.canPlace(this.currentBlock)) state = GameState.GAME_OVER;
    }
    public Board getBoard() { return board; }
    public Tetromino getCurrentBlock() { return currentBlock; }
    public Tetromino getNextBlock() { return nextBlock; }
    public GameConfig getConfig() { return config; }
    public GameState getState() { return state; }
    public int getScore() { return score; }
    public boolean isGameOver() { return state == GameState.GAME_OVER; }
    public boolean isPaused() { return state == GameState.PAUSED; }
    public int getDropIntervalMs() { return dropIntervalMs; }
    public int getTotalClearedLines() { return totalClearedLines; }
    public int getLastClearedLines() { return lastClearedLines; }
    public List<Integer> getClearingRows() { return clearingRows; }

    public GameSnapshot snapshot() {
        List<List<Integer>> cells = new ArrayList<>();
        List<List<TetrominoType>> cellTypes = new ArrayList<>();
        for (int row = 0; row < Board.HEIGHT; row++) {
            List<Integer> line = new ArrayList<>();
            List<TetrominoType> typeLine = new ArrayList<>();
            for (int col = 0; col < Board.WIDTH; col++) {
                line.add(board.getCell(row, col));
                typeLine.add(board.getCellType(row, col));
            }
            cells.add(line);
            cellTypes.add(typeLine);
        }
        boolean active = state == GameState.PLAYING
                || (state == GameState.PAUSED && stateBeforePause == GameState.PLAYING);
        return new GameSnapshot(cells, active ? PieceSnapshot.of(currentBlock) : null,
                PieceSnapshot.of(nextBlock), config, state, score, totalClearedLines,
                dropIntervalMs, clearingRows, cellTypes);
    }
    private boolean moveHorizontal(int delta) {
        if (state != GameState.PLAYING) return false;
        currentBlock.move(0, delta);
        if (board.canPlace(currentBlock)) return true;
        currentBlock.move(0, -delta);
        return false;
    }
    public boolean moveLeft() { return moveHorizontal(-1); }
    public boolean moveRight() { return moveHorizontal(1); }
    public boolean moveDown() {
        if (state != GameState.PLAYING) return false;
        currentBlock.moveDown();
        if (board.canPlace(currentBlock)) {
            score += scoring.dropPoints(1, dropIntervalMs);
            return true;
        }
        currentBlock.move(-1, 0);
        resolveLanding();
        return false;
    }
    private void resolveLanding() {
        board.lock(currentBlock);
        lastClearedLines = 0;
        clearingRows = board.findFullRows();
        if (clearingRows.isEmpty()) {
            spawnAfterLanding();
        } else {
            clearingElapsedMs = 0;
            state = GameState.CLEARING;
        }
    }
    /** Controller가 경과시간을 전달한다. 일시정지는 삭제 시간도 멈춘다. */
    public void advanceClear(int elapsedMs) {
        if (elapsedMs < 0) throw new IllegalArgumentException("Negative elapsed time");
        if (state != GameState.CLEARING) return;
        clearingElapsedMs = (int) Math.min(LINE_CLEAR_DURATION_MS, (long) clearingElapsedMs + elapsedMs);
        if (clearingElapsedMs < LINE_CLEAR_DURATION_MS) return;
        int previousLevel = totalClearedLines / LINES_PER_SPEED_UP;
        lastClearedLines = board.removeRows(clearingRows);
        totalClearedLines += lastClearedLines;
        score += scoring.linePoints(lastClearedLines);
        int currentLevel = totalClearedLines / LINES_PER_SPEED_UP;
        for (int i = previousLevel; i < currentLevel; i++) increaseSpeed();
        clearingRows = List.of();
        state = GameState.PLAYING;
        spawnAfterLanding();
    }
    private void spawnAfterLanding() {
        if (!spawnBlock()) state = GameState.GAME_OVER;
    }
    public boolean rotateClockwise() {
        if (state != GameState.PLAYING) return false;
        currentBlock.rotateClockwise();
        if (board.canPlace(currentBlock)) return true;
        currentBlock.rotateCounterClockwise();
        return false;
    }
    public Tetromino createRandomTetromino() { return generator.next(); }
    public boolean spawnBlock() {
        if (state != GameState.PLAYING || !board.canPlace(nextBlock)) return false;
        currentBlock = nextBlock;
        nextBlock = createRandomTetromino();
        return true;
    }
    public int hardDrop() {
        if (state != GameState.PLAYING) return 0;
        int droppedRows = 0;
        while (moveDown()) droppedRows++;
        return droppedRows;
    }
    public void tick() { moveDown(); }
    public void pause() {
        if (state != GameState.PAUSED && state != GameState.GAME_OVER) {
            stateBeforePause = state;
            state = GameState.PAUSED;
        }
    }
    public void resume() { if (state == GameState.PAUSED) state = stateBeforePause; }
    public void increaseSpeed() {
        dropIntervalMs = Math.max(MIN_DROP_INTERVAL_MS, dropIntervalMs - config.difficulty().speedStepMs());
    }
    /** 기존 fixture 호환. UI는 snapshot을 사용한다. */
    public void setCurrentBlock(Tetromino block) { currentBlock = Objects.requireNonNull(block); }
}
