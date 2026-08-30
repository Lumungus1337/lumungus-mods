package dev.lumungus.integration.migration;

import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Converts an already discovered inventory view into a migration snapshot without mutating it. */
public final class ReadOnlyInventorySnapshotter {
    private ReadOnlyInventorySnapshotter() {
    }

    public static MigrationInventorySnapshot capture(
            String source,
            int endpointCount,
            long slotCount,
            Collection<ResourceAmount> resources
    ) {
        Map<MigrationItemKey, Long> amounts = new LinkedHashMap<>();
        for (ResourceAmount resource : Objects.requireNonNull(resources, "resources")) {
            Objects.requireNonNull(resource, "resource");
            if (!resource.isEmpty()) {
                amounts.merge(MigrationItemKey.of(resource.stack()), resource.amount(), Math::addExact);
            }
        }
        return new MigrationInventorySnapshot(source, endpointCount, slotCount, amounts);
    }
}
