package com.team10.tetris.input;

import java.util.Optional;

import com.team10.tetris.game.GameCommand;

import javafx.scene.input.KeyCode;

/**
 * 1인 게임 화면의 키 해석. 화면·엔진을 모르고 눌린 키가 무슨 뜻인지만 돌려준다.
 *
 * <p>보조키(Alt·Ctrl·Meta·Shift)와 함께 누른 키는 무시한다. ESC는 설정, 1~3은 데모 아이템,
 * 4·5는 아이템 데모가 켜졌을 때만 데모 아이템이고 아니면 일반 조작 키로 해석한다.
 * 게임 조작 명령만 쓰는 화면(P2P 내 보드 등)은 {@link Action.Command}만 처리하면 된다.
 */
public final class GameKeyInput {

    /** 키 해석 결과 */
    public sealed interface Action {
        /** 바인딩된 게임 조작 */
        record Command(GameCommand command) implements Action { }
        /** 데모 아이템 요청 (1~5) */
        record DemoItem(int number) implements Action { }
        /** ESC: 설정 열기 */
        record OpenSettings() implements Action { }
    }

    private GameKeyInput() {
    }

    public static Optional<Action> resolve(KeyCode key, boolean modifierDown,
            GameKeyBindings bindings, boolean itemDemoEnabled) {
        if (modifierDown) return Optional.empty();
        if (key == KeyCode.ESCAPE) return Optional.of(new Action.OpenSettings());

        int demo = demoNumber(key);
        if (demo >= 1 && demo <= 3 || demo >= 4 && itemDemoEnabled) {
            return Optional.of(new Action.DemoItem(demo));
        }
        return bindings.commandFor(key).map(Action.Command::new);
    }

    private static int demoNumber(KeyCode key) {
        return switch (key) {
            case DIGIT1, NUMPAD1 -> 1;
            case DIGIT2, NUMPAD2 -> 2;
            case DIGIT3, NUMPAD3 -> 3;
            case DIGIT4, NUMPAD4 -> 4;
            case DIGIT5, NUMPAD5 -> 5;
            default -> 0;
        };
    }
}
