package dev.lumungus.core.api.storage;

import java.util.Objects;

/** Limits for one storage access. */
public record StorageCapacity(long maxTotalAmount, int maxDistinctTypes) {
    public StorageCapacity {
        if (maxTotalAmount < 0) {
            throw new IllegalArgumentException("Maximum total amount must not be negative");
        }
        if (maxDistinctTypes < 0) {
            throw new IllegalArgumentException("Maximum distinct types must not be negative");
        }
    }

    public boolean canContain(StorageSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return snapshot.storedTotalAmount() <= maxTotalAmount
            && snapshot.storedDistinctTypes() <= maxDistinctTypes;
    }
}
