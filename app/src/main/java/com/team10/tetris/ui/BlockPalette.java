package com.team10.tetris.ui;

import java.util.EnumMap;
import java.util.Map;

import com.team10.tetris.game.TetrominoType;

import javafx.scene.paint.Color;

/** 블록 종류별 색상과 무늬. 일반 모드와 색맹 모드 두 가지. */
public enum BlockPalette {

    NORMAL,
    COLOR_BLIND;

    /** 모든 블록에 공통으로 쓰는 어두운 외곽선. */
    public static final Color OUTLINE = Color.rgb(0, 0, 0, 0.45);

    /** 종류를 알 수 없는 고정 칸에 쓰는 기본 스타일. */
    public static final BlockStyle UNKNOWN = BlockStyle.plain(Color.web("#527A83"));

    // 밝은 블록에는 어두운 무늬, 어두운 블록에는 밝은 무늬
    private static final Color DARK_PATTERN = Color.rgb(0, 0, 0, 0.5);
    private static final Color LIGHT_PATTERN = Color.rgb(255, 255, 255, 0.6);

    private static final Map<TetrominoType, BlockStyle> NORMAL_STYLES = new EnumMap<>(TetrominoType.class);
    private static final Map<TetrominoType, BlockStyle> COLOR_BLIND_STYLES = new EnumMap<>(TetrominoType.class);

    static {
        NORMAL_STYLES.put(TetrominoType.I, BlockStyle.plain(Color.web("#22C7E8")));
        NORMAL_STYLES.put(TetrominoType.O, BlockStyle.plain(Color.web("#F4C430")));
        NORMAL_STYLES.put(TetrominoType.T, BlockStyle.plain(Color.web("#A45ADD")));
        NORMAL_STYLES.put(TetrominoType.S, BlockStyle.plain(Color.web("#00A69C")));
        NORMAL_STYLES.put(TetrominoType.Z, BlockStyle.plain(Color.web("#E83E8C")));
        NORMAL_STYLES.put(TetrominoType.J, BlockStyle.plain(Color.web("#315BD6")));
        NORMAL_STYLES.put(TetrominoType.L, BlockStyle.plain(Color.web("#F08A24")));

        COLOR_BLIND_STYLES.put(TetrominoType.I,
                new BlockStyle(Color.web("#56B4E9"), BlockPattern.HORIZONTAL_LINES, DARK_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.O,
                new BlockStyle(Color.web("#F0E442"), BlockPattern.DOTS, DARK_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.T,
                new BlockStyle(Color.web("#CC79A7"), BlockPattern.GRID, DARK_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.S,
                new BlockStyle(Color.web("#009E73"), BlockPattern.DIAGONAL_RIGHT, LIGHT_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.Z,
                new BlockStyle(Color.web("#D55E00"), BlockPattern.DIAGONAL_LEFT, LIGHT_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.J,
                new BlockStyle(Color.web("#0072B2"), BlockPattern.VERTICAL_LINES, LIGHT_PATTERN));
        COLOR_BLIND_STYLES.put(TetrominoType.L,
                new BlockStyle(Color.web("#E69F00"), BlockPattern.CROSS_DIAGONAL, DARK_PATTERN));
    }

    public static BlockPalette of(boolean colorBlindMode) {
        return colorBlindMode ? COLOR_BLIND : NORMAL;
    }

    /** 블록 종류의 스타일. 종류가 null이면 기본 스타일. */
    public BlockStyle styleOf(TetrominoType type) {
        if (type == null) {
            return UNKNOWN;
        }

        return this == COLOR_BLIND
                ? COLOR_BLIND_STYLES.get(type)
                : NORMAL_STYLES.get(type);
    }
}
