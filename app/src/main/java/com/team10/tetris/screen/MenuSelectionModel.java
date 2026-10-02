package com.team10.tetris.screen;

public final class MenuSelectionModel {
    private final int itemCount;
    private int selectedIndex;

    public MenuSelectionModel(int itemCount) {
        if (itemCount <= 0) {
            throw new IllegalArgumentException("A menu must contain at least one item");
        }
        this.itemCount = itemCount;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public void moveNext() {
        selectedIndex = (selectedIndex + 1) % itemCount;
    }

    public void movePrevious() {
        selectedIndex = Math.floorMod(selectedIndex - 1, itemCount);
    }

    public void select(int index) {
        if (index < 0 || index >= itemCount) {
            throw new IndexOutOfBoundsException("Menu index out of range: " + index);
        }
        selectedIndex = index;
    }
}
