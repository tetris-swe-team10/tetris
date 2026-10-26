package com.team10.tetris.input;

import com.team10.tetris.game.GameCommand;
import javafx.scene.input.KeyCode;
import java.util.*;

/** 게임 조작 전용 키 배치. 메뉴 탐색과 고정 단축키는 변경하지 않는다. */
public final class GameKeyBindings {
    public static final List<GameCommand> ACTIONS = List.of(
            GameCommand.LEFT, GameCommand.RIGHT, GameCommand.ROTATE, GameCommand.DOWN);
    private static final Set<KeyCode> RESERVED = Set.of(KeyCode.ESCAPE, KeyCode.SPACE,
            KeyCode.ENTER, KeyCode.TAB, KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,KeyCode.DIGIT4,
            KeyCode.NUMPAD1, KeyCode.NUMPAD2, KeyCode.NUMPAD3,KeyCode.NUMPAD4, KeyCode.UNDEFINED);
    private final Map<GameCommand, KeyCode> keys;

    public GameKeyBindings(Map<GameCommand, KeyCode> keys) {
        if (!keys.keySet().equals(new HashSet<>(ACTIONS)))
            throw new IllegalArgumentException("네 가지 조작 키를 모두 지정해 주세요.");
        Set<KeyCode> used = new HashSet<>();
        for (GameCommand action : ACTIONS) {
            KeyCode key = keys.get(action);
            if (key == null || key.isModifierKey() || RESERVED.contains(key))
                throw new IllegalArgumentException("ESC·SPACE·Enter·Tab·1~4 및 보조 키는 사용할 수 없습니다.");
            if (!used.add(key)) throw new IllegalArgumentException("다른 조작에 이미 사용 중인 키입니다.");
        }
        this.keys = Map.copyOf(keys);
    }
    public static GameKeyBindings defaults() {
        return new GameKeyBindings(Map.of(GameCommand.LEFT, KeyCode.LEFT, GameCommand.RIGHT, KeyCode.RIGHT,
                GameCommand.ROTATE, KeyCode.UP, GameCommand.DOWN, KeyCode.DOWN));
    }
    public KeyCode keyFor(GameCommand action) { return keys.get(action); }
    public Map<GameCommand, KeyCode> asMap() { return keys; }
    public Optional<GameCommand> commandFor(KeyCode key) {
        if (key == KeyCode.SPACE) return Optional.of(GameCommand.HARD_DROP);
        return keys.entrySet().stream().filter(entry -> entry.getValue() == key).map(Map.Entry::getKey).findFirst();
    }
    public static String label(GameCommand action) {
        return switch (action) {
            case LEFT -> "왼쪽 이동"; case RIGHT -> "오른쪽 이동";
            case ROTATE -> "회전"; case DOWN -> "내리기";
            default -> throw new IllegalArgumentException("변경할 수 없는 조작입니다.");
        };
    }
    public String description() {
        return String.join("   ", ACTIONS.stream().map(action -> label(action) + " " + keyFor(action).getName()).toList())
                + "   SPACE 바로 내리기";
    }
}
