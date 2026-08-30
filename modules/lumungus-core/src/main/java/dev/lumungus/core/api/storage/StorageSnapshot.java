package dev.lumungus.core.api.storage;

/** Immutable summary of the resources currently stored. */
public record StorageSnapshot(long storedTotalAmount, int storedDistinctTypes) {
    public StorageSnapshot {
        if (storedTotalAmount < 0) {
            throw new IllegalArgumentException("Stored total amount must not be negative");
        }
        if (storedDistinctTypes < 0) {
            throw new IllegalArgumentException("Stored distinct types must not be negative");
        }
        if (storedTotalAmount > 0 && storedDistinctTypes == 0) {
            throw new IllegalArgumentException("Stored resources must include a distinct type");
        }
        if (storedTotalAmount < storedDistinctTypes) {
            throw new IllegalArgumentException("Stored total amount must cover all distinct types");
        }
    }

    public boolean isEmpty() {
        return storedTotalAmount == 0;
    }
}
