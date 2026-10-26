package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class BlackoutViewTest {
    @Test void hidesOnlyLockedCellsAndRestoresLatestBoard() throws Exception {
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException alreadyStarted) { /* 다른 UI 테스트가 초기화 */ }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Board board = new Board();
                board.lock(new Tetromino(TetrominoType.O,18,0));
                GameEngine game=new GameEngine(board,null,new GameConfig(GameMode.ITEM,Difficulty.NORMAL),
                        ()->new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy(),
                        piece -> piece.attachItem(new com.team10.tetris.game.item.Blackout(),0,0));
                for(int n:new int[]{4,4,2}) {
                    for(int rr=0;rr<20;rr++) for(int cc=0;cc<10;cc++) board.setCell(rr,cc,0);
                    for(int rr=20-n;rr<20;rr++) for(int cc=0;cc<10;cc++) board.setCell(rr,cc,1);
                    game.hardDrop(); game.advanceClear(300);
                }
                for(int rr=0;rr<20;rr++) for(int cc=0;cc<10;cc++) board.setCell(rr,cc,0);
                board.lock(new Tetromino(TetrominoType.O,18,0));
                GameView view=new GameView(game);
                Scene scene=new Scene(view,960,720);
                view.applyCss(); view.layout();
                assertTrue(view.lookupAll(".label").stream().anyMatch(n->((Label)n).getText().contains("B 암전 · 고정 후")));
                Canvas canvas=(Canvas)view.getCenter();
                int cell=(int)canvas.getWidth()/Board.WIDTH;
                Color normal=canvas.snapshot(null,null).getPixelReader().getColor(cell/2,18*cell+cell/2);
                game.hardDrop(); game.hardDrop(); view.redraw();
                WritableImage hidden=canvas.snapshot(null,null);
                Color background=Color.web("#010205");
                assertEquals(background,hidden.getPixelReader().getColor(cell/2,18*cell+cell/2));
                assertNotEquals(background,normal);
                assertNotEquals(background,hidden.getPixelReader().getColor(3*cell+cell/2,cell/2));
                assertEquals(1,game.getBoard().getCell(18,0));
                view.setColorBlindMode(true); view.redraw();
                assertEquals(background,canvas.snapshot(null,null).getPixelReader().getColor(cell/2,18*cell+cell/2));
                // 원 내부 고정 블록은 보이고 원 밖은 가려지며 이동 시 시야도 이동한다.
                board.setCell(1,5,1);
                view.redraw();
                assertNotEquals(background,canvas.snapshot(null,null).getPixelReader().getColor(5*cell+cell/2,cell+cell/2));
                game.moveLeft(); game.moveLeft(); view.redraw();
                assertEquals(background,canvas.snapshot(null,null).getPixelReader().getColor(5*cell+cell/2,cell+cell/2));
                board.setCell(1,5,0);
                // 암전 중 바뀐 보드도 종료 시 최신 상태로 표시한다.
                board.setCell(18,0,0);
                game.hardDrop(); view.redraw();
                WritableImage restored=canvas.snapshot(null,null);
                assertEquals(Color.web("#060B16"),restored.getPixelReader().getColor(cell/2,18*cell+cell/2));
                assertNotEquals(background,restored.getPixelReader().getColor(cell/2,19*cell+cell/2));
                scene.setRoot(new javafx.scene.layout.StackPane());
            } catch(Throwable t) {failure.set(t);} finally {done.countDown();}
        });
        assertTrue(done.await(20,TimeUnit.SECONDS));
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
}
