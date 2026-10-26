package com.team10.tetris.controller;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppControllerTest {
    @Test
    void resetsScoresOnlyForAnExplicitConfirmation() {
        ButtonType yes = new ButtonType("예", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("아니요", ButtonBar.ButtonData.CANCEL_CLOSE);

        assertTrue(AppController.isResetConfirmed(yes));
        assertFalse(AppController.isResetConfirmed(no));
        assertFalse(AppController.isResetConfirmed(null));
    }
}
