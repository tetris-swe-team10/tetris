package com.team10.tetris.game;

import com.team10.tetris.game.item.*;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RotationLockTest {
    private GameEngine preview() {
        GameEngine game = new GameEngine(new Board(),null,new GameConfig(GameMode.ITEM,Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.T,0,3),new StandardScoringPolicy(),
                piece -> piece.attachItem(new RotationLock(),0,1));
        for(int n:new int[]{4,4,2}) {
            for(int r=0;r<20;r++) for(int c=0;c<10;c++) game.getBoard().setCell(r,c,0);
            for(int r=20-n;r<20;r++) for(int c=0;c<10;c++) game.getBoard().setCell(r,c,1);
            game.hardDrop(); game.advanceClear(300);
        }
        for(int r=0;r<20;r++) for(int c=0;c<10;c++) game.getBoard().setCell(r,c,0);
        return game;
    }
    @Test void nextDoesNotLockPreviousPieceButPromotionLocksKItself() {
        GameEngine game=preview();
        assertEquals("K",game.snapshot().nextBlock().item().symbol());
        assertFalse(game.snapshot().rotationLocked());
        assertTrue(game.rotateClockwise());
        PieceSnapshot expected=game.snapshot().nextBlock();
        game.hardDrop();
        assertTrue(game.snapshot().rotationLocked());
        assertFalse(game.snapshot().blackoutActive());
        assertEquals(expected.shape(),game.snapshot().currentBlock().shape());
        PieceSnapshot before=game.snapshot().currentBlock();
        assertFalse(game.rotateClockwise());
        new GameController(game).handle(GameCommand.ROTATE);
        assertEquals(before,game.snapshot().currentBlock());
    }
    @Test void movementDropsAndPauseWorkAndNextPieceCanRotate() {
        GameEngine game=preview(); game.hardDrop();
        assertTrue(game.moveLeft()); assertTrue(game.moveRight()); assertTrue(game.moveDown());
        GameController controller=new GameController(game);
        controller.pause(); controller.advance(10000); controller.resume();
        assertTrue(game.snapshot().rotationLocked());
        assertFalse(game.rotateClockwise());
        assertTrue(game.hardDrop()>0);
        assertFalse(game.snapshot().rotationLocked());
        assertTrue(game.rotateClockwise());
    }
    @Test void softLandingAndAutomaticLandingReleaseLock() {
        for(boolean automatic:new boolean[]{false,true}) {
            GameEngine game=preview(); game.hardDrop();
            while(game.snapshot().rotationLocked()) {
                if(automatic) new GameController(game).advance(game.getDropIntervalMs());
                else game.moveDown();
            }
            assertTrue(game.rotateClockwise());
        }
    }
    @Test void clearAndGameOverDoNotLeakLock() {
        GameEngine game=preview(); game.hardDrop();
        for(int c=0;c<10;c++) game.getBoard().setCell(19,c,1);
        game.hardDrop();
        assertEquals(GameState.CLEARING,game.getState());
        assertFalse(game.snapshot().rotationLocked());
        game.advanceClear(300);
        assertTrue(game.rotateClockwise());
        game=preview(); game.hardDrop();
        game.getBoard().setCell(0,4,1);
        game.hardDrop();
        assertEquals(GameState.GAME_OVER,game.getState());
        assertFalse(game.snapshot().rotationLocked());
    }
    @Test void bothPenaltiesAreExcludedWhileEitherIsReservedOrActive() {
        for(GameItem item:List.of(new Blackout(),new RotationLock())) {
            PenaltyController state=new PenaltyController(); item.onAssigned(state);
            for(int phase=0;phase<2;phase++) {
                assertFalse(new Blackout().isAvailable(state));
                assertFalse(new RotationLock().isAvailable(state));
                RandomItemAssigner assigner=new RandomItemAssigner(
                        List.of(new SmallBomb(),new Blackout(),new RotationLock()),new Random(1));
                Tetromino piece=new Tetromino(TetrominoType.T,0,3);
                assigner.attachTo(piece,state);
                assertInstanceOf(SmallBomb.class,piece.getItem().item());
                item.onSpawn(state); item.onLock(new Board(),0,0,state); state.activatePending();
            }
            state.finishActive();
            assertTrue(new RotationLock().isAvailable(state));
        }
    }
}
