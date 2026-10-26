package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class GhostBlockViewTest {
    private interface FxTask { void run() throws Exception; }
    private void onFx(FxTask task) throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException alreadyStarted) { }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try { task.run(); } catch (Throwable t) { failure.set(t); } finally { done.countDown(); }
        });
        assertTrue(done.await(25, TimeUnit.SECONDS));
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    private Color pixel(WritableImage image, double row, double col) {
        return image.getPixelReader().getColor((int)(col * image.getWidth() / Board.WIDTH),
                (int)(row * image.getHeight() / Board.HEIGHT));
    }

    @Test void rendersLandingAndBothGuidesAndFollowsMovementAtDifferentSizes() throws Exception {
        onFx(() -> {
            Board board = new Board();
            board.lock(new Tetromino(TetrominoType.I, 18, 3));
            GameEngine game = new GameEngine(board, new Tetromino(TetrominoType.O, 2, 3));
            GameView view = new GameView(game);
            Scene scene = new Scene(view, 960, 720);
            try {
                for (int width : new int[]{960, 640}) {
                    view.resize(width, 720); view.applyCss(); view.layout(); view.redraw();
                    Canvas canvas = (Canvas) view.getCenter();
                    WritableImage image = canvas.snapshot(null, null);
                    Color background = pixel(image, 12.5, 1.5);
                    // O는 18행의 받침 위인 16행에 착지한다.
                    assertNotEquals(background, pixel(image, 16.5, 3.5));
                    double outlineBrightness = 0;
                    for (double row = 16.05; row < 16.2; row += .01)
                        outlineBrightness = Math.max(outlineBrightness, pixel(image,row,3.5).getBrightness());
                    assertTrue(outlineBrightness > pixel(image, 16.5, 3.5).getBrightness() + .1,
                            "Outline must remain distinct at either scale");
                    for (double col : new double[]{3 + 2.0/28, 5 - 2.0/28}) {
                        boolean guideVisible = false;
                        for (double row = 5; row < 8; row += .1)
                            guideVisible |= pixel(image, row, col).getBrightness() > background.getBrightness() + .03;
                        assertTrue(guideVisible, "Both drop boundaries must be visible");
                    }
                    Color ghost = pixel(image, 16.5, 3.5);
                    view.setColorBlindMode(true);
                    assertEquals(ghost, pixel(canvas.snapshot(null, null), 16.5, 3.5));
                    view.setColorBlindMode(false);
                }
                view.resize(960, 720); view.layout(); view.redraw();
                capture(scene, "ghost-block");
                game.moveLeft(); game.moveLeft(); view.redraw();
                WritableImage moved = ((Canvas)view.getCenter()).snapshot(null, null);
                assertEquals(pixel(moved, 12.5, 8.5), pixel(moved, 16.5, 3.5));
                assertNotEquals(pixel(moved, 12.5, 8.5), pixel(moved, 18.5, 1.5));
                game.pause(); view.redraw();
                WritableImage paused = ((Canvas)view.getCenter()).snapshot(null,null);
                assertEquals(pixel(moved,18.5,1.5), pixel(paused,18.5,1.5));
                game.resume();
                game.setCurrentBlock(new Tetromino(TetrominoType.T, 3, 3));
                view.redraw(); capture(scene, "ghost-block-t");
            } finally { scene.setRoot(new Pane()); }
        });
    }

    @Test void blackoutHidesGhostAndGuidesTogetherThenRestoresThem() throws Exception {
        onFx(() -> {
            GameEngine game = new GameEngine(new GameConfig(GameMode.ITEM, Difficulty.NORMAL));
            game.configureDemoItems(true, 1);
            game.requestDemoItem(2); game.hardDrop(); game.hardDrop(); game.hardDrop();
            assertTrue(game.snapshot().blackoutActive());
            for (int r = 0; r < Board.HEIGHT; r++)
                for (int c = 0; c < Board.WIDTH; c++) game.getBoard().setCell(r,c,0);
            game.setCurrentBlock(new Tetromino(TetrominoType.O, 0, 3));
            GameView view = new GameView(game);
            Scene scene = new Scene(view, 960, 720);
            try {
                view.applyCss(); view.layout(); view.redraw();
                WritableImage hidden = ((Canvas)view.getCenter()).snapshot(null,null);
                assertEquals(Color.web("#010205"), pixel(hidden,18.5,3.5));
                assertEquals(Color.web("#010205"), pixel(hidden,8.2,3 + 2.0/28));
                game.hardDrop(); view.redraw();
                assertFalse(game.snapshot().blackoutActive());
                assertNotNull(game.snapshot().ghostBlock());
            } finally { scene.setRoot(new Pane()); }
        });
    }

    private void capture(Scene scene, String name) throws Exception {
        WritableImage image = scene.snapshot(null);
        BufferedImage png = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < png.getHeight(); y++)
            for (int x = 0; x < png.getWidth(); x++) png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
        Path path = Path.of("build", "ui-smoke", name + ".png");
        Files.createDirectories(path.getParent()); ImageIO.write(png,"png",path.toFile());
    }
}
