package com.team10.tetris.score;

import com.team10.tetris.game.Difficulty;
import com.team10.tetris.game.GameMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** TSV 영속성 및 모드/난이도별 상위10개 순위를 관리한다. */
public class ScoreboardManager {
    public static final int MAX_RECORDS = 10;
    public static final int UNRANKED = -1;
    private final Path filePath;

    public ScoreboardManager() {
        this(Path.of(System.getProperty("user.home"), ".team10-tetris", "scores.tsv"));
    }
    public ScoreboardManager(Path filePath) { this.filePath = filePath; }

    private String encode(GameRecord record) {
        String name = Base64.getEncoder().encodeToString(record.playerName().getBytes(StandardCharsets.UTF_8));
        return name + "\t" + record.score() + "\t" + record.clearedLines() + "\t" + record.playedAt()
                + "\t" + record.mode().name() + "\t" + record.difficulty().name() + System.lineSeparator();
    }

    public void save(GameRecord record) throws IOException {
        if (record == null) throw new IllegalArgumentException("게임 기록이 null일 수 없습니다.");
        Path parent = filePath.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Files.writeString(filePath, encode(record), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    public void save(String playerName, int score, int clearedLines) throws IOException {
        save(new GameRecord(playerName, score, clearedLines, LocalDateTime.now()));
    }

    public List<GameRecord> getRecords() throws IOException {
        List<GameRecord> records = new ArrayList<>();
        if (!Files.exists(filePath)) return records;
        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length != 4 && parts.length != 6) continue;
            try {
                records.add(new GameRecord(
                        new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8),
                        Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), LocalDateTime.parse(parts[3]),
                        parts.length == 4 ? GameMode.NORMAL : GameMode.valueOf(parts[4]),
                        parts.length == 4 ? Difficulty.NORMAL : Difficulty.valueOf(parts[5])));
            } catch (IllegalArgumentException | java.time.format.DateTimeParseException exception) {
                // 손상된 행만 건너뛴다. 파일 전체를 초기화하지 않는다.
            }
        }
        records.sort(Comparator.comparingInt(GameRecord::score).reversed()
                .thenComparing(GameRecord::playedAt, Comparator.reverseOrder()));
        return records;
    }

    public List<GameRecord> getRecords(GameMode mode, Difficulty difficulty) throws IOException {
        Objects.requireNonNull(mode); Objects.requireNonNull(difficulty);
        return getRecords().stream().filter(r -> r.mode() == mode && r.difficulty() == difficulty).toList();
    }
    public List<GameRecord> getTopRecords(int limit) throws IOException {
        if (limit < 0) throw new IllegalArgumentException("조회 개수는 음수일 수 없습니다.");
        return getRecords().stream().limit(limit).toList();
    }
    public List<GameRecord> getTopRecords(GameMode mode, Difficulty difficulty, int limit) throws IOException {
        if (limit < 0) throw new IllegalArgumentException("조회 개수는 음수일 수 없습니다.");
        return getRecords(mode, difficulty).stream().limit(limit).toList();
    }
    public int getHighScore() throws IOException {
        return getRecords().stream().findFirst().map(GameRecord::score).orElse(0);
    }
    public int getHighScore(GameMode mode, Difficulty difficulty) throws IOException {
        return getRecords(mode, difficulty).stream().findFirst().map(GameRecord::score).orElse(0);
    }
    public boolean isHighScore(int score) throws IOException {
        return isHighScore(score, GameMode.NORMAL, Difficulty.NORMAL);
    }
    public boolean isHighScore(int score, GameMode mode, Difficulty difficulty) throws IOException {
        if (score <= 0) return false;
        List<GameRecord> records = getRecords(mode, difficulty);
        return records.size() < MAX_RECORDS || score > records.get(MAX_RECORDS - 1).score();
    }
    public int saveAndGetRank(GameRecord record) throws IOException {
        if (record == null) throw new IllegalArgumentException("게임 기록이 null일 수 없습니다.");
        // 가득 찬 그룹의 최하위 동점이면 기존 기록 유지.
        if (!isHighScore(record.score(), record.mode(), record.difficulty())) return UNRANKED;
        save(record);
        trim();
        List<GameRecord> records = getRecords(record.mode(), record.difficulty());
        int index = records.indexOf(record);
        return index < 0 ? UNRANKED : index + 1;
    }
    public int saveAndGetRank(String playerName, int score, int clearedLines) throws IOException {
        return saveAndGetRank(new GameRecord(playerName, score, clearedLines, LocalDateTime.now()));
    }
    public int saveAndGetRank(String playerName, int score, int clearedLines,
            GameMode mode, Difficulty difficulty) throws IOException {
        return saveAndGetRank(new GameRecord(playerName, score, clearedLines, LocalDateTime.now(), mode, difficulty));
    }
    public void clear() throws IOException { Files.deleteIfExists(filePath); }

    private void trim() throws IOException {
        List<GameRecord> all = getRecords();
        List<GameRecord> kept = new ArrayList<>();
        for (GameMode mode : GameMode.values()) {
            for (Difficulty difficulty : Difficulty.values()) {
                all.stream().filter(r -> r.mode() == mode && r.difficulty() == difficulty)
                        .limit(MAX_RECORDS).forEach(kept::add);
            }
        }
        if (kept.size() == all.size()) return;
        // 기존 파일을 지우고 한 행씩 다시 쓰지 않는다.
        Path target = filePath.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), "scores-", ".tmp");
        try {
            StringBuilder content = new StringBuilder();
            for (GameRecord record : kept) content.append(encode(record));
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
