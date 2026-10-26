package com.team10.tetris.game;

import com.team10.tetris.game.item.SmallBomb;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BombFlashTest {
    private GameEngine game(Board board, int row) {
        Tetromino piece = new Tetromino(TetrominoType.O, row, 3);
        piece.attachItem(new SmallBomb(), 0, 0);
        return new GameEngine(board, piece, new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
    }

    @Test void lockedCellsStayUntilTheFlashFinishesAndTheNextPieceWaits() {
        GameEngine game = game(new Board(), 18);
        Tetromino next = game.getNextBlock();
        game.moveDown();
        GameSnapshot before = game.snapshot();
        assertEquals(GameState.ITEM_EFFECT, before.state());
        assertNull(before.currentBlock(), "Locked piece must not be drawn twice");
        assertEquals(4, before.effectCells().size());
        assertTrue(before.effectFlashVisible());
        assertTrue(before.clearingRows().isEmpty());
        assertEquals(1, before.cells().get(18).get(3));
        assertEquals(TetrominoType.O, before.cellTypes().get(18).get(3));
        assertSame(next, game.getNextBlock());
        assertThrows(UnsupportedOperationException.class, () -> before.effectCells().clear());
        for (GameCommand command : GameCommand.values()) new GameController(game).handle(command);
        assertFalse(game.moveLeft()); assertFalse(game.moveRight());
        assertFalse(game.rotateClockwise()); assertFalse(game.moveDown());
        assertFalse(game.spawnBlock()); assertEquals(0, game.hardDrop());
        game.advanceItemEffect(299);
        assertEquals(1, game.getBoard().getCell(18, 3));
        assertSame(next, game.getNextBlock());
        game.advanceItemEffect(1);
        assertEquals(GameState.PLAYING, game.getState());
        assertSame(next, game.getCurrentBlock());
        assertEquals(0, game.getBoard().getCell(18, 3));
        assertNull(game.getBoard().getCellType(18, 3));
        assertTrue(game.snapshot().effectCells().isEmpty());
        assertFalse(game.snapshot().effectFlashVisible());
        assertEquals(1, before.cells().get(18).get(3), "Old snapshot remains unchanged");
        assertEquals(4, before.effectCells().size());
        assertEquals(0, game.getScore()); assertEquals(0, game.getTotalClearedLines());
        game.getBoard().setCell(18, 3, 1);
        game.advanceItemEffect(300);
        assertEquals(1, game.getBoard().getCell(18, 3), "Effect runs only once");
    }

    @Test void whiteOverlayBlinksTwiceInThreeHundredMilliseconds() {
        GameEngine game = game(new Board(), 18);
        GameController controller = new GameController(game);
        controller.handle(GameCommand.DOWN);
        assertTrue(game.snapshot().effectFlashVisible());
        controller.advance(74); assertTrue(game.snapshot().effectFlashVisible());
        controller.advance(1); assertFalse(game.snapshot().effectFlashVisible());
        controller.advance(75); assertTrue(game.snapshot().effectFlashVisible());
        controller.advance(75); assertFalse(game.snapshot().effectFlashVisible());
        controller.advance(74); assertEquals(GameState.ITEM_EFFECT, game.getState());
        controller.advance(1); assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void pauseFreezesBothFlashPhaseAndRemovalDeadline() {
        GameEngine game = game(new Board(), 18);
        GameController controller = new GameController(game);
        controller.handle(GameCommand.DOWN); controller.advance(80);
        GameSnapshot pausedPhase = game.snapshot();
        controller.pause(); controller.pause();
        controller.advance(10_000); game.advanceItemEffect(10_000); game.tick();
        assertEquals(GameState.PAUSED, game.getState());
        assertEquals(pausedPhase.effectCells(), game.snapshot().effectCells());
        assertEquals(pausedPhase.effectFlashVisible(), game.snapshot().effectFlashVisible());
        assertNull(game.snapshot().currentBlock());
        assertEquals(1, game.getBoard().getCell(18, 3));
        controller.resume(); controller.advance(219);
        assertEquals(GameState.ITEM_EFFECT, game.getState());
        controller.advance(1);
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(0, game.getBoard().getCell(18, 3));
    }

    @Test void survivingFullRowStartsItsOwnAnimationAfterExplosionAndScoresOnce() {
        Board board = new Board();
        for (int c = 0; c < Board.WIDTH; c++) board.setCell(19, c, 1);
        GameEngine game = game(board, 17);
        GameController controller = new GameController(game);
        controller.handle(GameCommand.DOWN);
        assertEquals(GameState.ITEM_EFFECT, game.getState());
        assertTrue(game.snapshot().clearingRows().isEmpty());
        controller.advance(300);
        assertEquals(GameState.CLEARING, game.getState());
        assertEquals(List.of(19), game.snapshot().clearingRows());
        assertTrue(game.snapshot().effectCells().isEmpty());
        assertEquals(0, game.getScore());
        controller.advance(299); assertEquals(0, game.getScore());
        controller.advance(1); assertEquals(1000, game.getScore());
        assertEquals(1, game.getTotalClearedLines());
        game.advanceItemEffect(1000); game.advanceClear(1000);
        assertEquals(1000, game.getScore());
    }

    @Test void newPieceReceivesAFreshGravityClockAfterTheEffect() {
        GameEngine game = game(new Board(), 18);
        GameController controller = new GameController(game);
        controller.advance(900);
        controller.handle(GameCommand.DOWN);
        controller.advance(300);
        controller.advance(999);
        assertEquals(0, game.getCurrentBlock().getRow());
        controller.advance(1);
        assertEquals(1, game.getCurrentBlock().getRow());
        assertEquals(100, game.getScore());
    }

    @Test void blockedSpawnBecomesGameOverOnlyAfterTheFlashAndRestartHasNoEffect() {
        Board board = new Board(); board.setCell(0, 3, 1);
        GameEngine game = game(board, 18);
        game.moveDown();
        assertFalse(game.isGameOver());
        game.advanceItemEffect(Integer.MAX_VALUE);
        assertTrue(game.isGameOver());
        assertTrue(game.snapshot().effectCells().isEmpty());
        game.advanceItemEffect(300); game.resume(); game.hardDrop();
        assertTrue(game.isGameOver());
        GameEngine fresh = new GameEngine(game.getConfig());
        assertEquals(GameState.PLAYING, fresh.getState());
        assertTrue(fresh.snapshot().effectCells().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> fresh.advanceItemEffect(-1));
    }

    @Test void previewIsReadOnlyClippedAndContainsOnlyOccupiedCells() {
        Board board = new Board();
        board.setCell(19, 9, 1); board.setCell(18, 8, 1); board.setCell(17, 8, 1);
        List<CellPosition> preview = new SmallBomb().previewCells(board, 19, 9);
        assertEquals(List.of(new CellPosition(18, 8), new CellPosition(19, 9)), preview);
        assertEquals(1, board.getCell(19, 9));
        assertEquals(1, board.getCell(17, 8));
        assertThrows(UnsupportedOperationException.class, () -> preview.clear());
        assertTrue(new SmallBomb().previewCells(new Board(), 0, 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new SmallBomb().previewCells(board, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new CellPosition(20, 0));
    }
}
