package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import com.team10.tetris.screen.ModeSelectionScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class ModeSelectionLayoutTest {
    private static void fx(Runnable task) throws Exception {
        try {Platform.startup(()->Platform.setImplicitExit(false));}catch(IllegalStateException initialized){}
        CountDownLatch done=new CountDownLatch(1);AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{try{task.run();}catch(Throwable t){failure.set(t);}finally{done.countDown();}});
        assertTrue(done.await(30,TimeUnit.SECONDS));if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    @Test void oneScreenReflowsAcrossWindowPortraitAndFullScreenAndKeepsActions() throws Exception {
        fx(()->{
            AtomicReference<GameMode> chosen=new AtomicReference<>();AtomicInteger records=new AtomicInteger(),back=new AtomicInteger();
            ModeSelectionScreen root=new ModeSelectionScreen(chosen::set,records::incrementAndGet,back::incrementAndGet);
            double normalWidth=0;
            for(int[] size:new int[][]{{960,720},{640,900},{1920,1080},{640,480},{960,720}}) {
                Scene scene=new Scene(root,size[0],size[1]);root.applyCss();root.layout();root.applyCss();root.layout();
                GridPane grid=(GridPane)root.lookup("#mode-cards");
                Region normal=(Region)root.lookup(".normal-card"), item=(Region)root.lookup(".item-card");
                if(size[0]<760) assertTrue(item.getBoundsInParent().getMinY()>normal.getBoundsInParent().getMaxY());
                else assertTrue(item.getBoundsInParent().getMinX()>normal.getBoundsInParent().getMaxX());
                assertTrue(grid.getWidth()<=root.getWidth());
                for(Region card:List.of(normal,item)) {
                    assertTrue(card.getWidth()>180);
                    Button start=(Button)card.lookup(".mode-primary-button");
                    var bounds=start.getBoundsInParent();
                    assertTrue(bounds.getMinX()>=0 && bounds.getMaxX()<=card.getWidth()+1);
                    assertTrue(bounds.getMinY()>=0 && bounds.getMaxY()<=card.getHeight()+1);
                }
                if(size[1]>=700) {
                    for(var node:root.lookupAll(".mode-secondary-button"))
                        assertTrue(node.localToScene(node.getBoundsInLocal()).getMaxY()<=size[1]+1);
                }
                if(size[0]==960) normalWidth=normal.getWidth();
                if(size[0]==1920) assertTrue(normal.getWidth()>normalWidth);
                capture(scene,"mode-selection-"+size[0]+"x"+size[1]);
                ((Button)normal.lookup(".mode-primary-button")).fire();assertEquals(GameMode.NORMAL,chosen.get());
                root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.RIGHT,false,false,false,false));
                root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.ENTER,false,false,false,false));
                assertEquals(GameMode.ITEM,chosen.get());
                root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.LEFT,false,false,false,false));
                scene.setRoot(new StackPane());
            }
            Scene scene=new Scene(root,960,720);root.applyCss();root.layout();
            root.lookupAll(".button").stream().filter(n->n instanceof Button button && button.getText().equals("기록 보기"))
                    .map(n->(Button)n).findFirst().orElseThrow().fire();
            assertEquals(1,records.get());
            root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.ESCAPE,false,false,false,false));
            assertEquals(1,back.get());scene.setRoot(new StackPane());
        });
    }
    @Test void gameShowsActualModeAndDifficultyForAllSixSessions() throws Exception {
        fx(()->{
            for(GameMode mode:GameMode.values())for(Difficulty difficulty:Difficulty.values()) {
                GameEngine engine=new GameEngine(new GameConfig(mode,difficulty));GameView root=new GameView(engine);
                Scene scene=new Scene(root,960,720);root.applyCss();root.layout();
                Label modeLabel=(Label)root.lookup("#current-mode"), level=(Label)root.lookup("#current-difficulty");
                assertEquals(mode+" 모드",modeLabel.getText());assertEquals("난이도 · "+difficulty,level.getText());
                for(Label label:List.of(modeLabel,level)) {
                    var bounds=label.localToScene(label.getBoundsInLocal());
                    assertTrue(bounds.getMinX()>=0 && bounds.getMaxX()<=960);
                    assertTrue(bounds.getMinY()>=0 && bounds.getMaxY()<=720);
                }
                capture(scene,"session-"+mode.name()+"-"+difficulty.name());
                root.getEngine().pause();root.redraw();assertEquals("난이도 · "+difficulty,level.getText());
                scene.setRoot(new StackPane());
            }
        });
    }
    private static void capture(Scene scene,String name) {
        try {
            var image=scene.snapshot(null);BufferedImage png=new BufferedImage((int)image.getWidth(),(int)image.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<png.getHeight();y++)for(int x=0;x<png.getWidth();x++)png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
            Path file=Path.of("build/ui-smoke/"+name+".png");Files.createDirectories(file.getParent());ImageIO.write(png,"png",file.toFile());
        }catch(java.io.IOException error){throw new AssertionError(error);}
    }
}
