package com.team10.tetris.game;

/** 렌더링과 독립된 벌칙 수명. 종류별 발동 시점부터 대상 블록의 고정까지 유지한다. */
public final class PenaltyController {
    public enum Phase { IDLE, RESERVED, PENDING, ACTIVE }
    public enum Kind { BLACKOUT, ROTATION_LOCK }
    private Kind kind;
    private Phase phase = Phase.IDLE;

    public Phase phase() { return phase; }
    public boolean blackoutActive() { return active() && kind == Kind.BLACKOUT; }
    public boolean rotationLocked() { return active() && kind == Kind.ROTATION_LOCK; }
    public void activateRotationLock() {
        if (phase == Phase.RESERVED && kind == Kind.ROTATION_LOCK) phase = Phase.ACTIVE;
    }
    public boolean active() { return phase == Phase.ACTIVE; }
    public void finishActive() { if (active()) reset(); }
    public boolean canReserve() { return phase == Phase.IDLE; }
    public boolean reserve() { return reserve(Kind.BLACKOUT); }
    public boolean reserve(Kind kind) {
        if (!canReserve()) return false;
        this.kind = java.util.Objects.requireNonNull(kind);
        phase = Phase.RESERVED;
        return true;
    }
    public void landed() {
        if (phase == Phase.RESERVED && kind == Kind.BLACKOUT) phase = Phase.PENDING;
    }
    public void activatePending() {
        if (phase != Phase.PENDING) return;
        phase = Phase.ACTIVE;
    }
    public void reset() { phase = Phase.IDLE; kind = null; }
}
