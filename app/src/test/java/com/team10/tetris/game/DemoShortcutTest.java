package com.team10.tetris.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoShortcutTest {
    @Test void shortcutsRequireDemoItemModeAndPreservePreview() {
        GameEngine game=new GameEngine(new GameConfig(GameMode.ITEM,Difficulty.NORMAL));
        assertFalse(game.requestDemoItem(1));
        game.configureDemoItems(true,10);
        for(int i=1;i<=3;i++) {
            assertTrue(game.requestDemoItem(i));
            assertEquals(new String[]{"E","B","K"}[i-1],game.demoRequestedSymbol());
        }
        Tetromino preview=game.getNextBlock();
        game.hardDrop();
        assertSame(preview,game.getCurrentBlock());
        assertEquals("K",game.getNextBlock().getItem().item().symbol());
        assertEquals("",game.demoRequestedSymbol());
        GameEngine normal=new GameEngine(GameConfig.DEFAULT);
        normal.configureDemoItems(true,1);
        assertFalse(normal.requestDemoItem(1));
    }
    @Test void penaltyRequestWaitsUntilPriorEffectEnds() {
        GameEngine game=new GameEngine(new GameConfig(GameMode.ITEM,Difficulty.NORMAL));
        game.configureDemoItems(true,10);
        game.requestDemoItem(2); game.hardDrop();
        assertEquals("B",game.getNextBlock().getItem().item().symbol());
        game.requestDemoItem(3); game.hardDrop();
        assertNull(game.getNextBlock().getItem());
        game.hardDrop();
        assertTrue(game.snapshot().blackoutActive());
        assertNull(game.getNextBlock().getItem());
        game.hardDrop();
        assertFalse(game.snapshot().blackoutActive());
        assertEquals("K",game.getNextBlock().getItem().item().symbol());
    }
    @Test void disableCancelsPendingAndPauseRejectsInput() {
        GameEngine game=new GameEngine(new GameConfig(GameMode.ITEM,Difficulty.NORMAL));
        game.configureDemoItems(true,10);
        game.pause(); assertFalse(game.requestDemoItem(1)); game.resume();
        assertTrue(game.requestDemoItem(4));
        assertEquals("W",game.demoRequestedSymbol());
        assertFalse(game.requestDemoItem(5));
        game.requestDemoItem(1); game.configureDemoItems(false,10);
        game.hardDrop(); assertNull(game.getNextBlock().getItem());
    }
}
