package com.team10.tetris.score;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class ScoreboardManager {


    /** 스코어보드에 유지하는 최대 기록 수. */
    public static final int MAX_RECORDS = 10;

    /** 순위에 들지 못했음을 나타내는 값. */
    public static final int UNRANKED = -1;

    private final Path filePath;

    public ScoreboardManager() {
        this(
                Path.of(
                        System.getProperty("user.home"),
                        ".team10-tetris",
                        "scores.tsv"
                )
        );
    }

    public ScoreboardManager(Path filePath) {
        this.filePath = filePath;
    }

    public void save(GameRecord record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException(
                    "게임 기록이 null일 수 없습니다."
            );
        }

        if (record.score() < 0 || record.clearedLines() < 0) {
            throw new IllegalArgumentException(
                    "점수와 삭제한 줄 수는 음수일 수 없습니다."
            );
        }

        Path parent = filePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        String encodedName = Base64.getEncoder()
                .encodeToString(
                        record.playerName().getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        String line = encodedName
                + "\t" + record.score()
                + "\t" + record.clearedLines()
                + "\t" + record.playedAt()
                + System.lineSeparator();

        Files.writeString(
                filePath,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public void save(
            String playerName,
            int score,
            int clearedLines
    ) throws IOException {
        save(
                new GameRecord(
                        playerName,
                        score,
                        clearedLines,
                        LocalDateTime.now()
                )
        );
    }

    public List<GameRecord> getRecords() throws IOException {
        List<GameRecord> records = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return records;
        }

        for (String line : Files.readAllLines(
                filePath,
                StandardCharsets.UTF_8
        )) {
            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\t", -1);

            if (parts.length != 4) {
                continue;
            }

            try {
                String playerName = new String(
                        Base64.getDecoder().decode(parts[0]),
                        StandardCharsets.UTF_8
                );

                int score = Integer.parseInt(parts[1]);
                int clearedLines = Integer.parseInt(parts[2]);

                LocalDateTime playedAt =
                        LocalDateTime.parse(parts[3]);

                records.add(
                        new GameRecord(
                                playerName,
                                score,
                                clearedLines,
                                playedAt
                        )
                );
            } catch (IllegalArgumentException exception) {
                // 손상된 기록은 건너뛰고 나머지 기록을 조회
            }
        }

        records.sort(
                Comparator.comparingInt(GameRecord::score)
                        .reversed()
                        .thenComparing(
                                GameRecord::playedAt,
                                Comparator.reverseOrder()
                        )
        );

        return records;
    }

    public List<GameRecord> getTopRecords(int limit)
            throws IOException {

        if (limit < 0) {
            throw new IllegalArgumentException(
                    "조회 개수는 음수일 수 없습니다."
            );
        }

        return getRecords()
                .stream()
                .limit(limit)
                .toList();
    }

    public int getHighScore() throws IOException {
        List<GameRecord> records = getTopRecords(1);

        if (records.isEmpty()) {
            return 0;
        }

        return records.get(0).score();
    }

    /** 점수가 스코어보드에 등록될 수 있는지 확인. 동점이면 기존 기록을 유지한다. */
    public boolean isHighScore(int score) throws IOException {
        if (score <= 0) {
            return false;
        }

        List<GameRecord> records = getRecords();

        // 아직 자리가 남아있으면 무조건 등록 가능
        if (records.size() < MAX_RECORDS) {
            return true;
        }

        // 가득 찬 경우 최하위 기록보다 높아야 등록 가능
        return score > records.get(MAX_RECORDS - 1).score();
    }

    /** 기록을 저장하고 순위(1부터)를 반환. 등록되지 못하면 {@link #UNRANKED}. */
    public int saveAndGetRank(GameRecord record) throws IOException {
        save(record);
        trim();

        List<GameRecord> records = getRecords();

        for (int index = 0; index < records.size(); index++) {
            if (records.get(index).equals(record)) {
                return index + 1;
            }
        }

        // trim()에서 밀려난 경우
        return UNRANKED;
    }
    /**
     * 이름과 점수로 기록을 저장한 뒤 순위를 반환
     * @see #saveAndGetRank(GameRecord)
     */
    public int saveAndGetRank(
            String playerName,
            int score,
            int clearedLines
    ) throws IOException {
        return saveAndGetRank(
                new GameRecord(
                        playerName,
                        score,
                        clearedLines,
                        LocalDateTime.now()
                )
        );
    }
    /**
     * 저장된 모든 기록을 삭제
     * 설정 화면의 "스코어 보드 기록 초기화" 기능에서 사용
     */
    public void clear() throws IOException {
        Files.deleteIfExists(filePath);
    }

    /**
     * 상위 {@link #MAX_RECORDS}개만 남기고 나머지 기록을 정리
     */
    private void trim() throws IOException {
        List<GameRecord> records = getRecords();

        if (records.size() <= MAX_RECORDS) {
            return;
        }

        List<GameRecord> kept = List.copyOf(
                records.subList(0, MAX_RECORDS)
        );

        Files.deleteIfExists(filePath);

        for (GameRecord record : kept) {
            save(record);
        }
    }
}
