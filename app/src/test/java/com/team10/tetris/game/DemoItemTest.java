package com.team10.tetris.game;

import com.team10.tetris.game.item.SmallBomb;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoItemTest {
    private GameEngine game(GameMode mode) {
        return new GameEngine(new Board(),null,new GameConfig(mode,Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy(),
                piece -> piece.attachItem(new SmallBomb(),0,0));
    }
    private void clear(GameEngine game, int count) {
        for(int r=0;r<20;r++) for(int c=0;c<10;c++) game.getBoard().setCell(r,c,0);
        for(int r=20-count;r<20;r++) for(int c=0;c<10;c++) game.getBoard().setCell(r,c,1);
        game.hardDrop(); game.advanceClear(300);
    }
    @Test void oneLineAwardsNewNextAndKeepsDisplayedPreview() {
        GameEngine game=game(GameMode.ITEM);
        game.configureDemoItems(true,1);
        Tetromino preview=game.getNextBlock();
        clear(game,1);
        assertSame(preview,game.getCurrentBlock());
        assertNotNull(game.getNextBlock().getItem());
        assertEquals(1,game.getTotalClearedLines());
    }
    @Test void defaultAndNormalModeDoNotAwardAfterOneLine() {
        GameEngine standard=game(GameMode.ITEM);
        standard.configureDemoItems(false,1); clear(standard,1);
        assertNull(standard.getNextBlock().getItem());
        GameEngine normal=game(GameMode.NORMAL);
        normal.configureDemoItems(true,1); clear(normal,1);
        assertNull(normal.getNextBlock().getItem());
    }
    @Test void intervalChangeStartsFreshAndExcessAwardsAreQueued() {
        GameEngine game=game(GameMode.ITEM);
        clear(game,4);
        game.configureDemoItems(true,2);
        clear(game,1); assertNull(game.getNextBlock().getItem());
        clear(game,3); assertNotNull(game.getNextBlock().getItem());
        game.hardDrop(); assertNotNull(game.getNextBlock().getItem());
        assertThrows(IllegalArgumentException.class,()->game.configureDemoItems(true,0));
    }
}
