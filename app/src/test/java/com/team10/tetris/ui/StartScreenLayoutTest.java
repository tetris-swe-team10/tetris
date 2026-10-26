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
class StartScreenLayoutTest {
    @Test void menuFitsLandscapePortraitAndFullScreen() throws Exception {
        try {Platform.startup(()->Platform.setImplicitExit(false));} catch(IllegalStateException ready) {}
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{
            try {
                for(int[] size:new int[][]{{960,720},{640,900},{1920,1080},{640,480}}) {
                    StartScreen view=new StartScreen(Map.of(StartMenuAction.START_GAME,()->{},
                            StartMenuAction.OPEN_SETTINGS,()->{},StartMenuAction.OPEN_SCOREBOARD,()->{},
                            StartMenuAction.EXIT,()->{}),MenuKeyBindings.defaults());
                    Scene scene=new Scene(view,size[0],size[1]);
                    scene.getStylesheets().add(getClass().getResource("/com/team10/tetris/start-screen.css").toExternalForm());
                    view.applyCss(); view.layout();
                    for(var node:view.lookupAll(".menu-button")) {
                        var bounds=node.localToScene(node.getBoundsInLocal());
                        assertTrue(bounds.getMinX()>=0 && bounds.getMaxX()<=size[0]);
                        assertTrue(bounds.getMinY()>=0 && bounds.getMaxY()<=size[1]);
                        assertTrue(((Button)node).getWidth()>150);
                    }
                    WritableImage image=scene.snapshot(null);
                    BufferedImage png=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_ARGB);
                    for(int y=0;y<size[1];y++)for(int x=0;x<size[0];x++)png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
                    Path out=Path.of("build/ui-smoke/start-"+size[0]+"x"+size[1]+".png");
                    Files.createDirectories(out.getParent()); ImageIO.write(png,"png",out.toFile());
                }
            }catch(Throwable t){failure.set(t);}finally{done.countDown();}
        });
        assertTrue(done.await(30,TimeUnit.SECONDS));
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
}
