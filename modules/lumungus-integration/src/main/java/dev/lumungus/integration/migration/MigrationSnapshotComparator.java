package dev.lumungus.integration.migration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MigrationSnapshotComparator {
    private MigrationSnapshotComparator() {
    }

    public static MigrationComparisonReport compare(
            MigrationInventorySnapshot expected,
            MigrationInventorySnapshot actual
    ) {
        Set<MigrationItemKey> items = new LinkedHashSet<>(expected.itemAmounts().keySet());
        items.addAll(actual.itemAmounts().keySet());

        List<MigrationItemDelta> deltas = new ArrayList<>();
        for (MigrationItemKey item : items) {
            long expectedAmount = expected.itemAmounts().getOrDefault(item, 0L);
            long actualAmount = actual.itemAmounts().getOrDefault(item, 0L);
            if (expectedAmount != actualAmount) {
                deltas.add(new MigrationItemDelta(item, expectedAmount, actualAmount));
            }
        }
        return new MigrationComparisonReport(expected, actual, deltas);
    }
}
