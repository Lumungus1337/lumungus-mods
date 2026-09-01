package dev.lumungus.core.api.production;

public enum ProductionStatus {
    PLANNED,
    WAITING_FOR_MATERIAL,
    IN_PROGRESS,
    COMPLETE,
    BLOCKED;

    public boolean isTerminal() {
        return this == COMPLETE || this == BLOCKED;
    }

    public boolean canTransitionTo(ProductionStatus next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case PLANNED -> next == WAITING_FOR_MATERIAL || next == IN_PROGRESS || next == BLOCKED;
            case WAITING_FOR_MATERIAL -> next == IN_PROGRESS || next == BLOCKED;
            case IN_PROGRESS -> next == WAITING_FOR_MATERIAL || next == COMPLETE || next == BLOCKED;
            case COMPLETE, BLOCKED -> false;
        };
    }
}
