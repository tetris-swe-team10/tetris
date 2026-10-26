package com.team10.tetris.game;

import com.team10.tetris.settings.DropSpeed;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DropSpeedSettingsTest {
    @Test void speedAdjustsFallIntervalWithoutChangingClearAnimationTime() {
        for (DropSpeed speed : DropSpeed.values()) {
            GameEngine game = new GameEngine(new Board(),new Tetromino(TetrominoType.O,0,3));
            GameController controller = new GameController(game);
            controller.setDropIntervalAdjuster(speed::adjustInterval);
            int interval = speed.adjustInterval(game.getDropIntervalMs());
            controller.advance(interval-1);
            assertEquals(0,game.getCurrentBlock().getRow());
            controller.advance(1);
            assertEquals(1,game.getCurrentBlock().getRow());
            for (int col=0;col<10;col++) game.getBoard().setCell(19,col,1);
            game.hardDrop();
            assertEquals(GameState.CLEARING,game.getState());
            controller.advance(299);assertEquals(GameState.CLEARING,game.getState());
            controller.advance(1);assertEquals(GameState.PLAYING,game.getState());
        }
    }
}
