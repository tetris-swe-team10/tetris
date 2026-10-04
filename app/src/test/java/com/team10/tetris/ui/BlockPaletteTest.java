package com.team10.tetris.ui;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team10.tetris.game.TetrominoType;

import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BlockPaletteTest {

    @Test
    void ofReturnsPaletteForMode() {
        assertSame(BlockPalette.NORMAL, BlockPalette.of(false));
        assertSame(BlockPalette.COLOR_BLIND, BlockPalette.of(true));
    }

    @Test
    void everyTypeHasStyleInBothModes() {
        for (BlockPalette palette : BlockPalette.values()) {
            for (TetrominoType type : TetrominoType.values()) {
                assertNotNull(palette.styleOf(type), palette + " " + type);
            }
        }
    }

    @Test
    void unknownTypeUsesDefaultStyle() {
        assertSame(BlockPalette.UNKNOWN, BlockPalette.NORMAL.styleOf(null));
        assertSame(BlockPalette.UNKNOWN, BlockPalette.COLOR_BLIND.styleOf(null));
    }

    @Test
    void allBlocksHaveDifferentColorsInEachMode() {
        for (BlockPalette palette : BlockPalette.values()) {
            Set<Color> colors = new HashSet<>();

            for (TetrominoType type : TetrominoType.values()) {
                colors.add(palette.styleOf(type).fill());
            }

            assertEquals(TetrominoType.values().length, colors.size(), palette.toString());
        }
    }

    @Test
    void normalModeUsesDesignColors() {
        assertColor("#22C7E8", BlockPalette.NORMAL, TetrominoType.I);
        assertColor("#F4C430", BlockPalette.NORMAL, TetrominoType.O);
        assertColor("#A45ADD", BlockPalette.NORMAL, TetrominoType.T);
        assertColor("#00A69C", BlockPalette.NORMAL, TetrominoType.S);
        assertColor("#E83E8C", BlockPalette.NORMAL, TetrominoType.Z);
        assertColor("#315BD6", BlockPalette.NORMAL, TetrominoType.J);
        assertColor("#F08A24", BlockPalette.NORMAL, TetrominoType.L);
    }

    @Test
    void colorBlindModeUsesDesignColors() {
        assertColor("#56B4E9", BlockPalette.COLOR_BLIND, TetrominoType.I);
        assertColor("#F0E442", BlockPalette.COLOR_BLIND, TetrominoType.O);
        assertColor("#CC79A7", BlockPalette.COLOR_BLIND, TetrominoType.T);
        assertColor("#009E73", BlockPalette.COLOR_BLIND, TetrominoType.S);
        assertColor("#D55E00", BlockPalette.COLOR_BLIND, TetrominoType.Z);
        assertColor("#0072B2", BlockPalette.COLOR_BLIND, TetrominoType.J);
        assertColor("#E69F00", BlockPalette.COLOR_BLIND, TetrominoType.L);
    }

    @Test
    void normalModeHasNoPatterns() {
        for (TetrominoType type : TetrominoType.values()) {
            assertEquals(BlockPattern.NONE, BlockPalette.NORMAL.styleOf(type).pattern());
        }
    }

    @Test
    void colorBlindModeUsesDesignPatterns() {
        assertPattern(BlockPattern.HORIZONTAL_LINES, TetrominoType.I);
        assertPattern(BlockPattern.DOTS, TetrominoType.O);
        assertPattern(BlockPattern.GRID, TetrominoType.T);
        assertPattern(BlockPattern.DIAGONAL_RIGHT, TetrominoType.S);
        assertPattern(BlockPattern.DIAGONAL_LEFT, TetrominoType.Z);
        assertPattern(BlockPattern.VERTICAL_LINES, TetrominoType.J);
        assertPattern(BlockPattern.CROSS_DIAGONAL, TetrominoType.L);
    }

    @Test
    void colorBlindModeHasDifferentPatternForEveryBlock() {
        Set<BlockPattern> patterns = new HashSet<>();

        for (TetrominoType type : TetrominoType.values()) {
            patterns.add(BlockPalette.COLOR_BLIND.styleOf(type).pattern());
        }

        assertEquals(TetrominoType.values().length, patterns.size());
        assertFalse(patterns.contains(BlockPattern.NONE));
    }

    @Test
    void similarOrMirroredBlocksAreDistinguishable() {
        assertDistinguishable(TetrominoType.S, TetrominoType.Z);
        assertDistinguishable(TetrominoType.J, TetrominoType.L);
        assertDistinguishable(TetrominoType.J, TetrominoType.T);
    }

    @Test
    void patternColorContrastsWithBlockColor() {
        // 어두운 블록(S, Z, J)은 밝은 무늬, 밝은 블록(I, O, T, L)은 어두운 무늬
        for (TetrominoType type : new TetrominoType[] {TetrominoType.S, TetrominoType.Z, TetrominoType.J}) {
            assertEquals(1.0, BlockPalette.COLOR_BLIND.styleOf(type).patternColor().getBrightness(), 1e-9, type.toString());
        }

        for (TetrominoType type : new TetrominoType[] {TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.L}) {
            assertEquals(0.0, BlockPalette.COLOR_BLIND.styleOf(type).patternColor().getBrightness(), 1e-9, type.toString());
        }
    }

    private static void assertColor(String hex, BlockPalette palette, TetrominoType type) {
        assertEquals(Color.web(hex), palette.styleOf(type).fill(), palette + " " + type);
    }

    private static void assertPattern(BlockPattern expected, TetrominoType type) {
        assertEquals(expected, BlockPalette.COLOR_BLIND.styleOf(type).pattern(), type.toString());
    }

    private static void assertDistinguishable(TetrominoType a, TetrominoType b) {
        for (BlockPalette palette : BlockPalette.values()) {
            assertNotEquals(palette.styleOf(a).fill(), palette.styleOf(b).fill(), palette + " " + a + "/" + b);
        }

        assertNotEquals(
                BlockPalette.COLOR_BLIND.styleOf(a).pattern(),
                BlockPalette.COLOR_BLIND.styleOf(b).pattern(),
                a + "/" + b);
    }
}
