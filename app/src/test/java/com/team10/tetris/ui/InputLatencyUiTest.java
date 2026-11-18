package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class InputLatencyUiTest {
    private static void fx(Runnable action) throws Exception {
        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> { try { action.run(); done.complete(null); } catch (Throwable t) { done.completeExceptionally(t); } });
        done.get(10, TimeUnit.SECONDS); // Deadlock guard only; latency is measured separately.
    }
    @Test void threeInputsPerSecondReachTheModelAndRenderedCanvasWithinOneSecond() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); } catch (IllegalStateException alreadyStarted) { }
        AtomicReference<GameView> view = new AtomicReference<>();
        AtomicReference<Stage> stage = new AtomicReference<>();
        GameEngine engine = new GameEngine(new Board(), new Tetromino(TetrominoType.O, 0, 3));
        engine.setBaseDropIntervalMs(60_000); // Isolate keyboard movement from automatic falling.
        StringBuilder csv = new StringBuilder("index,latency_ns\n");
        try {
            fx(() -> {
                GameView game = new GameView(engine); view.set(game);
                Stage window = new Stage(); stage.set(window);
                window.setScene(new Scene(game, 960, 720)); window.show(); game.applyCss(); game.layout();
                ((Canvas)game.lookup("#game-board")).snapshot(null, null);
            });
            for (int i = 1; i <= 6; i++) {
                if (i > 1) Thread.sleep(334);
                int expectedRow = i;
                long start = System.nanoTime(); // Includes the JavaFX event queue delay.
                fx(() -> {
                    Canvas board = (Canvas)view.get().lookup("#game-board");
                    var beforeImage = board.snapshot(null, null);
                    double cell = beforeImage.getWidth() / Board.WIDTH;
                    int x = (int) (3.5 * cell);
                    int y = (int) ((expectedRow + 1.5) * cell);
                    int before = beforeImage.getPixelReader().getArgb(x, y);
                    view.get().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
                    assertEquals(expectedRow, engine.snapshot().currentBlock().row());
                    int after = board.snapshot(null, null).getPixelReader().getArgb(x, y);
                    assertNotEquals(before, after, "New occupied cell must be drawn after every input");
                    long latency = System.nanoTime() - start;
                    assertTrue(latency < 1_000_000_000L, "Event-to-canvas latency ns=" + latency);
                    csv.append(expectedRow).append(',').append(latency).append('\n');
                });
            }
            Path output = Path.of("build/performance/input-ui.csv");
            Files.createDirectories(output.getParent()); Files.writeString(output, csv);
        } finally {
            fx(() -> { if (stage.get() != null) { stage.get().getScene().setRoot(new StackPane()); stage.get().close(); } });
        }
    }
}
