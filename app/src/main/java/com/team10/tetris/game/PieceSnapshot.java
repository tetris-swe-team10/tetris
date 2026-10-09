package com.team10.tetris.game;
import java.util.Arrays;
import java.util.List;
public record PieceSnapshot(TetrominoType type, int row, int col, List<List<Integer>> shape) {
    public PieceSnapshot { shape = shape.stream().map(List::copyOf).toList(); }
    public static PieceSnapshot of(Tetromino piece) {
        return new PieceSnapshot(piece.getType(), piece.getRow(), piece.getCol(),
                Arrays.stream(piece.getShape())
                        .map(row -> Arrays.stream(row).boxed().toList()).toList());
    }
}
