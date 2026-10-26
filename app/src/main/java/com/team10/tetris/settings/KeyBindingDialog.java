package com.team10.tetris.settings;

import com.team10.tetris.game.GameCommand;
import com.team10.tetris.input.GameKeyBindings;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import java.util.EnumMap;
import java.util.Map;

/** 변경할 조작을 고른 뒤 실제 키를 누른다. 적용 전까지 원래 설정은 유지한다. */
public final class KeyBindingDialog extends Dialog<GameKeyBindings> {
    private final Map<GameCommand, KeyCode> draft = new EnumMap<>(GameCommand.class);
    private final Map<GameCommand, Button> buttons = new EnumMap<>(GameCommand.class);
    private GameCommand capturing;

    public KeyBindingDialog(Window owner, GameKeyBindings current) {
        if (owner != null) initOwner(owner);
        setTitle("키 변경");
        getDialogPane().getStylesheets().add(java.util.Objects.requireNonNull(
                getClass().getResource("/com/team10/tetris/dialog.css")).toExternalForm());
        getDialogPane().getStyleClass().add("tetris-dialog");
        draft.putAll(current.asMap());
        Label instructions = new Label("변경할 조작을 선택한 뒤 사용할 키를 누르세요.\nESC는 키 입력 취소, SPACE는 바로 내리기입니다.\nEnter·Tab·1~3 및 보조 키는 고정 기능으로 사용합니다.");
        instructions.setWrapText(true);
        instructions.setStyle("-fx-text-fill: #E8EEF2;");
        Label message = new Label();
        message.setId("key-binding-message"); message.setWrapText(true); message.setStyle("-fx-text-fill: #98A2B3;");
        VBox content = new VBox(12, instructions);
        content.setPrefWidth(400);
        content.setStyle("-fx-padding: 20;");
        for (GameCommand action : GameKeyBindings.ACTIONS) {
            Button button = new Button();
            button.setId("bind-" + action.name().toLowerCase(java.util.Locale.ROOT));
            button.setMaxWidth(Double.MAX_VALUE);
            buttons.put(action, button);
            update(action);
            button.setOnAction(event -> {
                if (capturing != null) update(capturing);
                capturing = action;
                button.setText(GameKeyBindings.label(action) + " : 키를 눌러 주세요");
                message.setText("ESC를 누르면 이번 키 입력을 취소합니다.");
            });
            content.getChildren().add(button);
        }
        content.getChildren().add(message);
        getDialogPane().setContent(content);
        ButtonType apply = new ButtonType("적용", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(apply, ButtonType.CANCEL);
        getDialogPane().setId("key-binding-dialog");
        getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (capturing == null) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    for (Button button : buttons.values()) {
                        if (getDialogPane().getScene() != null && getDialogPane().getScene().getFocusOwner() == button) {
                            button.fire(); event.consume(); break;
                        }
                    }
                }
                return;
            }
            event.consume();
            if (event.getCode() == KeyCode.ESCAPE) {
                update(capturing); capturing = null; message.setText("키 입력을 취소했습니다."); return;
            }
            try {
                if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown())
                    throw new IllegalArgumentException("조합 키 대신 키 하나를 눌러 주세요.");
                Map<GameCommand, KeyCode> candidate = new EnumMap<>(draft);
                candidate.put(capturing, event.getCode());
                GameKeyBindings checked = new GameKeyBindings(candidate);
                draft.clear(); draft.putAll(checked.asMap());
                update(capturing); capturing = null; message.setText("적용을 누르면 게임 조작 키가 변경됩니다.");
            } catch (IllegalArgumentException exception) { message.setText(exception.getMessage()); }
        });
        setResultConverter(type -> type == apply ? new GameKeyBindings(draft) : null);
        setOnShown(event -> Platform.runLater(() -> buttons.get(GameCommand.LEFT).requestFocus()));
    }
    private void update(GameCommand action) {
        buttons.get(action).setText(GameKeyBindings.label(action) + " : " + draft.get(action).getName());
    }
}
