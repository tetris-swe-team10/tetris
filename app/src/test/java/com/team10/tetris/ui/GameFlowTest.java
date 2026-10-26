package com.team10.tetris.ui;

import com.team10.tetris.App;
import com.team10.tetris.game.*;
import com.team10.tetris.score.ScoreboardManager;
import com.team10.tetris.screen.StartScreen;
import com.team10.tetris.screen.ModeSelectionScreen;
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
        try {
            Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(started::countDown);
        }
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
                button(stage.getScene().getRoot(), "게임 시작").fire();
                assertInstanceOf(ModeSelectionScreen.class, stage.getScene().getRoot());
                capture(stage.getScene(), "mode-selection");
                chooseDialog(Difficulty.HARD, true, nestedFailure);
                button(stage.getScene().getRoot(), "일반 모드 시작").fire();
                assertNull(nestedFailure.get());
                assertInstanceOf(ModeSelectionScreen.class, stage.getScene().getRoot());
                chooseDialog(Difficulty.HARD, false, nestedFailure);
                button(stage.getScene().getRoot(), "아이템 모드 시작").fire();
                assertNull(nestedFailure.get());
                GameView view = assertInstanceOf(GameView.class, stage.getScene().getRoot());
                assertEquals(GameMode.ITEM, view.getEngine().getConfig().mode());
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
                AtomicReference<Throwable> resultFailure = new AtomicReference<>();
                Platform.runLater(() -> {
                    try {
                        assertEquals(GameState.PAUSED, engine.getState());
                        DialogPane pane = Window.getWindows().stream().filter(Window::isShowing)
                                .map(w -> w.getScene().getRoot()).filter(r -> r instanceof DialogPane)
                                .map(r -> (DialogPane) r).findFirst().orElseThrow();
                        ((Button) pane.lookupButton(ButtonType.OK)).fire();
                    } catch (Throwable error) { resultFailure.set(error); }
                });
                button(view, "결과 보기").fire();
                if (resultFailure.get() != null) throw new AssertionError(resultFailure.get());
                assertEquals(GameState.CLEARING, engine.getState());
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
                @SuppressWarnings("unchecked")
                ComboBox<GameMode> modeFilter = (ComboBox<GameMode>) records.lookupAll(".combo-box").stream()
                        .map(node -> (ComboBox<?>) node)
                        .filter(choice -> choice.getValue() instanceof GameMode)
                        .findFirst().orElseThrow();
                @SuppressWarnings("unchecked")
                ComboBox<Difficulty> difficultyFilter = (ComboBox<Difficulty>) records.lookupAll(".combo-box").stream()
                        .map(node -> (ComboBox<?>) node)
                        .filter(choice -> choice.getValue() instanceof Difficulty)
                        .findFirst().orElseThrow();
                records.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));
                assertEquals(GameMode.ITEM, modeFilter.getValue());
                records.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT, false, false, false, false));
                assertEquals(GameMode.NORMAL, modeFilter.getValue());
                records.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
                records.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.RIGHT, false, false, false, false));
                assertEquals(Difficulty.HARD, difficultyFilter.getValue());
                assertTrue(records.lookupAll(".label").stream().anyMatch(n -> ((Label)n).getText().equals("HardPlayer")));
                capture(scene, "scoreboard-hard");
            } finally { stage.close(); }
        });
    }

    @Test void smallBombIsAnnouncedInNextAndWorksInBothPalettes() throws Exception {
        onFx(() -> {
            GameEngine engine = new GameEngine(new Board(), null,
                    new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                    () -> new Tetromino(TetrominoType.O, 0, 3), new StandardScoringPolicy(),
                    piece -> piece.attachItem(new com.team10.tetris.game.item.SmallBomb(), 0, 0));
            // 실제 엔진의 줄 삭제/10줄 경계를 통해 지급한다.
            for (int count : new int[] {4, 4, 2}) {
                Board board = engine.getBoard();
                for (int r = 0; r < Board.HEIGHT; r++)
                    for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
                for (int r = Board.HEIGHT - count; r < Board.HEIGHT; r++)
                    for (int c = 0; c < Board.WIDTH; c++) if (c != 4) board.setCell(r, c, 1);
                Tetromino piece = new Tetromino(TetrominoType.I, 16, 4);
                piece.rotateClockwise(); engine.setCurrentBlock(piece);
                engine.moveDown(); engine.advanceClear(300);
            }
            GameView view = new GameView(engine);
            Stage stage = new Stage();
            Scene scene = new Scene(view, 960, 640);
            stage.setScene(scene); stage.show();
            try {
                view.redraw();
                assertEquals(10, engine.getTotalClearedLines());
                assertTrue(((Label) view.lookup("#next-item-description")).getText().contains("E 작은 폭탄"));
                capture(scene, "small-bomb-next");
                view.setColorBlindMode(true);
                capture(scene, "small-bomb-next-colorblind");
                var predicted = engine.snapshot().nextBlock().item();
                view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));
                assertEquals(predicted, engine.snapshot().currentBlock().item());
                assertEquals("", ((Label) view.lookup("#next-item-description")).getText());
                capture(scene, "small-bomb-active-colorblind");

                Board board = engine.getBoard();
                for (int r = 0; r < Board.HEIGHT; r++)
                    for (int c = 0; c < Board.WIDTH; c++) board.setCell(r, c, 0);
                board.lock(new Tetromino(TetrominoType.I, 17, 3));
                board.lock(new Tetromino(TetrominoType.O, 18, 0));
                board.setCell(18, 4, 1);
                Tetromino bomb = new Tetromino(TetrominoType.I, 19, 3);
                bomb.attachItem(new com.team10.tetris.game.item.SmallBomb(), 0, 1);
                engine.setCurrentBlock(bomb);
                view.setColorBlindMode(false);
                capture(scene, "small-bomb-before");
                int score = engine.getScore();
                view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));
                assertEquals(score, engine.getScore());
                assertEquals(1, board.getCell(17, 4));
                assertEquals(0, board.getCell(18, 4));
                assertEquals(0, board.getCell(19, 4));
                assertEquals(1, board.getCell(19, 6));
                capture(scene, "small-bomb-after");
            } finally {
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.close();
            }
        });
    }
}
