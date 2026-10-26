package com.team10.tetris.game;

import com.team10.tetris.settings.DropSpeed;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DropSpeedSettingsTest {
    private GameEngine game(Difficulty difficulty) {
        return new GameEngine(new Board(),new Tetromino(TetrominoType.O,0,3),
                new GameConfig(GameMode.NORMAL,difficulty),
                () -> new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy());
    }
    private void clear(GameEngine game,int rows) {
        for(int r=0;r<20;r++)for(int c=0;c<10;c++)game.getBoard().setCell(r,c,0);
        for(int r=20-rows;r<20;r++)for(int c=0;c<10;c++)game.getBoard().setCell(r,c,1);
        game.hardDrop();assertEquals(GameState.CLEARING,game.getState());game.advanceClear(300);
    }
    @Test void basicSpeedSetsSameStartForEveryDifficultyAndOnlyDifficultyChangesAcceleration() {
        DropSpeed[] speeds={DropSpeed.SLOW,DropSpeed.NORMAL,DropSpeed.FAST};
        Difficulty[] difficulties={Difficulty.EASY,Difficulty.NORMAL,Difficulty.HARD};
        int[] bases={1250,1000,750};int[] reductions={80,100,120};
        for(int s=0;s<speeds.length;s++)for(int d=0;d<difficulties.length;d++) {
            GameEngine game=game(difficulties[d]);
            game.setBaseDropIntervalMs(speeds[s].adjustInterval(GameEngine.INITIAL_DROP_INTERVAL_MS));
            assertEquals(bases[s],game.getDropIntervalMs());
            clear(game,4);assertEquals(bases[s],game.getDropIntervalMs());
            clear(game,1);assertEquals(bases[s]-reductions[d],game.getDropIntervalMs());
            clear(game,4);assertEquals(bases[s]-reductions[d],game.getDropIntervalMs());
            clear(game,1);assertEquals(bases[s]-2*reductions[d],game.getDropIntervalMs());
            assertEquals(game.getDropIntervalMs(),game.snapshot().dropIntervalMs());
        }
    }
    @Test void actualFallAndScoringUseTheSameIntervalAsSnapshot() {
        for(DropSpeed speed:DropSpeed.values()) {
            GameEngine game=game(Difficulty.NORMAL);
            game.setBaseDropIntervalMs(speed.adjustInterval(GameEngine.INITIAL_DROP_INTERVAL_MS));
            GameController controller=new GameController(game);
            controller.advance(game.getDropIntervalMs()-1);assertEquals(0,game.getCurrentBlock().getRow());
            controller.advance(1);assertEquals(1,game.getCurrentBlock().getRow());
            assertEquals(speed==DropSpeed.FAST?200:100,game.getScore());
            for(int col=0;col<10;col++)game.getBoard().setCell(19,col,1);
            game.hardDrop();assertEquals(GameState.CLEARING,game.getState());
            controller.advance(299);assertEquals(GameState.CLEARING,game.getState());
            controller.advance(1);assertEquals(GameState.PLAYING,game.getState());
        }
    }
    @Test void changingBasePreservesAccelerationEvenAfterReachingMinimum() {
        GameEngine game=game(Difficulty.NORMAL);game.setBaseDropIntervalMs(750);
        for(int i=0;i<6;i++)game.increaseSpeed();
        assertEquals(200,game.getDropIntervalMs());
        game.setBaseDropIntervalMs(1250);assertEquals(650,game.getDropIntervalMs());
        game.setBaseDropIntervalMs(1000);assertEquals(400,game.getDropIntervalMs());
        for(int i=0;i<30;i++)game.increaseSpeed();
        game.setBaseDropIntervalMs(1250);assertEquals(200,game.getDropIntervalMs());
        assertThrows(IllegalArgumentException.class,()->game.setBaseDropIntervalMs(199));
        assertEquals(200,game.getDropIntervalMs());
    }
}
