package com.team10.tetris.game.item;

import com.team10.tetris.game.Tetromino;

/**
 * 블록에 붙지 않고 블록 자체가 되는 아이템(예: 무게추).
 * 지급 시 새 NEXT를 같은 생성 위치의 전용 블록으로 교체한다.
 * 이 블록은 보드에 고정되지 않으므로 onLock·rowsToClear는 호출되지 않는다.
 */
public interface PieceItem extends GameItem {
    Tetromino createPiece(int row, int col);
}