package com.team10.tetris.game.versus;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.team10.tetris.game.*;

class LocalVersusMatchTest {
    static AttackLines lines(int count, int hole) {
        List<Boolean> row=new ArrayList<>(Collections.nCopies(Board.WIDTH,true)); row.set(hole,false);
        return new AttackLines(Collections.nCopies(count,row));
    }
    static GameEngine engine(Board b,Tetromino piece) {
        return new GameEngine(b,piece,GameConfig.DEFAULT,()->new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy());
    }
    @Test void pendingQueueKeepsOldThenTopOfNewAndIgnoresWhenFull() {
        GameEngine e=engine(new Board(),new Tetromino(TetrominoType.O,0,3));
        e.receiveAttack(lines(8,0)); e.receiveAttack(lines(4,1)); e.receiveAttack(lines(2,2));
        assertEquals(10,e.getPendingAttack().size());
        assertFalse(e.getPendingAttack().rows().get(7).get(0));
        assertFalse(e.getPendingAttack().rows().get(8).get(1));
        assertTrue(e.getPendingAttack().rows().get(9).get(2));
        assertEquals(0,e.getBoard().getCell(19,0));
        e.hardDrop();
        assertTrue(e.getPendingAttack().isEmpty()); assertTrue(e.snapshot().isGarbage(19,0));
        assertEquals(0,e.getBoard().getCell(19,1));
    }
    @Test void doubleClearSendsHolesFromLastPieceOnlyAfterAnimation() {
        Board board=new Board();
        for(int r=18;r<20;r++) for(int c=0;c<10;c++) if(c!=3 && c!=4) board.setCell(r,c,1);
        GameEngine e=engine(board,new Tetromino(TetrominoType.O,18,3)); List<AttackLines> sent=new ArrayList<>(); e.setAttackListener(sent::add);
        e.receiveAttack(lines(1,0)); e.moveDown();
        assertEquals(GameState.CLEARING,e.getState()); assertTrue(sent.isEmpty()); assertEquals(1,e.getPendingAttack().size());
        e.advanceClear(299); assertTrue(sent.isEmpty());
        e.advanceClear(1); assertEquals(1,sent.size()); assertEquals(2,sent.get(0).size());
        assertEquals(List.of(true,true,true,false,false,true,true,true,true,true),sent.get(0).rows().get(0));
        assertEquals(1,e.snapshot().garbageCells().stream().filter(row->row.contains(true)).count());
    }
    @Test void singleClearDoesNotAttack() {
        Board board=new Board(); for(int c=0;c<10;c++) if(c!=3&&c!=4) board.setCell(19,c,1);
        GameEngine e=engine(board,new Tetromino(TetrominoType.O,18,3)); e.setAttackListener(a->fail("Single row attack"));
        e.moveDown(); e.advanceClear(300); assertEquals(1,e.getTotalClearedLines());
    }
    @Test void simultaneousClearsDeliverAfterBothUpdatesWithoutPlayerOrderAdvantage() {
        GameEngine[] engines=new GameEngine[2];
        for(int i=0;i<2;i++) {
            Board board=new Board();
            for(int r=18;r<20;r++) for(int c=0;c<10;c++) if(c!=3&&c!=4) board.setCell(r,c,1);
            engines[i]=engine(board,new Tetromino(TetrominoType.O,18,3));
        }
        var match=new LocalVersusMatch(VersusConfig.of(VersusRule.NORMAL,Difficulty.NORMAL),engines[0],engines[1]);
        match.handle(PlayerSide.PLAYER1,GameCommand.HARD_DROP); match.handle(PlayerSide.PLAYER2,GameCommand.HARD_DROP);
        match.advance(300);
        for(PlayerSide side:PlayerSide.values()) {
            assertEquals(2,match.snapshot(side).pendingAttack().size());
            assertFalse(match.snapshot(side).isGarbage(19,0));
        }
    }
    @Test void garbageIdentityMovesWithRowCompressionAndDisappearsWhenDestroyed() {
        Board board=new Board(); board.insertGarbage(lines(2,0)); board.removeRows(List.of(19));
        assertEquals(Board.GARBAGE,board.getCell(19,1)); board.setCell(19,1,0); assertEquals(0,board.getCell(19,1));
    }
    @Test void overflowLosesAtNextSpawnAndFreezesMatch() {
        Board b=new Board(); b.setCell(0,0,1); GameEngine first=engine(b,new Tetromino(TetrominoType.O,17,3));
        GameEngine second=engine(new Board(),new Tetromino(TetrominoType.O,0,3));
        LocalVersusMatch m=new LocalVersusMatch(VersusConfig.of(VersusRule.NORMAL,Difficulty.NORMAL),first,second);
        first.receiveAttack(lines(1,1)); assertTrue(m.result().isEmpty());
        m.handle(PlayerSide.PLAYER1,GameCommand.HARD_DROP); assertEquals(PlayerSide.PLAYER2,m.result().orElseThrow().winner());
        var snapshot=m.snapshot(PlayerSide.PLAYER2); m.advance(10000); m.handle(PlayerSide.PLAYER2,GameCommand.DOWN);
        assertEquals(snapshot,m.snapshot(PlayerSide.PLAYER2));
    }
    @Test void timerStopsForPauseExpiresExactlyAndAllowsDraw() {
        LocalVersusMatch m=new LocalVersusMatch(new VersusConfig(VersusRule.TIME_LIMIT,Difficulty.NORMAL,1));
        m.advance(400); m.pause(); m.advance(10000); assertEquals(600,m.remainingTimeMs());
        m.resume(); m.advance(599); assertTrue(m.result().isEmpty()); m.advance(1);
        assertTrue(m.result().orElseThrow().isDraw()); assertEquals(VersusResult.Reason.TIME_UP,m.result().orElseThrow().reason());
        assertThrows(IllegalArgumentException.class,()->m.advance(-1));
    }
    @Test void shiftedStackBlockingNextPieceAlsoEndsMatch() {
        Board board=new Board(); board.setCell(1,3,1);
        GameEngine first=engine(board,new Tetromino(TetrominoType.O,17,3));
        var match=new LocalVersusMatch(VersusConfig.of(VersusRule.NORMAL,Difficulty.NORMAL),first,
                engine(new Board(),new Tetromino(TetrominoType.O,0,3)));
        first.receiveAttack(lines(1,0)); match.handle(PlayerSide.PLAYER1,GameCommand.HARD_DROP);
        assertEquals(PlayerSide.PLAYER2,match.result().orElseThrow().winner());
    }
    @Test void higherScoreWinsAndItemModeUsesItems() {
        LocalVersusMatch m=new LocalVersusMatch(new VersusConfig(VersusRule.TIME_LIMIT,Difficulty.NORMAL,1));
        m.handle(PlayerSide.PLAYER1,GameCommand.DOWN); m.advance(1000);
        assertEquals(PlayerSide.PLAYER1,m.result().orElseThrow().winner());
        LocalVersusMatch item=new LocalVersusMatch(VersusConfig.of(VersusRule.ITEM,Difficulty.EASY));
        assertEquals(GameMode.ITEM,item.snapshot(PlayerSide.PLAYER1).config().mode());
    }
    @Test void lineItemClearsAndScoresButDoesNotSendAttack() {
        Board board=new Board();
        for(int r=18;r<20;r++) for(int c=0;c<10;c++) if(c!=3&&c!=4) board.setCell(r,c,1);
        Tetromino piece=new Tetromino(TetrominoType.O,18,3);
        piece.attachItem(new com.team10.tetris.game.item.LineClearItem(),0,0);
        GameEngine e=new GameEngine(board,piece,new GameConfig(GameMode.ITEM,Difficulty.NORMAL),
                ()->new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy());
        e.setAttackListener(a->fail("Item clear must not attack")); e.moveDown(); e.advanceClear(300);
        assertEquals(2,e.getTotalClearedLines()); assertTrue(e.getScore()>0);
    }
}
