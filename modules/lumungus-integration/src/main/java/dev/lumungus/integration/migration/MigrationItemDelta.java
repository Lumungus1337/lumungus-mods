package dev.lumungus.integration.migration;

import java.util.Objects;

public record MigrationItemDelta(MigrationItemKey item, long expectedAmount, long actualAmount) {
    public MigrationItemDelta {
        item = Objects.requireNonNull(item, "item");
        if (expectedAmount < 0 || actualAmount < 0) {
            throw new IllegalArgumentException("Delta amounts must not be negative");
        }
        if (expectedAmount == actualAmount) {
            throw new IllegalArgumentException("Equal amounts are not a delta");
        }
    }

    public long missingAmount() {
        return expectedAmount > actualAmount ? expectedAmount - actualAmount : 0;
    }

    public long excessAmount() {
        return actualAmount > expectedAmount ? actualAmount - expectedAmount : 0;
    }
}
