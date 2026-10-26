package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import com.team10.tetris.game.item.RandomItemAssigner;
import com.team10.tetris.game.item.WeightItem;
import com.team10.tetris.game.item.WeightPiece;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class WeightItemViewTest {
    @Test void nextPreviewDemoKeyAndKeyboardFollowWeightRules() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException started) { }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            Scene scene = null;
            try {
                GameEngine game = new GameEngine(new Board(), null, new GameConfig(GameMode.ITEM, Difficulty.NORMAL),
                        () -> new Tetromino(TetrominoType.T, 0, 3), new StandardScoringPolicy(),
                        new RandomItemAssigner(List.of(new WeightItem()), new Random(1)));
                game.configureDemoItems(true, 1);
                for (int c = 0; c < 10; c++) game.getBoard().setCell(19, c, 1);
                game.hardDrop(); game.advanceClear(300); // 1줄 → 새 NEXT가 무게추로 교체
                GameView view = new GameView(game);
                scene = new Scene(view, 960, 720); view.applyCss(); view.layout();

                assertTrue(hasLabel(view, "W 무게추 · 닿으면 아래 4열을 뚫고 내려가며 삭제"));
                assertTrue(view.lookupAll(".label").stream()
                        .anyMatch(n -> ((Label) n).getText().contains("4 무게추")));
                view.fireEvent(key(KeyCode.DIGIT4));
                assertEquals("W", game.demoRequestedSymbol());

                view.fireEvent(key(KeyCode.SPACE)); // T 하드 드롭 → 무게추가 현재 블록
                WeightPiece weight = assertInstanceOf(WeightPiece.class, game.getCurrentBlock());
                assertTrue(game.snapshot().currentBlock().isItemPiece());
                PieceSnapshot before = game.snapshot().currentBlock();
                view.fireEvent(key(KeyCode.UP));
                assertEquals(before, game.snapshot().currentBlock(), "회전 키 무시");
                view.fireEvent(key(KeyCode.LEFT));
                assertEquals(2, weight.getCol());
                view.fireEvent(key(KeyCode.SPACE)); // 닿음 → 바닥까지 파괴 → 사라짐
                assertNotSame(weight, game.getCurrentBlock());
                view.setColorBlindMode(true); view.redraw();
            } catch (Throwable t) { failure.set(t); }
            finally { if (scene != null) scene.setRoot(new StackPane()); done.countDown(); }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    private boolean hasLabel(GameView view, String text) {
        return view.lookupAll(".label").stream().anyMatch(node -> ((Label) node).getText().equals(text));
    }
}