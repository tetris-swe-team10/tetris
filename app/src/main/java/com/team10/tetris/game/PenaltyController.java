package com.team10.tetris.game;

/** 렌더링과 독립된 벌칙 수명. 다음 조작 블록의 고정까지 유지한다. */
public final class PenaltyController {
    public enum Phase { IDLE, RESERVED, PENDING, ACTIVE }
    private Phase phase = Phase.IDLE;

    public Phase phase() { return phase; }
    public boolean active() { return phase == Phase.ACTIVE; }
    public void finishActive() { if (active()) reset(); }
    public boolean canReserve() { return phase == Phase.IDLE; }
    public boolean reserve() {
        if (!canReserve()) return false;
        phase = Phase.RESERVED;
        return true;
    }
    public void landed() {
        if (phase == Phase.RESERVED) phase = Phase.PENDING;
    }
    public void activatePending() {
        if (phase != Phase.PENDING) return;
        phase = Phase.ACTIVE;
    }
    public void reset() { phase = Phase.IDLE; }
}
