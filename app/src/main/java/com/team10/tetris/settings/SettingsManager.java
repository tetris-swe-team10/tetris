package com.team10.tetris.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

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
        } catch (IOException exception) {
            return false;
        }
    }

    /** 색맹 모드 값을 저장한다. 파일에 있던 다른 설정은 그대로 둔다. */
    public void saveColorBlindMode(boolean colorBlindMode) throws IOException {
        Properties properties;

        try {
            properties = load();
        } catch (IOException exception) {
            // 기존 파일이 깨져 있으면 새로 쓴다
            properties = new Properties();
        }

        properties.setProperty(
                COLOR_BLIND_MODE_KEY,
                String.valueOf(colorBlindMode)
        );

        Path parent = filePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8
        )) {
            properties.store(writer, "Tetris settings");
        }
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
