package com.team10.tetris.settings;

import com.team10.tetris.game.*;
import com.team10.tetris.input.GameKeyBindings;
import com.team10.tetris.ui.GameView;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class SettingsAdditionsUiTest {
    @TempDir Path temp;
    private Stage stage;
    private GameSettings settings;
    private static void fx(Runnable task) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{try{task.run();}catch(Throwable t){failure.set(t);}finally{done.countDown();}});
        assertTrue(done.await(15,TimeUnit.SECONDS));if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    @BeforeAll static void start() throws Exception {
        try{Platform.startup(()->Platform.setImplicitExit(false));}catch(IllegalStateException started){}fx(()->{});
    }
    @BeforeEach void setup() throws Exception {fx(()->{stage=new Stage();settings=new GameSettings(new SettingsManager(temp.resolve("settings.properties")));});}
    @AfterEach void close() throws Exception {fx(()->{stage.setScene(new Scene(new StackPane()));stage.close();});}
    private void show(Parent root) {
        Scene scene=new Scene(root,960,720);
        scene.getStylesheets().add(getClass().getResource("/com/team10/tetris/settings.css").toExternalForm());
        stage.setScene(scene);stage.show();root.applyCss();root.layout();
    }
    private static void key(Node node,KeyCode code) {node.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));}
    private static void click(Parent root,String id) {((Button)root.lookup("#"+id)).fire();}
    private static DialogPane dialog() {
        return Window.getWindows().stream().filter(Window::isShowing).map(w->w.getScene().getRoot())
                .filter(root->root instanceof DialogPane).map(root->(DialogPane)root).findFirst().orElseThrow();
    }
    private static void finish(DialogPane pane,boolean apply) {
        ButtonType type=pane.getButtonTypes().stream().filter(t->t.getButtonData()==
                (apply?ButtonBar.ButtonData.OK_DONE:ButtonBar.ButtonData.CANCEL_CLOSE)).findFirst().orElseThrow();
        ((Button)pane.lookupButton(type)).fire();
    }
    private void editFromPanel(Parent root,Consumer<DialogPane> edit,boolean apply) {
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{
            DialogPane pane=null;
            try {pane=dialog();edit.accept(pane);}
            catch(Throwable t){failure.set(t);}
            finally {if(pane!=null)finish(pane,apply && failure.get()==null);}
        });
        click(root,"change-keys");
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    @Test void keyDialogAssignsFourKeysAndGameUsesThemImmediately() throws Exception {
        fx(()->{
            SettingsScreen root=new SettingsScreen(settings,m->{},()->{},()->{});show(root);
            assertFalse(root.lookup("#change-keys").isDisabled());
            editFromPanel(root,pane->{
                pane.lookup("#bind-left").requestFocus();key(pane,KeyCode.ENTER);key(pane,KeyCode.RIGHT);
                assertTrue(((Label)pane.lookup("#key-binding-message")).getText().contains("이미"));
                key(pane,KeyCode.ESCAPE);
                click(pane,"bind-left");key(pane,KeyCode.SPACE);
                assertTrue(((Label)pane.lookup("#key-binding-message")).getText().contains("사용할 수 없"));
                pane.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.A,true,false,false,false));
                assertTrue(((Label)pane.lookup("#key-binding-message")).getText().contains("조합"));
                key(pane,KeyCode.A);
                click(pane,"bind-right");key(pane,KeyCode.D);
                click(pane,"bind-rotate");key(pane,KeyCode.W);
                click(pane,"bind-down");key(pane,KeyCode.S);
                capture(pane.getScene(), "key-bindings");
            },true);
            assertEquals(KeyCode.A,settings.getKeyBindings().keyFor(GameCommand.LEFT));
            GameEngine game=new GameEngine(new Board(),new Tetromino(TetrominoType.T,0,3),GameConfig.DEFAULT,
                    ()->new Tetromino(TetrominoType.O,0,3),new StandardScoringPolicy());
            GameView view=new GameView(game,settings);show(view);
            key(view,KeyCode.LEFT);assertEquals(3,game.getCurrentBlock().getCol());
            view.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.A,true,false,false,false));
            assertEquals(3,game.getCurrentBlock().getCol());
            key(view,KeyCode.A);assertEquals(2,game.getCurrentBlock().getCol());
            key(view,KeyCode.D);assertEquals(3,game.getCurrentBlock().getCol());
            key(view,KeyCode.S);assertEquals(1,game.getCurrentBlock().getRow());
            var before=game.snapshot().currentBlock().shape();key(view,KeyCode.W);
            assertNotEquals(before,game.snapshot().currentBlock().shape());
            key(view,KeyCode.SPACE);assertTrue(game.getScore()>0);
        });
    }
    @Test void cancelDiscardsDraftAndPauseSettingsCanEditToo() throws Exception {
        fx(()->{
            PauseSettingsPane root=new PauseSettingsPane(settings,m->{},()->{},()->{},()->{});show(root);
            editFromPanel(root,pane->{click(pane,"bind-left");key(pane,KeyCode.Q);},false);
            assertEquals(KeyCode.LEFT,settings.getKeyBindings().keyFor(GameCommand.LEFT));
            editFromPanel(root,pane->{click(pane,"bind-left");key(pane,KeyCode.Q);},true);
            assertEquals(KeyCode.Q,settings.getKeyBindings().keyFor(GameCommand.LEFT));
        });
    }
    @Test void saveAndResetRefreshControlsPersistAndKeepNavigationWorking() throws Exception {
        AtomicReference<DisplayMode> display=new AtomicReference<>();AtomicInteger resetScores=new AtomicInteger();
        fx(()->{
            settings.setDropSpeed(DropSpeed.FAST);settings.setDisplayMode(DisplayMode.PORTRAIT);
            settings.setDemoMode(true);settings.setDemoItemLines(5);
            SettingsScreen root=new SettingsScreen(settings,display::set,resetScores::incrementAndGet,()->{});show(root);
            Node footer=root.lookup("#settings-footer");
            assertEquals(footer,root.lookup("#save-settings").getParent());
            assertEquals(footer,root.lookup("#reset-settings").getParent());
            assertTrue(((javafx.scene.layout.HBox)footer).getChildren().get(0) instanceof Button back
                    && back.getText().equals("시작 화면으로"));
            ((ScrollPane)root.lookup(".settings-scroll")).setVvalue(1);root.layout();
            capture(stage.getScene(), "settings-footer");
            click(root,"save-settings");
            assertEquals(DisplayMode.PORTRAIT,new GameSettings(new SettingsManager(temp.resolve("settings.properties"))).getDisplayMode());
            assertTrue(((Label)root.lookup("#settings-save-status")).getText().contains("저장했습니다"));
            click(root,"reset-settings");root.applyCss();root.layout();
            assertEquals(DisplayMode.WINDOWED,display.get());assertEquals(0,resetScores.get());
            assertEquals(DropSpeed.NORMAL,settings.getDropSpeed());assertFalse(settings.isDemoMode());
            var combo=(ComboBox<?>)root.lookup(".combo-box");assertEquals(DisplayMode.WINDOWED,combo.getValue());
            assertTrue(root.lookupAll(".toggle-button").stream().anyMatch(n->n instanceof ToggleButton t && t.getText().equals("보통") && t.isSelected()));
            Button save=(Button)root.lookup("#save-settings");save.requestFocus();key(root,KeyCode.RIGHT);
            assertEquals(root.lookup("#reset-settings"),stage.getScene().getFocusOwner());
            key(root,KeyCode.LEFT);key(root,KeyCode.ENTER);
            assertEquals(DropSpeed.NORMAL,new GameSettings(new SettingsManager(temp.resolve("settings.properties"))).getDropSpeed());
        });
    }
    @Test void footerFitsPortraitAndLandscapeInBothSettingsScreens() throws Exception {
        fx(() -> {
            for (boolean paused : new boolean[]{false, true}) {
                for (int width : new int[]{640, 960, 1920}) {
                    Parent root = paused ? new PauseSettingsPane(settings, mode -> {}, () -> {}, () -> {}, () -> {})
                            : new SettingsScreen(settings, mode -> {}, () -> {}, () -> {});
                    show(root);
                    stage.setWidth(width);stage.setHeight(900);
                    root.applyCss();root.layout();
                    var footer = (javafx.scene.layout.HBox)root.lookup("#settings-footer");
                    var save = root.lookup("#save-settings");
                    var reset = root.lookup("#reset-settings");
                    assertEquals(footer,save.getParent());assertEquals(footer,reset.getParent());
                    var leading = footer.getChildren().get(0).getBoundsInParent();
                    assertTrue(save.getBoundsInParent().getMinX() > leading.getMaxX());
                    assertTrue(reset.getBoundsInParent().getMaxX() <= footer.getWidth() + 1);
                    assertEquals(save.getBoundsInParent().getMinY(),reset.getBoundsInParent().getMinY(),1);
                    assertTrue(footer.getChildren().stream().filter(node -> node instanceof Button).count() == (paused ? 4 : 3));
                }
            }
        });
    }

    @Test void startupAppliesPersistedDisplayMode() throws Exception {
        fx(() -> {
            settings.setDisplayMode(DisplayMode.PORTRAIT);
            try { settings.save(); } catch (java.io.IOException error) { throw new AssertionError(error); }
            GameSettings restored = new GameSettings(new SettingsManager(temp.resolve("settings.properties")));
            new com.team10.tetris.controller.AppController(stage, restored,
                    new com.team10.tetris.score.ScoreboardManager(temp.resolve("scores.tsv"))).start();
            assertTrue(stage.getWidth() < 800);
            assertTrue(stage.getHeight() > stage.getWidth());
            assertEquals(javafx.scene.input.KeyCombination.NO_MATCH, stage.getFullScreenExitKeyCombination());
        });
    }
    private static void capture(Scene scene, String name) {
        try {
            scene.getRoot().applyCss(); scene.getRoot().layout();
            var image = scene.snapshot(null);
            var png = new java.awt.image.BufferedImage((int)image.getWidth(), (int)image.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            for (int y=0;y<png.getHeight();y++) for (int x=0;x<png.getWidth();x++)
                png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
            Path file = Path.of("build/ui-smoke/" + name + ".png");
            Files.createDirectories(file.getParent()); javax.imageio.ImageIO.write(png,"png",file.toFile());
        } catch (java.io.IOException error) { throw new AssertionError(error); }
    }

    @Test void failedSaveAndResetShowFailureAndPreserveState() throws Exception {
        Path blocker=temp.resolve("blocker");Files.writeString(blocker,"file");
        fx(()->{
            settings=new GameSettings(new SettingsManager(blocker.resolve("settings.properties")));
            settings.setDropSpeed(DropSpeed.FAST);
            SettingsPanel panel=new SettingsPanel(settings,m->{},()->{},null);
            var root=new javafx.scene.layout.VBox(panel,panel.createFooter());show(root);
            click(root,"save-settings");assertTrue(((Label)panel.lookup("#settings-save-status")).getText().contains("못했습니다"));
            click(root,"reset-settings");assertEquals(DropSpeed.FAST,settings.getDropSpeed());
            assertTrue(((Label)panel.lookup("#settings-save-status")).getText().contains("실패"));
        });
    }
}
