package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import com.team10.tetris.game.versus.AttackLines;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** 엔진 없이 스냅샷만으로 그리는 공용 렌더러 검증 (P2P 상대 보드와 같은 조건). */
@Tag("ui")
class CanvasBoardRendererTest {

    private static final int BOTTOM = Board.HEIGHT - 1;

    /** 맨 아래 줄: 0열 회색 공격 칸, 1열 I 블록 칸. 나머지는 비어 있다. */
    private static GameSnapshot bottomRowSnapshot() {
        List<List<Integer>> cells = new ArrayList<>();
        List<List<TetrominoType>> types = new ArrayList<>();
        List<List<Boolean>> garbage = new ArrayList<>();
        for (int r = 0; r < Board.HEIGHT; r++) {
            cells.add(new ArrayList<>(Collections.nCopies(Board.WIDTH, 0)));
            types.add(new ArrayList<>(Collections.nCopies(Board.WIDTH, (TetrominoType) null)));
            garbage.add(new ArrayList<>(Collections.nCopies(Board.WIDTH, false)));
        }
        cells.get(BOTTOM).set(0, 1); // 회색 여부는 셀 값이 아니라 garbageCells로 판단
        garbage.get(BOTTOM).set(0, true);
        cells.get(BOTTOM).set(1, 1);
        types.get(BOTTOM).set(1, TetrominoType.I);
        PieceSnapshot next = new PieceSnapshot(TetrominoType.O, 0, 4, List.of(List.of(1, 1), List.of(1, 1)));
        return new GameSnapshot(cells, null, next, GameConfig.DEFAULT, GameState.PLAYING, 0, 0, 1000,
                List.of(), types, false, false, List.of(), false, garbage, AttackLines.EMPTY);
    }

    private static Color centerOf(WritableImage image, double cell, int row, int col) {
        return image.getPixelReader().getColor((int) (col * cell + cell / 2), (int) (row * cell + cell / 2));
    }

    @Test void drawsGarbageGrayAndPaletteCellsFromSnapshotOnly() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            Canvas canvas = new Canvas(280, 560);
            renderer.drawBoard(canvas, bottomRowSnapshot());
            WritableImage image = canvas.snapshot(null, null);

            assertEquals(CanvasBoardRenderer.GARBAGE.fill(), centerOf(image, 28, BOTTOM, 0), "공격 칸은 회색");
            assertEquals(Color.web("#22C7E8"), centerOf(image, 28, BOTTOM, 1), "일반 칸은 팔레트 색");
            assertEquals(CanvasBoardRenderer.BOARD_BACKGROUND, centerOf(image, 28, BOTTOM, 2), "빈칸은 배경");
        });
    }

    @Test void colorBlindPaletteChangesBlocksButNotGarbage() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            renderer.setPalette(BlockPalette.COLOR_BLIND);
            Canvas canvas = new Canvas(280, 560);
            renderer.drawBoard(canvas, bottomRowSnapshot());
            WritableImage image = canvas.snapshot(null, null);

            assertEquals(CanvasBoardRenderer.GARBAGE.fill(), centerOf(image, 28, BOTTOM, 0));
            assertNotEquals(Color.web("#22C7E8"), centerOf(image, 28, BOTTOM, 1), "색맹 팔레트로 바뀜");
        });
    }

    @Test void cellSizeFollowsCanvasWidth() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            Canvas canvas = new Canvas(160, 320);
            renderer.drawBoard(canvas, bottomRowSnapshot());
            WritableImage image = canvas.snapshot(null, null);

            assertEquals(CanvasBoardRenderer.GARBAGE.fill(), centerOf(image, 16, BOTTOM, 0));
            assertEquals(Color.web("#22C7E8"), centerOf(image, 16, BOTTOM, 1));
        });
    }

    @Test void pendingAttackIsBottomAlignedWithHoles() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            Canvas canvas = new Canvas(100, 100); // 칸 10px, 최대 10줄
            List<Boolean> holeAtZero = new ArrayList<>(Collections.nCopies(Board.WIDTH, true));
            holeAtZero.set(0, false);
            List<Boolean> holeAtNine = new ArrayList<>(Collections.nCopies(Board.WIDTH, true));
            holeAtNine.set(9, false);
            renderer.drawPendingAttack(canvas, new AttackLines(List.of(holeAtZero, holeAtNine)));
            WritableImage image = canvas.snapshot(null, null);

            // 마지막 줄(holeAtNine)이 맨 아래, 그 위에 첫 줄(holeAtZero)
            assertEquals(CanvasBoardRenderer.GARBAGE.fill(), centerOf(image, 10, 9, 0));
            assertEquals(CanvasBoardRenderer.PANEL_BACKGROUND, centerOf(image, 10, 9, 9));
            assertEquals(CanvasBoardRenderer.PANEL_BACKGROUND, centerOf(image, 10, 8, 0));
            assertEquals(CanvasBoardRenderer.GARBAGE.fill(), centerOf(image, 10, 8, 9));
            assertEquals(CanvasBoardRenderer.PANEL_BACKGROUND, centerOf(image, 10, 7, 5), "그 위는 비어 있음");
        });
    }

    @Test void emptyPendingAttackClearsArea() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            Canvas canvas = new Canvas(100, 100);
            List<Boolean> full = Arrays.asList(new Boolean[Board.WIDTH]);
            Collections.fill(full, true);
            renderer.drawPendingAttack(canvas, new AttackLines(List.of(full)));
            renderer.drawPendingAttack(canvas, AttackLines.EMPTY);
            WritableImage image = canvas.snapshot(null, null);

            assertEquals(CanvasBoardRenderer.PANEL_BACKGROUND, centerOf(image, 10, 9, 5), "이전 공격 줄이 지워짐");
        });
    }

    @Test void drawsNextBlockCentered() throws Exception {
        onFx(() -> {
            CanvasBoardRenderer renderer = new CanvasBoardRenderer();
            Canvas canvas = new Canvas(120, 120); // 칸 24px, O 블록 48px가 가운데(36~84)
            renderer.drawNext(canvas, bottomRowSnapshot());
            WritableImage image = canvas.snapshot(null, null);

            assertEquals(Color.web("#F4C430"), image.getPixelReader().getColor(48, 48));
            assertEquals(CanvasBoardRenderer.PANEL_BACKGROUND, image.getPixelReader().getColor(10, 10));
        });
    }

    private static void onFx(Runnable body) throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException alreadyStarted) { /* 다른 UI 테스트가 초기화 */ }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try { body.run(); } catch (Throwable error) { failure.set(error); } finally { done.countDown(); }
        });
        assertTrue(done.await(10, TimeUnit.SECONDS));
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
}
