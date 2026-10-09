package com.team10.tetris.settings;

import java.util.ArrayList;
import java.util.List;

import com.team10.tetris.input.MenuCommand;
import com.team10.tetris.input.MenuKeyBindings;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** 설정 화면과 일시정지 창의 공통 키보드 탐색. */
final class SettingsNavigation {
    private SettingsNavigation() {}

    static void install(Parent root, Parent content, ScrollPane scrollPane) {
        List<Control> controls = new ArrayList<>();
        collect(content, controls);
        MenuKeyBindings bindings = MenuKeyBindings.defaults();
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown() || event.isControlDown() || event.isMetaDown()) return;
            List<Control> available = controls.stream()
                    .filter(control -> !control.isDisabled() && isVisible(control)).toList();
            if (available.isEmpty()) return;
            Node focused = root.getScene().getFocusOwner();
            int index = -1;
            for (int i = 0; i < available.size(); i++) {
                if (isInside(focused, available.get(i))) index = i;
            }
            Control current = index < 0 ? null : available.get(index);
            // 펼쳐진 목록의 방향키, Enter, ESC는 ComboBox가 처리한다.
            if (current instanceof ComboBox<?> combo && combo.isShowing()) return;
            KeyCode key = event.getCode();
            MenuCommand command = bindings.commandFor(key).orElse(null);
            if (command == MenuCommand.MOVE_PREVIOUS || command == MenuCommand.MOVE_NEXT
                    || key == KeyCode.LEFT || key == KeyCode.RIGHT) {
                int delta = command == MenuCommand.MOVE_PREVIOUS || key == KeyCode.LEFT ? -1 : 1;
                if (current instanceof ComboBox<?> combo
                        && (key == KeyCode.LEFT || key == KeyCode.RIGHT)) {
                    int count = combo.getItems().size();
                    if (count > 0) {
                        int selected = combo.getSelectionModel().getSelectedIndex();
                        combo.getSelectionModel().select(selected < 0 ? (delta > 0 ? 0 : count - 1)
                                : Math.floorMod(selected + delta, count));
                    }
                } else {
                    int next = index < 0 ? (delta > 0 ? 0 : available.size() - 1)
                            : Math.floorMod(index + delta, available.size());
                    focus(available.get(next), content, scrollPane);
                }
                event.consume();
            } else if (command == MenuCommand.CONFIRM) {
                if (current == null) focus(available.get(0), content, scrollPane);
                else if (current instanceof ButtonBase button) button.fire();
                else if (current instanceof ComboBox<?> combo) combo.show();
                event.consume();
            }
        });
    }

    private static boolean isVisible(Node node) {
        for (Node cursor = node; cursor != null; cursor = cursor.getParent()) {
            if (!cursor.isVisible()) return false;
        }
        return true;
    }

    private static boolean isInside(Node node, Node ancestor) {
        for (Node cursor = node; cursor != null; cursor = cursor.getParent()) {
            if (cursor == ancestor) return true;
        }
        return false;
    }

    private static void collect(Parent parent, List<Control> controls) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof ButtonBase || child instanceof ComboBox<?>) {
                controls.add((Control) child);
            } else if (child instanceof Parent nested) collect(nested, controls);
        }
    }

    private static void focus(Control control, Parent content, ScrollPane scrollPane) {
        control.requestFocus();
        Bounds bounds = content.sceneToLocal(control.localToScene(control.getBoundsInLocal()));
        double overflow = content.getLayoutBounds().getHeight() - scrollPane.getViewportBounds().getHeight();
        if (overflow > 0) {
            double top = scrollPane.getVvalue() * overflow;
            double bottom = top + scrollPane.getViewportBounds().getHeight();
            if (bounds.getMinY() < top) scrollPane.setVvalue(Math.max(0, bounds.getMinY() / overflow));
            else if (bounds.getMaxY() > bottom) scrollPane.setVvalue(Math.min(1,
                    (bounds.getMaxY() - scrollPane.getViewportBounds().getHeight()) / overflow));
        }
    }
}
