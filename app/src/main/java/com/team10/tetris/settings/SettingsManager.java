package com.team10.tetris.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.EnumMap;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.input.GameKeyBindings;
import javafx.scene.input.KeyCode;

/** 게임 설정을 파일에 저장하고 다음 실행 때 불러온다. */
public class SettingsManager {

    /** 색맹 모드 설정 키. 값은 true 또는 false. */
    public static final String COLOR_BLIND_MODE_KEY = "colorBlindMode";

    private final Path filePath;

    public SettingsManager() {
        this(
                Path.of(
                        System.getProperty("user.home"),
                        ".team10-tetris",
                        "settings.properties"
                )
        );
    }

    public SettingsManager(Path filePath) {
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return filePath;
    }

    /** 저장된 색맹 모드 값. 파일이 없거나 읽을 수 없으면 false. */
    public boolean loadColorBlindMode() {
        try {
            String value = load().getProperty(COLOR_BLIND_MODE_KEY);

            return value != null && Boolean.parseBoolean(value.trim());
        } catch (IOException | IllegalArgumentException exception) {
            return false;
        }
    }

    /** 색맹 모드 값을 저장한다. 파일에 있던 다른 설정은 그대로 둔다. */
    public void saveColorBlindMode(boolean colorBlindMode) throws IOException {
        Properties properties;

        try {
            properties = load();
        } catch (IOException | IllegalArgumentException exception) {
            // 기존 파일이 깨져 있으면 새로 쓴다
            properties = new Properties();
        }

        properties.setProperty(
                COLOR_BLIND_MODE_KEY,
                String.valueOf(colorBlindMode)
        );

        write(properties);
    }

    public SettingsData loadSettings() {
        Properties properties;
        try { properties = load(); }
        catch (IOException | IllegalArgumentException exception) { return SettingsData.defaults(); }
        GameKeyBindings keys;
        try {
            EnumMap<GameCommand, KeyCode> mapping = new EnumMap<>(GameCommand.class);
            for (GameCommand action : GameKeyBindings.ACTIONS) {
                mapping.put(action, KeyCode.valueOf(properties.getProperty("key." + action.name(),
                        GameKeyBindings.defaults().keyFor(action).name()).trim()));
            }
            keys = new GameKeyBindings(mapping);
        } catch (IllegalArgumentException exception) { keys = GameKeyBindings.defaults(); }
        int lines = 1;
        try { lines = Integer.parseInt(properties.getProperty("demoItemLines", "1").trim()); }
        catch (NumberFormatException ignored) { }
        if (lines < 1 || lines > 100) lines = 1;
        return new SettingsData(Boolean.parseBoolean(properties.getProperty(COLOR_BLIND_MODE_KEY, "false").trim()),
                readEnum(properties, "dropSpeed", DropSpeed.NORMAL),
                readEnum(properties, "displayMode", DisplayMode.WINDOWED),
                Boolean.parseBoolean(properties.getProperty("demoMode", "false").trim()), lines, keys);
    }

    private static <E extends Enum<E>> E readEnum(Properties properties, String name, E fallback) {
        try { return Enum.valueOf(fallback.getDeclaringClass(), properties.getProperty(name, fallback.name()).trim()); }
        catch (IllegalArgumentException exception) { return fallback; }
    }

    public void saveSettings(SettingsData data) throws IOException {
        Properties properties;
        try { properties = load(); }
        catch (IOException | IllegalArgumentException exception) { properties = new Properties(); }
        properties.setProperty(COLOR_BLIND_MODE_KEY, Boolean.toString(data.colorBlindMode()));
        properties.setProperty("dropSpeed", data.dropSpeed().name());
        properties.setProperty("displayMode", data.displayMode().name());
        properties.setProperty("demoMode", Boolean.toString(data.demoMode()));
        properties.setProperty("demoItemLines", Integer.toString(data.demoItemLines()));
        for (GameCommand action : GameKeyBindings.ACTIONS)
            properties.setProperty("key." + action.name(), data.keyBindings().keyFor(action).name());
        write(properties);
    }

    private void write(Properties properties) throws IOException {
        Path target = filePath.toAbsolutePath();
        Path parent = target.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "tetris-settings-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                properties.store(writer, "Tetris settings");
            }
            try {
                Files.move(temporary, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    private Properties load() throws IOException {
        Properties properties = new Properties();

        if (!Files.exists(filePath)) {
            return properties;
        }

        try (Reader reader = Files.newBufferedReader(
                filePath,
                StandardCharsets.UTF_8
        )) {
            properties.load(reader);
        }

        return properties;
    }
}
