package com.team10.tetris.game;

import org.junit.jupiter.api.Test;
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
}
