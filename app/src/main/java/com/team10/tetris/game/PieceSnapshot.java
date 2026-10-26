package com.team10.tetris.game;
import java.util.Arrays;
import java.util.List;
public record PieceSnapshot(TetrominoType type, int row, int col, List<List<Integer>> shape,
        ItemMarker item) {
    /** 효과 객체 대신 화면에 필요한 값만 복사한다. */
    public record ItemMarker(String symbol, String description, int row, int col) {}
    public PieceSnapshot(TetrominoType type, int row, int col, List<List<Integer>> shape) {
        this(type, row, col, shape, null);
    }
    public PieceSnapshot { shape = shape.stream().map(List::copyOf).toList(); }
    public static PieceSnapshot of(Tetromino piece) {
        var attached = piece.getItem();
        ItemMarker marker = attached == null ? null : new ItemMarker(attached.item().symbol(),
                attached.item().description(), attached.row(), attached.col());
        return new PieceSnapshot(piece.getType(), piece.getRow(), piece.getCol(),
                Arrays.stream(piece.getShape())
                        .map(row -> Arrays.stream(row).boxed().toList()).toList(), marker);
    }
}
