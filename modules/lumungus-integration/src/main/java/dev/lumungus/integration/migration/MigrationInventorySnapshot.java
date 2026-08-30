package dev.lumungus.integration.migration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable read-only inventory summary captured at one point in time. */
public record MigrationInventorySnapshot(
        String source,
        int endpointCount,
        long slotCount,
        Map<MigrationItemKey, Long> itemAmounts
) {
    public MigrationInventorySnapshot {
        source = Objects.requireNonNull(source, "source").trim();
        if (source.isEmpty()) {
            throw new IllegalArgumentException("Snapshot source must not be blank");
        }
        if (endpointCount < 0) {
            throw new IllegalArgumentException("Endpoint count must not be negative");
        }
        if (slotCount < 0) {
            throw new IllegalArgumentException("Slot count must not be negative");
        }

        LinkedHashMap<MigrationItemKey, Long> validatedAmounts = new LinkedHashMap<>();
        Objects.requireNonNull(itemAmounts, "itemAmounts").forEach((item, amount) -> {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(amount, "item amount");
            if (amount <= 0) {
                throw new IllegalArgumentException("Snapshot item amount must be positive");
            }
            validatedAmounts.merge(item, amount, Math::addExact);
        });
        itemAmounts = Collections.unmodifiableMap(validatedAmounts);
    }

    public long totalAmount() {
        long total = 0;
        for (long amount : itemAmounts.values()) {
            total = Math.addExact(total, amount);
        }
        return total;
    }

    public int distinctTypes() {
        return itemAmounts.size();
    }
}
