package com.team10.tetris.game;

import com.team10.tetris.game.item.*;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackoutTest {
    private GameEngine game(Board board, GameMode mode, double chance) {
        return new GameEngine(board, null, new GameConfig(mode, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                new RandomItemAssigner(java.util.List.of(new Blackout()), new Random(42)));
    }
    private GameEngine ready() {
        GameEngine game = game(new Board(), GameMode.ITEM, 1);
        reward(game);
        game.hardDrop(); // 10줄 달성 후 예고된 B를 조작
        return game;
    }
    @Test void previewsThenLastsForExactlyOneFollowingPiece() {
        GameEngine game = game(new Board(), GameMode.ITEM, 1);
        assertNull(game.getNextBlock().getItem());
        reward(game);
        Tetromino preview=game.getNextBlock();
        assertInstanceOf(Blackout.class,preview.getItem().item());
        assertFalse(game.snapshot().blackoutActive());
        game.hardDrop();
        assertSame(preview,game.getCurrentBlock());
        assertFalse(game.snapshot().blackoutActive());
        game.hardDrop();
        assertTrue(game.snapshot().blackoutActive());
        new GameController(game).advance(3500);
        assertTrue(game.snapshot().blackoutActive());
        game.hardDrop();
        assertFalse(game.snapshot().blackoutActive());
    }
    @Test void pausePreservesEffectAndSoftLandingEndsIt() {
        GameEngine game=ready(); game.hardDrop();
        GameController controller=new GameController(game);
        controller.pause(); controller.advance(10000);
        assertTrue(game.snapshot().blackoutActive());
        controller.resume();
        while(game.snapshot().blackoutActive()) game.moveDown();
        assertFalse(game.snapshot().blackoutActive());
    }
    @Test void clearDefersActivationAndTargetClearEndsEffectImmediately() {
        GameEngine game=ready();
        for(int c=0;c<10;c++) game.getBoard().setCell(19,c,1);
        game.hardDrop();
        assertEquals(GameState.CLEARING,game.getState());
        assertFalse(game.snapshot().blackoutActive());
        game.advanceClear(300);
        assertTrue(game.snapshot().blackoutActive());
        for(int c=0;c<10;c++) game.getBoard().setCell(19,c,1);
        game.hardDrop();
        assertEquals(GameState.CLEARING,game.getState());
        assertFalse(game.snapshot().blackoutActive());
        game.advanceClear(300);
        assertEquals(12,game.getTotalClearedLines());
    }
    @Test void hiddenBoardStillCollidesAndGameOverClearsEffect() {
        GameEngine game=ready(); game.hardDrop();
        game.getBoard().setCell(0,2,1);
        assertFalse(game.moveLeft());
        game.getBoard().setCell(0,3,1);
        game.hardDrop();
        assertEquals(GameState.GAME_OVER,game.getState());
        assertFalse(game.snapshot().blackoutActive());
        assertFalse(game(new Board(),GameMode.ITEM,0).snapshot().blackoutActive());
    }
    @Test void normalModeNeverAwardsItems() {
        GameEngine game=game(new Board(),GameMode.NORMAL,1);
        reward(game);
        assertNull(game.getNextBlock().getItem());
    }
    @Test void markerRemainsOnOccupiedCellThroughRotationAndRollback() {
        for (TetrominoType type:TetrominoType.values()) {
            Tetromino piece=new Tetromino(type,0,3);
            int[][] shape=piece.getShape();
            for(int r=0;r<shape.length;r++) for(int c=0;c<shape[r].length;c++) if(shape[r][c]==1) {
                piece = new Tetromino(type,0,3);
                piece.attachItem(new Blackout(),r,c);
                for(int i=0;i<4;i++) {piece.rotateClockwise();assertEquals(1,piece.getShape()[piece.getItem().row()][piece.getItem().col()]);}
                assertEquals(r,piece.getItem().row());assertEquals(c,piece.getItem().col());
                piece.rotateClockwise();piece.rotateCounterClockwise();
                assertEquals(r,piece.getItem().row());assertEquals(c,piece.getItem().col());
            }
        }
    }
    @Test void probabilityUsesSharedAssigner() {
        RandomItemAssigner assigner = new RandomItemAssigner(java.util.List.of(new SmallBomb(), new Blackout()),new Random(17));
        int count=0;
        for(int i=0;i<10000;i++) {
            Tetromino piece=new Tetromino(TetrominoType.T,0,3);
            assigner.attachTo(piece, new PenaltyController());
            if(piece.getItem().item() instanceof Blackout)count++;
        }
        assertTrue(count>4500 && count<5500,"observed="+count);
    }
    @Test void reservedOrActiveBlackoutIsExcludedWithoutLosingReward() {
        PenaltyController state=new PenaltyController(); state.reserve();
        RandomItemAssigner assigner=new RandomItemAssigner(java.util.List.of(new SmallBomb(),new Blackout()),new Random(1));
        for(int i=0;i<100;i++) {
            Tetromino piece=new Tetromino(TetrominoType.O,0,3);
            assigner.attachTo(piece,state);
            assertInstanceOf(SmallBomb.class,piece.getItem().item());
        }
    }
    private void reward(GameEngine game) {
        for(int n: new int[]{4,4,2}) {
            Board board=game.getBoard();
            for(int r=0;r<Board.HEIGHT;r++) for(int c=0;c<Board.WIDTH;c++) board.setCell(r,c,0);
            for(int r=20-n;r<20;r++) for(int c=0;c<10;c++) board.setCell(r,c,1);
            game.hardDrop(); game.advanceClear(300);
        }
        for(int r=0;r<Board.HEIGHT;r++) for(int c=0;c<Board.WIDTH;c++) game.getBoard().setCell(r,c,0);
    }
}
