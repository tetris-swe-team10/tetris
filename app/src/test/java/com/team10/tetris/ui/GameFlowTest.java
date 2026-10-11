package com.team10.tetris.ui;

import com.team10.tetris.App;
import com.team10.tetris.game.*;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.StartScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

/** JavaFX 실제 Scene의 선택·표시·기록 흐름을 검증한다. 결과 PNG는 build/ui-smoke에 저장. */
@Tag("ui")
class GameFlowTest {
    @TempDir Path temp;
    @BeforeAll static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(15, TimeUnit.SECONDS));
    }
    private interface FxTask { void run() throws Exception; }
    private void onFx(FxTask task) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try { task.run(); } catch (Throwable e) { failure.set(e); } finally { done.countDown(); }
        });
        assertTrue(done.await(25, TimeUnit.SECONDS), "JavaFX task timed out");
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
    private Button button(Parent root, String text) {
        return root.lookupAll(".button").stream().filter(n -> n instanceof Button b && b.getText().equals(text))
                .map(n -> (Button)n).findFirst().orElseThrow();
    }
    private void capture(Scene scene, String name) throws Exception {
        scene.getRoot().applyCss(); scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        BufferedImage png = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < png.getHeight(); y++)
            for (int x = 0; x < png.getWidth(); x++) png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        Path out = Path.of("build", "ui-smoke", name + ".png");
        Files.createDirectories(out.getParent());
        ImageIO.write(png, "png", out.toFile());
    }
    private void chooseDialog(Difficulty difficulty, boolean cancel, AtomicReference<Throwable> failure) {
        Platform.runLater(() -> {
            DialogPane pane = null;
            try {
                pane = Window.getWindows().stream().filter(Window::isShowing)
                        .map(w -> w.getScene().getRoot()).filter(r -> r instanceof DialogPane)
                        .map(r -> (DialogPane)r).findFirst().orElseThrow();
                if (!cancel) {
                    @SuppressWarnings("unchecked")
                    ComboBox<Difficulty> choice = (ComboBox<Difficulty>)pane.lookup(".combo-box");
                    choice.setValue(difficulty);
                }
                capture(pane.getScene(), cancel ? "difficulty-cancel" : "difficulty-hard");
                ((Button)pane.lookupButton(cancel ? ButtonType.CANCEL : ButtonType.OK)).fire();
            } catch (Throwable e) {
                failure.set(e);
                if (pane != null) ((Button)pane.lookupButton(ButtonType.CANCEL)).fire();
            }
        });
    }

    @Test void startMenuRequiresDifficultySelectionAndCancelKeepsMenu() throws Exception {
        onFx(() -> {
            Stage stage = new Stage();
            try {
                new App().start(stage);
                stage.getScene().getRoot().applyCss();
                AtomicReference<Throwable> nestedFailure = new AtomicReference<>();
                chooseDialog(Difficulty.HARD, true, nestedFailure);
                button(stage.getScene().getRoot(), "게임 시작").fire();
                assertNull(nestedFailure.get());
                assertInstanceOf(StartScreen.class, stage.getScene().getRoot());
                chooseDialog(Difficulty.HARD, false, nestedFailure);
                button(stage.getScene().getRoot(), "게임 시작").fire();
                assertNull(nestedFailure.get());
                GameView view = assertInstanceOf(GameView.class, stage.getScene().getRoot());
                assertEquals(Difficulty.HARD, view.getEngine().getConfig().difficulty());
                assertEquals(0, view.getScore());
                assertTrue(view.lookupAll(".combo-box").isEmpty(), "No in-game difficulty editing");
                view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
                assertEquals(100, view.getScore());
                capture(stage.getScene(), "game-hard");
                stage.getScene().setRoot(new javafx.scene.layout.Pane()); // stop Timeline
            } finally { stage.close(); }
        });
    }

    @Test void clearRowsAreVisibleAndResultSavesTheSameHardScore() throws Exception {
        onFx(() -> {
            Board board = new Board();
            for (int col = 0; col < Board.WIDTH; col++) if (col < 3 || col > 6) board.setCell(19, col, 1);
            GameConfig config = new GameConfig(GameMode.NORMAL, Difficulty.HARD);
            GameEngine engine = new GameEngine(board, new Tetromino(TetrominoType.I, 19, 3), config,
                    () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy());
            GameView view = new GameView(engine);
            Stage stage = new Stage();
            Scene scene = new Scene(view, 960, 640);
            stage.setScene(scene); stage.show();
            try {
                engine.moveDown(); view.redraw();
                assertEquals(GameState.CLEARING, engine.getState());
                assertTrue(view.lookupAll(".label").stream().anyMatch(n -> ((Label)n).getText().equals("LINE CLEAR")));
                capture(scene, "line-clear");
                button(view, "일시정지").fire();
                assertEquals(GameState.PAUSED, engine.getState());
                button(view, "계속하기").fire();
                assertEquals(GameState.CLEARING, engine.getState());
                assertEquals("일시정지", button(view, "일시정지").getText());
                engine.advanceClear(300); view.redraw();
                assertEquals(1000, view.getScore());
                assertTrue(view.lookupAll(".label").stream().anyMatch(n -> ((Label)n).getText().equals("1000")));
                ScoreboardManager manager = new ScoreboardManager(temp.resolve("scores.tsv"));
                for (int i = 1; i <= 9; i++) {
                    manager.saveAndGetRank("Hard" + i, i * 100, 1, GameMode.NORMAL, Difficulty.HARD);
                }
                GameOverView result = new GameOverView(manager);
                scene.setRoot(result);
                result.showResult(view.getScore(), engine.getTotalClearedLines(), config);
                result.applyCss(); result.layout();
                ((TextField)result.lookup(".text-field")).setText("HardPlayer");
                button(result, "저장").fire();
                assertEquals(1000, manager.getHighScore(GameMode.NORMAL, Difficulty.HARD));
                assertEquals(10, manager.getRecords(GameMode.NORMAL, Difficulty.HARD).size());
                assertEquals(0, manager.getHighScore(GameMode.NORMAL, Difficulty.NORMAL));
                capture(scene, "result-hard");
                ScoreboardView records = new ScoreboardView(manager);
                scene.setRoot(records); records.applyCss(); records.layout();
                for (Node node : records.lookupAll(".combo-box")) {
                    @SuppressWarnings("unchecked") ComboBox<Object> choice = (ComboBox<Object>)node;
                    if (choice.getValue() instanceof Difficulty) choice.setValue(Difficulty.HARD);
                }
                assertTrue(records.lookupAll(".label").stream().anyMatch(n -> ((Label)n).getText().equals("HardPlayer")));
                capture(scene, "scoreboard-hard");
            } finally { stage.close(); }
        });
    }
}
