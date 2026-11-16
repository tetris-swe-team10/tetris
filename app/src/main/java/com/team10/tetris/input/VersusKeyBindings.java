package com.team10.tetris.input;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import com.team10.tetris.game.GameCommand;
import com.team10.tetris.game.versus.PlayerSide;
import javafx.scene.input.KeyCode;

/** 1인 설정과 독립적으로 저장하는 2인 키. 두 플레이어 전체에서 중복을 금지한다. */
public final class VersusKeyBindings {
    public record Input(PlayerSide player, GameCommand command) {}
    private final Map<Input,KeyCode> keys;
    public VersusKeyBindings(Map<Input,KeyCode> keys) {
        if (keys.size() != 10) throw new IllegalArgumentException("두 플레이어의 5개 키가 필요합니다.");
        Set<KeyCode> used = new HashSet<>();
        for (PlayerSide player : PlayerSide.values()) for (GameCommand command : GameCommand.values()) {
            KeyCode key = keys.get(new Input(player,command));
            if (key == null || key.isModifierKey() || key == KeyCode.ESCAPE || key == KeyCode.TAB
                    || key == KeyCode.UNDEFINED || !used.add(key))
                throw new IllegalArgumentException("키가 중복되거나 예약된 키입니다.");
        }
        this.keys = Map.copyOf(keys);
    }
    public static VersusKeyBindings defaults() {
        Map<Input,KeyCode> keys = new HashMap<>();
        KeyCode[][] defaults = {{KeyCode.A,KeyCode.D,KeyCode.S,KeyCode.W,KeyCode.SPACE},
                {KeyCode.LEFT,KeyCode.RIGHT,KeyCode.DOWN,KeyCode.UP,KeyCode.ENTER}};
        GameCommand[] commands = {GameCommand.LEFT,GameCommand.RIGHT,GameCommand.DOWN,GameCommand.ROTATE,GameCommand.HARD_DROP};
        for (PlayerSide p:PlayerSide.values()) for(int i=0;i<commands.length;i++) keys.put(new Input(p,commands[i]),defaults[p.ordinal()][i]);
        return new VersusKeyBindings(keys);
    }
    public KeyCode keyFor(Input input) { return keys.get(input); }
    public Optional<Input> inputFor(KeyCode key) { return keys.entrySet().stream().filter(e->e.getValue()==key).map(Map.Entry::getKey).findFirst(); }
    public VersusKeyBindings with(Input input, KeyCode key) {
        Map<Input,KeyCode> changed = new HashMap<>(keys); changed.put(input,key); return new VersusKeyBindings(changed);
    }
    public String description(PlayerSide player) {
        return String.join(" · ", Arrays.stream(GameCommand.values()).map(c->label(c)+" "+keyFor(new Input(player,c)).getName()).toList());
    }
    public static String label(GameCommand command) {
        return command==GameCommand.HARD_DROP ? "바로 내리기" : GameKeyBindings.label(command);
    }
    private static String id(Input input) { return input.player()+"."+input.command(); }
    public void save(Path path) throws IOException {
        Properties p = new Properties(); keys.forEach((input,key)->p.setProperty(id(input),key.name()));
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(path.toAbsolutePath().getParent(),"versus-keys-",".tmp");
        try {
            try(Writer writer=Files.newBufferedWriter(temp)) { p.store(writer,"Versus key bindings"); }
            Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
    }
    public static VersusKeyBindings load(Path path) {
        try(Reader reader=Files.newBufferedReader(path)) {
            Properties p=new Properties(); p.load(reader); Map<Input,KeyCode> keys=new HashMap<>();
            for(PlayerSide side:PlayerSide.values()) for(GameCommand c:GameCommand.values()) {
                Input input=new Input(side,c); keys.put(input,KeyCode.valueOf(p.getProperty(id(input))));
            }
            return new VersusKeyBindings(keys);
        } catch(IOException|IllegalArgumentException|NullPointerException e) { return defaults(); }
    }
}
