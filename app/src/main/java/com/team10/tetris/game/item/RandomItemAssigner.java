package com.team10.tetris.game.item;

import com.team10.tetris.game.PenaltyController;
import com.team10.tetris.game.Tetromino;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * 등록된 아이템 전체에서 균등 선택한다. 부착형은 실제 점유 칸 중 한 칸에 붙이고,
 * 블록형(PieceItem)은 같은 위치의 전용 블록으로 교체한다.
 */
public final class RandomItemAssigner implements ItemAssigner {
    private final List<GameItem> items;
    private final RandomGenerator random;

    public RandomItemAssigner(List<GameItem> items, RandomGenerator random) {
        this.items = List.copyOf(items);
        if (this.items.isEmpty()) throw new IllegalArgumentException("No items registered");
        if (this.items.stream().map(GameItem::symbol).distinct().count() != this.items.size())
            throw new IllegalArgumentException("Duplicate item symbol");
        this.random = Objects.requireNonNull(random);
    }

    /** 부착만 하는 기존 계약. 블록형 아이템은 붙일 수 없으므로 후보에서 제외한다. */
    @Override
    public void attachTo(Tetromino piece) {
        attachFrom(piece, attachable(items));
    }

    @Override
    public void attachTo(Tetromino piece, PenaltyController penalties) {
        attachFrom(piece, attachable(available(penalties)));
    }

    /** 엔진 지급 경로: 블록형까지 포함한 전체 후보에서 균등 선택한다. */
    @Override
    public Tetromino award(Tetromino piece, PenaltyController penalties) {
        List<GameItem> eligible = available(penalties);
        if (eligible.isEmpty()) throw new IllegalStateException("No eligible items registered");
        GameItem item = eligible.get(random.nextInt(eligible.size()));
        if (item instanceof PieceItem pieceItem) return pieceItem.createPiece(piece.getRow(), piece.getCol());
        attachAt(piece, item);
        return piece;
    }

    private List<GameItem> available(PenaltyController penalties) {
        return items.stream().filter(item -> item.isAvailable(penalties)).toList();
    }

    private static List<GameItem> attachable(List<GameItem> candidates) {
        return candidates.stream().filter(item -> !(item instanceof PieceItem)).toList();
    }

    private void attachFrom(Tetromino piece, List<GameItem> eligible) {
        if (eligible.isEmpty()) throw new IllegalStateException("No eligible items registered");
        attachAt(piece, eligible.get(random.nextInt(eligible.size())));
    }

    private void attachAt(Tetromino piece, GameItem item) {
        int[][] shape = piece.getShape();
        List<int[]> occupied = new ArrayList<>();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) occupied.add(new int[] {row, col});
            }
        }
        int[] cell = occupied.get(random.nextInt(occupied.size()));
        piece.attachItem(item, cell[0], cell[1]);
    }
}