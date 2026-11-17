package com.team10.tetris.ui;

import java.nio.file.Path;
import java.util.*;
import com.team10.tetris.game.*;
import com.team10.tetris.game.versus.*;
import com.team10.tetris.input.VersusKeyBindings;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

/** 두 보드에 대한 입력을 하나의 화면 필터에서 분배한다. 기록 저장 경로는 없다. */
public final class LocalVersusView extends BorderPane {
    public static final Path KEY_FILE = Path.of(System.getProperty("user.home"),".team10-tetris","versus-keys.properties");
    private final VersusMatch match;
    private final BoardRenderer renderer;
    private final Canvas[] boards = {new Canvas(220,440),new Canvas(220,440)};
    private final Canvas[] next = {new Canvas(100,80),new Canvas(100,80)};
    private final Canvas[] pending = {new Canvas(70,70),new Canvas(70,70)};
    private final Label[] scores = {new Label(),new Label()};
    private final Label[] guides = {new Label(),new Label()};
    private final Label status = new Label();
    private final Set<KeyCode> held = new HashSet<>();
    private final Map<KeyCode,Integer> repeatAfterMs = new HashMap<>();
    private VersusKeyBindings keys;
    private boolean paused;
    private final AnimationTimer timer;

    public LocalVersusView(VersusMatch match, BoardRenderer renderer, Runnable restart, Runnable menu) {
        this(match,renderer,restart,menu,VersusKeyBindings.load(KEY_FILE));
    }
    public LocalVersusView(VersusMatch match, BoardRenderer renderer, Runnable restart, Runnable menu, VersusKeyBindings keys) {
        this.match=match; this.renderer=renderer;
        this.keys=Objects.requireNonNull(keys);
        AppFonts.apply(this);
        getStyleClass().add("local-versus");
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/team10/tetris/local-versus.css")).toExternalForm());
        HBox players=new HBox(24); players.setAlignment(Pos.CENTER);
        for(PlayerSide side:PlayerSide.values()) {
            int i=side.ordinal();
            VBox info=new VBox(4,new Label("NEXT"),next[i],new Label("받은 공격"),pending[i]);
            HBox area=new HBox(8,boards[i],info);
            Label guide=guides[i]; guide.setText(keys.description(side)); guide.setWrapText(true); guide.setMaxWidth(300);
            VBox player=new VBox(8,new Label("PLAYER "+(i+1)),scores[i],area,guide); players.getChildren().add(player);
        }
        ScrollPane scroll=new ScrollPane(players); scroll.setFitToWidth(true); scroll.setFitToHeight(true); setCenter(scroll);
        scroll.viewportBoundsProperty().addListener((o,oldBounds,bounds)->{
            double cell=Math.max(8,Math.min(32,Math.min((bounds.getWidth()-240)/20,(bounds.getHeight()-95)/20)));
            for(Canvas board:boards) { board.setWidth(cell*Board.WIDTH); board.setHeight(cell*Board.HEIGHT); }
            redraw();
        });
        Button pause=new Button("일시정지 / 계속 (ESC)"); pause.setOnAction(e->togglePause());
        Button configure=new Button("2인 키 설정"); configure.setOnAction(e->configureKeys());
        Button again=new Button("재시작"); again.setOnAction(e->restart.run());
        Button back=new Button("메인 메뉴"); back.setOnAction(e->menu.run());
        setTop(status); setBottom(new javafx.scene.layout.FlowPane(12,8,pause,configure,again,back));
        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED,e->{
            if(e.getCode()==KeyCode.ESCAPE) {
                if(!held.contains(e.getCode())) { togglePause(); held.add(e.getCode()); }
                e.consume(); return;
            }
            var input=keys.inputFor(e.getCode());
            if(input.isPresent()) {
                var action=input.get();
                boolean first=held.add(e.getCode());
                if(first) {
                    match.handle(action.player(),action.command());
                    if(action.command()!=GameCommand.HARD_DROP && action.command()!=GameCommand.ROTATE)
                        repeatAfterMs.put(e.getCode(),200);
                }
                redraw(); e.consume();
            }
        });
        addEventFilter(KeyEvent.KEY_RELEASED,e->{ held.remove(e.getCode()); repeatAfterMs.remove(e.getCode()); });
        timer=new AnimationTimer() {
            private long previous;
            public void handle(long now) {
                if(previous==0) previous=now;
                int elapsed=(int)Math.min(Integer.MAX_VALUE,(now-previous)/1_000_000);
                advanceFrame(elapsed);
                previous+=elapsed*1_000_000L; redraw();
            }
        };
        sceneProperty().addListener((o,oldScene,newScene)->{
            held.clear(); repeatAfterMs.clear();
            if(newScene==null) timer.stop(); else { timer.start(); requestFocus(); }
        });
        focusedProperty().addListener((o,oldValue,focused)->{if(!focused) { held.clear(); repeatAfterMs.clear(); }});
        redraw();
    }
    void advanceFrame(int elapsedMs) {
        match.advance(elapsedMs);
        if(!paused) for(KeyCode code:List.copyOf(held)) {
            if(!repeatAfterMs.containsKey(code)) continue;
            int until=repeatAfterMs.get(code)-elapsedMs;
            while(until<=0) {
                keys.inputFor(code).ifPresent(input->match.handle(input.player(),input.command()));
                until+=100;
            }
            repeatAfterMs.put(code,until);
        }
    }
    private void togglePause() {
        if(match.result().isPresent()) return;
        paused=!paused; held.clear(); repeatAfterMs.clear(); if(paused) match.pause(); else match.resume(); redraw(); requestFocus();
    }
    private void configureKeys() {
        if(!paused && match.result().isEmpty()) togglePause();
        Dialog<Void> dialog=new Dialog<>(); dialog.initOwner(getScene().getWindow()); dialog.setTitle("2인 키 설정");
        VBox rows=new VBox(6); Label error=new Label("변경 버튼을 누른 뒤 새 키를 입력하세요. ESC는 일시정지용입니다."); rows.getChildren().add(error);
        final VersusKeyBindings[] draft={keys};
        for(PlayerSide p:PlayerSide.values()) for(GameCommand c:GameCommand.values()) {
            var input=new VersusKeyBindings.Input(p,c); Button button=new Button(draft[0].keyFor(input).getName());
            button.setOnAction(e->{
                button.setText("키 입력…"); button.setOnKeyPressed(event->{
                    event.consume();
                    try { draft[0]=draft[0].with(input,event.getCode()); button.setText(event.getCode().getName()); button.setOnKeyPressed(null); error.setText(""); }
                    catch(IllegalArgumentException ex) { error.setText(ex.getMessage()); }
                }); button.requestFocus();
            });
            rows.getChildren().add(new HBox(8,new Label((p.ordinal()+1)+"P "+VersusKeyBindings.label(c)),button));
        }
        dialog.getDialogPane().setContent(rows); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION,e->{
            try { draft[0].save(KEY_FILE); keys=draft[0]; }
            catch(java.io.IOException ex) { error.setText("저장 실패: "+ex.getMessage()); e.consume(); }
        });
        dialog.showAndWait();
        for(PlayerSide side:PlayerSide.values()) guides[side.ordinal()].setText(keys.description(side));
        held.clear(); requestFocus();
    }
    public void redraw() {
        for(PlayerSide side:PlayerSide.values()) {
            int i=side.ordinal(); GameSnapshot s=match.snapshot(side);
            renderer.drawBoard(boards[i],s); renderer.drawNext(next[i],s); renderer.drawPendingAttack(pending[i],s.pendingAttack());
            scores[i].setText("점수 "+s.score()+" · 삭제 "+s.totalClearedLines()+"줄");
        }
        status.setText(match.result().map(r->r.isDraw()?"무승부":"PLAYER "+(r.winner().ordinal()+1)+" 승리").orElse(
                paused?"일시정지":match.config().rule().label()+(match.remainingTimeMs()<0?"":" · 남은 시간 "+((match.remainingTimeMs()+999)/1000)+"초")));
    }
}
