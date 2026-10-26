package com.team10.tetris.game;

import com.team10.tetris.game.item.ItemAssigner;
import com.team10.tetris.game.item.GameItem;
import com.team10.tetris.game.item.RandomItemAssigner;
import com.team10.tetris.game.item.SmallBomb;
import com.team10.tetris.game.item.Blackout;
import com.team10.tetris.game.item.RotationLock;
import com.team10.tetris.game.item.LineClearItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/** 순수 Java 모델: 점수·난이도·착지·삭제 상태 전이를 관리한다. */
public class GameEngine {
    public static final int INITIAL_DROP_INTERVAL_MS = 1000;
    public static final int LINE_CLEAR_DURATION_MS = 300;
    private static final int MIN_DROP_INTERVAL_MS = 200;
    private static final int LINES_PER_SPEED_UP = 5;
    private static final int LINES_PER_ITEM = 10;
    private final PenaltyController penalties = new PenaltyController();
    private final Board board;
    private final GameConfig config;
    private final PieceGenerator generator;
    private final ScoringPolicy scoring;
    private final ItemAssigner itemAssigner;
    private int pendingItems;
    private int itemLineInterval = LINES_PER_ITEM;
    private int itemProgress;
    private boolean demoMode;
    private GameItem demoRequestedItem;

    public boolean isItemDemoEnabled() { return demoMode && config.mode() == GameMode.ITEM; }
    public String demoRequestedSymbol() { return demoRequestedItem == null ? "" : demoRequestedItem.symbol(); }
    public boolean requestDemoItem(int number) {
        if (!isItemDemoEnabled() || state != GameState.PLAYING) return false;
        GameItem item = switch (number) {
            case 1 -> new SmallBomb();
            case 2 -> new Blackout();
            case 3 -> new RotationLock();
            default -> null;
        };
        if (item == null) return false;
        demoRequestedItem = item;
        return true;
    }


    public void configureDemoItems(boolean enabled, int lines) {
        if (lines < 1 || lines > 100) throw new IllegalArgumentException("Expected 1..100 lines");
        demoMode = enabled;
        if (!enabled) demoRequestedItem = null;
        int interval = enabled ? lines : LINES_PER_ITEM;
        if (itemLineInterval != interval) {
            itemLineInterval = interval;
            itemProgress = 0;
        }
    }
    private Tetromino currentBlock;
    private Tetromino nextBlock;
    private GameState state = GameState.PLAYING;
    private GameState stateBeforePause = GameState.PLAYING;
    private List<Integer> clearingRows = List.of();
    private int clearingElapsedMs;
    private GameItem pendingEffect;
    private CellPosition effectOrigin;
    private List<CellPosition> effectCells = List.of();
    private int effectElapsedMs;
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
        this(board, currentBlock, config, generator, scoring,
                new RandomItemAssigner(List.of(new SmallBomb(), new Blackout(), new RotationLock(), new LineClearItem()), new Random()));
    }
    public GameEngine(Board board, Tetromino currentBlock, GameConfig config,
            PieceGenerator generator, ScoringPolicy scoring, ItemAssigner itemAssigner) {
        this.board = Objects.requireNonNull(board);
        this.config = Objects.requireNonNull(config);
        this.generator = Objects.requireNonNull(generator);
        this.scoring = Objects.requireNonNull(scoring);
        this.itemAssigner = Objects.requireNonNull(itemAssigner);
        this.currentBlock = currentBlock == null ? createRandomTetromino() : currentBlock;
        nextBlock = createNextTetromino();
        if (!board.canPlace(this.currentBlock)) {
            state = GameState.GAME_OVER;
            penalties.reset();
            demoRequestedItem = null;
        }
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
                dropIntervalMs, clearingRows, cellTypes, penalties.blackoutActive(), penalties.rotationLocked(),
                effectCells, !effectCells.isEmpty() && effectElapsedMs / 75 % 2 == 0);
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
        penalties.finishActive();
        board.lock(currentBlock);
        lastClearedLines = 0;
        Set<Integer> requestedRows = Set.of();
        // 벌칙은 착지 때 종료하고, 삭제 효과만 연출 완료까지 보류한다.
        var attached = currentBlock.getItem();
        if (config.mode() == GameMode.ITEM && attached != null) {
            int row = currentBlock.getRow() + attached.row();
            int col = currentBlock.getCol() + attached.col();
            effectCells = List.copyOf(attached.item().previewCells(board, row, col));
            if (!effectCells.isEmpty() && attached.item().effectDurationMs() > 0) {
                pendingEffect = attached.item();
                effectOrigin = new CellPosition(row, col);
                effectElapsedMs = 0;
                state = GameState.ITEM_EFFECT;
                return;
            }
            effectCells = List.of();
            attached.item().onLock(board, row, col, penalties);
            requestedRows = attached.item().rowsToClear(board, row, col);
        }
        resolveLockedBoard(requestedRows);
    }
    private void resolveLockedBoard(Set<Integer> requestedRows) {
        Set<Integer> rows = new TreeSet<>(requestedRows);
        rows.addAll(board.findFullRows());
        clearingRows = List.copyOf(rows);
        if (clearingRows.isEmpty()) {
            spawnAfterLanding();
        } else {
            clearingElapsedMs = 0;
            state = GameState.CLEARING;
        }
    }
    public int getItemEffectRemainingMs() {
        return pendingEffect == null ? 0 : pendingEffect.effectDurationMs() - effectElapsedMs;
    }
    /** 줄 삭제와 동일한 게임 시계를 사용하며, 일시정지는 연출도 멈춘다. */
    public void advanceItemEffect(int elapsedMs) {
        if (elapsedMs < 0) throw new IllegalArgumentException("Negative elapsed time");
        if (state != GameState.ITEM_EFFECT) return;
        int duration = pendingEffect.effectDurationMs();
        effectElapsedMs = (int) Math.min(duration, (long) effectElapsedMs + elapsedMs);
        if (effectElapsedMs < duration) return;
        pendingEffect.onLock(board, effectOrigin.row(), effectOrigin.col(), penalties);
        Set<Integer> requestedRows = pendingEffect.rowsToClear(board, effectOrigin.row(), effectOrigin.col());
        pendingEffect = null;
        effectOrigin = null;
        effectCells = List.of();
        state = GameState.PLAYING;
        resolveLockedBoard(requestedRows);
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
        if (config.mode() == GameMode.ITEM) {
            itemProgress += lastClearedLines;
            pendingItems += itemProgress / itemLineInterval;
            itemProgress %= itemLineInterval;
        }
        score += scoring.linePoints(lastClearedLines);
        int currentLevel = totalClearedLines / LINES_PER_SPEED_UP;
        for (int i = previousLevel; i < currentLevel; i++) increaseSpeed();
        clearingRows = List.of();
        state = GameState.PLAYING;
        spawnAfterLanding();
    }
    private void spawnAfterLanding() {
        if (!spawnBlock()) {
            state = GameState.GAME_OVER;
            penalties.reset();
            demoRequestedItem = null;
        }
    }
    public boolean rotateClockwise() {
        if (state != GameState.PLAYING || penalties.rotationLocked()) return false;
        currentBlock.rotateClockwise();
        if (board.canPlace(currentBlock)) return true;
        currentBlock.rotateCounterClockwise();
        return false;
    }
    public Tetromino createRandomTetromino() { return generator.next(); }
    /** 10줄마다 새 NEXT에 공통 목록에서 선택한 아이템을 지급한다. */
    private Tetromino createNextTetromino() {
        Tetromino piece = createRandomTetromino();
        if (isItemDemoEnabled() && demoRequestedItem != null && demoRequestedItem.isAvailable(penalties)) {
            new RandomItemAssigner(List.of(demoRequestedItem), new Random()).attachTo(piece);
            demoRequestedItem.onAssigned(penalties);
            demoRequestedItem = null;
        } else if (config.mode() == GameMode.ITEM && pendingItems > 0) {
            itemAssigner.attachTo(piece, penalties);
            if (piece.getItem() != null) piece.getItem().item().onAssigned(penalties);
            pendingItems--;

        }
        return piece;
    }
    public int getClearRemainingMs() { return LINE_CLEAR_DURATION_MS - clearingElapsedMs; }
    public boolean spawnBlock() {
        if (state != GameState.PLAYING || !board.canPlace(nextBlock)) return false;
        currentBlock = nextBlock;
        penalties.activatePending();
        if (config.mode() == GameMode.ITEM && currentBlock.getItem() != null) {
            currentBlock.getItem().item().onSpawn(penalties);
        }
        nextBlock = createNextTetromino();
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
