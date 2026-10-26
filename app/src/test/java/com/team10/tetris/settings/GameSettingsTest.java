package com.team10.tetris.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSettingsTest {

    @TempDir
    Path tempDir;

    private Path settingsFile() {
        return tempDir.resolve("settings.properties");
    }

    private GameSettings createSettings() {
        return new GameSettings(new SettingsManager(settingsFile()));
    }

    @Test
    void colorBlindModeIsOffByDefault() {
        assertFalse(createSettings().isColorBlindMode());
    }

    @Test
    void loadsSavedValueOnStart()
            throws IOException {

        Files.writeString(
                settingsFile(),
                "colorBlindMode=true\n",
                StandardCharsets.UTF_8
        );

        assertTrue(createSettings().isColorBlindMode());
    }

    @Test
    void doesNotCreateFileWhenOnlyLoading() {
        createSettings();

        assertFalse(Files.exists(settingsFile()));
    }

    @Test
    void changingValueSavesItAutomatically() {
        GameSettings settings = createSettings();

        settings.setColorBlindMode(true);

        // 다음 실행을 흉내 내기 위해 파일에서 새로 읽음
        assertTrue(createSettings().isColorBlindMode());

        settings.setColorBlindMode(false);

        assertFalse(createSettings().isColorBlindMode());
    }

    @Test
    void changingPropertyDirectlyAlsoSaves() {
        GameSettings settings = createSettings();

        // 설정 화면의 토글이 바인딩으로 값을 바꾸는 경우
        settings.colorBlindModeProperty().set(true);

        assertTrue(createSettings().isColorBlindMode());
    }

    @Test
    void listenersAreNotifiedImmediately() {
        GameSettings settings = createSettings();
        List<Boolean> received = new ArrayList<>();

        settings.colorBlindModeProperty().addListener(
                (observable, oldValue, newValue) -> received.add(newValue));

        settings.setColorBlindMode(true);
        settings.setColorBlindMode(false);

        assertEquals(List.of(true, false), received);
    }

    @Test
    void valueChangesEvenWhenSavingFails()
            throws IOException {

        // 설정 폴더 자리에 파일이 있으면 저장할 수 없음
        Path blocker = tempDir.resolve("blocker");
        Files.writeString(blocker, "", StandardCharsets.UTF_8);

        GameSettings settings = new GameSettings(
                new SettingsManager(blocker.resolve("settings.properties")));

        assertDoesNotThrow(() -> settings.setColorBlindMode(true));
        assertTrue(settings.isColorBlindMode());
    }
    @Test
    void usesSafeDefaultsForScreenSettings() {
        GameSettings settings = createSettings();

        assertEquals(DropSpeed.NORMAL, settings.getDropSpeed());
        assertEquals(DisplayMode.WINDOWED, settings.getDisplayMode());
        assertFalse(settings.isDemoMode());
    }

    @Test
    void updatesScreenSettings() {
        GameSettings settings = createSettings();

        settings.setDropSpeed(DropSpeed.FAST);
        settings.setDisplayMode(DisplayMode.PORTRAIT);
        settings.setDemoMode(true);

        assertEquals(DropSpeed.FAST, settings.getDropSpeed());
        assertEquals(DisplayMode.PORTRAIT, settings.getDisplayMode());
        assertTrue(settings.isDemoMode());
    }

    @Test
    void adjustsDropIntervalWithSelectedSpeed() {
        GameSettings settings = createSettings();

        settings.setDropSpeed(DropSpeed.SLOW);
        assertEquals(1250, settings.adjustDropInterval(1000));
        settings.setDropSpeed(DropSpeed.NORMAL);
        assertEquals(1000, settings.adjustDropInterval(1000));
        settings.setDropSpeed(DropSpeed.FAST);
        assertEquals(750, settings.adjustDropInterval(1000));
    }

    @Test
    void rejectsInvalidBaseDropInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> DropSpeed.NORMAL.adjustInterval(0));
    }

    @Test void demoIntervalValidationAndNormalFallback() {
        GameSettings settings = createSettings();
        assertEquals(1, settings.getDemoItemLines());
        assertEquals(10, settings.getItemLineInterval());
        settings.setDemoMode(true);
        settings.setDemoItemLines(100);
        assertEquals(100, settings.getItemLineInterval());
        assertThrows(IllegalArgumentException.class, () -> settings.setDemoItemLines(0));
        assertThrows(IllegalArgumentException.class, () -> settings.setDemoItemLines(101));
        assertEquals(100, settings.getDemoItemLines());
        settings.setDemoMode(false);
        assertEquals(10, settings.getItemLineInterval());
    }
}
