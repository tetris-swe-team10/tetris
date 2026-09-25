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
}