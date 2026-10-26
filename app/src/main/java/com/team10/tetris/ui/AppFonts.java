package com.team10.tetris.ui;

import java.io.IOException;
import java.io.InputStream;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** OS 설치 글꼴 대신 앱에 포함한 동일한 한글 글꼴을 사용한다. */
public final class AppFonts {
    public static final String FAMILY = "Noto Sans KR";
    private static boolean loaded;

    private AppFonts() { }

    public static synchronized void load() {
        if (loaded) return;
        for (String weight : new String[]{"Regular", "Bold"}) {
            String resource = "/com/team10/tetris/fonts/NotoSansKR-" + weight + ".otf";
            try (InputStream stream = AppFonts.class.getResourceAsStream(resource)) {
                Font font = stream == null ? null : Font.loadFont(stream, 13);
                if (font == null || !FAMILY.equals(font.getFamily()))
                    throw new IllegalStateException("공통 글꼴을 불러오지 못했습니다: " + resource);
            } catch (IOException exception) {
                throw new IllegalStateException("공통 글꼴 읽기에 실패했습니다: " + resource, exception);
            }
        }
        loaded = true;
    }

    public static void apply(javafx.scene.Parent root) {
        load();
        String stylesheet = java.util.Objects.requireNonNull(AppFonts.class.getResource(
                "/com/team10/tetris/fonts.css")).toExternalForm();
        if (!root.getStylesheets().contains(stylesheet)) root.getStylesheets().add(stylesheet);
    }

    public static Font bold(double size) {
        load();
        return Font.font(FAMILY, FontWeight.BOLD, size);
    }
}
