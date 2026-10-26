package com.team10.tetris.ui;

import com.team10.tetris.input.MenuKeyBindings;
import com.team10.tetris.screen.StartScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class AppFontsViewTest {
    @Test void bundledRegularAndBoldAreActuallySelectedByCss() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException started) { }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                StartScreen root = new StartScreen(Map.of(), MenuKeyBindings.defaults());
                Scene scene = new Scene(root, 960, 720);
                scene.getStylesheets().add(getClass().getResource("/com/team10/tetris/start-screen.css").toExternalForm());
                root.applyCss(); root.layout();
                // Responsive layout updates inline font sizes; resolve that pending CSS pass too.
                root.applyCss();
                Label title = (Label) root.lookup(".title");
                Label guide = (Label) root.lookup(".guide");
                Font canvasBold = AppFonts.bold(18);
                assertEquals("Noto Sans KR", canvasBold.getFamily(), "Canvas font family");
                assertEquals("Bold", canvasBold.getStyle(), "Canvas font weight");
                assertEquals("Noto Sans KR", title.getFont().getFamily(), title.getStyle());
                assertEquals("Bold", title.getFont().getStyle());
                assertEquals("Noto Sans KR", guide.getFont().getFamily());
                assertEquals("Regular", guide.getFont().getStyle());
                assertNotNull(AppFonts.class.getResource("/com/team10/tetris/fonts/OFL.txt"));
                scene.setRoot(new StackPane());
            } catch (Throwable error) { failure.set(error); }
            finally { done.countDown(); }
        });
        assertTrue(done.await(15, TimeUnit.SECONDS));
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
}
