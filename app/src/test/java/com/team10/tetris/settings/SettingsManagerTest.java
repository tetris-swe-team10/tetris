package com.team10.tetris.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsManagerTest {

    @TempDir
    Path tempDir;

    private Path settingsFile() {
        return tempDir.resolve("settings.properties");
    }

    @Test
    void colorBlindModeIsOffWhenFileDoesNotExist() {
        SettingsManager manager = new SettingsManager(settingsFile());

        assertFalse(manager.loadSettings().colorBlindMode());
    }

    @Test
    void savesAndLoadsColorBlindMode()
            throws IOException {

        SettingsManager manager = new SettingsManager(settingsFile());

        manager.saveColorBlindMode(true);
        assertTrue(manager.loadSettings().colorBlindMode());

        manager.saveColorBlindMode(false);
        assertFalse(manager.loadSettings().colorBlindMode());
    }

    @Test
    void savedValueIsLoadedOnNextRun()
            throws IOException {

        new SettingsManager(settingsFile()).saveColorBlindMode(true);

        // 다음 실행을 흉내 내기 위해 새 객체로 다시 읽음
        SettingsManager nextRun = new SettingsManager(settingsFile());

        assertTrue(nextRun.loadSettings().colorBlindMode());
    }

    @Test
    void createsParentDirectoriesWhenSaving()
            throws IOException {

        Path file = tempDir.resolve("nested").resolve("settings.properties");
        SettingsManager manager = new SettingsManager(file);

        manager.saveColorBlindMode(true);

        assertTrue(Files.exists(file));
        assertTrue(manager.loadSettings().colorBlindMode());
    }

    @Test
    void readsHandWrittenSettingsFile()
            throws IOException {

        Files.writeString(
                settingsFile(),
                "colorBlindMode=true\n",
                StandardCharsets.UTF_8
        );

        assertTrue(new SettingsManager(settingsFile()).loadSettings().colorBlindMode());
    }

    @Test
    void invalidValueIsTreatedAsOff()
            throws IOException {

        Files.writeString(
                settingsFile(),
                "colorBlindMode=yes please\n",
                StandardCharsets.UTF_8
        );

        assertFalse(new SettingsManager(settingsFile()).loadSettings().colorBlindMode());
    }

    @Test
    void unreadableFileIsTreatedAsOff()
            throws IOException {

        // 파일 자리에 폴더가 있으면 읽을 수 없음
        Files.createDirectories(settingsFile());

        assertFalse(new SettingsManager(settingsFile()).loadSettings().colorBlindMode());
    }

    @Test
    void savingKeepsOtherSettings()
            throws IOException {

        Files.writeString(
                settingsFile(),
                "screenSize=large\n",
                StandardCharsets.UTF_8
        );

        new SettingsManager(settingsFile()).saveColorBlindMode(true);

        String content = Files.readString(settingsFile(), StandardCharsets.UTF_8);

        assertTrue(content.contains("screenSize=large"));
        assertTrue(content.contains("colorBlindMode=true"));
    }
}
