package com.team10.tetris.ui;

import com.team10.tetris.screen.*;
import com.team10.tetris.input.MenuKeyBindings;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class SettingsGameLayoutTest {
    @Test void settingsAndGameScreens() throws Exception {
        try {Platform.startup(()->Platform.setImplicitExit(false));} catch(IllegalStateException ready) {}
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{
            try {
                for(int[] size:new int[][]{{960,720},{640,900},{1920,1080}}) {
                for(boolean settings:new boolean[]{true,false}) {
                    javafx.scene.layout.Region view=settings ? new com.team10.tetris.settings.SettingsScreen(
                            new com.team10.tetris.settings.GameSettings(), mode->{},()->{},()->{})
                            : new GameView(new com.team10.tetris.game.GameEngine(com.team10.tetris.game.GameConfig.DEFAULT));
                    Scene scene=new Scene(view,size[0],size[1]);
                    scene.getStylesheets().add(getClass().getResource("/com/team10/tetris/settings.css").toExternalForm());
                    view.applyCss(); view.layout();
                    if (!settings) {
                        for (var node : view.lookupAll(".button")) {
                            var bounds = node.localToScene(node.getBoundsInLocal());
                            assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= size[0]);
                            assertTrue(bounds.getMinY() >= 0 && bounds.getMaxY() <= size[1]);
                        }
                    }
                    WritableImage image=scene.snapshot(null);
                    BufferedImage png=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_ARGB);
                    for(int y=0;y<size[1];y++)for(int x=0;x<size[0];x++)png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
                    Path out=Path.of("build/ui-smoke/"+(settings?"settings-":"game-")+size[0]+"x"+size[1]+".png");
                    Files.createDirectories(out.getParent()); ImageIO.write(png,"png",out.toFile());
                    scene.setRoot(new javafx.scene.layout.StackPane());
                }
                }
            }catch(Throwable t){failure.set(t);}finally{done.countDown();}
        });
        assertTrue(done.await(30,TimeUnit.SECONDS));
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
}
