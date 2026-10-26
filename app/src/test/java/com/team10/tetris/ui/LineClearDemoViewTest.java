package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class LineClearDemoViewTest {
    @Test void demoKeyFiveRequestsLineClearOnlyInDemo() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException started) { }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            Scene scene = null;
            try {
                GameEngine game = new GameEngine(new GameConfig(GameMode.ITEM, Difficulty.NORMAL));
                GameView view = new GameView(game);
                scene = new Scene(view, 960, 720); view.applyCss(); view.layout();

                view.fireEvent(key(KeyCode.DIGIT5)); // 데모 OFF → 예약하지 않음
                assertEquals("", game.demoRequestedSymbol());

                game.configureDemoItems(true, 10);
                view.fireEvent(key(KeyCode.NUMPAD5));
                assertEquals("L", game.demoRequestedSymbol());
                view.redraw();
                assertTrue(view.lookupAll(".label").stream()
                        .anyMatch(n -> ((Label) n).getText().contains("5 줄 삭제")));
            } catch (Throwable t) { failure.set(t); }
            finally { if (scene != null) scene.setRoot(new StackPane()); done.countDown(); }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
