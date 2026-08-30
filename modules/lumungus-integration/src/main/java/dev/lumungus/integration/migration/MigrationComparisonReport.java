package dev.lumungus.integration.migration;

import java.util.List;
import java.util.Objects;

public record MigrationComparisonReport(
        MigrationInventorySnapshot expected,
        MigrationInventorySnapshot actual,
        List<MigrationItemDelta> itemDeltas
) {
    public MigrationComparisonReport {
        expected = Objects.requireNonNull(expected, "expected");
        actual = Objects.requireNonNull(actual, "actual");
        itemDeltas = List.copyOf(Objects.requireNonNull(itemDeltas, "itemDeltas"));
    }

    public boolean metadataMatches() {
        return expected.endpointCount() == actual.endpointCount()
                && expected.slotCount() == actual.slotCount();
    }

    public boolean contentsMatch() {
        return itemDeltas.isEmpty();
    }

    public boolean exactMatch() {
        return metadataMatches() && contentsMatch();
    }

    public long missingTotal() {
        long total = 0;
        for (MigrationItemDelta delta : itemDeltas) {
            total = Math.addExact(total, delta.missingAmount());
        }
        return total;
    }

    public long excessTotal() {
        long total = 0;
        for (MigrationItemDelta delta : itemDeltas) {
            total = Math.addExact(total, delta.excessAmount());
        }
        return total;
    }
}
