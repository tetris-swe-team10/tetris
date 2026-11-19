package com.team10.tetris.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import com.team10.tetris.game.*;
import com.team10.tetris.game.versus.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
class LocalVersusViewTest {
    @Test void bothPlayersReceiveInputPauseTogetherAndScreenCanBeReplaced() throws Exception {
        try {Platform.startup(()->Platform.setImplicitExit(false));}catch(IllegalStateException ready){}
        CountDownLatch done=new CountDownLatch(1); AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{try {
            var match=new LocalVersusMatch(VersusConfig.of(VersusRule.NORMAL,Difficulty.NORMAL));
            var root=new LocalVersusView(match,new CanvasBoardRenderer(),()->{},()->{},com.team10.tetris.input.VersusKeyBindings.defaults());
            Scene scene=new Scene(root,960,640); root.applyCss(); root.layout();
            int first=match.snapshot(PlayerSide.PLAYER1).currentBlock().col();
            int second=match.snapshot(PlayerSide.PLAYER2).currentBlock().col();
            key(root,KeyCode.A); key(root,KeyCode.RIGHT);
            assertEquals(first-1,match.snapshot(PlayerSide.PLAYER1).currentBlock().col());
            assertEquals(second+1,match.snapshot(PlayerSide.PLAYER2).currentBlock().col());
            root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.A,false,false,false,false));
            root.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.RIGHT,false,false,false,false));
            root.advanceFrame(200);
            assertEquals(first-3,match.snapshot(PlayerSide.PLAYER1).currentBlock().col());
            assertEquals(second+3,match.snapshot(PlayerSide.PLAYER2).currentBlock().col());
            key(root,KeyCode.ESCAPE);
            assertEquals(GameState.PAUSED,match.snapshot(PlayerSide.PLAYER1).state());
            assertEquals(GameState.PAUSED,match.snapshot(PlayerSide.PLAYER2).state());
            var before=match.snapshot(PlayerSide.PLAYER1); key(root,KeyCode.S); assertEquals(before,match.snapshot(PlayerSide.PLAYER1));
            key(root,KeyCode.ESCAPE); assertEquals(GameState.PLAYING,match.snapshot(PlayerSide.PLAYER2).state());
            root.redraw();
            root.resize(640,900); root.applyCss(); root.layout(); root.layout();
            var canvases=root.lookupAll("Canvas").stream().filter(n->n instanceof javafx.scene.canvas.Canvas c && c.getHeight()>200).toList();
            assertEquals(2,canvases.size());
            for(var canvas:canvases) assertTrue(canvas.localToScene(canvas.getBoundsInLocal()).getMaxX()<=640);
            root.resize(960,640); root.layout(); root.layout();
            var board=new Board(); var engine=new GameEngine(board,new Tetromino(TetrominoType.O,0,3));
            engine.receiveAttack(com.team10.tetris.game.versus.AttackLines.EMPTY);
            board.setCell(19,1,Board.GARBAGE);
            var renderer=new CanvasBoardRenderer(); var canvas=new javafx.scene.canvas.Canvas(100,200);
            renderer.drawBoard(canvas,engine.snapshot());
            var gray=canvas.snapshot(null,null).getPixelReader().getColor(15,195);
            assertEquals(CanvasBoardRenderer.GARBAGE.fill(),gray);
            var image=scene.snapshot(null);
            java.nio.file.Path out=java.nio.file.Path.of("build/ui-smoke/local-versus.png"); java.nio.file.Files.createDirectories(out.getParent());
            var png=new java.awt.image.BufferedImage(960,640,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<640;y++) for(int x=0;x<960;x++) png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
            javax.imageio.ImageIO.write(png,"png",out.toFile());
            scene.setRoot(new StackPane());
        }catch(Throwable t){failure.set(t);}finally{done.countDown();}});
        assertTrue(done.await(30,TimeUnit.SECONDS)); if(failure.get()!=null) throw new AssertionError(failure.get());
    }
    private static void key(LocalVersusView view,KeyCode code) {
        view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));
        view.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED,"","",code,false,false,false,false));
    }
}
