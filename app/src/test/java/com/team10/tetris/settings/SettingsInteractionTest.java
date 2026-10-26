package com.team10.tetris.settings;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class SettingsInteractionTest {
    @TempDir Path temp;
    private Stage stage;
    private GameSettings settings;
    private static void fx(Runnable action) throws Exception {
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.runLater(()->{try{action.run();}catch(Throwable t){failure.set(t);}finally{done.countDown();}});
        assertTrue(done.await(15,TimeUnit.SECONDS));
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    @BeforeAll static void start() throws Exception {
        try {Platform.startup(()->Platform.setImplicitExit(false));}catch(IllegalStateException ready){}
        fx(()->{});
    }
    @BeforeEach void setup() throws Exception {
        fx(()->{settings=new GameSettings(new SettingsManager(temp.resolve("settings.properties")));stage=new Stage();});
    }
    @AfterEach void close() throws Exception {fx(()->stage.close());}
    private void show(Parent root) {
        Scene scene=new Scene(root,640,480);
        scene.getStylesheets().add(getClass().getResource("/com/team10/tetris/settings.css").toExternalForm());
        stage.setScene(scene);stage.show();root.applyCss();root.layout();
    }
    private ButtonBase button(Parent root,String text) {
        return java.util.stream.Stream.concat(root.lookupAll(".button").stream(), root.lookupAll(".toggle-button").stream()).filter(n->n instanceof ButtonBase b && b.getText().equals(text))
                .map(n->(ButtonBase)n).findFirst().orElseThrow();
    }
    private void key(Node node,KeyCode code) {node.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));}
    private boolean visible(Node node) {
        for(Node n=node;n!=null;n=n.getParent())if(!n.isVisible())return false;
        return true;
    }
    @Test void keyboardRowsOptionsDemoControlsAndPersistence() throws Exception {
        AtomicInteger back=new AtomicInteger(), reset=new AtomicInteger();
        fx(()->show(new SettingsScreen(settings,m->{},reset::incrementAndGet,back::incrementAndGet)));
        fx(()->{
            Parent root=stage.getScene().getRoot();
            assertEquals("느리게",((ButtonBase)stage.getScene().getFocusOwner()).getText());
            key(root,KeyCode.RIGHT);key(root,KeyCode.RIGHT);key(root,KeyCode.ENTER);
            assertEquals(DropSpeed.FAST,settings.getDropSpeed());
            key(root,KeyCode.DOWN);
            assertInstanceOf(ComboBox.class,stage.getScene().getFocusOwner());
            key(root,KeyCode.LEFT);
            assertEquals(DisplayMode.WINDOWED,settings.getDisplayMode());
            key(root,KeyCode.DOWN);
            assertEquals("키 변경", ((ButtonBase)stage.getScene().getFocusOwner()).getText());
            key(root,KeyCode.DOWN);key(root,KeyCode.ENTER);
            assertTrue(settings.isColorBlindMode());
            assertTrue(new SettingsManager(temp.resolve("settings.properties")).loadColorBlindMode());
            Node plus=root.lookup("#demo-item-lines-more");
            assertFalse(visible(plus));
            key(root,KeyCode.DOWN);key(root,KeyCode.ENTER);
            assertTrue(settings.isDemoMode());assertTrue(visible(plus));
            key(root,KeyCode.DOWN);key(root,KeyCode.DOWN);key(root,KeyCode.ENTER);
            assertEquals(2,settings.getDemoItemLines());
            ((Button)root.lookup("#demo-item-lines-less")).fire();
            assertEquals(1,settings.getDemoItemLines());
            button(root,"점수 초기화").requestFocus();key(root,KeyCode.ENTER);
            assertEquals(1,reset.get());
            button(root,"시작 화면으로").requestFocus();key(root,KeyCode.ENTER);
            assertEquals(1,back.get());key(root,KeyCode.ESCAPE);assertEquals(2,back.get());
            key(root,KeyCode.DOWN);key(root,KeyCode.UP);key(root,KeyCode.W);key(root,KeyCode.S);
            ScrollPane scroll=(ScrollPane)root.lookup(".settings-scroll");
            assertTrue(scroll.getVvalue()>=0 && scroll.getVvalue()<=1);
        });
    }
    @Test void popupRequiresConfirmationAndEscapeCancels() throws Exception {
        AtomicReference<DisplayMode> applied=new AtomicReference<>();
        fx(()->show(new SettingsScreen(settings,applied::set,()->{},()->{})));
        fx(()->{
            Parent root=stage.getScene().getRoot();
            ComboBox<?> combo=(ComboBox<?>)root.lookup(".combo-box");
            combo.requestFocus();key(root,KeyCode.ENTER);
            assertTrue(combo.isShowing());
            ListView<?> list=(ListView<?>)((ComboBoxListViewSkin<?>)combo.getSkin()).getPopupContent();
            key(list,KeyCode.DOWN);
            assertEquals(DisplayMode.WINDOWED,settings.getDisplayMode());assertNull(applied.get());
            key(list,KeyCode.ENTER);
            assertFalse(combo.isShowing());assertNotEquals(DisplayMode.WINDOWED,applied.get());
            DisplayMode confirmed=settings.getDisplayMode();
            combo.show();key(list,KeyCode.END);key(list,KeyCode.ESCAPE);
            assertEquals(confirmed,settings.getDisplayMode());
            combo.show();key(list,KeyCode.HOME);key(list,KeyCode.RIGHT);key(list,KeyCode.LEFT);
            key(list,KeyCode.PAGE_DOWN);key(list,KeyCode.PAGE_UP);key(list,KeyCode.A);key(list,KeyCode.TAB);
            assertEquals(confirmed,settings.getDisplayMode());
        });
    }
    @Test void pauseDialogActionsAndResizeRemainUsable() throws Exception {
        AtomicInteger resume=new AtomicInteger(),menu=new AtomicInteger();
        fx(()->show(new PauseSettingsPane(settings,m->{},()->{},resume::incrementAndGet,menu::incrementAndGet)));
        fx(()->{
            Parent root=stage.getScene().getRoot();
            button(root,"게임 계속").requestFocus();key(root,KeyCode.RIGHT);key(root,KeyCode.ENTER);
            assertEquals(1,menu.get());
            key(root,KeyCode.LEFT);key(root,KeyCode.ENTER);assertEquals(1,resume.get());
            key(root,KeyCode.ESCAPE);assertEquals(2,resume.get());
            stage.setWidth(960);stage.setHeight(720);root.applyCss();root.layout();
            ScrollPane scroll=(ScrollPane)root.lookup(".pause-settings-scroll");
            assertTrue(scroll.getWidth()<=root.getLayoutBounds().getWidth());
            assertTrue(scroll.getHeight()<=root.getLayoutBounds().getHeight());
            button(root,"느리게").fire();assertEquals(DropSpeed.SLOW,settings.getDropSpeed());
            button(root,"보통").fire();assertEquals(DropSpeed.NORMAL,settings.getDropSpeed());
        });
    }
    @Test void optionalKeyEditorCallbackIsInvoked() throws Exception {
        AtomicInteger changes=new AtomicInteger();
        fx(()->{
            SettingsPanel panel=new SettingsPanel(settings,m->{},()->{},changes::incrementAndGet);
            show(panel);button(panel,"키 변경").fire();assertEquals(1,changes.get());
        });
    }
}
