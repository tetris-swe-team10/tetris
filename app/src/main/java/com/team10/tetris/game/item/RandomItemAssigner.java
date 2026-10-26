package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** 등록된 아이템과 실제 점유 칸을 균등 선택한다. 다른 아이템은 목록에 추가한다. */
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

    @Override
    public void attachTo(Tetromino piece) {
        attachFrom(piece, items);
    }

    @Override
    public void attachTo(Tetromino piece, com.team10.tetris.game.PenaltyController penalties) {
        attachFrom(piece, items.stream().filter(item -> item.isAvailable(penalties)).toList());
    }

    private void attachFrom(Tetromino piece, List<GameItem> eligible) {
        if (eligible.isEmpty()) throw new IllegalStateException("No eligible items registered");
        int[][] shape = piece.getShape();
        List<int[]> occupied = new ArrayList<>();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) occupied.add(new int[] {row, col});
            }
        }
        GameItem item = eligible.get(random.nextInt(eligible.size()));
        int[] cell = occupied.get(random.nextInt(occupied.size()));
        piece.attachItem(item, cell[0], cell[1]);
    }
}
