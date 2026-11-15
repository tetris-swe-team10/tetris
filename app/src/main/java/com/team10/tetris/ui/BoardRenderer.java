package com.team10.tetris.ui;

import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.versus.AttackLines;

import javafx.scene.canvas.Canvas;

/**
 * 스냅샷만 받아 보드를 그린다. 엔진·컨트롤러를 모른다. (구현: 역할 B, GameView에서 그리기 코드를 분리)
 *
 * <p>1인 게임, 같은 PC 대전(역할 A), P2P 상대 보드(역할 C)가 모두 이 인터페이스로 그린다.
 * P2P에서는 네트워크로 받은 상대 {@link GameSnapshot}을 그대로 넘기면 된다.
 */
public interface BoardRenderer {

    /** 고정 블록, 현재 블록, 고스트, 회색(공격) 칸, 줄 삭제·아이템 연출을 그린다. */
    void drawBoard(Canvas canvas, GameSnapshot snapshot);

    /** NEXT 블록을 그린다. */
    void drawNext(Canvas canvas, GameSnapshot snapshot);

    /** 대기 중인 공격 줄(받은 줄 표시 영역)을 그린다. 비어 있으면 빈 영역만 그린다. */
    void drawPendingAttack(Canvas canvas, AttackLines pending);

    /** 일반 / 색맹 팔레트 전환 */
    void setPalette(BlockPalette palette);
}
