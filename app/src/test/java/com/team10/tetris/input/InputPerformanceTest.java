package com.team10.tetris.input;

import com.team10.tetris.game.*;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.concurrent.locks.LockSupport;
import static org.junit.jupiter.api.Assertions.*;

class InputPerformanceTest {
    @Test void oneTwoAndThreeInputsPerSecondAreNotDroppedAndRespondWithinOneSecond() throws Exception {
        StringBuilder measurements = new StringBuilder("rate,index,latency_ns\n");
        for (int rate : new int[]{1, 2, 3}) {
            GameEngine engine = new GameEngine(new Board(), new Tetromino(TetrominoType.O, 0, 3));
            GameController controller = new GameController(engine);
            long previous = 0;
            long period = (1_000_000_000L + rate - 1) / rate;
            for (int i = 1; i <= 6; i++) {
                if (previous != 0) {
                    long remaining;
                    while ((remaining = previous + period - System.nanoTime()) > 0) LockSupport.parkNanos(remaining);
                }
                long start = System.nanoTime();
                if (previous != 0) assertTrue(start - previous >= period);
                previous = start;
                controller.handle(GameKeyBindings.defaults().commandFor(KeyCode.DOWN).orElseThrow());
                assertEquals(i, controller.snapshot().currentBlock().row(), "Every input must move exactly one row");
                long latency = System.nanoTime() - start;
                assertTrue(latency < 1_000_000_000L, "Input latency ns=" + latency);
                measurements.append(rate).append(',').append(i).append(',').append(latency).append('\n');
            }
        }
        Path output = Path.of("build/performance/input-unit.csv");
        Files.createDirectories(output.getParent()); Files.writeString(output, measurements);
    }
}
