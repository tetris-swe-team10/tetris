package com.team10.tetris.game.item;

import com.team10.tetris.game.PenaltyController;

/** K가 붙은 블록 자체를 생성 방향으로 고정한다. 이동과 드롭은 제한하지 않는다. */
public final class RotationLock implements GameItem {
    @Override public String symbol() { return "K"; }
    @Override public String description() { return "회전 잠금 · 이 블록은 회전 불가"; }
    @Override public boolean isAvailable(PenaltyController penalties) { return penalties.canReserve(); }
    @Override public void onAssigned(PenaltyController penalties) {
        penalties.reserve(PenaltyController.Kind.ROTATION_LOCK);
    }
    @Override public void onSpawn(PenaltyController penalties) { penalties.activateRotationLock(); }
}
