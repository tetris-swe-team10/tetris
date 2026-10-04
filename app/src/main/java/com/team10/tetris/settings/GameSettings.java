package com.team10.tetris.settings;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * 실행 중인 게임의 현재 설정.
 * 앱 시작 시 파일에서 한 번 읽고, 값이 바뀌면 자동으로 파일에 저장한다.
 * 화면은 property에 리스너를 등록해 값이 바뀌는 즉시 반영한다.
 */
public class GameSettings {

    private final SettingsManager settingsManager;

    private final BooleanProperty colorBlindMode =
            new SimpleBooleanProperty(this, "colorBlindMode", false);

    public GameSettings(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;

        colorBlindMode.set(settingsManager.loadColorBlindMode());

        // 불러온 뒤에 등록해야 시작할 때 다시 저장하지 않음
        colorBlindMode.addListener(
                (observable, oldValue, newValue) -> saveColorBlindMode(newValue));
    }

    // 설정 화면 담당자: 토글과 양방향 바인딩해서 사용
    public BooleanProperty colorBlindModeProperty() {
        return colorBlindMode;
    }

    public boolean isColorBlindMode() {
        return colorBlindMode.get();
    }

    public void setColorBlindMode(boolean value) {
        colorBlindMode.set(value);
    }

    private void saveColorBlindMode(boolean value) {
        try {
            settingsManager.saveColorBlindMode(value);
        } catch (IOException exception) {
            // 저장에 실패해도 이번 실행에는 적용됨
            System.err.println("색맹 모드 설정을 저장하지 못했습니다: " + exception.getMessage());
        }
    }
}
