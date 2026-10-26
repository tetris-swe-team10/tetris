package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import com.team10.tetris.game.item.RotationLock;
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
class RotationLockViewTest {
    @Test void previewAndLockNoticeFollowActualKeyboardRotation() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch(IllegalStateException started) { }
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(() -> {
            Scene scene=null;
            try {
                GameEngine game=new GameEngine(new Board(),null,new GameConfig(GameMode.ITEM,Difficulty.NORMAL),
                        ()->new Tetromino(TetrominoType.T,0,3),new StandardScoringPolicy(),
                        piece->piece.attachItem(new RotationLock(),0,1));
                game.configureDemoItems(true,1);
                for(int c=0;c<10;c++)game.getBoard().setCell(19,c,1);
                game.hardDrop(); game.advanceClear(300);
                GameView view=new GameView(game);
                scene=new Scene(view,960,720); view.applyCss(); view.layout();
                assertTrue(hasLabel(view,"K 회전 잠금 · 이 블록은 회전 불가"));
                game.hardDrop(); view.redraw();
                assertTrue(hasLabel(view,"K 회전 잠금 · 현재 블록"));
                PieceSnapshot before=game.snapshot().currentBlock();
                view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.UP,false,false,false,false));
                assertEquals(before,game.snapshot().currentBlock());
                game.hardDrop(); view.redraw();
                assertFalse(hasLabel(view,"K 회전 잠금 · 현재 블록"));
                var shape=game.snapshot().currentBlock().shape();
                view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.UP,false,false,false,false));
                assertNotEquals(shape,game.snapshot().currentBlock().shape());
            } catch(Throwable t) {failure.set(t);}
            finally { if(scene!=null)scene.setRoot(new StackPane()); done.countDown(); }
        });
        assertTrue(done.await(20,TimeUnit.SECONDS));
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    private boolean hasLabel(GameView view,String text) {
        return view.lookupAll(".label").stream().anyMatch(node->((Label)node).getText().equals(text));
    }
}
