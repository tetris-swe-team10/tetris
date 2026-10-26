package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;

/**
 * 무게추 전용 블록. TetrominoType이 없으므로(type == null) RWS 생성기와 팔레트에 영향을 주지 않는다.
 * 모양은 고정(.WW. / WWWW)이며 회전하지 않는다. 처음 닿으면 anchored가 되어 좌우 이동이 잠긴다.
 */
public final class WeightPiece extends Tetromino {
    private static final int[][] SHAPE = {
            {0, 1, 1, 0},
            {1, 1, 1, 1}
    };
    private boolean anchored;

    public WeightPiece(WeightItem item, int row, int col) {
        super(null, row, col);
        attachItem(item, 1, 0); // 표시용 마커. 화면은 블록형 아이템의 모든 칸에 W를 그린다.
    }

    @Override public int[][] getShape() {
        return new int[][] {SHAPE[0].clone(), SHAPE[1].clone()};
    }
    @Override public boolean canRotate() { return false; }
    @Override public void rotateClockwise() { }
    @Override public void rotateCounterClockwise() { }

    public boolean isAnchored() { return anchored; }
    public void anchor() { anchored = true; }
    public int bottomRow() { return getRow() + SHAPE.length - 1; }
}