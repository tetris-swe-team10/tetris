package com.team10.tetris.game;

import org.junit.jupiter.api.Test;
import com.team10.tetris.game.item.*;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/** 여러 PR의 생명주기를 연결한 회귀 검증. */
class ItemIntegrationTest {
    private GameEngine demo() {
        GameEngine engine = new GameEngine(new GameConfig(GameMode.ITEM, Difficulty.NORMAL));
        engine.configureDemoItems(true, 1);
        return engine;
    }

    @Test void blackoutEndsOnBombLandingAndDoesNotBlockItsAnimationClock() {
        GameEngine engine = demo();
        engine.requestDemoItem(2); engine.hardDrop();
        engine.requestDemoItem(1); engine.hardDrop(); // B 현재, E NEXT
        assertEquals("B", engine.snapshot().currentBlock().item().symbol());
        engine.hardDrop(); // 암전 대상이 E
        assertTrue(engine.snapshot().blackoutActive());
        assertEquals("E", engine.snapshot().currentBlock().item().symbol());
        engine.hardDrop();
        assertEquals(GameState.ITEM_EFFECT, engine.getState());
        assertFalse(engine.snapshot().blackoutActive());
        int score = engine.getScore();
        GameController controller = new GameController(engine);
        controller.advance(299);
        assertEquals(GameState.ITEM_EFFECT, engine.getState());
        controller.advance(1);
        assertEquals(GameState.PLAYING, engine.getState());
        assertEquals(score, engine.getScore());
        assertTrue(engine.snapshot().effectCells().isEmpty());
    }

    @Test void rotationLockEndsBeforeTheFollowingBombIsControlled() {
        GameEngine engine = demo();
        engine.requestDemoItem(3); engine.hardDrop();
        engine.requestDemoItem(1); engine.hardDrop();
        assertTrue(engine.snapshot().rotationLocked());
        assertFalse(engine.rotateClockwise());
        engine.hardDrop();
        assertFalse(engine.snapshot().rotationLocked());
        assertEquals("E", engine.snapshot().currentBlock().item().symbol());
        engine.hardDrop();
        new GameController(engine).advance(300);
        assertEquals(GameState.PLAYING, engine.getState());
        assertFalse(engine.snapshot().rotationLocked());
    }

    @Test void blackoutTargetCanClearAnIncompleteLRowAndEndNormally() {
        GameEngine engine = demo();
        engine.requestDemoItem(2); engine.hardDrop();
        engine.hardDrop(); engine.hardDrop();
        assertTrue(engine.snapshot().blackoutActive());
        Board board = engine.getBoard();
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
        Tetromino line = new Tetromino(TetrominoType.I, 19, 3);
        line.attachItem(new LineClearItem(), 0, 0);
        engine.setCurrentBlock(line);
        int score = engine.getScore();
        engine.moveDown();
        assertFalse(engine.snapshot().blackoutActive());
        assertEquals(GameState.CLEARING, engine.getState());
        assertEquals(List.of(19), engine.getClearingRows());
        new GameController(engine).advance(300);
        assertEquals(score + 1000, engine.getScore());
        assertEquals(1, engine.getTotalClearedLines());
        assertEquals(GameState.PLAYING, engine.getState());
    }

    @Test void delayedEffectStillCollectsRequestedRowsAfterItsAnimation() {
        GameItem delayedLine = new GameItem() {
            public String symbol() { return "X"; }
            public String description() { return "통합 테스트 아이템"; }
            public List<CellPosition> previewCells(Board b, int r, int c) { return List.of(new CellPosition(r,c)); }
            public int effectDurationMs() { return 300; }
            public java.util.Set<Integer> rowsToClear(Board b, int r, int c) { return java.util.Set.of(r); }
        };
        Tetromino piece = new Tetromino(TetrominoType.O, 18, 3);
        piece.attachItem(delayedLine, 1, 0);
        GameEngine engine = new GameEngine(new Board(), piece,
                new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
        engine.moveDown();
        assertEquals(GameState.ITEM_EFFECT, engine.getState());
        GameController controller = new GameController(engine);
        controller.advance(300);
        assertEquals(GameState.CLEARING, engine.getState());
        assertEquals(List.of(19), engine.getClearingRows());
        controller.advance(300);
        assertEquals(1000, engine.getScore());
    }

    @Test void allFourItemsRemainSelectableAndPenaltiesExcludeOnlyBAndK() {
        RandomItemAssigner assigner = new RandomItemAssigner(
                List.of(new SmallBomb(), new Blackout(), new RotationLock(), new LineClearItem()), new Random(27));
        java.util.Set<String> symbols = new java.util.HashSet<>();
        for (int i = 0; i < 200; i++) {
            Tetromino piece = new Tetromino(TetrominoType.O, 0, 3);
            assigner.attachTo(piece, new PenaltyController());
            symbols.add(piece.getItem().item().symbol());
        }
        assertEquals(java.util.Set.of("E","B","K","L"), symbols);
        PenaltyController reserved = new PenaltyController(); reserved.reserve();
        symbols.clear();
        for (int i = 0; i < 200; i++) {
            Tetromino piece = new Tetromino(TetrominoType.O, 0, 3);
            assigner.attachTo(piece, reserved);
            symbols.add(piece.getItem().item().symbol());
        }
        assertEquals(java.util.Set.of("E","L"), symbols);
    }
}
