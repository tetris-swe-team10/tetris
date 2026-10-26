package com.team10.tetris.game.item;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.CellPosition;
import java.util.List;

/** 고정으로 발동하는 부착 아이템. 연출이 있으면 완료 후 효과를 한 번 호출한다. */
public interface GameItem {
    String symbol();
    String description();
    /** 빈 목록/0ms인 기존 아이템은 대기 없이 실행한다. 미리보기는 보드를 변경하지 않는다. */
    default List<CellPosition> previewCells(Board board, int row, int col) { return List.of(); }
    default int effectDurationMs() { return 0; }
    void onLock(Board board, int row, int col);
}
