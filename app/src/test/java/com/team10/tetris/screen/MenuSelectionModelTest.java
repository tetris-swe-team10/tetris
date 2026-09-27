package com.team10.tetris.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MenuSelectionModelTest {
    @Test
    void selectsFirstItemInitially() {
        MenuSelectionModel selection = new MenuSelectionModel(4);

        assertEquals(0, selection.selectedIndex());
    }

    @Test
    void wrapsFromLastItemToFirstItem() {
        MenuSelectionModel selection = new MenuSelectionModel(4);
        selection.select(3);

        selection.moveNext();

        assertEquals(0, selection.selectedIndex());
    }

    @Test
    void wrapsFromFirstItemToLastItem() {
        MenuSelectionModel selection = new MenuSelectionModel(4);

        selection.movePrevious();

        assertEquals(3, selection.selectedIndex());
    }

    @Test
    void rejectsMenuWithoutItems() {
        assertThrows(IllegalArgumentException.class, () -> new MenuSelectionModel(0));
    }
}
