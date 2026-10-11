package com.team10.tetris.ui;

import javafx.scene.paint.Color;

/** 블록 한 칸을 그릴 때 쓰는 채우기 색, 무늬, 무늬 색. */
public record BlockStyle(Color fill, BlockPattern pattern, Color patternColor) {

    public static BlockStyle plain(Color fill) {
        return new BlockStyle(fill, BlockPattern.NONE, Color.TRANSPARENT);
    }
}
