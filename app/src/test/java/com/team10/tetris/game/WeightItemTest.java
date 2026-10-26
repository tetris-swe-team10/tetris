package com.team10.tetris.game;

import com.team10.tetris.game.item.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** 무게추(W) 규칙 W-1~W-9 검증. */
class WeightItemTest {
    private final WeightItem weight = new WeightItem();

    /** 다음 블록은 항상 O, 지급은 항상 W인 테스트용 엔진. */
    private GameEngine game(Board board, Tetromino current) {
        return new GameEngine(board, current, new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                new RandomItemAssigner(List.of(weight), new Random(1)));
    }

    private WeightPiece piece(int row, int col) {
        return (WeightPiece) weight.createPiece(row, col);
    }

    /** 16~19행을 0~8열까지 채운다(9열은 비워 완성 행 없음). 10행 8열에 떠 있는 칸 하나. */
    private static Board stacked() {
        Board board = new Board();
        for (int r = 16; r < Board.HEIGHT; r++)
            for (int c = 0; c < 9; c++) board.setCell(r, c, 1);
        board.setCell(10, 8, 1);
        return board;
    }

    private static int filled(Board board) {
        int count = 0;
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) if (!board.isEmpty(r, c)) count++;
        return count;
    }

    /** 무게추가 3~6열을 16~19행까지 모두 뚫었고 범위 밖 칸은 그대로인지. */
    private static void assertDrilledThrough(Board board) {
        for (int r = 16; r < Board.HEIGHT; r++) {
            for (int c = 0; c < Board.WIDTH; c++) {
                boolean drilled = c >= 3 && c <= 6;
                int expected = drilled || c == 9 ? 0 : 1;
                assertEquals(expected, board.getCell(r, c), r + "," + c);
            }
        }
        assertEquals(1, board.getCell(10, 8), "범위 밖 떠 있는 칸 유지");
    }

    private void clear(GameEngine game, int rows) {
        Board board = game.getBoard();
        for (int r = 0; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
        for (int r = Board.HEIGHT - rows; r < Board.HEIGHT; r++)
            for (int c = 0; c < Board.WIDTH; c++) if (c != 4) board.setCell(r, c, 1);
        Tetromino piece = new Tetromino(TetrominoType.I, 16, 4);
        piece.rotateClockwise();
        game.setCurrentBlock(piece);
        game.moveDown(); game.advanceClear(300);
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void fixedShapeSymbolAndNoRotation() {
        WeightPiece w = piece(5, 3);
        assertNull(w.getType());
        assertArrayEquals(new int[] {0, 1, 1, 0}, w.getShape()[0]);
        assertArrayEquals(new int[] {1, 1, 1, 1}, w.getShape()[1]);
        w.getShape()[0][0] = 9;
        assertEquals(0, w.getShape()[0][0], "모양은 복사본으로 반환");
        assertEquals("W", weight.symbol());
        assertTrue(weight.description().startsWith("무게추"));
        assertDoesNotThrow(() -> new RandomItemAssigner(List.of(new SmallBomb(), new Blackout(),
                new RotationLock(), new LineClearItem(), weight), new Random()));

        assertFalse(w.canRotate());
        assertTrue(new Tetromino(TetrominoType.T, 0, 3).canRotate());
        w.rotateClockwise(); w.rotateCounterClockwise();
        assertEquals(0, w.getRotation());
        GameEngine game = game(new Board(), w);
        PieceSnapshot before = game.snapshot().currentBlock();
        assertFalse(game.rotateClockwise());
        new GameController(game).handle(GameCommand.ROTATE);
        assertEquals(before, game.snapshot().currentBlock());

        assertTrue(before.isItemPiece());
        assertEquals("W", before.item().symbol());
        Tetromino bomb = new Tetromino(TetrominoType.O, 0, 3);
        bomb.attachItem(new SmallBomb(), 0, 0);
        assertFalse(PieceSnapshot.of(bomb).isItemPiece());
        assertFalse(PieceSnapshot.of(new Tetromino(TetrominoType.O, 0, 3)).isItemPiece());
    }

    @Test void beforeContactMovesSidewaysAndScoresLikeANormalBlock() {
        WeightPiece w = piece(0, 3);
        GameEngine game = game(new Board(), w);
        assertTrue(game.moveLeft());
        assertEquals(2, w.getCol());
        while (game.moveRight()) { }
        assertEquals(6, w.getCol(), "4칸 너비라 오른쪽 벽은 6열");
        while (game.moveLeft()) { }
        assertEquals(0, w.getCol());
        assertTrue(game.moveDown());
        game.tick();
        assertEquals(2, w.getRow());
        assertEquals(200, game.getScore());
        assertFalse(w.isAnchored());
    }

    @Test void emptyBoardReachesFloorAndVanishesWithoutLocking() {
        WeightPiece w = piece(17, 3);
        GameEngine game = game(new Board(), w);
        assertTrue(game.moveDown());
        assertFalse(game.moveDown());
        assertNotSame(w, game.getCurrentBlock());
        assertEquals(TetrominoType.O, game.getCurrentBlock().getType());
        assertEquals(0, filled(game.getBoard()), "무게추는 보드에 남지 않는다");
        assertEquals(100, game.getScore());
        assertEquals(0, game.getTotalClearedLines());
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void contactLocksSidesAndDrillsOneRowPerTickWithoutPoints() {
        Board board = stacked();
        WeightPiece w = piece(13, 3);
        GameEngine game = game(board, w);
        GameController controller = new GameController(game);
        controller.advance(1000);
        assertEquals(14, w.getRow());
        assertEquals(100, game.getScore());

        controller.advance(1000); // 16행에 닿음
        assertTrue(w.isAnchored());
        assertEquals(14, w.getRow());
        assertFalse(game.moveLeft()); assertFalse(game.moveRight());
        assertEquals(3, w.getCol());
        assertEquals(1, board.getCell(16, 3), "닿는 순간에는 아직 파괴하지 않음");

        controller.advance(1000);
        assertEquals(15, w.getRow());
        for (int c = 3; c <= 6; c++) assertEquals(0, board.getCell(16, c));
        assertEquals(1, board.getCell(16, 2)); assertEquals(1, board.getCell(16, 7));
        assertEquals(1, board.getCell(17, 3), "아직 지나가지 않은 행은 유지");
        assertSame(w, game.getCurrentBlock());

        controller.advance(3000); // 17·18·19행 → 바닥 도달 즉시 사라짐
        assertNotSame(w, game.getCurrentBlock());
        assertDrilledThrough(board);
        assertEquals(100, game.getScore(), "파괴 구간·파괴 칸은 무득점");
        assertEquals(0, game.getTotalClearedLines());
        assertEquals(1000, game.getDropIntervalMs());
        assertTrue(game.getClearingRows().isEmpty());
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void downKeyAndHardDropFinishTheRemainingDrill() {
        for (int mode = 0; mode < 3; mode++) {
            Board board = stacked();
            WeightPiece w = piece(14, 3);
            GameEngine game = game(board, w);
            assertFalse(game.moveDown()); // 닿음
            assertTrue(w.isAnchored());
            switch (mode) {
                case 0 -> assertFalse(game.moveDown());
                case 1 -> assertEquals(0, game.hardDrop());
                default -> new GameController(game).handle(GameCommand.DOWN);
            }
            assertNotSame(w, game.getCurrentBlock());
            assertDrilledThrough(board);
            assertEquals(0, game.getScore());
        }
    }

    @Test void hardDropFromTheTopFallsThenDrillsToTheFloor() {
        Board board = stacked();
        WeightPiece w = piece(0, 3);
        GameEngine game = game(board, w);
        assertEquals(14, game.hardDrop());
        assertEquals(1400, game.getScore(), "닿기 전 낙하만 득점");
        assertNotSame(w, game.getCurrentBlock());
        assertDrilledThrough(board);
    }

    @Test void pauseStopsBothMovementAndDrilling() {
        Board board = stacked();
        WeightPiece w = piece(14, 3);
        GameEngine game = game(board, w);
        game.moveDown();
        GameController controller = new GameController(game);
        controller.pause();
        controller.advance(10_000); game.tick();
        assertFalse(game.moveDown()); assertEquals(0, game.hardDrop());
        assertFalse(game.moveLeft());
        assertEquals(14, w.getRow());
        assertEquals(1, board.getCell(16, 3));
        assertTrue(game.snapshot().currentBlock().isItemPiece());
        controller.resume();
        controller.advance(999);
        assertEquals(14, w.getRow());
        controller.advance(1);
        assertEquals(15, w.getRow());
        assertEquals(0, board.getCell(16, 3));
    }

    @Test void blackoutOnTheWeightEndsWhenItTouches() {
        GameEngine game = game(new Board(), null);
        game.configureDemoItems(true, 10);
        assertTrue(game.requestDemoItem(2)); game.hardDrop();
        assertTrue(game.requestDemoItem(4)); game.hardDrop();
        assertInstanceOf(WeightPiece.class, game.getNextBlock());
        game.hardDrop(); // B 착지 → 다음 블록(W)에 암전
        WeightPiece w = assertInstanceOf(WeightPiece.class, game.getCurrentBlock());
        assertTrue(game.snapshot().blackoutActive());
        while (!w.isAnchored()) game.tick();
        assertFalse(game.snapshot().blackoutActive());
        assertEquals(1, game.getBoard().getCell(14, 3));
        game.moveDown();
        assertEquals(0, filled(game.getBoard()), "쌓인 O 세 개를 모두 뚫음");
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test void demoShortcutAndBlockedSpawnGameOver() {
        GameEngine game = game(new Board(), null);
        game.configureDemoItems(true, 10);
        assertTrue(game.requestDemoItem(4));
        assertEquals("W", game.demoRequestedSymbol());
        game.hardDrop();
        Tetromino next = game.getNextBlock();
        assertInstanceOf(WeightPiece.class, next);
        assertEquals(0, next.getRow()); assertEquals(3, next.getCol());
        assertTrue(game.snapshot().nextBlock().isItemPiece());
        game.getBoard().setCell(1, 6, 1); // 무게추 아랫줄(1행 3~6열)을 막음
        game.hardDrop();
        assertTrue(game.isGameOver());
        assertFalse(game.moveDown());
        assertEquals("", game.demoRequestedSymbol());
    }

    @Test void tenLineBoundaryReplacesOnlyTheNewNext() {
        GameEngine game = game(new Board(), null);
        clear(game, 4); clear(game, 4); clear(game, 1);
        assertFalse(game.getNextBlock() instanceof WeightPiece);
        Tetromino oldNext = game.getNextBlock();
        clear(game, 1);
        assertEquals(10, game.getTotalClearedLines());
        assertSame(oldNext, game.getCurrentBlock(), "이미 보인 NEXT는 바꾸지 않음");
        Tetromino awarded = game.getNextBlock();
        assertInstanceOf(WeightPiece.class, awarded);
        assertEquals("W", PieceSnapshot.of(awarded).item().symbol());
        game.hardDrop();
        assertSame(awarded, game.getCurrentBlock());
        assertFalse(game.getNextBlock() instanceof WeightPiece);
    }

    @Test void assignerAttachesOrReplacesAndKeepsLambdaCompatibility() {
        for (int index = 0; index < 2; index++) {
            final int chosen = index;
            Random random = new Random() {
                @Override public int nextInt(int bound) { return Math.min(chosen, bound - 1); }
            };
            Tetromino original = new Tetromino(TetrominoType.O, 0, 3);
            Tetromino result = new RandomItemAssigner(List.of(new SmallBomb(), weight), random)
                    .award(original, new PenaltyController());
            if (chosen == 0) {
                assertSame(original, result);
                assertEquals("E", result.getItem().item().symbol());
            } else {
                assertInstanceOf(WeightPiece.class, result);
                assertNull(original.getItem());
                assertEquals(0, result.getRow()); assertEquals(3, result.getCol());
            }
        }
        RandomItemAssigner mixed = new RandomItemAssigner(List.of(weight, new SmallBomb()), new Random(3));
        for (int i = 0; i < 20; i++) {
            Tetromino t = new Tetromino(TetrominoType.T, 0, 3);
            mixed.attachTo(t);
            assertEquals("E", t.getItem().item().symbol(), "부착 전용 경로는 W를 고르지 않음");
        }
        RandomItemAssigner onlyWeight = new RandomItemAssigner(List.of(weight), new Random(3));
        assertThrows(IllegalStateException.class, () -> onlyWeight.attachTo(new Tetromino(TetrominoType.T, 0, 3)));
        assertThrows(IllegalStateException.class,
                () -> onlyWeight.attachTo(new Tetromino(TetrominoType.T, 0, 3), new PenaltyController()));

        ItemAssigner lambda = p -> p.attachItem(new SmallBomb(), 0, 0);
        Tetromino plain = new Tetromino(TetrominoType.O, 0, 3);
        assertSame(plain, lambda.award(plain, new PenaltyController()));
        assertEquals("E", plain.getItem().item().symbol());
    }

    @Test void allFiveItemsAreDrawnUniformlyAndPenaltiesKeepW() {
        RandomItemAssigner assigner = new RandomItemAssigner(List.of(new SmallBomb(), new Blackout(),
                new RotationLock(), new LineClearItem(), weight), new Random(2026));
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 5000; i++) {
            Tetromino t = assigner.award(new Tetromino(TetrominoType.O, 0, 3), new PenaltyController());
            counts.merge(t.getItem().item().symbol(), 1, Integer::sum);
        }
        assertEquals(Set.of("E", "B", "K", "L", "W"), counts.keySet());
        for (int count : counts.values()) assertTrue(count > 850 && count < 1150, "균등 1/5: " + counts);

        PenaltyController reserved = new PenaltyController(); reserved.reserve();
        Set<String> symbols = new HashSet<>();
        for (int i = 0; i < 300; i++)
            symbols.add(assigner.award(new Tetromino(TetrominoType.O, 0, 3), reserved).getItem().item().symbol());
        assertEquals(Set.of("E", "L", "W"), symbols);
    }
}