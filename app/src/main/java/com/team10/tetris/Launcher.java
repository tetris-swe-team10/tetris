package com.team10.tetris;

/** 클래스패스 배포용 진입점. JavaFX Application의 특수 실행 경로를 피한다. */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        App.main(args);
    }
}
